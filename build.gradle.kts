plugins {
    java
}

allprojects {
    repositories {
        mavenCentral()
        maven("http://jacamo.sourceforge.net/maven2") {
            isAllowInsecureProtocol = true
        }
        maven("https://jade.tilab.com/maven")
    }

    group = "it.unibo.ise"
}

subprojects {

    apply<JavaPlugin>()

    sourceSets {
        main {
            resources {
                srcDir("src/main/asl")
            }
        }
    }

    dependencies {
        implementation("org.jason-lang", "jason", "2.3")
    }

    configure<JavaPluginConvention> {
        sourceCompatibility = JavaVersion.VERSION_1_8
    }


    file(projectDir).listFiles().filter { it.extension == "mas2j" }.forEach { mas2jFile ->
        task<JavaExec>("run${mas2jFile.nameWithoutExtension.capitalize()}Mas") {
            group = "run"
            classpath = sourceSets.getByName("main").runtimeClasspath
            main = "jason.infra.centralised.RunCentralisedMAS"
            args(mas2jFile.path)
            standardInput = System.`in`
        }
    }
}

