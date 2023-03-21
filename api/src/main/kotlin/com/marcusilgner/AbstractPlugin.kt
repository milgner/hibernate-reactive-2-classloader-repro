package com.marcusilgner

import org.kodein.di.DI
import org.pf4j.Plugin

typealias DIInit = DI.MainBuilder.() -> Unit

/**
 * A base class for plugins which also allows configuration of the DI container
 */
abstract class AbstractPlugin : Plugin() {
    open fun configureDI(): DIInit = {}
}
