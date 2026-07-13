import xyz.srnyx.gradlegalaxy.data.config.DependencyConfig
import xyz.srnyx.gradlegalaxy.data.config.JavaSetupConfig
import xyz.srnyx.gradlegalaxy.enums.Repository
import xyz.srnyx.gradlegalaxy.enums.repository
import xyz.srnyx.gradlegalaxy.utility.magicMongo
import xyz.srnyx.gradlegalaxy.utility.setupLazyLibrary


plugins {
    application
    id("xyz.srnyx.gradle-galaxy") version "3.2.0"
    id("com.gradleup.shadow") version "9.5.1"
}

setupLazyLibrary(
    javaSetupConfig = JavaSetupConfig(
        group = "network.venox",
        version = "2.0.0",
        description = "A Discord bot for Venox Network"),
    jdaConfig = DependencyConfig(version = "6.4.2"),
    lazyLibraryConfig = DependencyConfig(version = "8cb5ea4"))
magicMongo(config = DependencyConfig(version = "3eff81b"))

repository(Repository.JITPACK)
dependencies {
    implementation("com.github.stokito:libretranslate-java:1.2.2") // Translate
    implementation("dev.arbjerg:lavaplayer:2.2.7") // TTS
    implementation("net.sf.sociaal:freetts:1.2.2") // TTS
}
