import org.jetbrains.kotlin.gradle.dsl.KotlinJvmCompile

plugins {
    val kotlinVersion = "1.5.10"
    kotlin("jvm") version kotlinVersion
    kotlin("plugin.serialization") version kotlinVersion
    java
}

group = "com.github.minxyzgo.rwserver.plugins.qqbot"
version = "0.0.1"


val kotlinCoroutinesVersion = "1.5.0"
val miraiVersion = "2.7-M2"

repositories {
    maven{ url = uri("https://maven.aliyun.com/nexus/content/groups/public/")}
    maven{ url = uri("https://www.jitpack.io")}
    jcenter()
    mavenCentral()
    mavenLocal()
}

dependencies {
    implementation("net.mamoe:mirai-core:$miraiVersion") {
        exclude("com.squareup.okhttp3", "okhttp")
        exclude("com.squareup.okio", "okio")
        exclude("com.google.code.gson")
        exclude("org.jetbrains.kotlin", "kotlin-reflect")
        exclude("io.netty")
    }
    implementation("net.mamoe:mirai-console:$miraiVersion")
    implementation("net.mamoe:mirai-console-terminal:$miraiVersion")
    compileOnly(fileTree(mapOf("dir" to "libs", "include" to listOf("*.jar"))))
    compileOnly("com.github.RW-HPS.RW-HPS:Server:-SNAPSHOT")
    compileOnly("org.jetbrains.kotlinx:kotlinx-coroutines-core:$kotlinCoroutinesVersion")
    testImplementation(fileTree(mapOf("dir" to "libs", "include" to listOf("*.jar"))))
    testImplementation("com.github.RW-HPS.RW-HPS:Server:-SNAPSHOT")
    testImplementation("org.jetbrains.kotlinx:kotlinx-coroutines-test:$kotlinCoroutinesVersion")
}

kotlin.sourceSets.all {
    languageSettings.useExperimentalAnnotation("net.mamoe.mirai.console.ConsoleFrontEndImplementation")
}

tasks.jar {
    manifest {
        attributes(
            mapOf(
                "Manifest-Version" to 1.0,
                "Main-Class" to "com.github.minxyzgo.rwserver.plugins.qqbot.QQBotPlugin"
             )
        )
    }

    //过滤所有不必要的便签信息
    exclude("META-INF/*.kotlin_module")
    exclude("META-INF/*.SF")
    exclude("META-INF/*.DSA")
    exclude("META-INF/LICENSE.txt")
    exclude("META-INF/NOTICE.txt")

    @Suppress("UNCHECKED_CAST")
    from(rootDir) {
        val file = File("${rootDir}/plugin.json")
        val gson = com.google.gson.GsonBuilder().setPrettyPrinting().create()
        val json = gson.fromJson(file.readText(Charsets.UTF_8), Map::class.java).toMutableMap()
        json["version"] = archiveVersion.get()
        file.writeText(gson.toJson(json), Charsets.UTF_8)
        include("plugin.json")
    }

    from (
        configurations.runtimeClasspath.get().mapNotNull {
            when {
                it.isDirectory -> it
                it.name.contains(Regex("kotlin-stdlib*")) -> null
                else -> zipTree(it)
            }
        }
    )
}

tasks.withType(KotlinJvmCompile::class.java) {
    kotlinOptions.jvmTarget = "1.8"
}
