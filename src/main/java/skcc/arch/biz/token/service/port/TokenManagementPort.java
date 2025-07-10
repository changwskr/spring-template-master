package skcc.arch.biz.token.service.port;

import skcc.arch.biz.token.domain.Token;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.Optional;

/**
 * 토큰 관리 인바운드 포트
 * 외부에서 토큰 관리 기능을 사용하기 위한 계약 정의
 */
public interface TokenManagementPort {
    
    // 토큰 생성
    String createToken(String userId);
    String createTokenWithUserInfo(String userId, String username, String email, String[] roles);
    
    // 토큰 검증
    boolean validateToken(String tokenValue);
    String validateTokenAndExtractUserId(String tokenValue);
    String extractUserIdFromToken(String tokenValue);
    
    // 토큰 관리
    void deactivateToken(Long tokenId);
    void reactivateToken(Long tokenId);
    void deleteToken(Long tokenId);
    void deactivateUserToken(String userId);
    
    // 토큰 정리
    void cleanupExpiredTokens();
    
    // 토큰 조회
    Optional<Token> findTokenById(Long tokenId);
    Page<Token> findTokensByCondition(String userId, Boolean isActive, Pageable pageable);
    
    // 통계
    TokenStatistics getTokenStatistics();
    
    /**
     * 토큰 통계 DTO
     */
    record TokenStatistics(
        long totalTokens,
        long activeTokens,
        long inactiveTokens,
        long expiredTokens
    ) {}
} 