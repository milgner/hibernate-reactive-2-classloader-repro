package com.marcusilgner.plugin1.models

import com.marcusilgner.api.HibernateConfiguration
import org.pf4j.Extension

@Extension
class ModelConfig1 : HibernateConfiguration {
    override fun annotatedClasses() = listOf(Model1::class.java)
}
