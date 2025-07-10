package skcc.arch.biz.token.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import skcc.arch.app.exception.CustomException;
import skcc.arch.app.exception.ErrorCode;
import skcc.arch.biz.token.domain.Token;
import skcc.arch.biz.token.service.port.JwtProviderPort;
import skcc.arch.biz.token.service.port.TokenManagementPort;
import skcc.arch.biz.token.service.port.TokenRepositoryPort;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

@Service
@RequiredArgsConstructor
@Slf4j
@Transactional
public class TokenService implements TokenManagementPort {
    
    private final TokenRepositoryPort tokenRepository;
    private final JwtProviderPort jwtProvider;
    
    @Value("${token.expiration-hours:24}")
    private int tokenExpirationHours;
    
    /**
     * 사용자 토큰 생성 및 저장
     */
    public String createToken(String userId) {
        log.info("TOKEN CREATE START - 토큰 생성 시작. userId: {}", userId);
        
        try {
            // 기존 활성 토큰 비활성화
            log.debug("TOKEN CREATE - 기존 활성 토큰 비활성화 시작. userId: {}", userId);
            deactivateUserToken(userId);
            log.debug("TOKEN CREATE - 기존 활성 토큰 비활성화 완료. userId: {}", userId);
            
            // 새 토큰 생성
            log.debug("TOKEN CREATE - JWT 토큰 생성 시작. userId: {}", userId);
            Map<String, Object> claims = new HashMap<>();
            claims.put("uid", userId);
            String tokenValue = jwtProvider.generateToken(claims);
            log.debug("TOKEN CREATE - JWT 토큰 생성 완료. userId: {}", userId);
            
            // 토큰 저장 (설정된 시간 만료)
            log.debug("TOKEN CREATE - DB에 토큰 저장 시작. userId: {}", userId);
            Token token = Token.builder()
                    .userId(userId)
                    .tokenValue(tokenValue)
                    .expiresAt(LocalDateTime.now().plusHours(tokenExpirationHours))
                    .isActive(true)
                    .build();
            
            tokenRepository.save(token);
            log.info("TOKEN CREATE SUCCESS - 새 토큰을 생성하고 저장했습니다. userId: {}, 만료시간: {}시간", userId, tokenExpirationHours);
            
            return tokenValue;
        } catch (Exception e) {
            log.error("TOKEN CREATE ERROR - 토큰 생성 중 오류 발생. userId: {}, error: {}", userId, e.getMessage(), e);
            throw e;
        }
    }
    
    /**
     * 사용자 정보를 포함한 토큰 생성 및 저장 (API 호출용)
     */
    public String createTokenWithUserInfo(String userId, String username, String email, String[] roles) {
        // 기존 활성 토큰 비활성화
        deactivateUserToken(userId);
        
        // 새 토큰 생성 (더 많은 claim 정보 포함)
        Map<String, Object> claims = new HashMap<>();
        claims.put("uid", userId);
        claims.put("username", username);
        claims.put("email", email);
        claims.put("role", roles);
        String tokenValue = jwtProvider.generateToken(claims);
        
        // 토큰 저장 (설정된 시간 만료)
        Token token = Token.builder()
                .userId(userId)
                .tokenValue(tokenValue)
                .expiresAt(LocalDateTime.now().plusHours(tokenExpirationHours))
                .isActive(true)
                .build();
        
        tokenRepository.save(token);
        log.info("사용자 정보를 포함한 토큰을 생성하고 저장했습니다. userId: {}, email: {}, 만료시간: {}시간", userId, email, tokenExpirationHours);
        
        return tokenValue;
    }
    
    /**
     * 토큰 검증
     */
    public boolean validateToken(String tokenValue) {
        try {
            // JWT 토큰 자체 검증
            String userId = jwtProvider.validateTokenAndExtractUID(tokenValue);
            
            // DB에서 토큰 조회
            Optional<Token> tokenOpt = tokenRepository.findByTokenValue(tokenValue);
            if (tokenOpt.isEmpty()) {
                log.warn("DB에서 토큰을 찾을 수 없습니다.");
                return false;
            }
            
            Token token = tokenOpt.get();
            
            // 토큰 활성화 상태 확인
            if (!token.getIsActive()) {
                log.warn("토큰이 비활성화되어 있습니다. userId: {}", token.getUserId());
                return false;
            }
            
            // 토큰 만료 여부 확인
            if (token.isExpired()) {
                log.warn("토큰이 만료되었습니다. userId: {}", token.getUserId());
                return false;
            }
            
            return true;
            
        } catch (CustomException e) {
            log.error("토큰 검증 실패: {}", e.getMessage());
            return false;
        } catch (Exception e) {
            log.error("토큰 검증 중 예상치 못한 오류 발생: {}", e.getMessage());
            return false;
        }
    }
    
