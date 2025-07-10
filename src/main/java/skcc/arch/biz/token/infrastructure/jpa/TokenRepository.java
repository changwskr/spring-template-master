package skcc.arch.biz.token.infrastructure.jpa;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import skcc.arch.biz.token.domain.Token;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

public interface TokenRepository extends JpaRepository<Token, Long> {
    
    // 토큰 값으로 토큰 조회
    Optional<Token> findByTokenValue(String tokenValue);
    
    // 사용자 ID로 활성 토큰 조회
    @Query("SELECT t FROM Token t WHERE t.userId = :userId AND t.isActive = true")
    Optional<Token> findActiveTokenByUserId(@Param("userId") String userId);
    
    // 만료된 토큰 조회
    @Query("SELECT t FROM Token t WHERE t.expiresAt < :now")
    List<Token> findExpiredTokens(@Param("now") LocalDateTime now);
    
    // 사용자의 모든 토큰 비활성화
    @Modifying
    @Query("UPDATE Token t SET t.isActive = false WHERE t.userId = :userId")
    void deactivateAllTokensByUserId(@Param("userId") String userId);
    
    // 활성 상태별 토큰 개수 조회
    long countByIsActive(boolean isActive);
    
    // 사용자 ID와 활성 상태로 토큰 조회 (페이징)
    Page<Token> findByUserIdContainingAndIsActive(String userId, boolean isActive, Pageable pageable);
    
    // 사용자 ID로 토큰 조회 (페이징)
    Page<Token> findByUserIdContaining(String userId, Pageable pageable);
    
    // 활성 상태로 토큰 조회 (페이징)
    Page<Token> findByIsActive(boolean isActive, Pageable pageable);
    
    @Modifying
    @Query("DELETE FROM Token t WHERE t.expiresAt < :now")
    void deleteExpiredTokens(@Param("now") LocalDateTime now);
} 