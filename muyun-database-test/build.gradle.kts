plugins {
    java
}

java {
    toolchain {
        languageVersion.set(JavaLanguageVersion.of(21))
    }
    withJavadocJar()
    withSourcesJar()
}

tasks.test {
    useJUnitPlatform()
}

dependencies {
    testImplementation(project(":muyun-database-jdbi-jdk8"))

    testImplementation(platform("org.junit:junit-bom:5.12.2"))
    testImplementation("org.junit.jupiter:junit-jupiter")
    testRuntimeOnly("org.junit.platform:junit-platform-launcher")

    testImplementation("com.zaxxer:HikariCP:6.3.0")
    testImplementation("org.postgresql:postgresql:42.7.5")
    testImplementation("com.mysql:mysql-connector-j:9.3.0")

    testImplementation("org.testcontainers:testcontainers:1.21.0")
    testImplementation("org.testcontainers:junit-jupiter:1.21.0")
    testImplementation("org.testcontainers:postgresql:1.21.0")
    testImplementation("org.testcontainers:mysql:1.21.0")

    testImplementation("ch.qos.logback:logback-classic:1.5.18")
}
