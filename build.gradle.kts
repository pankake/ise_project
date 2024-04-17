plugins {
    java
}

allprojects {
    apply<JavaPlugin>()

    java {
        toolchain {
            val javaLanguageVersion: Property<JavaLanguageVersion> = project.objects.property(JavaLanguageVersion::class.java)
            javaLanguageVersion.set(JavaLanguageVersion.of(17))
            languageVersion.set(javaLanguageVersion)
        }
    }

    repositories {
        mavenCentral()
    }

    group = "it.unibo.ise"
}

subprojects {
    sourceSets {
        main {
            resources {
                srcDir("src/main/asl")
            }
        }
    }

    dependencies {
        implementation("net.sf.jason", "jason", "2.3")
    }

    file(projectDir).listFiles().filter { it.extension == "mas2j" }.forEach { mas2jFile ->
        task<JavaExec>("run${mas2jFile.nameWithoutExtension}") {
            group = "run"
            classpath = sourceSets.getByName("main").runtimeClasspath
            mainClass.set("jason.infra.centralised.RunCentralisedMAS")
            args(mas2jFile.path)
            standardInput = System.`in`
            javaLauncher.set(javaToolchains.launcherFor(java.toolchain))
        }
    }
}

