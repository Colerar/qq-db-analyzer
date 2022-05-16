import com.github.gmazzo.gradle.plugins.BuildConfigSourceSet
import org.jetbrains.kotlin.gradle.tasks.KotlinCompile
import java.time.ZoneOffset
import java.time.ZonedDateTime
import java.util.*

plugins {
  kotlin("jvm")
  kotlin("plugin.serialization")

  application

  id("com.github.johnrengelman.shadow")

  // code linter
  id("org.jlleitschuh.gradle.ktlint")
  id("org.jlleitschuh.gradle.ktlint-idea")

  // build config
  id("com.github.gmazzo.buildconfig")
}

group = "moe.sdl.analyzer.qqdb"
version = "0.1.0-DEV"

repositories {
  mavenCentral()
  maven("https://s01.oss.sonatype.org/content/repositories/snapshots/")
}

dependencies {
  // Coroutines
  implementation(KotlinX.coroutines.core)
  implementation(KotlinX.coroutines.jdk9)
  // Datetime
  implementation(KotlinX.datetime)
  // Serialization
  implementation(KotlinX.serialization.core)
  implementation(KotlinX.serialization.json)

  // IO
  implementation(Square.okio)

  // Console
  implementation("com.github.ajalt.clikt:clikt:_")
  implementation("com.github.ajalt.mordant:mordant:_")

  // Database
  implementation("org.jetbrains.exposed:exposed-core:_")
  implementation("org.jetbrains.exposed:exposed-dao:_")
  implementation("org.jetbrains.exposed:exposed-jdbc:_")
  implementation("org.jetbrains.exposed:exposed-kotlin-datetime:_")
  implementation("org.xerial:sqlite-jdbc:_")
  implementation("com.zaxxer:HikariCP:_")

  implementation("ch.qos.logback:logback-classic:_")
  implementation("io.github.microutils:kotlin-logging-jvm:_")

  // test only
  testImplementation(Kotlin.Test.junit5)
}

application {
  // Define the main class for the application.
  mainClass.set("$group.MainKt")
  applicationName = rootProject.name
}

tasks.test {
  useJUnitPlatform()
}

tasks.withType<KotlinCompile> {
  kotlinOptions.apply {
    jvmTarget = "17"
    freeCompilerArgs = arrayListOf("-opt-in=kotlin.RequiresOptIn")
  }
}


val commitHash by lazy {
  val commitHashCommand = "git rev-parse --short HEAD"
  Runtime.getRuntime().exec(commitHashCommand).inputStream.bufferedReader().readLine() ?: "UnkCommit"
}

val branch by lazy {
  val branchCommand = "git rev-parse --abbrev-ref HEAD"
  Runtime.getRuntime().exec(branchCommand).inputStream.bufferedReader().readLine() ?: "UnkBranch"
}

val epochTime: Long by lazy {
  ZonedDateTime.now(ZoneOffset.UTC).toEpochSecond()
}

fun BuildConfigSourceSet.string(name: String, value: String) = buildConfigField("String", name, "\"$value\"")
fun BuildConfigSourceSet.stringNullable(name: String, value: String?) =
  buildConfigField("String?", name, value?.let { "\"$value\"" } ?: "null")

fun BuildConfigSourceSet.long(name: String, value: Long) = buildConfigField("long", name, value.toString())
fun BuildConfigSourceSet.longNullable(name: String, value: Long?) =
  buildConfigField("Long?", name, value?.let { "$value" } ?: "null")


fun Project.getRootProjectLocalProps(): Map<String, String> {
  val file = project.rootProject.file("local.properties")
  return if (file.exists()) {
    file.reader().use {
      Properties().apply {
        load(it)
      }
    }.toMap().map {
      it.key.toString() to it.value.toString()
    }.toMap()
  } else emptyMap()
}

val props = getRootProjectLocalProps()

buildConfig {
  packageName("$group.config")
  useKotlinOutput { topLevelConstants = true }
  string("VERSION", version.toString())
  string("BUILD_BRANCH", branch)
  string("COMMIT_HASH", commitHash)
  long("BUILD_EPOCH_TIME", epochTime)
  string("PROJECT_URL", "https://github.com/Colerar/qq-db-analyzer")

  sourceSets["test"].apply {
    stringNullable("TEST_DB_PATH", props["test.dbfile"])
    longNullable("TEST_GROUP", props["test.group"]?.toLongOrNull())
  }
}
