plugins {
    id("java-library")
    id("xyz.jpenilla.run-paper") version "3.0.2"
}

repositories {
    mavenCentral()
    maven("https://repo.papermc.io/repository/maven-public/")
    maven("https://repo.extendedclip.com/content/repositories/placeholderapi/")
    maven("https://jitpack.io")
    maven("https://repo.essentialsx.net/releases")
    maven("https://repo.codemc.io/repository/creatorfromhell")
}

dependencies {
    compileOnly("net.milkbowl.vault:VaultUnlockedAPI:2.20") {
        exclude(group = "org.bukkit")
        exclude(group = "org.spigotmc")
        exclude(group = "io.papermc.paper")
        exclude(group = "com.destroystokyo.paper")
    }
    compileOnly("io.papermc.paper:paper-api:26.1.2.build.+")
    compileOnly("me.clip:placeholderapi:2.12.3")
    compileOnly("net.dmulloy2:ProtocolLib:5.4.0")
    compileOnly("com.github.Jikoo:OpenInv:5.1.12")
    compileOnly("net.essentialsx:EssentialsX:2.19.0") {
        exclude(group = "org.bstats", module = "bstats-bukkit")
        exclude(group = "org.spigotmc")
        exclude(group = "io.papermc.paper")
    }

}

java {
    toolchain.languageVersion = JavaLanguageVersion.of(25)
}

tasks {
    runServer {
        // Configure the Minecraft version for our task.
        // This is the only required configuration besides applying the plugin.
        // Your plugin's jar (or shadowJar if present) will be used automatically.
        minecraftVersion("26.2")
        jvmArgs("-Xms2G", "-Xmx2G")
    }

    processResources {
        val props = mapOf("version" to version)
        filesMatching("plugin.yml") {
            expand(props)
        }
    }
}
