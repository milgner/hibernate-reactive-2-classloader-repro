package com.marcusilgner.api

import io.vertx.core.Verticle
import org.pf4j.ExtensionPoint

interface Worker : Verticle, ExtensionPoint
