plugins {
    id("java-library")
}

repositories {
    jcenter()
    mavenCentral()
}

dependencies {
    val vertxVersion = "4.0.0-milestone4"
    val guiceVersion = "4.2.3"
    val guavaVersion = "28.2-jre"
    val slf4jVersion = "1.7.30"
    val jacksonVersion = "2.10.3"
    val kotlinVersion = "1.3.72"
    // This dependency is exported to consumers, that is to say found on their compile classpath.
    api("org.apache.commons:commons-math3:3.6.1")
    api("io.vertx:vertx-core:$vertxVersion")
    api("io.vertx:vertx-web:$vertxVersion")
    api("io.vertx:vertx-web-templ-thymeleaf:$vertxVersion")
    api("javax.inject:javax.inject:1")
    api("com.google.inject:guice:$guiceVersion")
    // https://mvnrepository.com/artifact/javax.ws.rs/jsr311-api
    compileOnly("javax.ws.rs:jsr311-api:1.1.1")
    compileOnly("org.jetbrains.kotlin:kotlin-stdlib-jdk8:$kotlinVersion")
    implementation(project(":forest-kotlin"))
    implementation("com.google.guava:guava:$guavaVersion")
    implementation("org.slf4j:slf4j-api:$slf4jVersion")
    implementation("com.fasterxml.jackson.core:jackson-core:$jacksonVersion")
    implementation("com.fasterxml.jackson.core:jackson-databind:$jacksonVersion")
    implementation("com.fasterxml.jackson.core:jackson-annotations:$jacksonVersion")
    implementation("com.fasterxml.jackson.dataformat:jackson-dataformat-yaml:$jacksonVersion")


    implementation("com.google.guava:guava:$guavaVersion")

    testImplementation("org.junit.jupiter:junit-jupiter-api:5.5.2")
    testRuntimeOnly("org.junit.jupiter:junit-jupiter-engine:5.5.2")
}

val test by tasks.getting(Test::class) {
    useJUnitPlatform()
}
