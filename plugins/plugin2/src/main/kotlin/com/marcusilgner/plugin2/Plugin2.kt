package com.marcusilgner.plugin2

import com.marcusilgner.AbstractPlugin
import com.marcusilgner.DIInit
import org.apache.logging.log4j.kotlin.Logging

class Plugin2 : AbstractPlugin(), Logging {
    override fun configureDI(): DIInit = {}
}
