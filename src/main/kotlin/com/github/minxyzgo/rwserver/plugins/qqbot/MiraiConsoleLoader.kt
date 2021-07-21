package com.github.minxyzgo.rwserver.plugins.qqbot

import com.github.dr.rwserver.data.global.Data
import com.github.dr.rwserver.util.log.Log
import com.github.minxyzgo.rwserver.plugins.qqbot.core.MiraiPlugin
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.launch
import net.mamoe.mirai.console.ConsoleFrontEndImplementation
import net.mamoe.mirai.console.MiraiConsoleImplementation.Companion.start
import net.mamoe.mirai.console.command.*
import net.mamoe.mirai.console.command.descriptor.ExperimentalCommandDescriptors
import net.mamoe.mirai.console.terminal.MiraiConsoleImplementationTerminal
import net.mamoe.mirai.console.util.ConsoleExperimentalApi
import net.mamoe.mirai.utils.MiraiLogger
import net.mamoe.mirai.utils.SimpleLogger

@ConsoleExperimentalApi
@ExperimentalCommandDescriptors
object MiraiConsoleLoader : MiraiConsoleImplementationTerminal() {

    private fun tagClog(tag: String, text: String) {
        Log.clog(" $tag $text ")
    }

    @JvmStatic
    @ConsoleFrontEndImplementation
    fun resolveConsole() {
        MiraiLogger.setDefaultLoggerCreator {
            SimpleLogger { priority, msg, throwable ->
                when (priority) {
                    SimpleLogger.LogPriority.WARNING -> {
                        tagClog("Warn/C", "warning: $msg")
                        throwable?.printStackTrace()
                    }
                    SimpleLogger.LogPriority.ERROR -> {
                        tagClog("Err/C", "error: $msg")
                        throwable?.printStackTrace()
                    }
                    SimpleLogger.LogPriority.INFO -> {
                        if (it?.startsWith("Bot") == true)
                            msg?.let { it1 -> tagClog("Info/C", it1) }
                        throwable?.printStackTrace()
                    }
                    else -> {
                        // ignore
                    }
                }
            }
        }

        this.start()
        val consoleLogger = MiraiLogger.create("consoleLogger")
        Data.SERVERCOMMAND.register(
            "mirai",
            "[command...]",
            "输出命令，重定向到Mirai-Console-Terminal"
        ) {
            var cmd = it.joinToString(" ", prefix = CommandManager.commandPrefix)
            launch {
                if(cmd == "?") {
                    cmd = CommandManager.commandPrefix + BuiltInCommands.HelpCommand.primaryName
                }
                try {

                    // consoleLogger.debug("INPUT> $next")

                    when (val result = ConsoleCommandSender.executeCommand(cmd)) {
                        is CommandExecuteResult.Success -> {
                        }
                        is CommandExecuteResult.IllegalArgument -> { // user wouldn't want stacktrace for a parser error unless it is in debugging mode (to do).
                            val message = result.exception.message
                            if (message != null) {
                                consoleLogger.warning(message)
                            } else consoleLogger.warning(result.exception)
                        }
                        is CommandExecuteResult.ExecutionFailed -> {
                            consoleLogger.error(result.exception)
                        }
                        is CommandExecuteResult.UnresolvedCommand -> {
                            consoleLogger.warning("未知指令: $cmd, 输入 ? 获取帮助")
                        }
                        is CommandExecuteResult.PermissionDenied -> {
                            consoleLogger.warning("权限不足.")
                        }
                        is CommandExecuteResult.UnmatchedSignature -> {
                            consoleLogger.warning (
                                """
                                    参数不匹配, 你是否想执行:
                                    ${result.command}
                                    ${result.call}
                                """
                            )
                        }
                        is CommandExecuteResult.Failure -> {
                            consoleLogger.warning(result.toString())
                        }
                    }
                } catch (e: InterruptedException) {
                    return@launch
                } catch (e: CancellationException) {
                    return@launch
                } catch (e: Throwable) {
                    consoleLogger.error("Unhandled exception", e)
                }
            }
        }
        MiraiPlugin.onEnable()
    }
}