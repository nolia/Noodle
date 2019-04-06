import org.jetbrains.kotlin.gradle.tasks.KotlinCompile

group = "com.noodle"
version = "0.9"

plugins {
    java
    kotlin("jvm")
}

repositories {
    jcenter()
}

dependencies {
    implementation(kotlin("stdlib"))
    implementation("com.google.code.gson:gson:2.8.5")

    testImplementation("junit:junit:4.12")
    testImplementation("io.mockk:mockk:1.9.3.kotlin12")
    testImplementation("io.mockk:mockk-common:1.9.3.kotlin12")
}

// Extra.
val compileKotlin: KotlinCompile by tasks

compileKotlin.kotlinOptions {
    freeCompilerArgs = listOf("-XXLanguage:+InlineClasses")
}
