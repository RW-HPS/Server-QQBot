package com.github.minxyzgo.rwserver.plugins.qqbot.util

import com.github.dr.rwserver.data.global.Data
import com.github.dr.rwserver.game.GameMaps
import com.github.dr.rwserver.util.Time
import com.github.dr.rwserver.util.file.FileUtil
import com.github.dr.rwserver.util.zip.zip.ZipDecoder
import com.github.minxyzgo.rwserver.plugins.qqbot.QQBotPlugin
import net.mamoe.mirai.console.command.descriptor.ExperimentalCommandDescriptors
import net.mamoe.mirai.console.util.ConsoleExperimentalApi
import net.mamoe.mirai.contact.Group
import net.mamoe.mirai.utils.RemoteFile
import org.w3c.dom.Element
import org.w3c.dom.Node
import org.w3c.dom.NodeList
import java.io.BufferedOutputStream
import java.io.FileOutputStream
import java.io.InputStream
import java.net.HttpURLConnection
import java.net.URL
import javax.xml.parsers.DocumentBuilderFactory

/**
 *@return 返回插件运行时间。以毫秒为单位。
 */
@ConsoleExperimentalApi
@ExperimentalCommandDescriptors
fun operationTime() = Time.millis() - QQBotPlugin.startTime

/**
 * 以指定map名和指定后缀从[Data.Plugin_Maps_Path]获取文件输入流。
 * @param mapName 指定的地图名
 * @param suffix 指定的后缀
 * @return 返回InputStream。如果给定FileUtil没有被创建或是文件夹，则返回null
 */
fun getMapFileInputStreamBySuffix(mapName: String, suffix: String): InputStream? {
    val mapData = Data.game.mapsData[mapName]
    val fileUtil = FileUtil.file(Data.Plugin_Maps_Path)

    return mapData.run {
        when (mapData.mapFileType) {
            GameMaps.MapFileType.file -> fileUtil.toPath(mapFileName + suffix).file.run { if(exists() && !isDirectory) inputStream() else null }
            GameMaps.MapFileType.zip -> ZipDecoder(fileUtil.toPath(zipFileName).toPath(mapFileName + suffix).file.apply { if(!exists() || isDirectory) return@run null }).stream
            else -> throw UnsupportedOperationException("Unsupported web file")
        }
    }
}

inline fun NodeList.forEach(action: (Node) -> Unit) {
    for(i in 0 until length) {
        action(item(i))
    }
}

//will support value class
/**
 * 传入一个map(tmx)的输入流，解析后并返回其地图模式
 *
 * 一个可以解析的简单的map结构:
 *
 * <p>
 *
 *     <map>
 *         <objectgroup name="Triggers">
 *             <object name="map_info" x="40" y="41" width="203" height="122">
 *                 <properties>
 *                     <property name="type" value="skirmish"/>
 *                 </properties>
 *            </object>
 *         </objectgroup>
 *     </map>
 * </p>
 *
 * 这将会返回 skirmish
 * @param xmlInput 要解析的地图输入流
 * @return 要返回的地图模式。如果没有，则返回 null
 */
fun parseMapType(xmlInput: InputStream): String? {
    var result: String? = null
    val domParserFactory = DocumentBuilderFactory.newInstance()
    val docBuilder = domParserFactory.newDocumentBuilder()
    val doc = docBuilder.parse(xmlInput)
    val root = doc.documentElement
    val nodeList = root.getElementsByTagName("objectgroup")
    try {
        nodeList.forEach loop@{ node ->
            if (node.nodeType == Node.ELEMENT_NODE) {
                (node as Element).getElementsByTagName("object").forEach objectEach@{ obj ->
                    if ((obj as Element).getAttribute("name") != "map_info") return@objectEach
                    val properties = obj.getElementsByTagName("properties")
                    val base = properties.item(0)
                    (base as Element).getElementsByTagName("property").forEach { property ->
                        println("pro:${property.nodeName}")
                        if ((property as Element).getAttribute("name") == "type") {
                            result = property.getAttribute("value")
                            return@loop
                        }
                    }
                }
            }
        }
    } catch(e: Exception) {
        e.printStackTrace()
    }

    return result
}

/**
 * 根据给定url和，path来下载文件。你有义务捕捉任何错误。
 * @see downloadMapFileFromGroup
 * @see downloadAnyFromGroup
 * @param url 给定下载链接
 * @param savePath 保存文件路径
 */
@Throws(Exception::class)
fun downloadFile(url: String, savePath: String) {
    val conn = URL(url).openConnection() as HttpURLConnection
    conn.connect()
    conn.inputStream.use { input ->
        BufferedOutputStream(FileOutputStream(savePath)).use { output ->
            input.copyTo(output)
        }
    }

    conn.disconnect()
}

/**
 * 从一个群中下载地图，将自动下载 [path].tmx或[path].save。如果在同一目录下找到[path]_map.png，也自动下载。
 * 会将其保存在 [Data.Plugin_Maps_Path]。
 * @see downloadFile
 * @param group 从给定群下载文件
 * @param path 群文件的下载路径，根目录为[RemoteFile.ROOT_PATH]
 * @param maxSize 下载文件最大大小
 * @return 返回下载的结果
 */
suspend fun downloadMapFileFromGroup(group: Group, path: String, maxSize: Long): DownloadResult {
    val files = group.filesRoot
    //不使用FileUtil是因为会将其创建为文件夹
    val userDir = System.getProperty("user.dir")
    val map = files.resolve("$path.tmx").run { if (exists()) this else files.resolve("$path.save") }
    if (!map.exists() || map.isDirectory()) return DownloadResult.NotFound
    if(map.length() > maxSize) return DownloadResult.OutOfMaxSize
    val png = files.resolve("${path}_map.png")
    return map.getDownloadInfo()?.run {

        var result: DownloadResult? = null
        val savePath = "${Data.Plugin_Maps_Path}/${map.name}"
        try { downloadFile(url, userDir + savePath)} catch (e: Exception) {
            e.printStackTrace()
            result = DownloadResult.DownloadFailed
        }
        if(png.exists() && !png.isDirectory() && png.length() <= maxSize) png.getDownloadInfo()?.let {
            try {
                downloadFile(it.url, userDir + "${Data.Plugin_Maps_Path}/${png.name}")
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
        result ?: DownloadResult.Success
    } ?: DownloadResult.NoDownloadInfo
}

/**
 * 从给定群中下载任何文件。
 * @see downloadMapFileFromGroup
 */
suspend fun downloadAnyFromGroup(group: Group, path: String, savePath: String): DownloadResult {
    val files = group.filesRoot
    val reallySavePath = if(savePath.startsWith("/")) savePath else "/$savePath"
    return files.resolve(path).getDownloadInfo()?.run {
        var result: DownloadResult? = null
        try {
            downloadFile(url, reallySavePath)
        } catch (e: Exception) {
            e.printStackTrace()
            result = DownloadResult.DownloadFailed
        }
        result ?: DownloadResult.Success
    } ?: DownloadResult.NotFound
}

enum class DownloadResult {
    Success, NotFound, NoDownloadInfo, DownloadFailed, OutOfMaxSize
}