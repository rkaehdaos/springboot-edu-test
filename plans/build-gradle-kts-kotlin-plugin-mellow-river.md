# Kotlin 플러그인 추가 계획

## Context

Spring Boot 3.5.16 / Java 25 교육용 프로젝트에 Kotlin 지원을 추가한다. 기존 컨벤션대로 플러그인 버전은 `gradle/libs.versions.toml` Version Catalog에서 중앙 관리한다. 최신 안정 버전은 **Kotlin 2.4.0** (2026-07 기준, [Gradle Plugin Portal](https://plugins.gradle.org/plugin/org.jetbrains.kotlin.jvm), [kotlinlang.org releases](https://kotlinlang.org/docs/releases.html)).

## 변경 파일

### 1. `gradle/libs.versions.toml`

`[versions]`에 추가:
```toml
kotlin = "2.4.0"
```

`[plugins]`에 추가 (기존 alias 스타일과 동일하게):
```toml
kotlin-jvm = { id = "org.jetbrains.kotlin.jvm", version.ref = "kotlin" }
kotlin-spring = { id = "org.jetbrains.kotlin.plugin.spring", version.ref = "kotlin" }
```
- `kotlin-spring`(all-open)은 Spring 프록시 대상 클래스(`@Component`, `@Configuration` 등)를 자동 `open` 처리 — Spring 프로젝트에서 사실상 필수.

### 2. `build.gradle.kts`

`plugins` 블록에 추가:
```kotlin
alias(libs.plugins.kotlin.jvm)
alias(libs.plugins.kotlin.spring)
```

Kotlin 컴파일 설정 추가 (기존 Java 25 toolchain과 정합):
```kotlin
kotlin {
    compilerOptions {
        jvmTarget = org.jetbrains.kotlin.gradle.dsl.JvmTarget.JVM_25
        freeCompilerArgs.add("-Xjsr305=strict")
    }
}
```
- Kotlin 플러그인은 `java.toolchain`(25)을 자동으로 따르므로 별도 toolchain 설정 불필요.

Spring Boot BOM의 Kotlin 버전 정렬 — Boot 3.5 BOM이 관리하는 `kotlin-stdlib` 버전(2.1.x)이 플러그인 버전(2.4.0)과 어긋나므로, `plugins` 블록 아래에 오버라이드 추가:
```kotlin
extra["kotlin.version"] = libs.versions.kotlin.get()
```

소스 디렉터리: Kotlin 플러그인이 `src/main/kotlin`, `src/test/kotlin`을 자동 인식하므로 추가 설정 불필요.

## 검증

```bash
./gradlew clean build   # 컴파일 + PMD + 테스트 + JaCoCo 전체 통과 확인
./gradlew dependencies --configuration compileClasspath | grep kotlin-stdlib  # 2.4.0 정렬 확인
```

## 비고

- PMD는 Java 소스만 분석하므로 Kotlin 추가와 충돌 없음.
- GraalVM native 빌드는 Kotlin과 호환되나, native 검증은 기존 `./gradlew nativeCompile` 흐름 그대로.
