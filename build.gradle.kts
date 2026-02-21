import org.jetbrains.kotlin.gradle.dsl.JvmTarget
import org.openapitools.generator.gradle.plugin.tasks.GenerateTask

plugins {
    kotlin("multiplatform") version "2.2.21"
    kotlin("plugin.serialization") version "2.2.21"
    id("com.android.library") version "8.13.2"
    id("org.openapi.generator") version "7.20.0"
    id("maven-publish")
    id("signing")
}

group = "io.github.rotesblatt"

version = "1.6.0"

repositories {
    mavenCentral()
    google()
}

android {
    namespace = "io.github.rotesblatt.hextractor"
    compileSdk = 36

    defaultConfig { minSdk = 24 }

    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_17
        targetCompatibility = JavaVersion.VERSION_17
    }
}

kotlin {
    androidTarget {
        compilations.all {
            compileTaskProvider.configure {
                compilerOptions {
                    jvmTarget.set(JvmTarget.JVM_17)
                }
            }
        }
        publishLibraryVariants("release")
    }

    iosX64()
    iosArm64()
    iosSimulatorArm64()

    sourceSets {
        val commonMain by getting {
            dependencies {
                implementation("org.jetbrains.kotlinx:kotlinx-serialization-json:1.6.2")
                implementation("io.ktor:ktor-client-core:2.3.7")
                implementation("io.ktor:ktor-client-content-negotiation:2.3.7")
                implementation("io.ktor:ktor-serialization-kotlinx-json:2.3.7")
            }
        }

        val commonTest by getting { dependencies { implementation(kotlin("test")) } }

        val androidMain by getting {
            dependencies { implementation("io.ktor:ktor-client-android:2.3.7") }
        }
        
        
        val iosX64Main by getting
        val iosArm64Main by getting
        val iosSimulatorArm64Main by getting
        val iosMain by creating {
            dependsOn(commonMain)
            iosX64Main.dependsOn(this)
            iosArm64Main.dependsOn(this)
            iosSimulatorArm64Main.dependsOn(this)
            dependencies { implementation("io.ktor:ktor-client-darwin:2.3.7")}
        }
        
    }
}

// Automatically find all OpenAPI spec files in the openApi directory
val openApiDir = file("$rootDir/openApi")
val openApiFiles = openApiDir.listFiles { file -> 
    file.extension in listOf("yml", "yaml") 
}?.toList() ?: emptyList()

// Dynamically create generation tasks for each OpenAPI file
val generationTasks = openApiFiles.map { specFile ->
    // Derive a clean name from the file (e.g., "hex-tractor-open-api.yml" -> "hextractor")
    val baseName = specFile.nameWithoutExtension
        .replace("-open-api", "")
        .replace("_open_api", "")
        .replace("-api", "")
        .replace("_api", "")
    
    // Create camelCase name for task (e.g., "hex-tractor" -> "hexTractor")
    val taskName = baseName.split("-", "_")
        .mapIndexed { index, part -> 
            if (index == 0) part.lowercase() 
            else part.replaceFirstChar { it.uppercase() }
        }
        .joinToString("")
    
    // Create dot-separated package name (e.g., "hex-tractor" -> "hextractor")  
    val packageName = baseName.replace("-", "").replace("_", "").lowercase()
    
    val generateTask = tasks.register<GenerateTask>("generate${taskName.replaceFirstChar { it.uppercase() }}Api") {
        generatorName.set("kotlin")
        inputSpec.set(specFile.absolutePath)
        outputDir.set("${layout.buildDirectory.get().asFile}/generated/$taskName")
        apiPackage.set("io.$packageName.api")
        modelPackage.set("io.$packageName.model")
        invokerPackage.set("io.$packageName.client")

        configOptions.set(
            mapOf(
                "library" to "multiplatform",
                "dateLibrary" to "kotlinx-datetime",
                "useCoroutines" to "true",
                "enumPropertyNaming" to "UPPERCASE",
                "collectionType" to "list",
                "packageName" to "io.$packageName"
            )
        )
    }
    generateTask
}

// Add all generated sources to Kotlin source sets
kotlin.sourceSets.getByName("commonMain") {
    openApiFiles.forEach { specFile ->
        val baseName = specFile.nameWithoutExtension
            .replace("-open-api", "")
            .replace("_open_api", "")
            .replace("-api", "")
            .replace("_api", "")
        val taskName = baseName.split("-", "_")
            .mapIndexed { idx, part -> 
                if (idx == 0) part.lowercase() 
                else part.replaceFirstChar { it.uppercase() }
            }
            .joinToString("")
        kotlin.srcDir("${layout.buildDirectory.get().asFile}/generated/$taskName/src/main/kotlin")
    }
}

