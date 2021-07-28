package com.github.minxyzgo.rwserver.plugins.qqbot.util

import com.typesafe.config.*
import kotlin.reflect.*

/**
 * 为config添加注释。这将会遍历[clazz]中的所有Field，并添加[ConfigComment.comment]
 * 注意，如果需要输出字符串，需要在[ConfigObject.render]之前调用这个方法。否则不会得到正确的hocon字符。
 * @param clazz 需要遍历的class，一般为 data class
 */
fun Config.parseComment(clazz: KClass<out Any>) {
    this.entrySet().forEach { entry ->
        val comment = clazz.java
            .getDeclaredField(entry.key.split(".")[1])
            .getDeclaredAnnotation(ConfigComment::class.java)
            .comment
        //可能会有更好的方法，但目前实在没找到。
        Class.forName("com.typesafe.config.impl.AbstractConfigValue")
            .getDeclaredField("origin")
            .apply { isAccessible = true }
            .set(entry.value, ConfigOriginFactory.newSimple(
                comment.trimIndent()
            ))
    }
}

/**
 * 为config添加注释。
 * @see Config.parseComment
 * @param comment 需要添加的注释。注意，该注释会在为config添加注释时自动调用[String.trimIndent]
 * 你不必也不能显式调用它，因为它是编译时常量。
 */
@Target(AnnotationTarget.FIELD)
@Retention(AnnotationRetention.RUNTIME)
annotation class ConfigComment(
    val comment: String
)
