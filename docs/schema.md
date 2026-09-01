# 데이터베이스 스키마 (참고용)

`spring.jpa.hibernate.ddl-auto=update` 설정으로 애플리케이션 최초 실행 시
JPA 엔티티 기준으로 테이블이 자동 생성됩니다. 아래는 참고용 구조 요약입니다.

## 테이블 개요

| 테이블 | 설명 |
|---|---|
| users | 회원 (이름, 이메일, 비밀번호 해시, 권한) |
| products | 상품 (완제품/재료/키트) |
| cart_items | 장바구니 |
| orders | 주문 (배송지, 결제 상태) |
| order_items | 주문 상품 상세 |
| class_schedules | 클래스 일정 (날짜, 정원, 예약 인원) |
| reservations | 클래스 예약 |
| custom_orders | 주문제작 신청 (사이즈/색상/도안/견적/상태) |
| reviews | 상품/클래스 리뷰 |

## 준비할 것

1. 로컬에 MySQL 설치 및 실행
2. 데이터베이스 생성:
   ```sql
   CREATE DATABASE greenhealing_studio CHARACTER SET utf8mb4;
   ```
3. `src/main/resources/application.yml`의 `datasource.username` / `password`를
   본인 환경에 맞게 수정 (비밀번호는 환경변수 `DB_PASSWORD`로 주입 권장)
4. 최초 실행 시 Hibernate가 테이블을 자동 생성합니다 (`ddl-auto: update`)

## 향후 방향

- 스키마가 어느 정도 안정되면 `ddl-auto: validate`로 전환하고
  Flyway/Liquibase 같은 마이그레이션 도구 도입을 검토하세요.
