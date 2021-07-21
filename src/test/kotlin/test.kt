
import com.github.dr.rwserver.Main
import com.github.dr.rwserver.command.ClientCommands
import com.github.dr.rwserver.command.LogCommands
import com.github.dr.rwserver.command.ServerCommands
import com.github.dr.rwserver.core.Initialization
import com.github.dr.rwserver.core.thread.Threads
import com.github.dr.rwserver.data.global.Data
import com.github.dr.rwserver.data.plugin.PluginManage
import com.github.dr.rwserver.func.StrCons
import com.github.dr.rwserver.game.EventType
import com.github.dr.rwserver.plugin.PluginsLoad
import com.github.dr.rwserver.struct.Seq
import com.github.dr.rwserver.util.file.LoadConfig
import com.github.dr.rwserver.util.game.Events
import com.github.dr.rwserver.util.log.Log
import com.github.minxyzgo.rwserver.plugins.qqbot.QQBotPlugin
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.setMain
import net.mamoe.mirai.console.command.descriptor.ExperimentalCommandDescriptors
import net.mamoe.mirai.console.util.ConsoleExperimentalApi
import java.util.logging.Level
import java.util.logging.Logger


val info = """
    --------------------------------------------------------------------------------
    			  _______            __           _    _ _____   _____ 
    			 |  __ \\ \\        / /          | |  | |  __ \\ / ____|
    			 | |__) \\ \\  /\\  / /   ______  | |__| | |__) | (___  
    			 |  _  / \\ \\/  \\/ /   |______| |  __  |  ___/ \\___ \\ 
    			 | | \\ \\  \\  /\\  /             | |  | | |     ____) |
    			 |_|  \\_\\  \\/  \\/              |_|  |_|_|    |_____/
    --------------------------------------------------------------------------------
""".trimIndent()

typealias DataSeq = Seq<PluginsLoad.PluginLoadData>
@Suppress("UNCHECKED_CAST")
@ConsoleExperimentalApi
@ExperimentalCoroutinesApi
@ExperimentalCommandDescriptors
fun main() {
    Dispatchers.setMain(Dispatchers.Unconfined)

    Log.set("ALL")
    Log.setPrint(true)
    Logger.getLogger("io.netty").level = Level.OFF
    println(info)
    Log.clog("Load ing...")

    Data.core.load()

    Initialization()
    Data.config = LoadConfig(Data.Plugin_Data_Path, "Config.json")
    ServerCommands(Data.SERVERCOMMAND)
    ClientCommands(Data.CLIENTCOMMAND)
    LogCommands(Data.LOGCOMMAND)

    Log.clog("正在加载命令")

    (PluginManage::class.java.getDeclaredField("pluginData").also {
        it.isAccessible = true
        it.set(null, DataSeq())
    }.get(null) as DataSeq).add(
        PluginsLoad.PluginLoadData(
            "test",
            "Minxyzgo",
            "test test",
            ">= 1.3.0",
            QQBotPlugin()
        )
    )
    PluginManage.runOnEnable()
    PluginManage.runRegisterServerCommands(Data.SERVERCOMMAND)
    PluginManage.runRegisterClientCommands(Data.CLIENTCOMMAND)
    PluginManage.runRegisterEvents()

    Main.loadNetCore()
    Main.loadUnitList()
    val threadMethod = Main::class.java.getDeclaredMethod("buttonMonitoring").also {
        it.isAccessible = true
    }
    Threads.newThreadCore {
        threadMethod.invoke(null)
    }

    Events.fire(EventType.ServerLoadEvent())
    PluginManage.runInit()

    Data.SERVERCOMMAND.handleMessage("start", StrCons {
        Log.clog(it)
    })
}