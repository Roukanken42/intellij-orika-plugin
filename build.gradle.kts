import de.undercouch.gradle.tasks.download.Download
import io.gitlab.arturbosch.detekt.Detekt
import org.gradle.api.tasks.testing.logging.TestExceptionFormat
import org.jetbrains.changelog.closure
import org.jetbrains.changelog.markdownToHTML
import org.jetbrains.kotlin.gradle.tasks.KotlinCompile

plugins {
    // Java support
    id("java")
    // Kotlin support
    id("org.jetbrains.kotlin.jvm") version "1.4.10"
    // gradle-intellij-plugin - read more: https://github.com/JetBrains/gradle-intellij-plugin
    id("org.jetbrains.intellij") version "0.4.22"
    // gradle-changelog-plugin - read more: https://github.com/JetBrains/gradle-changelog-plugin
    id("org.jetbrains.changelog") version "0.5.0"
    // detekt linter - read more: https://detekt.github.io/detekt/gradle.html
    id("io.gitlab.arturbosch.detekt") version "1.13.1"
    // ktlint linter - read more: https://github.com/JLLeitschuh/ktlint-gradle
    id("org.jlleitschuh.gradle.ktlint") version "9.4.0"
    id("de.undercouch.download") version "4.0.4"
}

// Import variables from gradle.properties file
val pluginGroup: String by project
// `pluginName_` variable ends with `_` because of the collision with Kotlin magic getter in the `intellij` closure.
// Read more about the issue: https://github.com/JetBrains/intellij-platform-plugin-template/issues/29
val pluginName_: String by project
val pluginVersion: String by project
val pluginSinceBuild: String by project
val pluginUntilBuild: String by project

val platformType: String by project
val platformVersion: String by project
val platformDownloadSources: String by project

group = pluginGroup
version = pluginVersion

// Configure project's dependencies
repositories {
    mavenCentral()
    jcenter()
}
dependencies {
    detektPlugins("io.gitlab.arturbosch.detekt:detekt-formatting:1.13.1")

    implementation("ma.glasnost.orika:orika-core:1.5.4")
//    implementation("org.jetbrains.kotlin:kotlin-reflect:1.4.10")

    testImplementation("io.strikt:strikt-core:latest.release")
//    testImplementation("org.junit.jupiter:junit-jupiter-api:5.4.2")
//    testRuntimeOnly("org.junit.jupiter:junit-jupiter-engine:5.4.2")
}

// Configure gradle-intellij-plugin plugin.
// Read more: https://github.com/JetBrains/gradle-intellij-plugin
intellij {
    pluginName = pluginName_
    version = platformVersion
    type = platformType
    downloadSources = platformDownloadSources.toBoolean()
    updateSinceUntilBuild = true

//  Plugin Dependencies:
//  https://www.jetbrains.org/intellij/sdk/docs/basics/plugin_structure/plugin_dependencies.html
//
    setPlugins("java")
}

// Configure detekt plugin.
// Read more: https://detekt.github.io/detekt/kotlindsl.html
detekt {
    config = files("./detekt-config.yml")
    buildUponDefaultConfig = true

    reports {
        html.enabled = false
        xml.enabled = false
        txt.enabled = false
    }
}

tasks {
    // Set the compatibility versions to 1.8
    withType<JavaCompile> {
        sourceCompatibility = "1.8"
        targetCompatibility = "1.8"
    }
    listOf("compileKotlin", "compileTestKotlin").forEach {
        getByName<KotlinCompile>(it) {
            kotlinOptions.jvmTarget = "1.8"
        }
    }

    val libs = register<Sync>("libs") {
        from(configurations.runtimeClasspath)
        into("$buildDir/libs")
        preserve {
            include("intellij-orika-plugin-*.jar")
        }
        rename("orika-core-1.5.4.jar", "orika.jar")
    }

    val mockJdkVersion = "JDK-1.8"
    val location = "https://github.com/JetBrains/intellij-community/raw/master/java/mock$mockJdkVersion"
    val destination = "$buildDir/mock$mockJdkVersion"
    val downloadMockJdk = register<Download>("downloadMockJdk") {
        src(arrayOf(
            "$location/jre/lib/annotations.jar",
            "$location/jre/lib/rt.jar"
        ))
        dest("$destination/jre/lib/")
        overwrite(false)
        quiet(false)
    }

    test {
//        useJUnitPlatform()
        dependsOn(libs, downloadMockJdk)
        testLogging {
            exceptionFormat = TestExceptionFormat.FULL
            showStandardStreams = true
        }
    }

    withType<Detekt> {
        jvmTarget = "1.8"
    }

    patchPluginXml {
        version(pluginVersion)
        sinceBuild(pluginSinceBuild)
        untilBuild(pluginUntilBuild)

        // Extract the <!-- Plugin description --> section from README.md and provide for the plugin's manifest
        pluginDescription(
            closure {
                File("./README.md").readText().lines().run {
                    val start = "<!-- Plugin description -->"
                    val end = "<!-- Plugin description end -->"

                    if (!containsAll(listOf(start, end))) {
                        throw GradleException("Plugin description section not found in README.md file:\n$start ... $end")
                    }
                    subList(indexOf(start) + 1, indexOf(end))
                }.joinToString("\n").run { markdownToHTML(this) }
            }
        )

        // Get the latest available change notes from the changelog file
        changeNotes(
            closure {
                changelog.getLatest().toHTML()
            }
        )
    }

    publishPlugin {
        dependsOn("patchChangelog")
        token(System.getenv("PUBLISH_TOKEN"))
        channels(pluginVersion.split('-').getOrElse(1) { "default" }.split('.').first())
    }
}
