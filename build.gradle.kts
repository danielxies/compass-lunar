plugins {
    java
    id("com.github.weave-mc.weave-gradle") version "649dba7468"
}

group = "me.meredith"
version = "1.0"

minecraft {
    version("1.8.9")
}

repositories {
    maven("https://jitpack.io")
    maven("https://repo.spongepowered.org/maven/")
}

dependencies {
    compileOnly("com.github.weave-mc:weave-loader:v0.2.4")
    compileOnly("org.spongepowered:mixin:0.8.5")
}

tasks.compileJava {
    options.release.set(11)
}

sourceSets {
    named("main") {
        java.srcDirs("src/main/java")
        resources {
            srcDirs("src/main/resources")
        }
    }
}

tasks {
    processResources {
        duplicatesStrategy = DuplicatesStrategy.INCLUDE
        
        // Copy root config files
        from("src/main/resources") {
            include("weave.mod.json")
            include("weavefks.mixins.json")
        }
        
        // Copy assets with correct structure
        from("src/main/resources") {
            include("assets/**")
        }
        
        doFirst {
            println("Processing resources...")
            sourceSets["main"].resources.files.forEach { 
                println("Found resource: ${it.path}")
            }
        }
    }
}