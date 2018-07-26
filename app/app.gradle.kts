import com.github.jengelman.gradle.plugins.shadow.tasks.ShadowJar
import org.gradle.jvm.tasks.Jar
import java.nio.file.Files
import java.nio.file.StandardOpenOption

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
}

val dependencyProjects = listOf(
        project(":api"),
        project(":plugins:base"),
        project(":plugins:cameraserver"),
        project(":plugins:networktables"),
        project(":plugins:powerup")
)

dependencies {
    dependencyProjects.forEach {
        compile(it)
    }
    compile(group = "de.huxhorn.lilith", name = "de.huxhorn.lilith.3rdparty.junique", version = "1.0.4")
    compile(group = "com.github.samcarlberg", name = "update-checker", version = "+")
    compile(group = "com.github.zafarkhaja", name = "java-semver", version = "0.9.0")
    compile(group = "org.apache.commons", name = "commons-csv", version = "1.5")
    compile(group = "no.tornado", name = "fxlauncher", version = "1.0.20")

    fun aether(name: String) = create(group = "org.eclipse.aether", name = name, version = "1.1.0")
    compile(aether("aether-api"))
    compile(aether("aether-util"))
    compile(aether("aether-impl"))
    compile(aether("aether-connector-basic"))
    compile(aether("aether-transport-file"))
    compile(aether("aether-transport-http"))

    fun maven(name: String) = create(group = "org.apache.maven", name = name, version = "3.5.4")
    compile(maven("maven-core"))

    testCompile(project("test_plugins"))
}

val theMainClassName = "edu.wpi.first.shuffleboard.app.Main"

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

java.sourceSets["main"].resources {
    srcDir("src/main/generated/resources")
}

/**
 * @return [edu.wpi.first.wpilib.versioning.WPILibVersioningPluginExtension.version] value or null
 * if that value is the empty string.
 */
fun getWPILibVersion(): String? = if (WPILibVersion.version != "") WPILibVersion.version else null

fun Project.createDepsTask(): Task {
    return this.task("generateDeps") {
        val file = File("src/main/generated/resources/project-deps-${this@createDepsTask.name}.txt")
        outputs.file(file)
        doLast {
            fun generateStandardDeps(): String {
                val builder = StringBuilder()
                project.configurations.getByName("runtimeClasspath").resolvedConfiguration.firstLevelModuleDependencies.forEach {
                    it.moduleArtifacts.forEach {
                        val id = it.moduleVersion.id
                        if (!id.group.contains("shuffleboard") && id.group != "org.eclipse.aether" && id.group != "org.apache.maven" && id.group != "org.apache.commons") {
                            if (!it.file.name.endsWith("-all.jar") && !it.file.name.matches(Regex(".*linux|win|windows|mac.*$", RegexOption.IGNORE_CASE))) {
                                builder.append("${id.group}:${id.name}:${id.version}\n")
                            }
                        }
                    }
                }
                return builder.toString()
            }

            val standardDeps = generateStandardDeps()
            file.parentFile.mkdirs()
            file.createNewFile()
            Files.write(file.absoluteFile.toPath(), standardDeps.toByteArray(), StandardOpenOption.WRITE)
        }
    }
}

/**
 * Only include shuffleboard files (API, app, and plugins). All 3rd party libraries will be downloaded and cached at
 * runtime to reduce download size.
 */
tasks.withType<ShadowJar> {
    val function: (FileTreeElement) -> Boolean = {
        if (it.isDirectory || it.file == null || it.file.isDirectory) {
            // Note: the shadow plugin has a custom implementation of FileTreeElement that _always_ returns null for
            // the file. Since it is only used when resolving files from other projects (eg the API project),
            // we can safely assume the element represents a file in a subproject
            true
        } else {
            val file = it.file
            val absolutePath = file.absolutePath
            val replace = absolutePath.replace('\\', '/')
            //println("Checking $replace")
            replace.matches(Regex(pattern = "^META-INF|(.+/(aether|fxlauncher|maven|commons|http|plexus|api|app|plugin)).*$"))
        }
    }
    this@withType.include(function)
}

val generateDepsTask = createDepsTask()
dependencyProjects.forEach {
    generateDepsTask.dependsOn(it.createDepsTask())
}

val generateDepsFile = task("generateDepsFile") {
    dependsOn(generateDepsTask)
    doLast {
        generateDepsTask.outputs
    }
}

tasks.withType<ShadowJar> {
    dependsOn(generateDepsFile)
}

tasks.withType<JavaExec> {
    dependsOn(generateDepsFile)
}
