package com.github.minxyzgo.rwserver.plugins.qqbot

import com.github.dr.rwserver.data.global.Data
import com.github.dr.rwserver.dependent.LibraryManager
import com.github.dr.rwserver.plugin.Plugin
import com.github.dr.rwserver.util.Time
import com.github.minxyzgo.rwserver.plugins.qqbot.core.MiraiPlugin
import net.mamoe.mirai.console.ConsoleFrontEndImplementation
import net.mamoe.mirai.console.command.descriptor.ExperimentalCommandDescriptors
import net.mamoe.mirai.console.util.ConsoleExperimentalApi


@ConsoleExperimentalApi
@ExperimentalCommandDescriptors
class QQBotPlugin : Plugin() {

    init {
        val libraryManager = LibraryManager(true, Data.Plugin_Lib_Path)
        libraryManager.importLib("org.jetbrains.kotlin", "kotlin-reflect", "1.5.21")
        libraryManager.loadToClassLoader()
    }

    companion object {
        const val pluginVersion = "0.0.1"
        var startTime = 0L
    }

    @ConsoleFrontEndImplementation
    override fun onEnable() {
        startTime = Time.millis()
        pluginData.read()
        MiraiConsoleLoader.resolveConsole()
    }

    override fun onDisable() {
        MiraiPlugin.onDisable()
        Data.SERVERCOMMAND.handleMessage("mirai stop")
    }
}