import xyz.srnyx.gradlegalaxy.enums.repository
import xyz.srnyx.gradlegalaxy.utility.lazyLibrary
import xyz.srnyx.gradlegalaxy.utility.magicMongo
import xyz.srnyx.gradlegalaxy.utility.setupJda


plugins {
    application
    id("xyz.srnyx.gradle-galaxy") version "1.3.3"
    id("com.gradleup.shadow") version "8.3.8"
    id("dev.reformator.stacktracedecoroutinator") version "2.5.6"
}

magicMongo("ef0c2370bd")
lazyLibrary("botcommands-v3-SNAPSHOT")
setupJda("5.6.1", "network.venox", "2.0.0", "A Discord bot for Venox Network")

repository("https://maven.dynomake.it/releases/") // space.dynomake:libretranslate-java
dependencies {
    implementation("space.dynomake", "libretranslate-java", "1.0.9") // Translate
    implementation("com.github.walkyst", "lavaplayer-fork", "1.4.3") // TTS
    implementation("net.sf.sociaal", "freetts", "1.2.2") // TTS

    compileOnly("org.spongepowered:configurate-yaml:4.1.2") // For documentation
    compileOnly("org.mongodb", "mongodb-driver-sync", "5.2.0") // For documentation
}

// Fix some tasks
tasks["distZip"].dependsOn("shadowJar")
tasks["distTar"].dependsOn("shadowJar")
tasks["startScripts"].dependsOn("shadowJar")
tasks["startShadowScripts"].dependsOn("jar")
