package com.greenhealing.studio.common.config;

import com.greenhealing.studio.auth.domain.User;
import com.greenhealing.studio.auth.repository.UserRepository;
import com.greenhealing.studio.lesson.domain.ClassSchedule;
import com.greenhealing.studio.lesson.repository.ClassScheduleRepository;
import com.greenhealing.studio.product.domain.Product;
import com.greenhealing.studio.product.repository.ProductRepository;
import com.greenhealing.studio.review.domain.Review;
import com.greenhealing.studio.review.repository.ReviewRepository;
import com.greenhealing.studio.studio.domain.Studio;
import com.greenhealing.studio.studio.repository.StudioRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;

/**
 * 개발용 샘플 데이터 자동 삽입.
 * 각 테이블이 비어있을 때만 동작하므로, 몇 번을 재시작해도 중복으로 쌓이지 않는다.
 *
 * 나중에 실제 관리자 페이지(상품/클래스 등록, 입점 신청/승인)가 만들어지면
 * 이 클래스는 지우거나 최소한으로 줄여도 된다.
 */
@Component
@RequiredArgsConstructor
public class DataInitializer implements CommandLineRunner {

    private final StudioRepository studioRepository;
    private final ProductRepository productRepository;
    private final ClassScheduleRepository classScheduleRepository;
    private final ReviewRepository reviewRepository;
    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    @Override
    public void run(String... args) {
        seedPlatformAdmin();
        List<Studio> studios = seedStudios();
        List<Product> products = seedProducts(studios);
        seedClasses(studios);
        User demoUser = seedDemoCustomer();
        seedReviews(demoUser, products);
    }

    // ---------- 플랫폼 관리자(SUPER_ADMIN) ----------
    private void seedPlatformAdmin() {
        if (userRepository.existsByUsername("platform")) {
            return;
        }
        userRepository.save(User.builder()
                .name("플랫폼 관리자")
                .username("platform")
                .email("platform@greenhealing.studio")
                .password(passwordEncoder.encode("platform1234!")) // 로컬 테스트 전용, 배포 시 반드시 변경/삭제
                .phone("010-0000-0001")
                .role(User.Role.SUPER_ADMIN)
                .build());
    }

    // ---------- 입점 공방 3곳 (공방 관리자 계정 포함) ----------
    private List<Studio> seedStudios() {
        if (studioRepository.count() > 0) {
            return studioRepository.findAll();
        }

        User owner1 = createStudioAdmin("studio1", "김그린", "studio1@greenhealing.studio", "010-1111-0001");
        User owner2 = createStudioAdmin("studio2", "이포레", "studio2@greenhealing.studio", "010-1111-0002");
        User owner3 = createStudioAdmin("studio3", "박모먼트", "studio3@greenhealing.studio", "010-1111-0003");

        Studio studio1 = Studio.builder().owner(owner1).name("그린힐링 스튜디오")
                .description("터프팅 완제품과 재료를 판매하는 공방입니다.").build();
        studio1.approve(); // 승인된 상태로 시작 (즉시 이용 가능한 대표 공방)

        Studio studio2 = Studio.builder().owner(owner2).name("포레스트 터프팅")
                .description("숲과 자연을 모티브로 한 러그를 만드는 공방입니다.").build();
        studio2.approve();

        Studio studio3 = Studio.builder().owner(owner3).name("모먼트 클래스룸")
                .description("원데이클래스와 커플 클래스를 전문으로 운영하는 공방입니다.").build();
        studio3.approve();

        return studioRepository.saveAll(List.of(studio1, studio2, studio3));
    }

    private User createStudioAdmin(String username, String name, String email, String phone) {
        return userRepository.save(User.builder()
                .name(name)
                .username(username)
                .email(email)
                .password(passwordEncoder.encode(username + "1234!")) // 로컬 테스트 전용 비밀번호
                .phone(phone)
                .role(User.Role.STUDIO_ADMIN)
                .build());
    }

