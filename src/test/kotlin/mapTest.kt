import com.github.minxyzgo.rwserver.plugins.qqbot.util.*
import java.io.*

fun main() {
    for(fi in File(".").listFiles()) {
        println(fi.name)
    }
    println(parseMapType(Thread.currentThread().contextClassLoader.getResourceAsStream("test.tmx")))
}
