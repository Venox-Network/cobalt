version = "1.1.2"
group = "network.venox"

plugins {
    application
    id("com.github.johnrengelman.shadow") version "7.1.2"
}

repositories {
    mavenCentral() // org.spongepowered:configurate-yaml, io.github.freya022:BotCommands, info.debatty:java-string-similarity
    maven("https://jitpack.io") // io.github.freya022:BotCommands
    maven("https://repo.clojars.org") // net.clojars.suuft:libretranslate-java
}

dependencies {
    implementation("net.dv8tion", "JDA", "5.0.0-beta.4") {
        exclude("net.dv8tion", "opus-java")
    }
    implementation("org.spongepowered", "configurate-yaml", "4.1.2")
    implementation("io.github.freya022", "BotCommands", "2.8.4")
    implementation("org.postgresql", "postgresql", "42.5.4")
    implementation("com.zaxxer", "HikariCP", "5.0.1")
    implementation("ch.qos.logback", "logback-classic", "1.4.5")
    implementation("info.debatty", "java-string-similarity", "2.0.0")
    implementation("net.clojars.suuft", "libretranslate-java", "1.0.5")
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