    /**
     * 토큰 검증 및 사용자 ID 추출 (성능 최적화)
     */
    public String validateTokenAndExtractUserId(String tokenValue) {
        log.debug("TOKEN VALIDATE START - 토큰 검증 및 사용자 ID 추출 시작");
        
        try {
            // JWT 토큰 자체 검증
            log.debug("TOKEN VALIDATE - JWT 토큰 자체 검증 시작");
            String userId = jwtProvider.validateTokenAndExtractUID(tokenValue);
            log.debug("TOKEN VALIDATE - JWT 토큰 자체 검증 완료. userId: {}", userId);
            
            // DB에서 토큰 조회
            log.debug("TOKEN VALIDATE - DB에서 토큰 조회 시작. userId: {}", userId);
            Optional<Token> tokenOpt = tokenRepository.findByTokenValue(tokenValue);
            if (tokenOpt.isEmpty()) {
                log.warn("TOKEN VALIDATE FAILED - DB에서 토큰을 찾을 수 없음. userId: {}", userId);
                throw new CustomException(ErrorCode.TOKEN_NOT_FOUND_IN_DB);
            }
            
            Token token = tokenOpt.get();
            log.debug("TOKEN VALIDATE - DB에서 토큰 조회 완료. userId: {}", userId);
            
            // 토큰 활성화 상태 확인
            log.debug("TOKEN VALIDATE - 토큰 활성화 상태 확인. userId: {}", userId);
            if (!token.getIsActive()) {
                log.warn("TOKEN VALIDATE FAILED - 토큰이 비활성화됨. userId: {}", userId);
                throw new CustomException(ErrorCode.TOKEN_DEACTIVATED);
            }
            
            // 토큰 만료 여부 확인
            log.debug("TOKEN VALIDATE - 토큰 만료 여부 확인. userId: {}", userId);
            if (token.isExpired()) {
                log.warn("TOKEN VALIDATE FAILED - 토큰이 만료됨. userId: {}", userId);
                throw new CustomException(ErrorCode.JWT_EXPIRED_TOKEN);
            }
            
            log.debug("TOKEN VALIDATE SUCCESS - 토큰 검증 성공. userId: {}", userId);
            return token.getUserId();
            
        } catch (CustomException e) {
            log.error("TOKEN VALIDATE ERROR - 토큰 검증 실패: {}", e.getMessage());
            throw e;
        } catch (Exception e) {
            log.error("TOKEN VALIDATE ERROR - 토큰 검증 중 예상치 못한 오류 발생: {}", e.getMessage(), e);
            throw new CustomException(ErrorCode.JWT_INVALID);
        }
    }
    
    /**
     * 토큰으로 사용자 ID 추출
     */
    public String extractUserIdFromToken(String tokenValue) {
        try {
            Optional<Token> tokenOpt = tokenRepository.findByTokenValue(tokenValue);
            if (tokenOpt.isEmpty()) {
                throw new CustomException(ErrorCode.TOKEN_NOT_FOUND_IN_DB);
            }
            
            Token token = tokenOpt.get();
            
            // 토큰 활성화 상태 확인
            if (!token.getIsActive()) {
                throw new CustomException(ErrorCode.TOKEN_DEACTIVATED);
            }
            
            // 토큰 만료 여부 확인
            if (token.isExpired()) {
                throw new CustomException(ErrorCode.JWT_EXPIRED_TOKEN);
            }
            
            return token.getUserId();
            
        } catch (CustomException e) {
            throw e;
        } catch (Exception e) {
            log.error("토큰에서 사용자 ID 추출 중 오류 발생: {}", e.getMessage());
            throw new CustomException(ErrorCode.JWT_INVALID);
        }
    }
    
    /**
     * 사용자 토큰 비활성화
     */
    public void deactivateUserToken(String userId) {
        log.debug("TOKEN DEACTIVATE START - 사용자 토큰 비활성화 시작. userId: {}", userId);
        
        try {
            Optional<Token> tokenOpt = tokenRepository.findActiveTokenByUserId(userId);
            if (tokenOpt.isPresent()) {
                Token token = tokenOpt.get();
                token.deactivate();
                tokenRepository.save(token);
                log.info("TOKEN DEACTIVATE SUCCESS - 사용자 토큰을 비활성화했습니다. userId: {}", userId);
            } else {
                log.debug("TOKEN DEACTIVATE - 비활성화할 활성 토큰이 없습니다. userId: {}", userId);
            }
        } catch (Exception e) {
            log.error("TOKEN DEACTIVATE ERROR - 토큰 비활성화 중 오류 발생. userId: {}, error: {}", userId, e.getMessage(), e);
            throw e;
        }
    }
    
