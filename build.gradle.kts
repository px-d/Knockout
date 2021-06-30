import org.jetbrains.kotlin.gradle.tasks.KotlinCompile

plugins {
    java
    kotlin("jvm") version "1.5.0"
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
    maven("https://repo.cloudnetservice.eu/repository/releases/")
}
dependencies {
    compileOnly("org.spigotmc:spigot:1.16.5-R0.1-SNAPSHOT")
    compileOnly("com.onarandombox.multiversecore:Multiverse-Core:4.2.2")
    compileOnly("de.dytanic.cloudnet", "cloudnet-bridge", "3.3.0-RELEASE")
    implementation(kotlin("stdlib"))
    implementation("net.axay", "kspigot", "1.16.29")
}
tasks.withType<KotlinCompile> {
    kotlinOptions.jvmTarget = jvmVersionString
}
tasks {
    shadowJar {
        relocate("net.axay.kspigot", "de.soho5k.knockout.shadow.net.axay.kspigot")
    }
}