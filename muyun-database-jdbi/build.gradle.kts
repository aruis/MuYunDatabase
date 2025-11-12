plugins {
    java
    `java-library`
}

java {
    toolchain {
        languageVersion.set(JavaLanguageVersion.of(17))
    }
    withJavadocJar()
    withSourcesJar()
}

dependencies {
    api(project(":muyun-database-core"))
    api("org.jdbi:jdbi3-core:3.50.0")
}
