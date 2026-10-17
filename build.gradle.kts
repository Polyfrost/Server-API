subprojects {
    apply(plugin = "java-library")

    group = "org.polyfrost.oneclient"
    version = "0.1.0"

    repositories {
        mavenCentral()
        maven("https://hub.spigotmc.org/nexus/content/repositories/snapshots/")
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

project(":bukkit") {
    dependencies {
        "implementation"(project(":common"))
        "compileOnly"("org.spigotmc:spigot-api:1.8.8-R0.1-SNAPSHOT") {
            exclude(group = "net.md-5", module = "bungeecord-chat")
        }
    }

    tasks.named<ProcessResources>("processResources") {
        inputs.property("version", version)
        filesMatching("plugin.yml") {
            expand("version" to version)
        }
    }

    tasks.named<Jar>("jar") {
        archiveBaseName.set("OneClientServerAPI-Bukkit")
        dependsOn(":api:jar", ":common:jar")
        from(project(":api").the<SourceSetContainer>()["main"].output)
        from(project(":common").the<SourceSetContainer>()["main"].output)
    }
}
