import org.jetbrains.kotlin.gradle.dsl.JvmTarget

plugins {
    kotlin("multiplatform") version "1.9.22"
    kotlin("plugin.serialization") version "1.9.22"
    id("org.openapi.generator") version "7.3.0"
    id("maven-publish")
    id("signing")
}

group = "io.github.rotesblatt"
version = "1.6.0"

repositories {
    mavenCentral()
}

kotlin {
    jvm {
        compilations.all {
            compilerOptions.configure {
                jvmTarget.set(JvmTarget.JVM_17)
            }
        }
    }
    
    iosX64()
    iosArm64()
    iosSimulatorArm64()
    
    androidNativeArm32()
    androidNativeArm64()
    androidNativeX86()
    androidNativeX64()
    
    sourceSets {
        val commonMain by getting {
            dependencies {
                implementation("org.jetbrains.kotlinx:kotlinx-serialization-json:1.6.2")
                implementation("io.ktor:ktor-client-core:2.3.7")
                implementation("io.ktor:ktor-client-content-negotiation:2.3.7")
                implementation("io.ktor:ktor-serialization-kotlinx-json:2.3.7")
            }
        }
        
        val commonTest by getting {
            dependencies {
                implementation(kotlin("test"))
            }
        }
        
        val jvmMain by getting
        val jvmTest by getting
        
        val iosX64Main by getting
        val iosArm64Main by getting
        val iosSimulatorArm64Main by getting
        val iosMain by creating {
            dependsOn(commonMain)
            iosX64Main.dependsOn(this)
            iosArm64Main.dependsOn(this)
            iosSimulatorArm64Main.dependsOn(this)
        }
    }
}

// OpenAPI Generator Configuration
openApiGenerate {
    generatorName.set("kotlin")
    inputSpec.set("$rootDir/openApi/hex-tractor-open-api.yml")
    outputDir.set("$buildDir/generated")
    apiPackage.set("com.hextractor.api")
    modelPackage.set("com.hextractor.api.model")
    invokerPackage.set("com.hextractor.api.client")
    
    configOptions.set(mapOf(
        "library" to "multiplatform",
        "dateLibrary" to "kotlinx-datetime",
        "useCoroutines" to "true",
        "enumPropertyNaming" to "UPPERCASE",
        "collectionType" to "list"
    ))
}

// Add generated sources to Kotlin source sets
kotlin.sourceSets.getByName("commonMain") {
    kotlin.srcDir("$buildDir/generated/src/commonMain/kotlin")
}

tasks.named("compileKotlinJvm") {
    dependsOn("openApiGenerate")
}

tasks.named("compileKotlinIosX64") {
    dependsOn("openApiGenerate")
}

tasks.named("compileKotlinIosArm64") {
    dependsOn("openApiGenerate")
}

tasks.named("compileKotlinIosSimulatorArm64") {
    dependsOn("openApiGenerate")
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
            name = "CentralPortal"
            url = uri("https://central.sonatype.com/api/v1/publisher/upload?publishingType=USER_MANAGED")
            credentials {
                username = System.getenv("MAVEN_USERNAME")
                password = System.getenv("MAVEN_PASSWORD")
            }
        }
    }
}

signing {
    useInMemoryPgpKeys(
        System.getenv("GPG_PRIVATE_KEY"),
        System.getenv("GPG_PASSPHRASE")
    )
    sign(publishing.publications)
}

// Only sign if publishing
tasks.withType<Sign>().configureEach {
    onlyIf { System.getenv("GPG_PRIVATE_KEY") != null }
}
