
import org.gradle.jvm.tasks.Jar

plugins {
    application
}
apply {
    plugin("com.github.johnrengelman.shadow")
    plugin("maven-publish")
}

description = """
All of the application specific code that makes shuffleboard run.
""".trimMargin()

repositories {
    maven {
        setUrl("https://dl.bintray.com/samcarlberg/maven-artifacts/")
    }
    maven {
        setUrl("https://dl.bintray.com/jerady/maven/")
    }
}

dependencies {
    compile(project(":api"))
    compile(project(path = ":plugins:base"))
    compile(project(path = ":plugins:cameraserver"))
    compile(project(path = ":plugins:networktables"))
    compile(project(path = ":plugins:powerup"))
    compile(group = "de.huxhorn.lilith", name = "de.huxhorn.lilith.3rdparty.junique", version = "1.0.4")
    compile(group = "com.github.samcarlberg", name = "update-checker", version = "+")
    compile(group = "com.github.samcarlberg", name = "fxbehaviors", version = "0.1.0-java8") // TODO remove java8 classifier when we move to Java 10/11
    compile(group = "com.github.zafarkhaja", name = "java-semver", version = "0.9.0")
    compile(group = "org.apache.commons", name = "commons-csv", version = "1.5")
    compile(group = "de.jensd", name = "fontawesomefx", version = "8.9")
    testCompile(project("test_plugins"))
}

val theMainClassName = "edu.wpi.first.shuffleboard.app.Shuffleboard"

application {
    mainClassName = theMainClassName
    applicationDefaultJvmArgs = listOf("-Xverify:none", "-Dprism.order=d3d,es2,sw")
}

tasks.withType<Jar> {
    manifest {
        attributes["Main-Class"] = theMainClassName
    }
}

/**
 * Make tests get the most recent version of the test plugin jar.
 */
tasks.withType<Test> {
    dependsOn(project("test_plugins").tasks["jar"])
}

/**
 * Lets tests use the output of the test_plugins build.
 */
java.sourceSets["test"].resources.srcDirs += File(project("test_plugins").buildDir, "libs")

/**
 * @return [edu.wpi.first.wpilib.versioning.WPILibVersioningPluginExtension.version] value or null
 * if that value is the empty string.
 */
fun getWPILibVersion(): String? = if (WPILibVersion.version != "") WPILibVersion.version else null
