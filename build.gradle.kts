subprojects {
    apply(plugin = "java-library")

    group = "org.polyfrost.oneclient"
    version = "0.1.0"

    repositories {
        mavenCentral()
        maven("https://hub.spigotmc.org/nexus/content/repositories/snapshots/")
        maven("https://repo.papermc.io/repository/maven-public/")
    }

    tasks.withType<JavaCompile>().configureEach {
        options.release.set(8)
        options.encoding = "UTF-8"
        options.compilerArgs.add("-Xlint:-options")
    }
}

project(":common") {
    dependencies {
        "api"(project(":api"))
    }
}

configure(listOf("bukkit", "bungee", "minestom").map { project(":$it") }) {
    dependencies {
        "api"(project(":common"))
    }

    tasks.named<ProcessResources>("processResources") {
        inputs.property("version", version)
        filesMatching(listOf("plugin.yml", "bungee.yml")) {
            expand("version" to version)
        }
    }

    tasks.named<Jar>("jar") {
        archiveBaseName.set("OneClientServerAPI-" + project.name.replaceFirstChar(Char::titlecase))
        manifest.attributes("Implementation-Version" to version)
        dependsOn(":api:jar", ":common:jar")
        from(project(":api").the<SourceSetContainer>()["main"].output)
        from(project(":common").the<SourceSetContainer>()["main"].output)
    }
}

project(":bukkit") {
    val sourceSets = the<SourceSetContainer>()
    val folia = sourceSets.create("folia") {
        compileClasspath += sourceSets["main"].output
    }

    dependencies {
        "compileOnly"("org.spigotmc:spigot-api:1.8.8-R0.1-SNAPSHOT") {
            exclude(group = "net.md-5", module = "bungeecord-chat")
        }
        "foliaCompileOnly"("dev.folia:folia-api:26.2.build.7-beta")
    }

    tasks.named<JavaCompile>("compileFoliaJava") {
        options.release.set(25)
    }

    tasks.named<Jar>("jar") {
        from(folia.output)
    }
}

project(":bungee") {
    dependencies {
        "compileOnly"("net.md-5:bungeecord-api:1.21-R0.4")
    }
}


project(":minestom") {
    dependencies {
        "compileOnly"("net.minestom:minestom:2026.10.07-26.2")
        "compileOnly"("org.slf4j:slf4j-api:2.0.17")
    }

    tasks.withType<JavaCompile>().configureEach {
        options.release.set(25)
    }
}

