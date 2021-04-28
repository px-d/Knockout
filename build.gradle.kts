import org.jetbrains.kotlin.gradle.tasks.KotlinCompile
import com.github.jengelman.gradle.plugins.shadow.tasks.ShadowJar

plugins {
    java
    kotlin("jvm") version "1.4.31"
    id("com.github.johnrengelman.shadow") version "6.1.0"
}

val jvmVersion = JavaVersion.VERSION_1_8
val jvmVersionString = jvmVersion.majorVersion.let { if (it.toInt() <= 10) "1.$it" else it }

group = "de.soho5k"
version = "1.0"

repositories {
    mavenCentral()
    mavenLocal()
    maven("https://jitpack.io")
    maven("https://repo.codemc.io/repository/maven-snapshots/")
    maven("http://repo.onarandombox.com/content/groups/public/")
}
dependencies {
    implementation(kotlin("stdlib"))
    compileOnly("org.spigotmc:spigot:1.16.5-R0.1-SNAPSHOT")
    implementation("net.axay", "kspigot", "1.16.26")
    compileOnly("com.onarandombox.multiversecore:Multiverse-Core:4.2.2")
    compileOnly("redis.clients:jedis:3.5.1")
    compileOnly("de.hglabor:server-queue-core:0.0.2")
}
tasks.withType<KotlinCompile> {
    kotlinOptions.jvmTarget = jvmVersionString
}
tasks {
    shadowJar {
        relocate("net.axay.kspigot", "de.soho5k.knockout.shadow.net.axay.kspigot")
    }
}