// Ensure OpenAPI generation runs before compilation and source jar creation
afterEvaluate {
    tasks.withType<org.jetbrains.kotlin.gradle.tasks.KotlinCompile>().configureEach {
        generationTasks.forEach { apiGen -> dependsOn(apiGen) }
    }
    // Ensure all klib compilation tasks depend on all OpenAPI generation tasks
    tasks.matching { it.name.endsWith("Klibrary") }.configureEach {
        generationTasks.forEach { dependsOn(it) }
    }
    tasks.matching { it.name.contains("sourcesJar") }.configureEach {
        generationTasks.forEach { dependsOn(it) }
    }
    tasks.matching { it.name.contains("SourcesJar") }.configureEach {
        generationTasks.forEach { dependsOn(it) }
    }
    tasks.matching { it.name.contains("extractDeepLinksForAar") }.configureEach {
        generationTasks.forEach { dependsOn(it) }
    }
    tasks.matching { it.name.contains("merge") }.configureEach {
        generationTasks.forEach { dependsOn(it) }
    }
    tasks.matching { it.name.contains("package") }.configureEach {
        generationTasks.forEach { dependsOn(it) }
    }
    // Ensure metadata generation and publication tasks depend on assemble
    tasks.matching { it.name.startsWith("generateMetadataFileFor") && it.name.endsWith("Publication") }.configureEach {
        dependsOn(tasks.named("assemble"))
    }
    tasks.matching { it.name.startsWith("publish") && it.name.contains("Publication") }.configureEach {
        dependsOn(tasks.named("assemble"))
    }
    
    // Ensure iOS klib tasks run before their corresponding metadata generation
    tasks.matching { it.name.contains("iosArm64") && it.name.startsWith("generateMetadataFileFor") }.configureEach {
        tasks.findByName("iosArm64MainKlibrary")?.let { dependsOn(it) }
    }
    tasks.matching { it.name.contains("iosX64") && it.name.startsWith("generateMetadataFileFor") }.configureEach {
        tasks.findByName("iosX64MainKlibrary")?.let { dependsOn(it) }
    }
    tasks.matching { it.name.contains("iosSimulatorArm64") && it.name.startsWith("generateMetadataFileFor") }.configureEach {
        tasks.findByName("iosSimulatorArm64MainKlibrary")?.let { dependsOn(it) }
    }

    tasks.matching { it.name == "prepareAndroidMainArtProfile" || it.name == "compileCommonMainKotlinMetadata" || it.name == "compileKotlinIosArm64" || it.name == "compileKotlinIosSimulatorArm64" || it.name == "compileKotlinIosX64" || it.name == "compileAndroidMain" }
    .configureEach {
        generationTasks.forEach { dependsOn(it) }
    }
}

// Publishing configuration
publishing {
    publications {
        publications.withType<MavenPublication> {
            pom {
                name.set("Hex-Tractor API Client")
                description.set("Generated Kotlin Multiplatform client for Hex-Tractor API")
                url.set("https://github.com/RotesBlatt/hex-specs")

                licenses {
                    license {
                        name.set("MIT License")
                        url.set("https://opensource.org/licenses/MIT")
                    }
                }

                developers {
                    developer {
                        id.set("RotesBlatt")
                        name.set("Jonathan Liersch")
                        email.set("blauerhut04@gmail.com")
                    }
                }

                scm {
                    connection.set("scm:git:git://github.com/RotesBlatt/hex-specs.git")
                    developerConnection.set("scm:git:ssh://github.com/RotesBlatt/hex-specs.git")
                    url.set("https://github.com/RotesBlatt/hex-specs")
                }
            }
        }
    }

    repositories {
        maven {
            name = "LocalBuild"
            url = uri(layout.buildDirectory.dir("repo"))
        }
    }
}

signing {
    useInMemoryPgpKeys(System.getenv("GPG_PRIVATE_KEY"), System.getenv("GPG_PASSPHRASE"))
    sign(publishing.publications)
}

// Only sign if publishing
tasks.withType<Sign>().configureEach { onlyIf { System.getenv("GPG_PRIVATE_KEY") != null } }

// Task to create deployment bundle for Central Portal
tasks.register<Zip>("createDeploymentBundle") {
    generationTasks.forEach { dependsOn(it) }
    dependsOn(tasks.named("publishAllPublicationsToLocalBuildRepository"))
    archiveFileName.set("deployment-bundle-${project.version}.zip")
    destinationDirectory.set(layout.buildDirectory.dir("distributions"))
    from(layout.buildDirectory.dir("repo"))
}
