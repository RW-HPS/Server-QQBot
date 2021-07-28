package com.github.minxyzgo.rwserver.plugins.qqbot

import com.github.dr.rwserver.data.global.*
import com.github.dr.rwserver.dependent.*
import com.github.dr.rwserver.plugin.*
import com.github.dr.rwserver.util.*
import com.github.minxyzgo.rwserver.plugins.qqbot.core.*
import com.github.minxyzgo.rwserver.plugins.qqbot.util.*
import com.google.gson.*
import kotlinx.coroutines.*
import net.mamoe.mirai.console.*
import net.mamoe.mirai.console.command.descriptor.*
import net.mamoe.mirai.console.util.*

@ConsoleExperimentalApi
@ExperimentalCommandDescriptors
class QQBotPlugin(test: Boolean = false) : Plugin() {

    init {
        if(!test) {
            val libraryManager = LibraryManager(true, Data.Plugin_Lib_Path)
            //libraryManager.importLib("org.jetbrains.kotlin", "kotlin-reflect", "1.5.21")
            libraryManager.importLib("org.jetbrains.exposed", "exposed-jdbc", exposedVersion)
            libraryManager.importLib("org.jetbrains.exposed", "exposed-core", exposedVersion)
            libraryManager.importLib("org.xerial", "sqlite-jdbc", sqliteVersion)
           // libraryManager.importLib("net.mamoe", "mirai-console-terminal", miraiVersion)
            libraryManager.loadToClassLoader()
        }
    }

    companion object {
        val miraiVersion by version()
        val sqliteVersion by version()
        val exposedVersion by version()

        val pluginVersion by lazy {
            val builder = GsonBuilder().create()
            builder.fromJson(
                QQBotPlugin::class
                    .java.classLoader
                    .getResourceAsStream("plugin.json")!!
                    .bufferedReader(Charsets.UTF_8),
                Map::class.java
            ).toMap()["version"] as String
        }
        internal var startTime = 0L
    }

    @ConsoleFrontEndImplementation
    override fun onEnable() {
        startTime = Time.millis()
        MiraiConsoleLoader.resolveConsole()
    }

    @OptIn(ExperimentalCoroutinesApi::class)
    override fun onDisable() {
        MiraiPlugin.onDisable()
        Data.SERVERCOMMAND.handleMessage("mirai stop")
    }
}