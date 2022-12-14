import org.jetbrains.kotlin.gradle.tasks.KotlinCompile

plugins {
    id("fabric-loom")
    kotlin("jvm").version(System.getProperty("kotlin_version"))
    id("fr.stardustenterprises.rust.wrapper") version "3.2.5" apply false
    id("fr.stardustenterprises.rust.importer") version "3.2.5"
}

base { archivesName.set(project.extra["archives_base_name"] as String) }
version = project.extra["mod_version"] as String
group = project.extra["maven_group"] as String

repositories {}

dependencies {
    minecraft("com.mojang", "minecraft", project.extra["minecraft_version"] as String)
    mappings("net.fabricmc", "yarn", project.extra["yarn_mappings"] as String, null, "v2")
    modImplementation("net.fabricmc", "fabric-loader", project.extra["loader_version"] as String)
    modImplementation(
        "net.fabricmc.fabric-api",
        "fabric-api",
        project.extra["fabric_version"] as String
    )
    modImplementation(
        "net.fabricmc",
        "fabric-language-kotlin",
        project.extra["fabric_language_kotlin_version"] as String
    )
    rust(project(":d3r"))
}

subprojects {
    group = "net.liquidev.d3r"
    version = "0.1.0"
}

rustImport {
    baseDir.set("/d3r")
    layout.set("flat")
}

val mainSourceSet = sourceSets.main.get()

val datagenDir = layout.projectDirectory.dir("src").dir(mainSourceSet.name).dir("generated")
loom {
    runs {
        create("datagenClient") {
            inherit(runConfigs["client"])
            name("Data Generation")
            vmArg("-Dfabric-api.datagen")
            vmArg("-Dfabric-api.datagen.output-dir=$datagenDir")
            vmArg("-Dfabric-api.datagen.modid=dawd3")
            ideConfigGenerated(true)
            runDir("build/datagen")
        }
    }

    accessWidenerPath.set(file("src/main/resources/dawd3.accesswidener"))
}

sourceSets.named(mainSourceSet.name) {
    resources.srcDir(datagenDir)
}

tasks {
    val javaVersion = JavaVersion.toVersion((project.extra["java_version"] as String).toInt())

    withType<JavaCompile> {
        options.encoding = "UTF-8"
        sourceCompatibility = javaVersion.toString()
        targetCompatibility = javaVersion.toString()
        options.release.set(javaVersion.toString().toInt())
    }

    withType<KotlinCompile> {
        kotlinOptions {
            jvmTarget = javaVersion.toString()
        }
    }

    jar {
        from("LICENSE") { rename { "${it}_${base.archivesName}" } }
    }

    processResources {
        filesMatching("fabric.mod.json") {
            expand(
                mutableMapOf(
                    "version" to project.extra["mod_version"] as String,
                    "fabricloader" to project.extra["loader_version"] as String,
                    "fabric_api" to project.extra["fabric_version"] as String,
                    "fabric_language_kotlin" to project.extra["fabric_language_kotlin_version"] as String,
                    "minecraft" to project.extra["minecraft_version"] as String,
                    "java" to project.extra["java_version"] as String
                )
            )
        }
        filesMatching("*.mixins.json") { expand(mutableMapOf("java" to project.extra["java_version"] as String)) }
    }

    java {
        toolchain { languageVersion.set(JavaLanguageVersion.of(javaVersion.toString())) }
        sourceCompatibility = javaVersion
        targetCompatibility = javaVersion
        withSourcesJar()
    }
}
