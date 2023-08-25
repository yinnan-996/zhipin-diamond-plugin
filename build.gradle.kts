plugins {
    id("java")
    id("org.jetbrains.kotlin.jvm") version "1.8.22"
    id("org.jetbrains.intellij") version "1.15.0"
    id("io.freefair.lombok") version "8.2.2"
}

group = "com.zhipin"
version = "1.6"

repositories {
    mavenCentral()
}

// Configure Gradle IntelliJ Plugin
// Read more: https://plugins.jetbrains.com/docs/intellij/tools-gradle-intellij-plugin.html
intellij {
    version.set("2020.1.1")
    type.set("IC") // Target IDE Platform

    plugins.set(listOf("com.intellij.java"))
}


dependencies {
    implementation("com.squareup.retrofit2", "retrofit", "2.6.2")
    implementation("com.squareup.retrofit2", "converter-jackson", "2.6.2")
    implementation("com.squareup.retrofit2", "converter-gson", "2.6.2")
    implementation("com.alibaba", "fastjson", "1.2.83")
    implementation("org.apache.commons", "commons-collections4", "4.1")
    testImplementation("junit", "junit", "4.12")
}



tasks {
    // Set the JVM compatibility versions
    withType<JavaCompile> {
        sourceCompatibility = "11"
        targetCompatibility = "11"
    }
    withType<org.jetbrains.kotlin.gradle.tasks.KotlinCompile> {
        kotlinOptions.jvmTarget = "11"
    }

    patchPluginXml {
        sinceBuild.set("201.0")
        untilBuild.set("232.*")
    }

    signPlugin {
        certificateChain.set(System.getenv("CERTIFICATE_CHAIN"))
        privateKey.set(System.getenv("PRIVATE_KEY"))
        password.set(System.getenv("PRIVATE_KEY_PASSWORD"))
    }

    publishPlugin {
        token.set(System.getenv("PUBLISH_TOKEN"))
    }
}
