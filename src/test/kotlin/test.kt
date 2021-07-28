
import com.github.dr.rwserver.*
import com.github.dr.rwserver.command.*
import com.github.dr.rwserver.core.*
import com.github.dr.rwserver.core.thread.*
import com.github.dr.rwserver.data.global.*
import com.github.dr.rwserver.data.plugin.*
import com.github.dr.rwserver.func.*
import com.github.dr.rwserver.game.*
import com.github.dr.rwserver.plugin.*
import com.github.dr.rwserver.struct.*
import com.github.dr.rwserver.util.file.*
import com.github.dr.rwserver.util.game.*
import com.github.dr.rwserver.util.log.*
import com.github.minxyzgo.rwserver.plugins.qqbot.*
import kotlinx.coroutines.*
import kotlinx.coroutines.test.*
import net.mamoe.mirai.console.command.descriptor.*
import net.mamoe.mirai.console.util.*
import java.util.logging.*


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
            QQBotPlugin(true)
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