    // ---------- 상품 30개 (공방 3곳에 분산 배치) ----------
    private List<Product> seedProducts(List<Studio> studios) {
        if (productRepository.count() > 0) {
            return productRepository.findAll();
        }

        Studio s1 = studios.get(0);
        Studio s2 = studios.get(1);
        Studio s3 = studios.get(2);

        return productRepository.saveAll(List.of(
                // ---------- 그린힐링 스튜디오(s1) : 완제품 위주 ----------
                Product.builder().studio(s1).name("그린 플라워 러그").category("완제품").price(65000).stock(8)
                        .description("꽃 모양으로 터프팅한 그린 톤 러그입니다. 아이 방이나 거실 포인트로 잘 어울려요.")
                        .imageUrl("/images/products/green-flower-rug.jpg").build(),
                Product.builder().studio(s1).name("리프 미니 러그").category("완제품").price(48000).stock(12)
                        .description("잎사귀 모양의 아치형 미니 러그. 협탁이나 화장대 앞에 두기 좋은 사이즈입니다.")
                        .imageUrl("/images/products/leaf-mini-rug.jpg").build(),
                Product.builder().studio(s1).name("포레스트 러그").category("완제품").price(72000).stock(6)
                        .description("숲을 닮은 그린·아이보리 조합의 오각형 러그입니다.")
                        .imageUrl("/images/products/forest-rug.jpg").build(),
                Product.builder().studio(s1).name("모스그린 러그").category("완제품").price(68000).stock(10)
                        .description("러그건으로 한 올 한 올 실을 심어 완성한 터프팅 러그입니다. 부드러운 촉감과 은은한 그린 컬러가 특징입니다.")
                        .imageUrl("/images/products/moss-green-rug.jpg").build(),
                Product.builder().studio(s1).name("클레이 쿠션 커버").category("완제품").price(32000).stock(15)
                        .description("베이지·그린 배색의 터프팅 쿠션 커버. 40x40cm 쿠션에 맞는 사이즈입니다.")
                        .imageUrl("/images/products/clay-cushion-cover.jpg").build(),
                Product.builder().studio(s1).name("선셋 러그").category("완제품").price(75000).stock(5)
                        .description("따뜻한 오렌지·브라운 톤으로 포인트를 준 러그입니다.")
                        .imageUrl("/images/products/sunset-rug.jpg").build(),
                Product.builder().studio(s1).name("클라우드 러그").category("완제품").price(64000).stock(9)
                        .description("구름 모양으로 터프팅한 화이트·그레이 톤 러그입니다.")
                        .imageUrl("/images/products/cloud-rug.jpg").build(),
                Product.builder().studio(s1).name("데이지 플라워 러그").category("완제품").price(58000).stock(11)
                        .description("데이지 꽃 모양의 파스텔톤 러그로 협탁이나 창가에 잘 어울립니다.")
                        .imageUrl("/images/products/daisy-flower-rug.jpg").build(),
                Product.builder().studio(s1).name("체커 플라워 방석").category("완제품").price(36000).stock(14)
                        .description("체커보드 패턴과 플라워 포인트가 어우러진 방석입니다.")
                        .imageUrl("/images/products/checker-flower-cushion.jpg").build(),
                Product.builder().studio(s1).name("문 러그 (초승달)").category("완제품").price(52000).stock(7)
                        .description("초승달 모양의 아이보리 톤 러그입니다.").build(),
                Product.builder().studio(s1).name("터프팅 스타터 키트").category("키트").price(28000).stock(20)
                        .description("터프팅을 처음 시작하는 분들을 위한 재료 키트. 실, 도안, 기본 설명서가 포함되어 있습니다.")
                        .imageUrl("/images/products/tufting-starter-kit.jpg").build(),
                Product.builder().studio(s1).name("키즈 터프팅 키트").category("키트").price(24000).stock(18)
                        .description("아이들도 안전하게 사용할 수 있는 어린이용 터프팅 키트입니다.").build(),

                // ---------- 포레스트 터프팅(s2) : 완제품 + 재료 ----------
                Product.builder().studio(s2).name("웨이브 러그").category("완제품").price(70000).stock(6)
                        .description("물결 무늬로 터프팅한 그린·블루 톤 러그입니다.").build(),
                Product.builder().studio(s2).name("허니콤 쿠션 커버").category("완제품").price(34000).stock(13)
                        .description("육각형 패턴이 돋보이는 쿠션 커버, 45x45cm 사이즈입니다.").build(),
                Product.builder().studio(s2).name("선인장 미니 러그").category("완제품").price(41000).stock(10)
                        .description("선인장 모양의 그린 톤 미니 러그로 인테리어 소품에 좋습니다.").build(),
                Product.builder().studio(s2).name("스마일 러그").category("완제품").price(59000).stock(8)
                        .description("동글동글한 스마일 모양의 옐로우 톤 러그입니다.").build(),
                Product.builder().studio(s2).name("체크 패턴 발매트").category("완제품").price(45000).stock(12)
                        .description("체크무늬로 터프팅한 현관용 발매트입니다.").build(),
                Product.builder().studio(s2).name("무지개 벽걸이").category("완제품").price(56000).stock(9)
                        .description("무지개 모양으로 터프팅한 벽걸이 인테리어 소품입니다.").build(),
                Product.builder().studio(s2).name("터프팅 원사 세트 (그린 컬러)").category("재료").price(19000).stock(30)
                        .description("그린 계열 톤온톤 원사 5색 세트. 자체 작업이나 커스텀 제작 시 활용하기 좋습니다.")
                        .imageUrl("/images/products/tufting-yarn-set.jpg").build(),
                Product.builder().studio(s2).name("터프팅 원사 세트 (베이지 컬러)").category("재료").price(19000).stock(28)
                        .description("베이지·아이보리 계열 톤온톤 원사 5색 세트입니다.").build(),
                Product.builder().studio(s2).name("터프팅 원사 세트 (파스텔 컬러)").category("재료").price(21000).stock(22)
                        .description("연핑크·연보라 등 파스텔 톤 원사 5색 세트입니다.").build(),
                Product.builder().studio(s2).name("모노까이 백킹 원단 (1m)").category("재료").price(12000).stock(40)
                        .description("터프팅 작업용 모노까이 백킹 원단, 1m 단위 판매입니다.").build(),
                Product.builder().studio(s2).name("미니 러그 원데이 키트").category("키트").price(35000).stock(16)
                        .description("한 번에 완성할 수 있는 미니 러그 전용 키트입니다.").build(),

                // ---------- 모먼트 클래스룸(s3) : 재료·도구 + 클래스용 키트 ----------
                Product.builder().studio(s3).name("러그건 (컷파일)").category("재료").price(89000).stock(6)
                        .description("컷파일 방식의 터프팅 러그건입니다. 초보자도 사용하기 쉽습니다.").build(),
                Product.builder().studio(s3).name("러그건 (루프파일)").category("재료").price(95000).stock(5)
                        .description("루프파일 방식의 터프팅 러그건입니다. 도톰한 질감 표현에 좋습니다.").build(),
                Product.builder().studio(s3).name("터프팅 프레임 (중형)").category("재료").price(45000).stock(9)
                        .description("60x90cm 작업이 가능한 중형 터프팅 프레임입니다.").build(),
                Product.builder().studio(s3).name("가위 세트 (전기 클리퍼 포함)").category("재료").price(38000).stock(11)
                        .description("러그 마감 작업용 가위와 전기 클리퍼가 포함된 세트입니다.").build(),
                Product.builder().studio(s3).name("글루건 & 라텍스 마감재").category("재료").price(15000).stock(25)
                        .description("완성된 러그 뒷면 마감용 라텍스와 글루건 세트입니다.").build(),
                Product.builder().studio(s3).name("쿠션 커버 DIY 키트").category("키트").price(32000).stock(14)
                        .description("쿠션 커버 제작에 필요한 재료가 모두 포함된 키트입니다.").build(),
                Product.builder().studio(s3).name("커플 클래스 홈키트").category("키트").price(68000).stock(8)
                        .description("두 명이 함께 즐길 수 있는 2인용 홈 키트입니다.").build()
        ));
    }

