import org.jetbrains.kotlin.gradle.dsl.*

plugins {
    val kotlinVersion = "1.5.10"
    kotlin("jvm") version kotlinVersion
    kotlin("plugin.serialization") version kotlinVersion
    java
}

group = "com.github.minxyzgo.rwserver.plugins.qqbot"
version = "0.0.2"


val kotlinCoroutinesVersion: String by project
val miraiVersion: String by project
val exposedVersion: String by project
val sqliteVersion: String by project

repositories {
    maven{ url = uri("https://maven.aliyun.com/nexus/content/groups/public/")}
    maven{ url = uri("https://www.jitpack.io")}
    jcenter()
    mavenCentral()
    mavenLocal()
}

dependencies {
    compileOnly("org.xerial:sqlite-jdbc:$sqliteVersion")
    compileOnly("org.jetbrains.exposed:exposed-jdbc:$exposedVersion")
    compileOnly("org.jetbrains.exposed:exposed-core:$exposedVersion")
    implementation("io.github.config4k:config4k:0.4.2")
    implementation("net.mamoe:mirai-core:$miraiVersion") {
        exclude("com.squareup.okhttp3", "okhttp")
        exclude("com.squareup.okio", "okio")
        exclude("com.google.code.gson")
        exclude("org.jetbrains.kotlin", "kotlin-reflect")
        exclude("org.jetbrains.kotlinx", "kotlinx-coroutines-core")
        exclude("io.netty")
    }
    implementation("net.mamoe:mirai-console:$miraiVersion")
    implementation("net.mamoe:mirai-console-terminal:$miraiVersion")
    compileOnly("com.google.code.gson:gson:2.8.6")
    compileOnly("net.mamoe:mirai-console-terminal:$miraiVersion")
    compileOnly(fileTree(mapOf("dir" to "libs", "include" to listOf("*.jar"))))
    compileOnly("com.github.RW-HPS.RW-HPS:Server:4b2b348dae") {
        exclude("org.jetbrains.kotlin")
        exclude("org.jetbrains.kotlinx")
    }
    compileOnly("org.jetbrains.kotlinx:kotlinx-coroutines-core:$kotlinCoroutinesVersion")
    testImplementation("com.squareup.okhttp3:okhttp:4.9.1")
    testImplementation("io.netty:netty-all:4.1.66.Final")
    testImplementation("com.google.code.gson:gson:2.8.6")
    testImplementation("com.alibaba:fastjson:1.2.58")
    testImplementation("org.jetbrains.kotlinx:kotlinx-coroutines-core:$kotlinCoroutinesVersion")
    testImplementation("org.xerial:sqlite-jdbc:$sqliteVersion")
    testImplementation("org.jetbrains.exposed:exposed-jdbc:$exposedVersion")
    testImplementation("org.jetbrains.exposed:exposed-core:$exposedVersion")
    //testImplementation(fileTree(mapOf("dir" to "libs", "include" to listOf("*.jar"))))
    testImplementation("com.github.RW-HPS.RW-HPS:Server:4b2b348dae")
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

    from(rootDir) {
        val file = File("${rootDir}/plugin.json")
        val gson = com.google.gson.GsonBuilder().setPrettyPrinting().create()
        val json = gson.fromJson(file.readText(Charsets.UTF_8), Map::class.java).toMutableMap()
        json["version"] = archiveVersion.get()
        file.writeText(gson.toJson(json), Charsets.UTF_8)
        include("gradle.properties")
        rename("gradle.properties","version.properties")
        include("plugin.json")
    }

    from (
        configurations.runtimeClasspath.get().mapNotNull {
            when {
                it.isDirectory -> it
                it.name.contains
                    (Regex("kotlin-stdlib*")
                ) -> null
                else -> zipTree(it)
            }
        }
    )
}

tasks.withType(KotlinJvmCompile::class.java) {
    kotlinOptions.jvmTarget = "1.8"
}
