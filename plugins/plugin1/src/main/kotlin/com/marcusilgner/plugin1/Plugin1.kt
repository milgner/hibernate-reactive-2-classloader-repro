package com.marcusilgner.plugin1

import com.marcusilgner.AbstractPlugin
import com.marcusilgner.DIInit
import org.apache.logging.log4j.kotlin.Logging

class Plugin1 : AbstractPlugin(), Logging {
    override fun start() {
        logger.info("Starting Plugin 1")
    }

    override fun stop() {
        logger.info("Stopping Plugin 1")
    }

    override fun configureDI(): DIInit = {}
}