    // ---------- 클래스 6개 (공방 3곳에 분산) ----------
    private void seedClasses(List<Studio> studios) {
        if (classScheduleRepository.count() > 0) {
            return;
        }

        Studio s1 = studios.get(0);
        Studio s2 = studios.get(1);
        Studio s3 = studios.get(2);

        // 정원 대비 예약 인원을 다르게 넣어서 "예약가능/마감임박/마감" 상태를 골고루 테스트할 수 있게 구성
        List<ClassSchedule> classes = List.of(
                build(s1, "터프팅 원데이클래스", LocalDate.now().plusDays(3), LocalTime.of(10, 0), LocalTime.of(13, 0), 4, 45000, 1),
                build(s1, "터프팅 원데이클래스", LocalDate.now().plusDays(5), LocalTime.of(14, 0), LocalTime.of(17, 0), 4, 45000, 4), // 마감
                build(s2, "터프팅 원데이클래스", LocalDate.now().plusDays(7), LocalTime.of(10, 0), LocalTime.of(13, 0), 6, 45000, 3),
                build(s2, "미니 러그 클래스", LocalDate.now().plusDays(8), LocalTime.of(19, 0), LocalTime.of(21, 0), 4, 38000, 0),
                build(s3, "터프팅 원데이클래스", LocalDate.now().plusDays(10), LocalTime.of(10, 0), LocalTime.of(13, 0), 6, 45000, 5), // 마감임박
                build(s3, "커플 클래스", LocalDate.now().plusDays(12), LocalTime.of(14, 0), LocalTime.of(17, 0), 2, 90000, 0)
        );

        classScheduleRepository.saveAll(classes);
    }

