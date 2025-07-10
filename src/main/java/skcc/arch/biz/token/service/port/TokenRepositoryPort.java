package skcc.arch.biz.token.service.port;

import skcc.arch.biz.token.domain.Token;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

/**
 * 토큰 저장소 아웃바운드 포트
 * 도메인이 외부 저장소와 상호작용하기 위한 계약 정의
 */
public interface TokenRepositoryPort {
    
    // 기본 CRUD
    Token save(Token token);
    Optional<Token> findById(Long id);
    void deleteById(Long id);
    long count();
    
    // 비즈니스 조회
    Optional<Token> findByTokenValue(String tokenValue);
    Optional<Token> findActiveTokenByUserId(String userId);
    List<Token> findExpiredTokens(LocalDateTime now);
    
    // 상태별 조회
    long countByIsActive(boolean isActive);
    Page<Token> findByUserIdContaining(String userId, Pageable pageable);
    Page<Token> findByIsActive(boolean isActive, Pageable pageable);
    Page<Token> findByUserIdContainingAndIsActive(String userId, boolean isActive, Pageable pageable);
    Page<Token> findAll(Pageable pageable);
    
    // 배치 작업
    void deactivateAllTokensByUserId(String userId);
    void deleteExpiredTokens(LocalDateTime now);
} 