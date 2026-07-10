plugins {
    java
    jacoco
    pmd
    alias(libs.plugins.spring.boot)
    alias(libs.plugins.spring.dependency.management)
    alias(libs.plugins.hibernate)
    alias(libs.plugins.graalvm.native)
}

group = "com.example"
version = "0.0.1-SNAPSHOT"

java {
    toolchain { languageVersion = JavaLanguageVersion.of(25) }
    sourceCompatibility = JavaVersion.VERSION_25
}

configurations {
    compileOnly { extendsFrom(configurations.annotationProcessor.get()) }
}

repositories {
//    mavenLocal()
    mavenCentral()
//    maven { url = uri("http://repo.spring.io/release") }
//    maven { url = uri("http://repo.spring.io/snapshot") }
//    maven { url = uri("https://repo.spring.io/libs-snapshot") }
//    maven { url = uri("http://repo.spring.io/milestone") }
//    maven { url = uri("https://repo.spring.io/libs-milestone") }
    gradlePluginPortal()
}

dependencies {
//    implementation(libs.spring.boot.starter.actuator)
//    implementation(libs.spring.boot.starter.batch)
//    implementation(libs.spring.boot.starter.data.jpa)
//    implementation(libs.spring.boot.starter.data.ldap)
//    implementation(libs.spring.boot.starter.data.redis)
//    implementation(libs.spring.boot.starter.graphql)
//    implementation(libs.spring.boot.starter.hateoas)
//    implementation(libs.spring.boot.starter.jdbc)
//    implementation(libs.spring.boot.starter.oauth2.authorization.server)
//    implementation(libs.spring.boot.starter.oauth2.resource.server)
//    implementation(libs.spring.boot.starter.security)
//    implementation(libs.spring.boot.starter.thymeleaf)
    implementation(libs.spring.boot.starter.web)
//    implementation(libs.spring.boot.starter.web.services)
//    implementation(libs.spring.boot.admin.starter.client)
//    implementation(libs.spring.boot.admin.starter.server)
//    implementation(libs.micrometer.tracing.bridge.brave)
//    implementation(libs.spring.data.rest.hal.explorer)
    implementation(libs.spring.kafka)
//    implementation(libs.spring.modulith.starter.core)
//    implementation(libs.spring.modulith.starter.jpa)
//    implementation(libs.thymeleaf.extras.springsecurity6)
    compileOnly(libs.lombok)
    developmentOnly(libs.spring.boot.devtools)
//    developmentOnly(libs.spring.boot.docker.compose)
    runtimeOnly(libs.h2)
    runtimeOnly(libs.mysql.connector.j)
//    runtimeOnly(libs.micrometer.registry.datadog)
//    runtimeOnly(libs.micrometer.registry.prometheus)
    runtimeOnly(libs.postgresql)
//    runtimeOnly(libs.spring.modulith.actuator)
//    runtimeOnly(libs.spring.modulith.observability)
    annotationProcessor(libs.spring.boot.configuration.processor)
    annotationProcessor(libs.lombok)
    testImplementation(libs.spring.boot.starter.test)
    testImplementation(libs.spring.boot.testcontainers)
//    testImplementation(libs.unboundid.ldapsdk)
//    testImplementation(libs.spring.webflux)
//    testImplementation(libs.spring.batch.test)
//    testImplementation(libs.spring.graphql.test)
//    testImplementation(libs.spring.kafka.test)
//    testImplementation(libs.spring.modulith.starter.test)
//    testImplementation(libs.spring.security.test)
    testImplementation(libs.bundles.testcontainers)
    testCompileOnly(libs.lombok)
    testAnnotationProcessor(libs.lombok)
}

dependencyManagement {
    imports {
        mavenBom("de.codecentric:spring-boot-admin-dependencies:${libs.versions.spring.boot.admin.get()}")
        mavenBom("org.springframework.modulith:spring-modulith-bom:${libs.versions.spring.modulith.get()}")
    }
}

//test{} 와 tasks.named('test') {}의 차이
//test{}: 기본 test task 직접 구성, 선언적(평가 단계에서 해당 task 있다고 가정 -> 정의 되어 있어야 한다)
//tasks.named('test')
// - gradle 5.1 이상
// - lazy task 구성 : 실제 실행되거나 다른 task에 의해 실제로 참조 될 때까지 구성 연기
// - 빌드 성능 향상 가능, task  의존성 복잡한 대규모 유리

tasks.named("compileJava") {
    dependsOn(tasks.clean)
}
tasks.withType<JavaCompile> {
    options.compilerArgs.add("-Xlint:deprecation")
}


// pmd
pmd {
    toolVersion = libs.versions.pmd.get()
    isConsoleOutput = true
    isIgnoreFailures = true
    incrementalAnalysis.set(true)
    ruleSets = emptyList()
    ruleSetFiles = files(rootProject.file(".github/pmd/ruleset.xml"))
}
tasks.named<Pmd>("pmdMain") {
    setIncludes(listOf("**/*"))
//    ruleSetFiles = files(rootProject.file(".github/pmd/ruleset.xml"))
}

tasks.named<Pmd>("pmdTest") {
    setExcludes(listOf("**/*"))
}

// Spring Boot AOT 자동 생성 소스(build/generated/aotSources 등)는
// 프레임워크가 생성한 빌드 산출물이므로 PMD 분석 대상에서 제외한다.
// 태스크가 존재하지 않아도 안전하도록 matching 사용.
tasks.matching { it.name in listOf("pmdAot", "pmdAotTest") }.configureEach {
    enabled = false
}

tasks.named<Test>("test") {
    dependsOn("pmdMain")
    useJUnitPlatform()
    maxParallelForks = Runtime.getRuntime().availableProcessors()
    finalizedBy(tasks.named("jacocoTestCoverageVerification"))
}
tasks.named("nativeTest") {
    dependsOn(tasks.check)
}
tasks.named("nativeCompile") {
    dependsOn(tasks.named("nativeTest"))
}


graalvmNative {
    binaries {
        named("main") {}
        named("test") {
//            buildArgs.add("--parallelism=29") // Thread count, 기본값은 max
//            메모리 할당 : GRADLE이 판단해서 너무 높거나 하면 알아서 조절
//            buildArgs.add("-J-Xmx38G") // GIGA
//            buildArgs.add("-J-XX:MaxRAMPercentage=55.0") // PERCENT
        }
        configureEach {
            verbose = true
            buildArgs.add("-H:-CheckToolchain")
        }
    }
}


tasks.jacocoTestReport {
    reports { csv.required = true }
}
tasks.jacocoTestCoverageVerification {
    dependsOn(tasks.jacocoTestReport)
    violationRules {
        rule { limit { minimum = "0.8".toBigDecimal() } }
        rule {
            isEnabled = false // rule 비활성화 -> 해당 룰이 적용되지 않음
            element = "CLASS"
            includes = listOf("com.example.spring3edutest.*") //해당 패키지의 CLASS에 룰 적용

            limit {
                counter = "Line"
                value = "TOTALCOUNT"
                minimum = "0.1".toBigDecimal()
                maximum = "0.5".toBigDecimal()
            }
        }
    }
}