    /**
     * 개별 토큰 비활성화 (토큰 ID로)
     */
    public void deactivateToken(Long tokenId) {
        log.info("TOKEN DEACTIVATE START - 개별 토큰 비활성화 시작. tokenId: {}", tokenId);
        
        try {
            Optional<Token> tokenOpt = tokenRepository.findById(tokenId);
            if (tokenOpt.isEmpty()) {
                log.warn("TOKEN DEACTIVATE FAILED - 비활성화할 토큰을 찾을 수 없음. tokenId: {}", tokenId);
                throw new CustomException(ErrorCode.NOT_FOUND_ELEMENT);
            }
            
            Token token = tokenOpt.get();
            if (!token.getIsActive()) {
                log.warn("TOKEN DEACTIVATE FAILED - 이미 비활성화된 토큰. tokenId: {}", tokenId);
                throw new CustomException(ErrorCode.INVALID_REQUEST);
            }
            
            token.deactivate();
            tokenRepository.save(token);
            
            log.info("TOKEN DEACTIVATE SUCCESS - 개별 토큰 비활성화 완료. tokenId: {}", tokenId);
            
        } catch (CustomException e) {
            log.error("TOKEN DEACTIVATE ERROR - 토큰 비활성화 실패. tokenId: {}, error: {}", tokenId, e.getMessage());
            throw e;
        } catch (Exception e) {
            log.error("TOKEN DEACTIVATE ERROR - 토큰 비활성화 중 예상치 못한 오류 발생. tokenId: {}, error: {}", tokenId, e.getMessage(), e);
            throw new CustomException(ErrorCode.INTERNAL_SERVER_ERROR);
        }
    }
    
    /**
     * 개별 토큰 재활성화 (토큰 ID로)
     */
    public void reactivateToken(Long tokenId) {
        log.info("TOKEN REACTIVATE START - 개별 토큰 재활성화 시작. tokenId: {}", tokenId);
        
        try {
            Optional<Token> tokenOpt = tokenRepository.findById(tokenId);
            if (tokenOpt.isEmpty()) {
                log.warn("TOKEN REACTIVATE FAILED - 재활성화할 토큰을 찾을 수 없음. tokenId: {}", tokenId);
                throw new CustomException(ErrorCode.NOT_FOUND_ELEMENT);
            }
            
            Token token = tokenOpt.get();
            if (token.getIsActive()) {
                log.warn("TOKEN REACTIVATE FAILED - 이미 활성화된 토큰. tokenId: {}", tokenId);
                throw new CustomException(ErrorCode.INVALID_REQUEST);
            }
            
            if (token.isExpired()) {
                log.warn("TOKEN REACTIVATE FAILED - 만료된 토큰은 재활성화할 수 없음. tokenId: {}", tokenId);
                throw new CustomException(ErrorCode.JWT_EXPIRED_TOKEN);
            }
            
            // 동일 사용자의 다른 활성 토큰이 있다면 비활성화
            deactivateUserToken(token.getUserId());
            
            // 토큰 재활성화
            token.reactivate();
            tokenRepository.save(token);
            
            log.info("TOKEN REACTIVATE SUCCESS - 개별 토큰 재활성화 완료. tokenId: {}", tokenId);
            
        } catch (CustomException e) {
            log.error("TOKEN REACTIVATE ERROR - 토큰 재활성화 실패. tokenId: {}, error: {}", tokenId, e.getMessage());
            throw e;
        } catch (Exception e) {
            log.error("TOKEN REACTIVATE ERROR - 토큰 재활성화 중 예상치 못한 오류 발생. tokenId: {}, error: {}", tokenId, e.getMessage(), e);
            throw new CustomException(ErrorCode.INTERNAL_SERVER_ERROR);
        }
    }
    
    /**
     * 개별 토큰 삭제 (토큰 ID로)
     */
    public void deleteToken(Long tokenId) {
        log.info("TOKEN DELETE START - 개별 토큰 삭제 시작. tokenId: {}", tokenId);
        
        try {
            Optional<Token> tokenOpt = tokenRepository.findById(tokenId);
            if (tokenOpt.isEmpty()) {
                log.warn("TOKEN DELETE FAILED - 삭제할 토큰을 찾을 수 없음. tokenId: {}", tokenId);
                throw new CustomException(ErrorCode.NOT_FOUND_ELEMENT);
            }
            
            tokenRepository.deleteById(tokenId);
            log.info("TOKEN DELETE SUCCESS - 개별 토큰 삭제 완료. tokenId: {}", tokenId);
            
        } catch (CustomException e) {
            log.error("TOKEN DELETE ERROR - 토큰 삭제 실패. tokenId: {}, error: {}", tokenId, e.getMessage());
            throw e;
        } catch (Exception e) {
            log.error("TOKEN DELETE ERROR - 토큰 삭제 중 예상치 못한 오류 발생. tokenId: {}, error: {}", tokenId, e.getMessage(), e);
            throw new CustomException(ErrorCode.INTERNAL_SERVER_ERROR);
        }
    }
    
