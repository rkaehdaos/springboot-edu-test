# Gradle build deprecated API / warning 수정 Plan

## Context

`./gradlew clean build --warning-mode all` 실행 시 빌드는 성공하지만(BUILD SUCCESSFUL), 세 종류의 deprecation/warning 이 출력된다. Gradle 10 이후 일부 API가 **에러로 승격**될 예정이므로 지금 정리해 향후 Gradle/Testcontainers 업그레이드에 대비한다.

빌드에서 확인된 경고:

1. **Testcontainers `KafkaContainer` deprecated** (우리 코드) — `TestSpring3EduTestApplication.java:8,18-19`
   - `org.testcontainers.containers.KafkaContainer` 가 deprecated 됨.
2. **Gradle 프로젝트 의존성 표기법 deprecated** (플러그인 내부) — "Using a Project object as a dependency notation has been deprecated. This will fail with an error in Gradle 10."
   - stacktrace 상 원인은 **우리 build.gradle.kts 가 아니라** GraalVM buildtools 플러그인 `1.1.3` 내부(`NativeImagePlugin.createNativeConfigurations`, `NativeImagePlugin.java:950`)에서 발생.
3. **Lombok `sun.misc.Unsafe` 호출 경고** (라이브러리 내부) — `lombok.permit.Permit` 가 호출.
   - Lombok 내부 구현 이슈로 **우리 코드로는 수정 불가**. 이번 범위에서 제외(하단 "제외 항목" 참고).

**이번 수정 범위(사용자 확정):** 1번 KafkaContainer 마이그레이션 + 2번 GraalVM 플러그인 업그레이드.

---

## 수정 1 — Testcontainers KafkaContainer 마이그레이션

### 근거
Testcontainers 1.21.x 는 신규 Kafka 컨테이너 API를 제공한다 (캐시에서 `kafka-1.21.4.jar` 확인):
- `org.testcontainers.kafka.KafkaContainer` — Apache Kafka 네이티브 이미지(`apache/kafka`)용
- `org.testcontainers.kafka.ConfluentKafkaContainer` — **Confluent 이미지(`confluentinc/cp-kafka`)용** ← 현재 사용 이미지에 정확히 대응
- `org.testcontainers.containers.KafkaContainer` — deprecated (현재 사용 중)

현재 코드는 `confluentinc/cp-kafka:latest` 이미지를 쓰므로, 정확한 대체재는 **`ConfluentKafkaContainer`** 이다.
`javap` 로 `ConfluentKafkaContainer(DockerImageName)` 생성자 존재를 확인했고, `GenericContainer` 를 상속하므로 Spring Boot `@ServiceConnection` 과 호환된다.

### 변경 파일
`src/test/java/com/example/spring3edutest/TestSpring3EduTestApplication.java`

- import 변경: `org.testcontainers.containers.KafkaContainer` → `org.testcontainers.kafka.ConfluentKafkaContainer` (8번 라인)
- `kafkaContainer()` 빈 (18-19번 라인):
  - 반환 타입 `KafkaContainer` → `ConfluentKafkaContainer`
  - `new KafkaContainer(DockerImageName.parse("confluentinc/cp-kafka:latest"))` → `new ConfluentKafkaContainer(DockerImageName.parse("confluentinc/cp-kafka:latest"))`

변경 후 코드 형태:
```java
import org.testcontainers.kafka.ConfluentKafkaContainer;
// ...
@Bean
@ServiceConnection
ConfluentKafkaContainer kafkaContainer() {
    return new ConfluentKafkaContainer(DockerImageName.parse("confluentinc/cp-kafka:latest"));
}
```

> 참고: 의존성 좌표(`gradle/libs.versions.toml` 의 `testcontainers-kafka = "org.testcontainers:kafka"`)는 그대로 두어도 신규/구 API가 같은 모듈에 포함되므로 변경 불필요.

---

## 수정 2 — GraalVM native-build-tools 플러그인 업그레이드 (보류)

### 결론: 1.1.3 유지 (업그레이드 보류)
"Project object as a dependency notation" 경고는 stacktrace 상 GraalVM buildtools `1.1.3` 플러그인 내부에서 발생한다(우리 코드 아님). 최신 `1.1.4`(2026-07-07)로 업그레이드를 시도했으나 **Gradle Plugin Portal 에 아직 게시되지 않아 빌드가 실패**한다:

```
Plugin [id: 'org.graalvm.buildtools.native', version: '1.1.4'] was not found ...
could not resolve plugin artifact 'org.graalvm.buildtools.native:...:1.1.4'
```

(1.1.4 는 GitHub Releases 에만 존재, Plugin Portal 미게시)

→ **버전을 1.1.3 으로 되돌림.** `gradle/libs.versions.toml` 은 변경 없음(`graalvm-buildtools = "1.1.3"` 유지).

### 잔존 경고 처리 (문서화)
- "Project object as a dependency notation" 경고는 플러그인 상류(GraalVM buildtools) 이슈이므로 우리 코드로 제거 불가. **잔존 허용**.
- **후속 조치:** `1.1.4` 가 Gradle Plugin Portal 에 게시되면 `libs.versions.toml` 의 버전을 올리고 빌드 로그로 경고 소거 여부를 재검증한다.

---

## 제외 항목 (이번 범위 밖)

- **Lombok `sun.misc.Unsafe` 경고**: `lombok.permit.Permit` 내부 호출로 발생. 우리 코드/설정으로 제거 불가하며 Lombok 라이브러리 자체 업데이트가 필요한 사항이라 이번 수정에서 제외. (향후 Lombok 버전 업그레이드 시 자연 해소 기대.)

---

## 검증 (Verification)

1. 마이그레이션/업그레이드 적용 후 전체 빌드로 경고 소거 확인:
   ```bash
   ./gradlew clean build --warning-mode all 2>&1 | tee /tmp/build-after.log
   ```
   - `BUILD SUCCESSFUL` 유지 확인
   - 로그에서 `KafkaContainer ... has been deprecated` **사라짐** 확인
   - 로그에서 `Using a Project object as a dependency notation` **사라짐 여부** 확인 (수정 2의 핵심 검증 지점)
   - Lombok `sun.misc.Unsafe` 경고는 잔존해도 정상(제외 항목)

2. Kafka 컨테이너가 실제 기동/연결되는지 확인 (Docker 필요):
   ```bash
   ./gradlew test
   ```
   - Testcontainers 기반 컨텍스트 로딩 테스트가 통과하는지 확인. `@ServiceConnection` 이 `ConfluentKafkaContainer` 를 정상 인식하는지 확인.

3. (선택) Native 경로에 영향 없는지 스모크 체크:
   ```bash
   ./gradlew help --warning-mode all --stacktrace 2>&1 | grep -A3 "Project object" || echo "no project-notation warning"
   ```
