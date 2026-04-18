import xyz.srnyx.gradlegalaxy.data.config.DependencyConfig
import xyz.srnyx.gradlegalaxy.data.config.JavaSetupConfig
import xyz.srnyx.gradlegalaxy.enums.Repository
import xyz.srnyx.gradlegalaxy.enums.repository
import xyz.srnyx.gradlegalaxy.utility.magicMongo
import xyz.srnyx.gradlegalaxy.utility.setupLazyLibrary


plugins {
    application
    id("xyz.srnyx.gradle-galaxy") version "2.1.0"
    id("com.gradleup.shadow") version "8.3.9"
}

setupLazyLibrary(
    javaSetupConfig = JavaSetupConfig(
        group = "network.venox",
        version = "2.0.0",
        description = "A Discord bot for Venox Network"),
    jdaConfig = DependencyConfig(version = "6.4.1"),
    lazyLibraryConfig = DependencyConfig(version = "b5ead96"))
magicMongo(config = DependencyConfig(version = "2.0.1"))

repository(Repository.DYNOMAKE, Repository.JITPACK)
dependencies {
    implementation("space.dynomake:libretranslate-java:1.0.9") // Translate
    implementation("dev.arbjerg:lavaplayer:2.2.6") // TTS
    implementation("net.sf.sociaal:freetts:1.2.2") // TTS
}
