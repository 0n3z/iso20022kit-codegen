import org.jetbrains.kotlin.gradle.tasks.KotlinCompile

plugins {
    kotlin("jvm") version "1.3.31"
    `maven-publish`
//    `java-library`
}

group = "io.pnyx"
version = "1.0-SNAPSHOT"


repositories {
    mavenCentral()
}

dependencies {
    compile(kotlin("stdlib", "1.3.31"))
    implementation(group = "com.google.auto.service", name = "auto-service", version = "1.0-rc5")
}

val compileKotlin: KotlinCompile by tasks

compileKotlin.kotlinOptions {
    jvmTarget = "1.8"

}

tasks.wrapper {
    gradleVersion = "5.4.1"
}

publishing {
    publications {
        create<MavenPublication>("default") {
            from(components["java"])
        }
    }
}

