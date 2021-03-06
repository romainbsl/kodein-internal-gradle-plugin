package org.kodein.internal.gradle.settings

import org.gradle.api.Plugin
import org.gradle.api.initialization.Settings
import org.gradle.api.internal.plugins.DslObject
import org.gradle.kotlin.dsl.get
import org.gradle.kotlin.dsl.maven
import org.kodein.internal.gradle.KodeinVersions
import java.io.File
import java.util.*

@Suppress("UnstableApiUsage")
class KodeinSettingsPlugin : Plugin<Settings> {

    private lateinit var settings: Settings

    private val localProperties: Properties by lazy {
        File("${settings.rootDir}/kodein.local.properties")
            .takeIf { it.exists() }
            ?.inputStream()
            ?.use { Properties().apply { load(it) } }
            ?: Properties()

    }

    fun findLocalProperty(key: String): String? =
        System.getenv("KODEIN_LOCAL_${key.toUpperCase()}")
            ?: localProperties.getProperty(key)
            ?: DslObject(settings).asDynamicObject.tryGetProperty("org.kodein.local.$key")
                .takeIf { it.isFound } ?.value as String?

    private fun Settings.applyPlugin() {

        // https://github.com/gradle/gradle/issues/11412
        System.setProperty("org.gradle.internal.publish.checksums.insecure", "true")

        val version = buildscript.configurations["classpath"].dependencies.first { it.group == "org.kodein.internal.gradle" && it.name == "kodein-internal-gradle-settings" } .version

        pluginManagement {
            repositories {
                mavenLocal()
                jcenter()
                google()
                maven(url = "https://plugins.gradle.org/m2/")
                maven(url = "https://dl.bintray.com/kodein-framework/Kodein-Internal-Gradle")
                maven(url = "https://dl.bintray.com/kodein-framework/kodein-dev")
                maven(url = "https://dl.bintray.com/kotlin/kotlin-eap")
            }

            resolutionStrategy {
                eachPlugin {
                    val id = requested.id.id
                    when {
                        id.startsWith("org.kodein.") -> useModule("org.kodein.internal.gradle:kodein-internal-gradle-plugin:$version")
                        id == "kotlinx-serialization" -> useModule("org.jetbrains.kotlin:kotlin-serialization:${KodeinVersions.kotlin}")
                    }
                }
            }
        }
    }

    override fun apply(settings: Settings) {
        this.settings = settings
        settings.applyPlugin()
    }

    companion object {
        fun get(settings: Settings): KodeinSettingsPlugin = settings.plugins.getPlugin(KodeinSettingsPlugin::class.java)
    }
}
