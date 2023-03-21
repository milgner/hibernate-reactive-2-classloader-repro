package com.marcusilgner

import org.apache.logging.log4j.kotlin.Logging
import org.kodein.di.DI
import org.pf4j.*
import java.nio.file.Path

// the plugin manager is needed to construct the DI container but the extension
// factory also needs to inject the DI context into the extensions
typealias GetDI = () -> DI

/**
 * The plugin manager provides the configuration on how to find plugins as well as the
 * implementation for plugin factory and extension factory.
 */
class PluginManager(importPaths: List<Path>, private val baseDiGetter: GetDI) :
    DefaultPluginManager(importPaths), Logging {
    private val pluginDIs = mutableMapOf<ClassLoader, DI>()

    override fun createPluginFactory(): org.pf4j.PluginFactory {
        return PluginFactory()
    }

    override fun createExtensionFactory(): org.pf4j.ExtensionFactory {
        return ExtensionFactory()
    }

    override fun createPluginDescriptorFinder(): CompoundPluginDescriptorFinder {
        return CompoundPluginDescriptorFinder().add(ManifestPluginDescriptorFinder())
    }

    /**
     * The plugin factory instantiates plugins and if they implement
     * [AbstractPlugin::configureDI], the plugin-specific dependencies will be
     * made available in the plugin-specific dependency injection container.
     */
    private inner class PluginFactory : DefaultPluginFactory(), Logging {
        override fun createInstance(pluginClass: Class<*>, pluginWrapper: PluginWrapper): Plugin? {
            try {
                logger.info("Instantiating plugin ${pluginClass.canonicalName}")

                val constructor = pluginClass.getConstructor()
                val instance = constructor.newInstance()
                val diConfig = pluginClass.getMethod("configureDI")
                val pluginDI = run {
                    @Suppress("UNCHECKED_CAST")
                    val configBlock = diConfig.invoke(instance) as DIInit
                    DI {
                        extend(baseDiGetter.invoke())
                        this.configBlock()
                    }
                }
                pluginDIs[pluginWrapper.pluginClassLoader] = pluginDI
                return instance as Plugin
            } catch (e: Exception) {
                logger.error("Failed to instantiate plugin", e)
            }
            return null
        }
    }

    /**
     * This extension factory supports extensions with a constructor that receives a
     * [org.kodein.di.DI] instance.
     */
    private inner class ExtensionFactory : DefaultExtensionFactory() {
        override fun <T : Any?> create(extensionClass: Class<T>): T? {
            val pluginDI = pluginDIs[extensionClass.classLoader]
            if (pluginDI == null) {
                logger.error("Trying to instantiate extension from plugin without DI container")
            } else {
                try {
                    val diCtor = extensionClass.getConstructor(DI::class.java)
                    logger.debug("Extension has DI-Aware constructor: $extensionClass")
                    return if (pluginDIs.containsKey(extensionClass.classLoader)) {
                        diCtor.newInstance(pluginDI)
                    } else {
                        logger.error("Extension wants DI but it's not available on Plugin")
                        null
                    }
                } catch (e: NoSuchMethodException) {
                    logger.debug("Extension has no DI-Aware constructor: $extensionClass")
                }
            }
            return super.create(extensionClass)
        }
    }
}
