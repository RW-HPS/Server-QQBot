package com.github.minxyzgo.rwserver.plugins.qqbot.sql

import com.github.dr.rwserver.data.global.*
import com.github.dr.rwserver.game.*
import com.github.dr.rwserver.util.*
import com.github.dr.rwserver.util.alone.annotations.*
import com.github.dr.rwserver.util.encryption.*
import com.github.dr.rwserver.util.file.*
import com.github.dr.rwserver.util.game.*
import com.github.minxyzgo.rwserver.plugins.qqbot.data.*
import com.github.minxyzgo.rwserver.plugins.qqbot.sql.UidSql.Users.uuid
import net.mamoe.mirai.*
import net.mamoe.mirai.console.command.descriptor.*
import net.mamoe.mirai.console.util.*
import org.jetbrains.exposed.sql.*
import org.jetbrains.exposed.sql.transactions.*
import kotlin.random.*

@DidNotFinish
@ConsoleExperimentalApi
@ExperimentalCommandDescriptors
object UidSql {
    private const val dbPath: String = "server-qqbot-sqlite.db"

    private object Users : Table() {
        val id = integer("id").autoIncrement()
        val uuid = varchar("uuid", 100)
        var ip = varchar("ip", 100)
        val qq = long("qq")
        val warning = integer("warning")
        val isBan = bool("isBan")
        override val primaryKey: PrimaryKey = PrimaryKey(id, name = "PK_Users_ID")
    }
    init {
        Database.connect(
            "jdbc:sqlite:${FileUtil.toFolder(Data.Plugin_Data_Path).toPath(dbPath).file.path}",
            driver = "org.sqlite.JDBC",
            user = "root",
            password = ""
        )

        transaction {
            addLogger(StdOutSqlLogger)
            SchemaUtils.create(Users)

            Events.on(EventType.PlayerConnectPasswdCheckEvent::class.java) { event ->
                if(Users.exists()) {
                    Users.selectAll().firstOrNull { event.abstractNetConnect.player?.uuid == it[uuid] }?.let {
                        event.result = true
                        return@on
                    }
                }
                println("passwd: ${event.passwd}")
                if(event.passwd.startsWith("注册")) {
                    Bot.instances.forEach { bot ->
                        val group = bot.getGroup(PluginData.instance.`main-group-id`)
                        val splitStr = event.passwd.split("注册")
                        if(splitStr.size < 2) {
                            event.abstractNetConnect.sendKick("错误: 缺少qq号参数")
                            return@on
                        }
                        val castResult = kotlin.runCatching { splitStr[1].toLong() }
                        if(castResult.isFailure) {
                            event.abstractNetConnect.sendKick("错误: 不正确的qq号参数")
                            return@on
                        }
                        group!![castResult.getOrThrow()]?.let { member ->
                            val sha256 = Sha().sha256(Random(Time.millis()).nextDouble().toString())
                        }
                    }

                }
            }
        }
    }
}