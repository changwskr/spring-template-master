package skcc.arch.biz.token.infrastructure.jpa;

import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Repository;
import skcc.arch.biz.token.domain.Token;
import skcc.arch.biz.token.service.port.TokenRepositoryPort;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

/**
 * 토큰 저장소 JPA 어댑터
 * TokenRepositoryPort를 구현하여 JPA 기반 데이터 접근 제공
 */
@Repository
@RequiredArgsConstructor
public class TokenRepositoryAdapter implements TokenRepositoryPort {
    
    private final TokenRepository jpaRepository;
    
    @Override
    public Token save(Token token) {
        return jpaRepository.save(token);
    }
    
    @Override
    public Optional<Token> findById(Long id) {
        return jpaRepository.findById(id);
    }
    
    @Override
    public void deleteById(Long id) {
        jpaRepository.deleteById(id);
    }
    
    @Override
    public long count() {
        return jpaRepository.count();
    }
    
    @Override
    public Optional<Token> findByTokenValue(String tokenValue) {
        return jpaRepository.findByTokenValue(tokenValue);
    }
    
    @Override
    public Optional<Token> findActiveTokenByUserId(String userId) {
        return jpaRepository.findActiveTokenByUserId(userId);
    }
    
    @Override
    public List<Token> findExpiredTokens(LocalDateTime now) {
        return jpaRepository.findExpiredTokens(now);
    }
    
    @Override
    public long countByIsActive(boolean isActive) {
        return jpaRepository.countByIsActive(isActive);
    }
    
    @Override
    public Page<Token> findByUserIdContaining(String userId, Pageable pageable) {
        return jpaRepository.findByUserIdContaining(userId, pageable);
    }
    
    @Override
    public Page<Token> findByIsActive(boolean isActive, Pageable pageable) {
        return jpaRepository.findByIsActive(isActive, pageable);
    }
    
    @Override
    public Page<Token> findByUserIdContainingAndIsActive(String userId, boolean isActive, Pageable pageable) {
        return jpaRepository.findByUserIdContainingAndIsActive(userId, isActive, pageable);
    }
    
    @Override
    public Page<Token> findAll(Pageable pageable) {
        return jpaRepository.findAll(pageable);
    }
    
    @Override
    public void deactivateAllTokensByUserId(String userId) {
        jpaRepository.deactivateAllTokensByUserId(userId);
    }
    
    @Override
    public void deleteExpiredTokens(LocalDateTime now) {
        jpaRepository.deleteExpiredTokens(now);
    }
} 