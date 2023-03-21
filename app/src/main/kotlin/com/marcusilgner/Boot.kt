package com.marcusilgner

import com.linecorp.kotlinjdsl.query.HibernateMutinyReactiveQueryFactory
import com.linecorp.kotlinjdsl.query.creator.SubqueryCreatorImpl
import com.marcusilgner.api.HibernateConfiguration
import io.vertx.config.ConfigRetriever
import io.vertx.config.ConfigRetrieverOptions
import io.vertx.core.Vertx
import io.vertx.kotlin.config.configRetrieverOptionsOf
import io.vertx.kotlin.coroutines.await
import org.apache.logging.log4j.kotlin.Logging
import org.hibernate.boot.registry.classloading.internal.ClassLoaderServiceImpl
import org.hibernate.boot.registry.classloading.internal.TcclLookupPrecedence
import org.hibernate.boot.registry.classloading.spi.ClassLoaderService
import org.hibernate.cfg.Configuration
import org.hibernate.reactive.mutiny.Mutiny
import org.hibernate.reactive.provider.ReactiveServiceRegistryBuilder
import org.hibernate.reactive.provider.Settings
import org.hibernate.reactive.vertx.VertxInstance
import org.hibernate.service.ServiceRegistry
import org.kodein.di.DI
import org.kodein.di.bindProvider
import org.kodein.di.bindSingleton
import org.kodein.di.instance
import java.nio.file.Paths

suspend fun main() {
    Boot.run()
}

/** Boots the application 🚀 */
object Boot : Logging {

    /** The plugin manager loads plugins and provides functionality to query extensions */
    private val pluginManager: org.pf4j.PluginManager by lazy {
        val pluginsDir = System.getProperty("pf4j.pluginsDir", "./plugins")
        val pluginPaths = listOf(Paths.get(pluginsDir))
        PluginManager(pluginPaths) { di }
    }

    private val configRetrieverOptions: ConfigRetrieverOptions by lazy {
        configRetrieverOptionsOf(includeDefaultStores = true)
    }

    // during startup, we need to first construct a Vertx instance and config ConfigRetriever
    // to read the configuration. However, configuration might have some information that
    // requires re-initialisation of a new Vertx instance afterwards
    private lateinit var vertx: Vertx
    private lateinit var configRetriever: ConfigRetriever

    /**
     * The main Dependency Injection container which extends each plugin-specific DI instance: all
     * bindings registered here are available to all plugins
     */
    private val di: DI = DI {
        bindProvider { vertx }
        bindProvider { configRetriever }
        bindSingleton { buildJpaSessionFactory(pluginManager) }
        bindSingleton { pluginManager }
        bindSingleton {
            HibernateMutinyReactiveQueryFactory(
                sessionFactory = instance(),
                subqueryCreator = SubqueryCreatorImpl(),
            )
        }
    }

    /** The entry point of the application */
    suspend fun run() {
        initializeVertx()
        initializeDatabaseConfiguration()
        // first load the plugins, making extension points available in the system
        pluginManager.loadPlugins()
        println("Plugins: ${pluginManager.plugins.size}")
        pluginManager.startPlugins()
        doStuff()
    }

    private suspend fun doStuff() {
        val sessionFactory: HibernateMutinyReactiveQueryFactory by di.instance()
        sessionFactory.transactionWithFactory { session, reactiveQueryFactory ->
            println(session.isOpen)
        }
    }

    /**
     * Initialize Vert.x. After reading the configuration, it might be necessary
     * to re-instantiate it with new options.
     */
    private fun initializeVertx() {
        vertx = Vertx.vertx()
        configRetriever = ConfigRetriever.create(vertx, configRetrieverOptions)
    }


    /**
     * Encapsulates configuration options related to database access which are needed in both
     * Hibernate and Flyway
     */
    data class DatabaseConfig(
        val url: String,
        val username: String,
        val password: String,
    )

    // The configuration will be loaded based on Vert.x configuration
    lateinit var databaseConfig: DatabaseConfig

    // can't be `by lazy` because it needs to be `suspend`
    private suspend fun initializeDatabaseConfiguration() {
        if (Boot::databaseConfig.isInitialized) {
            return
        }

        logger.info("Loading database configuration")

        val config = configRetriever.config.await()

        val dbConfig = config.getJsonObject("database")
        val url = dbConfig.getString("url")
        val username = dbConfig.getString("username")
        val password = dbConfig.getString("password")

        databaseConfig = DatabaseConfig("jdbc:$url", username, password)
    }

    private fun buildJpaSessionFactory(
        pluginManager: org.pf4j.PluginManager,
    ): Mutiny.SessionFactory {
        logger.info("Configuring Hibernate")
        val configuration = Configuration()
        configuration
            .setProperty(
                Settings.JAKARTA_JPA_PERSISTENCE_PROVIDER,
                "org.hibernate.reactive.provider.ReactivePersistenceProvider",
            )
            .setProperty(Settings.DIALECT, "org.hibernate.dialect.PostgreSQL10Dialect")
            .setProperty(Settings.URL, databaseConfig.url)
            .setProperty(Settings.USER, databaseConfig.username)
            .setProperty(Settings.PASS, databaseConfig.password)
            .setProperty(Settings.SHOW_SQL, pluginManager.isDevelopment.toString())
            .setProperty(Settings.FORMAT_SQL, pluginManager.isDevelopment.toString())

        val classloaders = mutableSetOf<ClassLoader>()
        for (extension in pluginManager.getExtensions(HibernateConfiguration::class.java)) {
            logger.info("Processing extension $extension")
            for (clz in extension.annotatedClasses()) {
                logger.info("Adding class $clz")
                classloaders.add(clz.classLoader)
                configuration.addAnnotatedClass(clz)
            }
        }

        val serviceRegistry = hibernateServiceRegistry(configuration, classloaders)
        val jpaSessionFactory = configuration.buildSessionFactory(serviceRegistry)
        logger.info("JPA SessionFactory initialised")
        return jpaSessionFactory.unwrap(Mutiny.SessionFactory::class.java)
    }

    private fun hibernateServiceRegistry(
        configuration: Configuration,
        classloaders: Set<ClassLoader>,
    ): ServiceRegistry {
        // since every plugin has its own classloader and Hibernate needs access to classes inside
        // the plugins, the classloader service needs manual composition based on all upstream
        // classloaders
        val classLoaderService = ClassLoaderServiceImpl(classloaders, TcclLookupPrecedence.BEFORE)
        val builder =
            ReactiveServiceRegistryBuilder()
                .addService(ClassLoaderService::class.java, classLoaderService)
                // see
                // https://hibernate.org/reactive/documentation/1.0/reference/html_single/#_vert_x_instance_service
                // it avoids Hibernate Reactive having to instantiate its own Vert.x
                .addService(
                    VertxInstance::class.java,
                    VertxInstance { vertx },
                )
                .applySettings(configuration.properties)
        return builder.build()
    }
}
