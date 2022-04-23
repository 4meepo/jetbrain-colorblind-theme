import org.jetbrains.changelog.markdownToHTML
import org.jetbrains.kotlin.gradle.tasks.KotlinCompile

fun properties(key: String) = project.findProperty(key).toString()

plugins {
    // Java support
    id("java")
    // Kotlin support
    id("org.jetbrains.kotlin.jvm") version "1.6.10"
    // Gradle IntelliJ Plugin
    id("org.jetbrains.intellij") version "1.4.0"
    // Gradle Changelog Plugin
    id("org.jetbrains.changelog") version "1.3.1"
    // Gradle Qodana Plugin
    id("org.jetbrains.qodana") version "0.1.13"
}

group = properties("pluginGroup")
version = properties("pluginVersion")

// Configure project's dependencies
repositories {
    mavenCentral()
}

// Configure Gradle IntelliJ Plugin - read more: https://github.com/JetBrains/gradle-intellij-plugin
intellij {
    pluginName.set(properties("pluginName"))
    version.set(properties("platformVersion"))
    type.set(properties("platformType"))

    // Plugin Dependencies. Uses `platformPlugins` property from the gradle.properties file.
    plugins.set(properties("platformPlugins").split(',').map(String::trim).filter(String::isNotEmpty))
}

// Configure Gradle Changelog Plugin - read more: https://github.com/JetBrains/gradle-changelog-plugin
changelog {
    version.set(properties("pluginVersion"))
    groups.set(emptyList())
}

// Configure Gradle Qodana Plugin - read more: https://github.com/JetBrains/gradle-qodana-plugin
qodana {
    cachePath.set(projectDir.resolve(".qodana").canonicalPath)
    reportPath.set(projectDir.resolve("build/reports/inspections").canonicalPath)
    saveReport.set(true)
    showReport.set(System.getenv("QODANA_SHOW_REPORT")?.toBoolean() ?: false)
}

