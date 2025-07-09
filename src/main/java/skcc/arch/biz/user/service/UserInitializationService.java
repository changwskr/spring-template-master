package skcc.arch.biz.user.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.stereotype.Service;
import skcc.arch.biz.user.domain.UserCreate;

@Service
@RequiredArgsConstructor
@Slf4j
public class UserInitializationService implements ApplicationRunner {

    private final UserService userService;

    @Override
    public void run(ApplicationArguments args) throws Exception {
        try {
            // 테스트용 사용자가 없으면 생성
            log.info("초기 사용자 생성 시작");
            createInitialUser();
            log.info("초기 사용자 생성 완료");
        } catch (Exception e) {
            log.error("초기 사용자 생성 중 오류 발생: {}", e.getMessage());
        }
    }

    private void createInitialUser() {
        UserCreate adminUser = UserCreate.builder()
                .userId("admin")
                .username("관리자")
                .email("admin@example.com")
                .password("admin123")
                .address("서울특별시 강남구")
                .job("시스템 관리자")
                .age(35)
                .company("SK C&C")
                .build();

        userService.signUp(adminUser);
        log.info("초기 관리자 사용자 생성: {}", adminUser.getUserId());
    }
} 