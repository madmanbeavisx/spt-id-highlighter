import org.jetbrains.kotlin.gradle.dsl.JvmTarget

plugins {
    id("java")
    id("org.jetbrains.kotlin.jvm") version "2.1.10"
    id("org.jetbrains.intellij") version "1.17.4"
}

group = "com.madmanbeavis"
version = "1.3.0"

repositories {
    mavenCentral()
}

dependencies {
    implementation("com.google.code.gson:gson:2.10.1")
    testImplementation("org.jetbrains.kotlin:kotlin-test")
    testImplementation("org.junit.jupiter:junit-jupiter:5.10.0")
    testImplementation("io.mockk:mockk:1.13.8")
}

intellij {
    version.set("2024.1")
    type.set("RD")
    plugins.set(listOf("rider-plugins-appender", "JavaScript"))
}

tasks {
    register<BuildDataTask>("buildData") {
        description = "Builds SPT database files from assets"
        group = "spt"
        localesDirectory.set(layout.projectDirectory.dir("src/main/resources/assets/database/locales/global"))
        itemsFile.set(layout.projectDirectory.file("src/main/resources/assets/database/templates/items.json"))
        handbookFile.set(layout.projectDirectory.file("src/main/resources/assets/database/templates/handbook.json"))
        outputDirectory.set(layout.projectDirectory.dir("src/main/resources/database"))
    }

    withType<JavaCompile> {
        sourceCompatibility = "17"
        targetCompatibility = "17"
    }
    withType<org.jetbrains.kotlin.gradle.tasks.KotlinCompile> {
        compilerOptions.jvmTarget.set(JvmTarget.JVM_17)
    }

    test {
        useJUnitPlatform()
    }

    patchPluginXml {
        sinceBuild.set("241")
        untilBuild.set("253.*")
    }

    signPlugin {
        certificateChain.set(System.getenv("CERTIFICATE_CHAIN").orEmpty())
        privateKey.set(System.getenv("PRIVATE_KEY").orEmpty())
        password.set(System.getenv("PRIVATE_KEY_PASSWORD").orEmpty())

        doFirst {
            if (certificateChain.get().isEmpty()) {
                throw GradleException(
                    "Missing CERTIFICATE_CHAIN environment variable. " +
                            "This task requires plugin signing credentials. " +
                            "Set CERTIFICATE_CHAIN, PRIVATE_KEY, and PRIVATE_KEY_PASSWORD environment variables."
                )
            }
            if (privateKey.get().isEmpty()) {
                throw GradleException(
                    "Missing PRIVATE_KEY environment variable. " +
                            "This task requires plugin signing credentials."
                )
            }
            if (password.get().isEmpty()) {
                throw GradleException(
                    "Missing PRIVATE_KEY_PASSWORD environment variable. " +
                            "This task requires plugin signing credentials."
                )
            }
        }
    }

    publishPlugin {
        token.set(System.getenv("PUBLISH_TOKEN").orEmpty())

        doFirst {
            if (token.get().isEmpty()) {
                throw GradleException(
                    "Missing PUBLISH_TOKEN environment variable. " +
                            "This task requires a JetBrains Marketplace token. " +
                            "Set the PUBLISH_TOKEN environment variable or use: ./gradlew publishPlugin -Ppublish.token=YOUR_TOKEN"
                )
            }
        }
    }

    // Run buildData before processing resources
    processResources {
        dependsOn("buildData")
    }

    // Make build task depend on buildPlugin
    build {
        dependsOn("buildPlugin")
    }
}
