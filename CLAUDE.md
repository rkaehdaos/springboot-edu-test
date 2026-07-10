# CLAUDE.md

This file provides guidance to Claude Code (claude.ai/code) when working with code in this repository.

## Project Structure

Spring Boot 3.5.16 애플리케이션 (Java 25)으로 교육용 테스트 프로젝트입니다.

**Build system:**
- Gradle Kotlin DSL (`build.gradle.kts`, `settings.gradle.kts`)
- Version Catalog로 플러그인/의존성 버전 중앙 관리 (`gradle/libs.versions.toml`)
- 라이브러리 버전은 Spring Boot BOM(`io.spring.dependency-management`)이 관리하므로 카탈로그에는 좌표만 선언

**Main packages:**
- `com.example.spring3edutest` - 메인 애플리케이션 패키지
- `com.example.spring3edutest.greeting` - 인사말 관련 컨트롤러와 모델

## Build Commands

**Basic build and test:**
```bash
./gradlew build
./gradlew test
./gradlew clean build
```

**Code quality checks:**
```bash
./gradlew pmdMain        # PMD 정적 분석 (메인 코드)
./gradlew check          # 모든 검증 작업 실행
./gradlew jacocoTestReport                 # 코드 커버리지 리포트 생성
./gradlew jacocoTestCoverageVerification   # 커버리지 검증 (80% 최소)
```

**Run application:**
```bash
./gradlew bootRun
```

**Native image (GraalVM):**
```bash
./gradlew nativeCompile  # Native 바이너리 컴파일
./gradlew nativeTest     # Native 테스트 실행
```

## Test Strategy

- JUnit 5 플랫폼 사용
- Testcontainers 지원 (MySQL, PostgreSQL, Kafka)
- MockMvc를 사용한 웹 레이어 테스트
- JaCoCo 코드 커버리지 80% 최소 요구사항
- 병렬 테스트 실행 (최대 프로세서 수만큼)

## Code Quality

- PMD 정적 분석 활성화 (`.github/pmd/ruleset.xml` 규칙 사용)
- JaCoCo 코드 커버리지 검증
- Lombok 사용 (컴파일 및 테스트 시)
- Java 21 언어 기능 활용

## Dependencies

**Core:**
- Spring Boot Web
- Spring Kafka
- H2, MySQL, PostgreSQL 드라이버
- Lombok

**Testing:**
- Spring Boot Test
- Testcontainers (MySQL, PostgreSQL, Kafka)

## Development Notes

- `test` 작업은 `pmdMain` 의존성을 가짐 (코드 품질 우선 실행)
- 컴파일 시 deprecation 경고 표시
- UTF-8 인코딩 강제 설정
- GraalVM native image 지원