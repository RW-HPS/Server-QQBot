package com.github.minxyzgo.rwserver.plugins.qqbot.core

import com.github.dr.rwserver.data.global.Data
import com.github.dr.rwserver.data.global.NetStaticData
import com.github.dr.rwserver.game.GameMaps
import com.github.dr.rwserver.util.file.FileUtil
import com.github.dr.rwserver.util.game.CommandHandler
import com.github.dr.rwserver.util.game.CommandHandler.ResponseType.*
import com.github.dr.rwserver.util.log.Log
import com.github.minxyzgo.rwserver.plugins.qqbot.QQBotPlugin
import com.github.minxyzgo.rwserver.plugins.qqbot.util.*
import kotlinx.coroutines.launch
import net.mamoe.mirai.console.command.descriptor.ExperimentalCommandDescriptors
import net.mamoe.mirai.console.plugin.jvm.JvmPluginDescription
import net.mamoe.mirai.console.plugin.jvm.KotlinPlugin
import net.mamoe.mirai.console.util.ConsoleExperimentalApi
import net.mamoe.mirai.contact.Contact
import net.mamoe.mirai.contact.Group
import net.mamoe.mirai.event.events.GroupMessageEvent
import net.mamoe.mirai.event.events.NudgeEvent
import net.mamoe.mirai.event.globalEventChannel
import net.mamoe.mirai.message.data.Message
import net.mamoe.mirai.message.data.PlainText
import net.mamoe.mirai.message.data.content
import net.mamoe.mirai.utils.ExternalResource.Companion.uploadAsImage

@ConsoleExperimentalApi
@ExperimentalCommandDescriptors
object MiraiPlugin : KotlinPlugin(
    JvmPluginDescription(
        "com.github.minxyzgo.mirai-console-rwserver-plugin",
        QQBotPlugin.pluginVersion
    ) {
        this.author("minxyzgo")
    }
) {
    private val miraiCommands = object : CommandHandler(".") {
        init {
            val command = this
            register<GroupMessageEvent>(
                "help",
                "查看所有指令"
            ) { _, event ->
                val info = buildString {
                    append("所有命令: \n")
                    command.commandList.forEach {
                        append("命令: ${it.text} ${
                            it.paramText.run { if(isBlank()) "" else ", 参数: $this"}
                        }, 介绍: ${it.description}\n")
                    }
                }

                event.group.run { launch { sendMessage(info) } }
            }

            register<GroupMessageEvent>(
                "status",
                "查看服务器状态"
            ) { _, event ->
                event.group.sendStatus()
            }

            register<GroupMessageEvent>(
                "upload",
                "[map/all] [path...]",
                "向服务器上传文件。可选type: map, all"
            ) { args, event ->
                val group = event.group
                group.launch {
                    val path = args.toMutableList().also { it.removeFirst() }.joinToString(" ")
                    when (val type = args[0]) {
                        "map", "all" -> {
                            when(
                                if(type == "map") downloadMapFileFromGroup(group, path, 1000_0000)
                                else run {
                                    if(event.sender.permission.level < 1) {
                                        group.sendMessage("你没有权限")
                                        return@launch
                                    }

                                    val savePath = FileUtil.file(Data.Plugin_Data_Path + "/download/")
                                    if(!savePath.exists()) savePath.file.mkdirs()
                                    downloadAnyFromGroup(group, path, savePath.file.path)
                                }
                            ) {
                                DownloadResult.Success -> {
                                    Data.game.mapsData.clear()
                                    Data.game.checkMaps()
                                    group.sendMessage("重新加载地图成功!")
                                }

                                DownloadResult.OutOfMaxSize -> group.sendMessage("下载失败: 大小超出限制")
                                DownloadResult.NotFound -> group.sendMessage("下载失败: 没有找到地图文件")
                                DownloadResult.NoDownloadInfo -> group.sendMessage("下载失败: 没有找到下载信息")
                                DownloadResult.DownloadFailed -> group.sendMessage("下载失败: 网络连接超时或其它原因")
                            }
                        }

                        else -> group.sendMessage("无效的type参数")
                    }
                }
            }
        }
    }
    override fun onEnable() {
        globalEventChannel().subscribeAlways<GroupMessageEvent> {
            when(miraiCommands.handleMessage(message.content, this).type) {
                fewArguments -> group.sendMessage("执行命令${message.content}失败，太少的参数")
                manyArguments -> group.sendMessage("执行命令${message.content}失败，太多的参数")
                noCommand, valid -> {  }
                unknownCommand -> group.sendMessage("执行命令${message.content}失败，没有找到这样的命令")
                else -> Log.clog("unknown")
            }
        }

        globalEventChannel().subscribeAlways<NudgeEvent> {
            this.subject.run { if(this is Group) sendStatus() }
        }
    }

    fun Contact.sendStatus() {
        val contact = this
        this.launch {
            val currentMap = Data.game.maps

            val image = if(currentMap.mapType == GameMaps.MapType.defaultMap)
                this::class.java.classLoader.getResourceAsStream("${currentMap.mapPlayer}${currentMap.mapName}_map.png")!!.uploadAsImage(contact)
            else getMapFileInputStreamBySuffix(currentMap.mapData.mapFileName, "_map.png")?.uploadAsImage(contact)

            val time = operationTime()
            val hours = time / 3600000
            val minutes = (time % 3600000) / 60000
            val seconds = (time % 60000) / 1000
            var baseMessage: Message = PlainText("""
                当前房主: ${Data.game.playerData.filterNotNull().firstOrNull { it.isAdmin }?.name.run { if(this.isNullOrBlank()) "Admin" else this }}
                地图名: ${currentMap.mapName}
                地图模式: ${
                    if(currentMap.mapType == GameMaps.MapType.defaultMap) 
                        "skirmish" 
                    else getMapFileInputStreamBySuffix(
                        currentMap.mapData.mapFileName, 
                        currentMap.mapData.type
                    )?.let { 
                        parseMapType(
                            it
                        ) 
                    }
                }
                地图人数: ${currentMap.mapPlayer}
                地图类型: ${currentMap.mapType.name}
                服务器人数: ${Data.game.playerData.filterNotNull().size}/${com.github.dr.rwserver.data.global.Data.game.maxPlayer}
                服务器状态: ${if (Data.game.isStartGame) "已开始" else "战役室"}
                服务器版本: ${Data.SERVER_CORE_VERSION}
                已BAN人数: ${Data.core.admin.bannedIPs.size()}
                协议版本: ${NetStaticData.protocolData.AbstractNetConnectVersion}
                服务器已运行${hours}时${minutes}分${seconds}秒
                com.github@RW-HPS
            """.trimIndent())
            baseMessage = (image ?: PlainText("<no map png>\n")) + baseMessage
            contact.sendMessage(baseMessage)
        }
    }
}