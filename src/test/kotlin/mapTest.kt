import com.github.minxyzgo.rwserver.plugins.qqbot.util.parseMapType
import java.io.File

fun main() {
    for(fi in File(".").listFiles()) {
        println(fi.name)
    }
    println(parseMapType(Thread.currentThread().contextClassLoader.getResourceAsStream("test.tmx")))
}