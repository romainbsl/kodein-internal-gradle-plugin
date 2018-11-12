import com.jfrog.bintray.gradle.BintrayExtension

plugins {
    `maven-publish`
    `java-library`
    id("org.gradle.kotlin.kotlin-dsl")
    kotlin("jvm")
    id("com.jfrog.bintray")
}

repositories {
    jcenter()
}

dependencies {
    implementation(gradleApi())
    implementation(gradleKotlinDsl())
}