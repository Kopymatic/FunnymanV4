import org.jetbrains.kotlin.config.KotlinCompilerVersion

plugins {
    id("org.jetbrains.kotlin.jvm") version "1.6.21"
    id("java")
    id("com.github.johnrengelman.shadow") version "7.1.2"
}

group = "org.example"
version = "4.0.0-PTB8"

repositories {
    mavenCentral()
    maven("https://jitpack.io/")
}

val exposedVersion = "0.40.1"

dependencies {
    implementation(kotlin("stdlib", KotlinCompilerVersion.VERSION))
    testImplementation("org.junit.jupiter:junit-jupiter-api:5.9.0")
    testRuntimeOnly("org.junit.jupiter:junit-jupiter-engine:5.9.0")
    implementation("net.dv8tion:JDA:5.0.0-beta.5")
    implementation("com.github.minndevelopment:jda-ktx:9fc90f6")
    implementation("ch.qos.logback:logback-classic:1.4.3")
    // https://mvnrepository.com/artifact/org.json/json
    implementation("org.json:json:20220924")

    implementation("org.postgresql:postgresql:42.5.0")
    implementation("org.jetbrains.exposed:exposed-core:$exposedVersion")
    implementation("org.jetbrains.exposed:exposed-dao:$exposedVersion")
    implementation("org.jetbrains.exposed:exposed-jdbc:$exposedVersion")
}


val jar by tasks.getting(Jar::class) {
    manifest {
        attributes["Main-Class"] = "BotKt"
    }
}
//test {
//    useJUnitPlatform()
//}

//jar {
//    duplicatesStrategy = DuplicatesStrategy.WARN
//    manifest {
//        attributes(
//                "Main-Class" = "BotKt"
//        )
//    }
//    // This line of code recursively collects and copies all of a project"s files
//    // and adds them to the JAR itself. One can extend this task, to skip certain
//    // files or particular types at will
//    from { configurations.compileClasspath.collect { it.isDirectory() ? it : zipTree(it) } }
//}