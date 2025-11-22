import org.jetbrains.kotlin.gradle.dsl.JvmTarget

plugins {
    id("java")
    id("org.jetbrains.kotlin.jvm") version "2.1.10"
    id("org.jetbrains.intellij.platform") version "2.10.4"
}

group = "com.madmanbeavis"
version = "1.5.1"

java {
    toolchain {
        languageVersion.set(JavaLanguageVersion.of(17))
    }
}

repositories {
    mavenCentral()
    intellijPlatform { defaultRepositories() }
}

dependencies {
    implementation("com.google.code.gson:gson:2.10.1")

    testImplementation(kotlin("test"))
    testImplementation("org.junit.jupiter:junit-jupiter:5.10.0")
    testImplementation("io.mockk:mockk:1.13.8")

    intellijPlatform {
        rider("2024.2")
        bundledPlugin("JavaScript")
        pluginVerifier()
        zipSigner()
        testFramework(org.jetbrains.intellij.platform.gradle.TestFrameworkType.Platform)
    }
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
        options.encoding = "UTF-8"
    }
    withType<org.jetbrains.kotlin.gradle.tasks.KotlinCompile> {
        incremental = true
        compilerOptions {
            jvmTarget.set(JvmTarget.JVM_17)
            freeCompilerArgs.add("-Xjvm-default=all")
            freeCompilerArgs.add("-opt-in=kotlin.RequiresOptIn")
        }
    }

    test {
        useJUnitPlatform()
    }

    processResources {
        dependsOn("buildData")
    }

    // Keep your behavior of producing the plugin on `build`
    build {
        dependsOn("buildPlugin")
    }
}

intellijPlatform {

    buildSearchableOptions = true
    projectName = project.name
    pluginConfiguration {
        id = "com.madmanbeavis.spt-id-highlighter"
        name = "SPT ID Highlighter"

        description = """
            A comprehensive development tool for the <a href="https://github.com/sp-tarkov">Single Player Tarkov</a> project. Provides intelligent ID recognition, rich documentation, and extensive customization for SPT game object development.
            <br><br>
            <b>Core Features:</b>
            <ul>
                <li><b>Smart ID Recognition:</b> Automatic detection and highlighting of 24-character SPT IDs across JSON, TypeScript, JavaScript, and C# files</li>
                <li><b>Comprehensive Database:</b> Built-in support for 4,195+ items, 87 categories, quests, traders, locations, and all SPT game objects</li>
                <li><b>Inline Item Hints:</b> SPT item names appear directly next to MongoDB IDs in your code for quick reference</li>
                <li><b>Rich Tooltips:</b> Hover over inline hints to see detailed information including names, descriptions, stats, and metadata in 17 languages</li>
                <li><b>Custom ID Management:</b> Define your own IDs in configurable files (.sptids, sptids.json) with instant live-reload</li>
                <li><b>MongoDB ID Generator:</b> Generate valid MongoDB ObjectIds with a single keystroke (Ctrl+Shift+Alt+W)</li>
            </ul>
            <br>
            <b>Customization & Theming:</b>
            <ul>
                <li><b>Flexible Highlighting:</b> Customize text styles (bold, italic, underline) and background highlighting per your preference</li>
                <li><b>Type-Specific Colors:</b> Assign different colors for Items, Ammo, Weapons, Quests, Traders, Locations, and Customization items</li>
                <li><b>Theme Sharing:</b> Export and import theme configurations (.spttheme files) to share with your team</li>
            </ul>
            <br>
            <b>Multilingual Support:</b>
            <ul>
                <li>17 languages: English, Russian, Chinese (Simplified), Czech, Spanish, Mexican Spanish, French, German, Hungarian, Italian, Japanese, Korean, Polish, Portuguese, Romanian, Slovak, Turkish</li>
            </ul>
            <br>
            <b>Perfect for SPT mod developers, server administrators, and contributors working with Tarkov game data.</b>
        """.trimIndent()

        changeNotes.set(
            """
            <h3>1.5.1 - Critical Bug Fix</h3>
            <ul>
                <li><b>Fixed ArrayIndexOutOfBoundsException</b> - Replaced Trove4j TIntHashSet with standard HashSet to fix negative array index crashes</li>
                <li><b>Improved stability</b> - Resolved freezing issues caused by hash collision bugs in primitive collections</li>
            </ul>
            <p>This release fixes the critical bug introduced in 1.5.0 that caused IDE freezing when using the ID Highlighter.</p>

            <h3>1.5.0 - Performance Optimization Release</h3>
            <ul>
                <li><b>Fixed critical thread contention</b> - Eliminated IDE freezing caused by HashMap TreeNode blocking 6+ background threads</li>
                <li><b>Thread-safe collections</b> - Replaced blocking maps with ConcurrentHashMap for lock-free reads</li>
                <li><b>Added cancellation support</b> - All operations now respond to cancellation requests</li>
                <li><b>Memory limits</b> - Added bounds to prevent unbounded growth (500 hints/file, 50 annotations/element)</li>
                <li><b>Optimized hot paths</b> - Pre-compiled regex, early validation, reduced logging overhead</li>
            </ul>
        """.trimIndent()
        )

        ideaVersion {
            sinceBuild.set("242")
            untilBuild.set("253.*")
        }

        vendor {
            name = "MadManBeavis"
            email = "madmanbeavis@gmail.com"
            url = "https://github.com/madmanbeavisx"
        }
    }

    signing {
        certificateChain.set(System.getenv("CERTIFICATE_CHAIN").orEmpty())
        privateKey.set(System.getenv("PRIVATE_KEY").orEmpty())
        password.set(System.getenv("PRIVATE_KEY_PASSWORD").orEmpty())
    }

    publishing {
        token.set(System.getenv("PUBLISH_TOKEN").orEmpty())
    }
}

tasks.named("signPlugin").configure {
    doFirst {
        val missing = listOf(
            "CERTIFICATE_CHAIN" to System.getenv("CERTIFICATE_CHAIN").orEmpty(),
            "PRIVATE_KEY" to System.getenv("PRIVATE_KEY").orEmpty(),
            "PRIVATE_KEY_PASSWORD" to System.getenv("PRIVATE_KEY_PASSWORD").orEmpty()
        ).filter { it.second.isEmpty() }
        if (missing.isNotEmpty()) {
            throw GradleException(
                "Missing env vars for signing: ${missing.joinToString { it.first }}."
            )
        }
    }
}

tasks.named("publishPlugin").configure {
    doFirst {
        if (System.getenv("PUBLISH_TOKEN").isNullOrEmpty()) {
            throw GradleException(
                "Missing PUBLISH_TOKEN. Provide env var or use: ./gradlew publishPlugin -Ppublish.token=YOUR_TOKEN"
            )
        }
    }
}
