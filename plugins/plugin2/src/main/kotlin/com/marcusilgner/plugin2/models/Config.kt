package com.marcusilgner.plugin2.models

import com.marcusilgner.api.HibernateConfiguration
import org.pf4j.Extension

@Extension
class Config : HibernateConfiguration {
    override fun annotatedClasses() = listOf(Model2::class.java)
}
