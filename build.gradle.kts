plugins {
    id("java")
    application
    `maven-publish`
}

group = "syspro.tm"
version = "2025.11.8"

repositories {
    mavenCentral()
}

java {
    withSourcesJar()
    withJavadocJar()

    toolchain {
        languageVersion = JavaLanguageVersion.of(25)
    }
}

dependencies {
    implementation("commons-io:commons-io:2.20.0")
    testImplementation(platform("org.junit:junit-bom:6.0.1"))
    testImplementation("org.junit.jupiter:junit-jupiter")
    testRuntimeOnly("org.junit.platform:junit-platform-launcher")
}

val enableNativeAccess = "--enable-native-access=syspro.tm.RegexApp"

tasks.test {
    useJUnitPlatform()
    jvmArgs(enableNativeAccess)
}

application {
    mainModule = "syspro.tm.RegexApp"
    mainClass = "syspro.tm.Main"
    applicationDefaultJvmArgs += listOf(enableNativeAccess)
}

if (rootProject.hasProperty("syspro.tm.repository.url")) {
    publishing {
        publications {
            create<MavenPublication>("maven") {
                from(components["java"])
            }
        }
        repositories {
            maven {
                url = uri(rootProject.property("syspro.tm.repository.url") as String)

                credentials(AwsCredentials::class) {
                    accessKey = rootProject.property("syspro.tm.repository.accessKey") as String
                    secretKey = rootProject.property("syspro.tm.repository.secretKey") as String
                }
            }
        }
    }
}