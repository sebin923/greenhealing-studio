# 그린힐링 스튜디오 (Green Healing Studio)

터프팅 공방을 위한 상품판매·클래스예약·주문제작 통합 쇼핑몰 웹서비스
(2026학년도 예비캡스톤 프로젝트)

## 기술 스택

- Java 17
- Spring Boot 3.3 (Web, Data JPA, Security, Validation, Thymeleaf)
- MySQL 8
- Maven
- IntelliJ IDEA / Git

## 1. 사전 준비

| 항목 | 버전/설치 |
|---|---|
| JDK | 17 이상 (Temurin 권장) |
| MySQL | 8.0 이상, 로컬에 설치 후 실행 |
| IntelliJ IDEA | Community 또는 Ultimate |
| Git | 최신 버전 |

## 2. 프로젝트 열기 (IntelliJ)

1. IntelliJ IDEA 실행 → **Open** → 이 프로젝트 폴더(`greenhealing-studio`) 선택
2. `pom.xml`을 Maven 프로젝트로 자동 인식 → 의존성 다운로드 대기
   (인터넷이 연결되어 있어야 Maven Central에서 라이브러리를 받아옵니다)
3. Lombok 플러그인 설치 확인: `Settings > Plugins > Lombok` 설치 및 활성화
   → `Settings > Build, Execution, Deployment > Compiler > Annotation Processors`
   에서 **Enable annotation processing** 체크

## 3. 데이터베이스 준비

MySQL 접속 후:

```sql
CREATE DATABASE greenhealing_studio CHARACTER SET utf8mb4;
```

`src/main/resources/application.yml`에서 계정 정보 확인/수정:

```yaml
spring:
  datasource:
    url: jdbc:mysql://localhost:3306/greenhealing_studio?useSSL=false&serverTimezone=Asia/Seoul&characterEncoding=UTF-8
    username: root
    password: ${DB_PASSWORD:changeme}
```

비밀번호는 코드에 직접 적지 말고, 실행 시 환경변수로 주입하는 걸 권장합니다.

- IntelliJ Run Configuration → Environment variables에 `DB_PASSWORD=내비밀번호` 추가
- 또는 터미널에서 `export DB_PASSWORD=내비밀번호` 후 실행

## 4. 실행

**IntelliJ**: `GreenhealingStudioApplication.java` 우클릭 → Run

**터미널** (로컬에 Maven이 설치되어 있는 경우):
```bash
mvn spring-boot:run
```

> 이 스켈레톤에는 Maven Wrapper(`mvnw`)가 포함되어 있지 않습니다.
> IntelliJ에서 열면 내장 Maven으로 바로 실행되고, 터미널에서 wrapper를 쓰고 싶다면
> 로컬에서 `mvn -N io.takari:maven:wrapper` 한 번 실행해 생성하면 됩니다.

정상 구동 시 브라우저에서 http://localhost:8080 접속 → 홈 화면 확인

## 5. 프로젝트 구조

```
src/main/java/com/greenhealing/studio
├── domain        # JPA 엔티티 (User, Product, ClassSchedule, Reservation, CustomOrder, Order, OrderItem, CartItem, Review)
├── repository     # Spring Data JPA Repository
├── service        # 비즈니스 로직 (회원가입, 인증 등)
├── controller     # 화면/API 컨트롤러
└── config         # Security, JPA Auditing 설정

src/main/resources
├── application.yml
├── templates      # Thymeleaf 화면
└── static         # css/js/images
```

## 6. 현재까지 구현된 것 (개발 환경 구축 단계)

- [x] Spring Boot 프로젝트 골격 (Maven, Java 17)
- [x] JPA 엔티티 설계 (회원/상품/장바구니/주문/클래스예약/주문제작/리뷰)
- [x] Spring Security + BCrypt 비밀번호 해시, 로그인/회원가입 폼
- [x] 상품 목록/상세 최소 화면
- [ ] 장바구니·결제(테스트 결제) 기능
- [ ] 클래스 예약(캘린더, 정원 관리) 기능
- [ ] 주문제작 신청 기능
- [ ] 관리자 페이지
- [ ] AWS 배포

## 7. Git 사용 규칙

- 브랜치 전략: main 단일 브랜치 (이전 프로젝트와 동일)
- `.env`, `application-local.yml` 등 민감정보 파일은 절대 커밋하지 않기 (`.gitignore`에 포함됨)
