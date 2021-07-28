package com.github.minxyzgo.rwserver.plugins.qqbot.data

import com.github.dr.rwserver.data.global.*
import com.github.dr.rwserver.util.file.*
import com.github.minxyzgo.rwserver.plugins.qqbot.util.*
import com.typesafe.config.*
import io.github.config4k.*

data class PluginData(
    @ConfigComment("""
        设置下载文件最大的大小。单位: 字节
    """)
    val `download-max-length`: Long = 1000_000,
    @ConfigComment("""
        设置每分钟下载地图的最大数量。注意，这是所有群同时计数的
    """)
    val `download-max-amount-per-minutes`: Int = 5,
    @ConfigComment("""
        设置主要群。
        暂时无用
    """)
    val `main-group-id`: Long = 123456789
    //以下尚未完成
//    @ConfigComment("""
//        设置最大警告次数。当某人警告次数超过此限制时，他将会自动被服务器BAN。
//    """)
//    val `max-warning-times`: Int = 5,
//    @ConfigComment("""
//        设置是否启用严格模式。如果启用，则在服务器BAN玩家的同时kick
//    """)
//    val `strict-mode`: Boolean = false
) {
    @Suppress("UNCHECKED_CAST")
    companion object {
        val instance by lazy {
            val result: PluginData
            val file = FileUtil.toFolder(Data.Plugin_Data_Path).toPath("server-qqbot-config.conf")
            if(file.notExists()) {
                result = PluginData()
                val config = result.toConfig("config")
                file.file.writeText(
                    config
                        .apply { parseComment(PluginData::class) }
                        .root()
                        .render(
                            ConfigRenderOptions.defaults().setJson(false)
                        )
                )
            } else {
                result = ConfigFactory.parseFile(file.file)
                    .extract("config")
            }

            result
        }
    }
}