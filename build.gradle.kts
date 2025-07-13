import xyz.srnyx.gradlegalaxy.enums.repository
import xyz.srnyx.gradlegalaxy.utility.magicMongo
import xyz.srnyx.gradlegalaxy.utility.setupLazyLibrary


plugins {
    application
    id("xyz.srnyx.gradle-galaxy") version "1.3.3"
    id("com.gradleup.shadow") version "8.3.8"
}

repositories.mavenLocal()

magicMongo("ef0c2370bd")
setupLazyLibrary("976f3062f3", "5.6.1", "network.venox", "2.0.0", "A Discord bot for Venox Network")

repository("https://maven.dynomake.space/releases/") // space.dynomake:libretranslate-java
dependencies {
    implementation("space.dynomake", "libretranslate-java", "1.0.9") // Translate
    implementation("com.github.walkyst", "lavaplayer-fork", "1.4.3") // TTS
    implementation("net.sf.sociaal", "freetts", "1.2.2") // TTS

    compileOnly("io.github.freya022", "BotCommands", "2.10.4") // For documentation
    compileOnly("org.mongodb", "mongodb-driver-sync", "5.2.0") // For documentation
}