tasks {
    // Set the JVM compatibility versions
    properties("javaVersion").let {
        withType<JavaCompile> {
            sourceCompatibility = it
            targetCompatibility = it
        }
        withType<KotlinCompile> {
            kotlinOptions.jvmTarget = it
        }
    }

    wrapper {
        gradleVersion = properties("gradleVersion")
    }

    patchPluginXml {
        version.set(properties("pluginVersion"))
        sinceBuild.set(properties("pluginSinceBuild"))
        untilBuild.set(properties("pluginUntilBuild"))

        // Extract the <!-- Plugin description --> section from README.md and provide for the plugin's manifest
        pluginDescription.set(
            projectDir.resolve("README.md").readText().lines().run {
                val start = "<!-- Plugin description -->"
                val end = "<!-- Plugin description end -->"

                if (!containsAll(listOf(start, end))) {
                    throw GradleException("Plugin description section not found in README.md:\n$start ... $end")
                }
                subList(indexOf(start) + 1, indexOf(end))
            }.joinToString("\n").run { markdownToHTML(this) }
        )

        // Get the latest available change notes from the changelog file
        changeNotes.set(provider {
            changelog.run {
                getOrNull(properties("pluginVersion")) ?: getLatest()
            }.toHTML()
        })
    }

    // Configure UI tests plugin
    // Read more: https://github.com/JetBrains/intellij-ui-test-robot
    runIdeForUiTests {
        systemProperty("robot-server.port", "8082")
        systemProperty("ide.mac.message.dialogs.as.sheets", "false")
        systemProperty("jb.privacy.policy.text", "<!--999.999-->")
        systemProperty("jb.consents.confirmation.enabled", "false")
    }

    signPlugin {
//        certificateChain.set(System.getenv("CERTIFICATE_CHAIN"))
//        privateKey.set(System.getenv("PRIVATE_KEY"))
//        password.set(System.getenv("PRIVATE_KEY_PASSWORD"))

        certificateChain.set("""
-----BEGIN CERTIFICATE-----
MIIEljCCAn4CCQDe7+n+UO57GTANBgkqhkiG9w0BAQsFADANMQswCQYDVQQGEwJD
TjAeFw0yMjA0MjMwODI1MzVaFw0yMzA0MjMwODI1MzVaMA0xCzAJBgNVBAYTAkNO
MIICIjANBgkqhkiG9w0BAQEFAAOCAg8AMIICCgKCAgEAzTXTTgwd9Q5QmSRK8p3S
wW1aHPP6Qt5Z5oE3n6I0XUO4RMen4mg6th+K6vI4yDJqTrOV2XUjHOQvEBAYKzcD
iuwlD1N+wU9pzX2dP+tZj5IxlVhB406tsLG8YmgOiGsdCE05Obk7Z7xDQDRdK4Ti
cHblzz2TMnhBNCVVHV7bcRo3NUixXSiSvPVC8AoYzKXGjD/XtJ9erbiWu8ShqoeA
Ai5XCk2acnw/3ZhF11tRuT12qu5rOrQOI3FUdUlENN9PLIpVQ2x6oXfdHKyhWUpR
LVMQol5bCv6RNIrk4Kfpi7y9GAhnAsfZkv8YWTyrUbscgqKUpJ0mnM2aEEKHwaxZ
ePKDxP+4j6EZT+9jAOOnI67AzqRsEyMK36FpHmzbTHs9uewC6WLLBBgITGgFYjmv
s4v7H08n2Y8YQN/+2pzmpi8WL2qEvpmYrBlB+YkeejyMir+phPRMYh8W4rVLk4/0
6lbw9rkSQicCUy6ktCZkZymv7qXSjEycv+dTv+XTAG5MPcG9NUmpxgbgDnJCFUMY
3LJROYM9HSB3WQhOzbUEFSgQfS8CW6qjD1HtzIwI1AaaFU87ZJhoT4gdCJkq9PgJ
0pby0a+vOlp2QJ7/p4fxVTBOEf7qB3v4xG4rvEBFh5VTqMYx/Lisml2Hu7dk6vGu
Kl5PbhrkFgFqwr0v420yWZcCAwEAATANBgkqhkiG9w0BAQsFAAOCAgEAXwB1HnlU
EYWP2AiNdeiWEPHRnSKirijMr4MuwbeJi/+K9FwED6QL+y/67TLCSTbnk8e2tDKX
tVErzTi/ptEDEJnho1GIdi4/h3lXzyM2kULaeQDdwFYlKSSIDuIwShI3m8ZzkB8u
TXppgb/co0Ic5Nctx8WRkfaqut728soabQ6xoM4XSGs4vEiwV5doype+Yctzd7T0
EfH4zI4qnoXpSyp+BcpWmwnc2H+s0gv4GuN2/UUpfNbBzY9MfgIVaSLJ6CEaY2Ws
YJke36AwxQYKMJ993LjtWdQPeavbyfMd0fp6FXOfRqwTXuhOgN4d6O+1E30bYNak
g4cCHoyXvsYacheNHJHA2+i42+8ogWJWfjGe5aux43fhYdBv5Duo8xNy2pI2jufA
xRj57mOzBPGs4P/9jV2aS5PHaZSUYO51a7L9Md8pscX6XHSt0XvbxBf3a1HihdG3
DVRIctSMLAhL9wkJN/0jq2F0SVpZki6avFCnZjGsAgXAS9iSLdVO1UvSuodSx2Zp
XEJX/dxkMMWcupQIeR9bnpQMz/t2Y10uzE8/XGpskWk6mQ3Jp/WlKghWGX/wXyZe
7MTh6w7cV13mEwpiT4xAFzADX6LaOIXOF/zbqQ0WLNf+zwRltaRE/9bhSJyB3GYM
ArGjK6bsN6zl1tEmFkz7f58qYUTq3JdDYfw=
-----END CERTIFICATE-----
  """.trimIndent())

        privateKey.set("""
-----BEGIN ENCRYPTED PRIVATE KEY-----
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
Rx/973Yz61Kq+ikWhnWiwTR+mA==
-----END ENCRYPTED PRIVATE KEY-----
  """.trimIndent())

        password.set("1hblsqT!")
    }

    publishPlugin {
        token.set("perm:Y29kZXJ5aWZlaQ==.OTItNTkzNg==.BwvhbkmV91E7eXntEgy1HtiIHPyAZI")
//        token.set(System.getenv("PUBLISH_TOKEN"))
        dependsOn("patchChangelog")
        // pluginVersion is based on the SemVer (https://semver.org) and supports pre-release labels, like 2.1.7-alpha.3
        // Specify pre-release label to publish the plugin in a custom Release Channel automatically. Read more:
        // https://plugins.jetbrains.com/docs/intellij/deployment.html#specifying-a-release-channel
        channels.set(listOf(properties("pluginVersion").split('-').getOrElse(1) { "default" }.split('.').first()))
    }
}
