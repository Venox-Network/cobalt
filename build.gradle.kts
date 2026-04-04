import xyz.srnyx.gradlegalaxy.data.config.DependencyConfig
import xyz.srnyx.gradlegalaxy.data.config.JavaSetupConfig
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
    jdaConfig = DependencyConfig(version = "6.4.1"),
    lazyLibraryConfig = DependencyConfig(version = "4733f172b8"))
magicMongo(config = DependencyConfig(version = "ef0c2370bd"))

dependencies {
    implementation("com.github.stokito", "libretranslate-java", "v1.2.2") // Translate
    implementation("com.github.walkyst", "lavaplayer-fork", "1.4.3") // TTS
    implementation("net.sf.sociaal", "freetts", "1.2.2") // TTS

    compileOnly("org.mongodb", "mongodb-driver-sync", "5.2.0") // For documentation
}
