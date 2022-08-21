import org.apache.tools.ant.taskdefs.Mkdir
import org.jetbrains.kotlin.gradle.tasks.KotlinCompile

plugins {
    kotlin("jvm") version "1.7.10"
}

group = "com.vygovskiy.vpplugin"
version = "1.0-SNAPSHOT"

val userHome = System.getProperty("user.home")
val userName = System.getProperty("user.name")
val vpPluginsDir = "${userHome}\\AppData\\Roaming\\VisualParadigm\\plugins"
val vpPluginName = project.name
val vpPluginDir = "$vpPluginsDir/$vpPluginName"

repositories {
    mavenCentral()
}

dependencies {
    compileOnly(files("lib/openapi.jar"))
    testImplementation(kotlin("test"))
}

tasks.test {
    useJUnitPlatform()
}

tasks.withType<KotlinCompile> {
    kotlinOptions.jvmTarget = "11"
}

//java {
//    toolchain {
//        languageVersion.set(JavaLanguageVersion.of(11))
//    }
//}


val createPluginDir by tasks.register("vpCreatePluginDir") {
    group = "Visual Paradigm"
    description = "Create plugin's directory"

    doLast {
        mkdir(vpPluginDir)
    }
}

tasks.register("vpInstallPlugin") {
    group = "Visual Paradigm"
    description = "Copy all plugins file into Visual Paradigm plugins folder"

    dependsOn(
        copyClasses,
        copyDependenciesJars
    )

}


val deletePluginDir by tasks.register<Delete>("vpDeletePluginDir") {
    group = "Visual Paradigm"
    description = "Delete plugin's folder"
    delete(vpPluginDir)
}


val copyClasses by tasks.register<Copy>("vpCopyClasses") {
    group = "Visual Paradigm"
    description = "Compile and copy classes into plugins folder. Use it for hot-reload "

    dependsOn("build")
    from(
        "$buildDir/classes/kotlin/main",
        "$buildDir/resources/main"
    )
    into(vpPluginDir)
}

val copyDependenciesJars by tasks.register<Copy>("vpCopyDependenciesJars") {
    group = "Visual Paradigm"
    description = "Copy plugin's dependencies into plugin folder"

    dependsOn(copyPluginXml)

    from(
        getJarDependencies()
            .map {it.absoluteFile}
            .toTypedArray()
    )
    into("$vpPluginDir/lib")
}

val copyPluginXml by tasks.register("vpCopyPluginXml") {
    group = "Visual Paradigm"
    description = "Prepare and copy plugin.xml into plugin folder"

    dependsOn(createPluginDir)

    doLast {
        val runtimeSection = getJarDependencies().joinToString(
            separator = "\n",
            prefix = "\n  <runtime>\n",
            postfix = "\n  </runtime>\n"
        ) {"    <library path='lib/${it.name}' relativePath='true'/>"}

        val pluginXmlFile = File("$buildDir/resources/main/plugin.xml").readText()
        val content = pluginXmlFile
            .replace("<runtime/>", runtimeSection, false)
            .replace("#pluginId#", vpPluginName)
            .replace("#user#", userName)
        File("$vpPluginDir/plugin.xml").writeText(content)
    }
}

fun getJarDependencies(): FileCollection {
    return configurations.runtimeClasspath.get()
        .filter {it.name.endsWith("jar")}

}