    /**
     * 만료된 토큰 정리
     */
    public void cleanupExpiredTokens() {
        log.info("TOKEN CLEANUP START - 만료된 토큰 정리 시작");
        
        try {
            LocalDateTime now = LocalDateTime.now();
            log.debug("TOKEN CLEANUP - 만료된 토큰 삭제 쿼리 실행. 기준시간: {}", now);
            
            // 만료된 토큰 조회 (삭제 전 통계를 위해)
            java.util.List<Token> expiredTokens = tokenRepository.findExpiredTokens(now);
            int expiredCount = expiredTokens.size();
            
            if (expiredCount > 0) {
                tokenRepository.deleteExpiredTokens(now);
                log.info("TOKEN CLEANUP SUCCESS - 만료된 토큰 정리 완료. 삭제된 토큰 수: {}", expiredCount);
            } else {
                log.info("TOKEN CLEANUP - 정리할 만료된 토큰이 없습니다.");
            }
        } catch (Exception e) {
            log.error("TOKEN CLEANUP ERROR - 만료된 토큰 정리 중 오류 발생: {}", e.getMessage(), e);
            throw e;
        }
    }
    
    /**
     * 토큰 ID로 토큰 조회
     */
    @Override
    public Optional<Token> findTokenById(Long tokenId) {
        log.debug("TOKEN FIND START - 토큰 ID로 조회. tokenId: {}", tokenId);
        try {
            Optional<Token> token = tokenRepository.findById(tokenId);
            log.debug("TOKEN FIND SUCCESS - 토큰 조회 완료. tokenId: {}, found: {}", tokenId, token.isPresent());
            return token;
        } catch (Exception e) {
            log.error("TOKEN FIND ERROR - 토큰 조회 중 오류 발생. tokenId: {}, error: {}", tokenId, e.getMessage(), e);
            throw e;
        }
    }
    
    /**
     * 조건에 따른 토큰 조회
     */
    @Override
    public Page<Token> findTokensByCondition(String userId, Boolean isActive, Pageable pageable) {
        log.debug("TOKEN FIND CONDITION START - 조건별 토큰 조회. userId: {}, isActive: {}", userId, isActive);
        
        try {
            Page<Token> tokens;
            
            if (userId != null && !userId.trim().isEmpty() && isActive != null) {
                tokens = tokenRepository.findByUserIdContainingAndIsActive(userId.trim(), isActive, pageable);
            } else if (userId != null && !userId.trim().isEmpty()) {
                tokens = tokenRepository.findByUserIdContaining(userId.trim(), pageable);
            } else if (isActive != null) {
                tokens = tokenRepository.findByIsActive(isActive, pageable);
            } else {
                tokens = tokenRepository.findAll(pageable);
            }
            
            log.debug("TOKEN FIND CONDITION SUCCESS - 조건별 토큰 조회 완료. 결과 수: {}", tokens.getTotalElements());
            return tokens;
            
        } catch (Exception e) {
            log.error("TOKEN FIND CONDITION ERROR - 조건별 토큰 조회 중 오류 발생. userId: {}, isActive: {}, error: {}", userId, isActive, e.getMessage(), e);
            throw e;
        }
    }
    
    /**
     * 토큰 통계 조회
     */
    @Override
    public TokenStatistics getTokenStatistics() {
        log.debug("TOKEN STATISTICS START - 토큰 통계 조회 시작");
        
        try {
            long totalTokens = tokenRepository.count();
            long activeTokens = tokenRepository.countByIsActive(true);
            long inactiveTokens = tokenRepository.countByIsActive(false);
            long expiredTokens = tokenRepository.findExpiredTokens(LocalDateTime.now()).size();
            
            TokenStatistics statistics = new TokenStatistics(totalTokens, activeTokens, inactiveTokens, expiredTokens);
            
            log.debug("TOKEN STATISTICS SUCCESS - 토큰 통계 조회 완료. 총: {}, 활성: {}, 비활성: {}, 만료: {}", 
                    totalTokens, activeTokens, inactiveTokens, expiredTokens);
            
            return statistics;
            
        } catch (Exception e) {
            log.error("TOKEN STATISTICS ERROR - 토큰 통계 조회 중 오류 발생: {}", e.getMessage(), e);
            throw e;
        }
    }
} 