plugins {
    java
    checkstyle
    signing
    id("io.github.jeadyx.sonatype-uploader") version "2.8"
    id("org.kordamp.gradle.jandex") version "2.1.0"
}

allprojects {
    group = "net.ximatai.muyun.database"
    version = "1.0.0-SNAPSHOT"
//    version = "0.1.11"

    repositories {
        maven { url = uri("https://mirrors.cloud.tencent.com/repository/maven") }
        mavenCentral()
    }
}

subprojects {
    apply {
        plugin("java")
        plugin("checkstyle")
        plugin("maven-publish")
        plugin("signing")
        plugin("io.github.jeadyx.sonatype-uploader")
        plugin("org.kordamp.gradle.jandex")
    }

    java {
        toolchain {
            languageVersion.set(JavaLanguageVersion.of(21))
        }
        withJavadocJar()
        withSourcesJar()
    }

    publishing {
        publications {
            create<MavenPublication>("mavenJava") {
                from(components["java"])
                pom {
                    description =
                        "A lightweight database wrapper based on Jdbi, enabling incremental table and column creation while providing recommended CRUD functions."
                    url = "https://github.com/ximatai/MuYunDatabase"
                    licenses {
                        license {
                            name = "The Apache License, Version 2.0"
                            url = "http://www.apache.org/licenses/LICENSE-2.0.txt"
                        }
                    }
                    developers {
                        developer {
                            id = "aruis"
                            name = "Rui Liu"
                            email = "lovearuis@gmail.com"
                            organization = "戏码台"
                        }
                    }
                    scm {
                        connection = "scm:git:git://github.com/ximatai/MuYunDatabase.git"
                        developerConnection = "scm:git:ssh://github.com/ximatai/MuYunDatabase.git"
                        url = "https://github.com/ximatai/MuYunDatabase"
                    }
                }
            }
        }
        repositories {
//            maven {
//                url = uri(layout.buildDirectory.dir("repo"))
//            }

            maven {
                url = uri("http://192.168.3.19:8081/repository/maven-snapshots/")
                isAllowInsecureProtocol = true
                credentials {
                    username = findProperty("office19.maven.username").toString()
                    password = findProperty("office19.maven.password").toString()
                }
            }
        }
    }

    sonatypeUploader {
        repositoryPath = layout.buildDirectory.dir("repo").get().asFile.path
        tokenName = findProperty("sonatype.token").toString()
        tokenPasswd = findProperty("sonatype.password").toString()
    }

    signing {
        sign(publishing.publications["mavenJava"])
        useInMemoryPgpKeys(
            findProperty("signing.keyId").toString(),
            findProperty("signing.secretKey").toString(),
            findProperty("signing.password").toString()
        )
    }

    tasks.withType<Javadoc> {
        enabled = false
    }

    tasks.named<Checkstyle>("checkstyleMain") {
        dependsOn(tasks.named("jandex"))
    }

    tasks.named<Javadoc>("javadoc") {
        mustRunAfter(tasks.named("jandex"))
    }

    tasks.withType<GenerateModuleMetadata> {
        suppressedValidationErrors.add("enforced-platform")
    }

    tasks.withType<JavaCompile> {
        options.encoding = "UTF-8"
        options.compilerArgs.add("-parameters")
    }

    tasks.withType<Test> {
        maxHeapSize = "2g"
    }

}

tasks.register("prepareRepo") {
    group = "release"
    dependsOn(subprojects.flatMap { it.tasks.matching { task -> task.name == "publishMavenJavaPublicationToMavenRepository" } })
}

tasks.register("releaseAllJars") {
    group = "release"
    dependsOn(subprojects.flatMap { it.tasks.matching { task -> task.name == "publishToSonatype" } })
}