    private ClassSchedule build(Studio studio, String title, LocalDate date, LocalTime start, LocalTime end,
                                int capacity, int price, int reservedCount) {
        ClassSchedule cs = ClassSchedule.builder()
                .studio(studio).title(title).classDate(date).startTime(start).endTime(end)
                .capacity(capacity).price(price).build();
        for (int i = 0; i < reservedCount; i++) {
            cs.reserve(); // 실제 엔티티 로직을 그대로 태워서 reservedCount를 채움
        }
        return cs;
    }

    // ---------- 소비자(CUSTOMER) 테스트 계정 ----------
    private User seedDemoCustomer() {
        return userRepository.findByUsername("demo").orElseGet(() ->
                userRepository.save(User.builder()
                        .name("데모 사용자")
                        .username("demo")
                        .email("demo@greenhealing.studio")
                        .password(passwordEncoder.encode("demo1234!")) // 로컬 테스트 전용 계정, 실제 배포 시 삭제할 것
                        .phone("010-0000-0000")
                        .role(User.Role.CUSTOMER)
                        .build())
        );
    }

    private void seedReviews(User demoUser, List<Product> products) {
        if (reviewRepository.count() > 0 || products.isEmpty()) {
            return;
        }

        reviewRepository.saveAll(List.of(
                Review.builder().user(demoUser).product(products.get(0)).rating(5)
                        .content("색감이 사진이랑 똑같아요. 촉감도 부드럽고 만족스러워요.").build(),
                Review.builder().user(demoUser).product(products.get(4)).rating(4)
                        .content("배송도 빠르고 포장도 꼼꼼했습니다.").build(),
                Review.builder().user(demoUser).product(products.get(1)).rating(5)
                        .content("사이즈가 딱 원하던 크기였어요. 재구매 의사 있습니다.").build()
        ));
    }
}