package com.marcusilgner.api

import org.pf4j.ExtensionPoint

interface HibernateConfiguration : ExtensionPoint {
    fun annotatedClasses(): List<Class<*>>
}
