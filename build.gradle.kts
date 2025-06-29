import xyz.srnyx.gradlegalaxy.enums.Repository
import xyz.srnyx.gradlegalaxy.enums.repository
import xyz.srnyx.gradlegalaxy.utility.magicMongo
import xyz.srnyx.gradlegalaxy.utility.setupLazyLibrary


plugins {
    application
    id("xyz.srnyx.gradle-galaxy") version "1.3.3"
    id("com.gradleup.shadow") version "8.3.6"
}

repositories.mavenLocal()

magicMongo("ef0c2370bd")
setupLazyLibrary("3.2.0", "5.3.2", "network.venox", "1.2.0", "A Discord bot for Venox Network")

repository(Repository.CLOJARS)

dependencies {
    implementation("net.clojars.suuft", "libretranslate-java", "1.0.5") // Translate
    implementation("com.github.walkyst", "lavaplayer-fork", "1.4.2") // TTS
    implementation("net.sf.sociaal", "freetts", "1.2.2") // TTS

    compileOnly("io.github.freya022", "BotCommands", "2.10.4") // For documentation
    compileOnly("org.mongodb", "mongodb-driver-sync", "5.2.0") // For documentation
}
