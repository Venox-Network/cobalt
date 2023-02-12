version = "1.0.0"
group = "network.venox"

plugins {
    java
    id("com.github.johnrengelman.shadow") version "7.1.2"
}

repositories {
    maven("https://m2.dv8tion.net/releases") // net.dv8tion:JDA
    mavenCentral() // org.spongepowered:configurate-yaml, info.debatty:java-string-similarity
}

dependencies {
    implementation("net.dv8tion", "JDA", "5.0.0-beta.2") {
        exclude("net.dv8tion", "opus-java")
    }
    implementation("org.spongepowered", "configurate-yaml", "4.1.2")
    implementation("info.debatty", "java-string-similarity", "2.0.0")
}

//application.mainClass.set("network.venox.cobalt.Cobalt")

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
    }

    // Disable unnecessary tasks
    classes { enabled = false }
    jar { enabled = false }
    compileTestJava { enabled = false }
    processTestResources { enabled = false }
    testClasses { enabled = false }
    test { enabled = false }
    check { enabled = false }
}
