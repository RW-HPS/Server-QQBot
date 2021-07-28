package com.github.minxyzgo.rwserver.plugins.qqbot.util

import com.github.minxyzgo.rwserver.plugins.qqbot.*
import net.mamoe.mirai.console.command.descriptor.*
import net.mamoe.mirai.console.util.*
import java.util.*
import kotlin.properties.*
import kotlin.reflect.*

/**
 * 委托方法。委托一个对象的get函数，这将会exit
 * 自动读取jar包下的version.properties文件，并找到与其[KProperty.name]一样的键值
 *
 * 通常，这将返回String
 */
@OptIn(ConsoleExperimentalApi::class, ExperimentalCommandDescriptors::class)
fun version() = GetProperty("version.properties", QQBotPlugin::class.java.classLoader)

@OptIn(ConsoleExperimentalApi::class, ExperimentalCommandDescriptors::class)
typealias Project = QQBotPlugin.Companion

class PropertyProvider(
    private val key: String,
    private val path: String,
    private val classLoader: ClassLoader
): ReadOnlyProperty<Project, String?> {
    override fun getValue(thisRef: Project, property: KProperty<*>): String? {
        return Properties().apply {
            load(classLoader
                .getResourceAsStream(path)
            )
        }.getProperty(key)
    }
}

class GetProperty(
    private val path: String,
    private val classLoader: ClassLoader
) {
    operator fun provideDelegate(
        thisRef: Project,
        prop: KProperty<*>
    ): PropertyProvider {
        val propertyName = prop.name
        return PropertyProvider(propertyName, path, classLoader)
    }
}