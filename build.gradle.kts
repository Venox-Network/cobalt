import xyz.srnyx.gradlegalaxy.data.config.DependencyConfig
import xyz.srnyx.gradlegalaxy.data.config.JavaSetupConfig
import xyz.srnyx.gradlegalaxy.enums.repository
import xyz.srnyx.gradlegalaxy.utility.magicMongo
import xyz.srnyx.gradlegalaxy.utility.setupLazyLibrary


plugins {
    application
    id("xyz.srnyx.gradle-galaxy") version "2.0.2"
    id("com.gradleup.shadow") version "8.3.9"
}

setupLazyLibrary(
    javaSetupConfig = JavaSetupConfig(
        group = "network.venox",
        version = "2.0.0",
        description = "A Discord bot for Venox Network"),
    jdaConfig = DependencyConfig(version = "6.3.0"),
    lazyLibraryConfig = DependencyConfig(version = "fabec338ba"))
magicMongo(config = DependencyConfig(version = "ef0c2370bd"))

repository("https://maven.dynomake.it/releases/") // space.dynomake:libretranslate-java
dependencies {
    implementation("space.dynomake", "libretranslate-java", "1.0.9") // Translate
    implementation("com.github.walkyst", "lavaplayer-fork", "1.4.3") // TTS
    implementation("net.sf.sociaal", "freetts", "1.2.2") // TTS

    compileOnly("org.mongodb", "mongodb-driver-sync", "5.2.0") // For documentation
}
