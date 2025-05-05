plugins {
    java
    `java-library`
}

java {
    toolchain {
        languageVersion.set(JavaLanguageVersion.of(8))
    }
    withJavadocJar()
    withSourcesJar()
}

dependencies {
    api(project(":muyun-database-core"))
    api("org.jdbi:jdbi3-core:3.39.1") // 最后一个支持Java 8的版本
}
