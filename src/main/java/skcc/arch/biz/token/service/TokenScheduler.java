package skcc.arch.biz.token.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
@Slf4j
@ConditionalOnProperty(name = "token.cleanup.enabled", havingValue = "true", matchIfMissing = true)
public class TokenScheduler {
    
    private final TokenService tokenService;
    
    /**
     * 만료된 토큰을 주기적으로 정리
     * 매 시간마다 실행
     */
    @Scheduled(fixedRateString = "${token.cleanup-interval:3600000}")
    public void cleanupExpiredTokens() {
        log.info("SCHEDULER START - 만료된 토큰 정리 작업을 시작합니다.");
        try {
            tokenService.cleanupExpiredTokens();
            log.info("SCHEDULER SUCCESS - 만료된 토큰 정리 작업이 완료되었습니다.");
        } catch (Exception e) {
            log.error("SCHEDULER ERROR - 만료된 토큰 정리 작업 중 오류 발생: {}", e.getMessage(), e);
        }
    }
    
    /**
     * 매일 자정에 토큰 정리 통계 로그
     */
    @Scheduled(cron = "0 0 0 * * *")
    public void logTokenCleanupStats() {
        log.info("SCHEDULER STATS START - 토큰 정리 통계 로그를 출력합니다.");
        try {
            // 필요 시 통계 정보 수집 및 로그 출력
            log.info("SCHEDULER STATS SUCCESS - 토큰 정리 통계 로그 출력 완료");
        } catch (Exception e) {
            log.error("SCHEDULER STATS ERROR - 통계 로그 출력 중 오류 발생: {}", e.getMessage(), e);
        }
    }
} 