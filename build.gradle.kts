version = "1.1.2"

plugins {
    application
    id("com.github.johnrengelman.shadow") version "7.1.2"
}

repositories {
    mavenCentral() // org.spongepowered:configurate-yaml, io.github.freya022:BotCommands, info.debatty:java-string-similarity
    maven("https://jitpack.io") // io.github.freya022:BotCommands, com.github.walkyst:lavaplayer-fork
    maven("https://repo.clojars.org") // net.clojars.suuft:libretranslate-java
}

dependencies {
    implementation("net.dv8tion", "JDA", "5.0.0-beta.6") // JDA
    implementation("org.spongepowered", "configurate-yaml", "4.1.2") // Data storage
    implementation("io.github.freya022", "BotCommands", "2.8.4") // Command framework
    implementation("org.postgresql", "postgresql", "42.5.4") // Database
    implementation("com.zaxxer", "HikariCP", "5.0.1") // Database
    implementation("ch.qos.logback", "logback-classic", "1.4.5") // Logging
    implementation("info.debatty", "java-string-similarity", "2.0.0") // QOTD
    implementation("net.clojars.suuft", "libretranslate-java", "1.0.5") // Translate
    implementation("com.github.walkyst", "lavaplayer-fork", "1.4.0") // TTS
    implementation("net.sf.sociaal", "freetts", "1.2.2") // TTS
}

application.mainClass.set("network.venox.cobalt.Cobalt")

tasks {
    // Remove '-all' from the JAR file name and clean up the build folder
    shadowJar {
        archiveClassifier.set("")
    }

    // Make 'gradle build' run 'gradle shadowJar'
    build {
        dependsOn("shadowJar")
    }

    // Text encoding
    compileJava {
        options.encoding = "UTF-8"
        options.compilerArgs.plusAssign("-parameters")
    }

    // Disable unnecessary tasks
    classes { enabled = false }
    jar { enabled = false }
    distTar { enabled = false }
    distZip { enabled = false }
    shadowDistTar { enabled = false }
    shadowDistZip { enabled = false }
    compileTestJava { enabled = false }
    processTestResources { enabled = false }
    testClasses { enabled = false }
    test { enabled = false }
    check { enabled = false }
}
