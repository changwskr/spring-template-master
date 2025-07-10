package skcc.arch.biz.auth.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import skcc.arch.biz.user.domain.User;
import skcc.arch.biz.user.service.port.UserRepositoryPort;
import skcc.arch.biz.token.service.TokenService;

import java.util.Optional;

@Service
@RequiredArgsConstructor
@Slf4j
public class AuthService {

    private final UserRepositoryPort userRepositoryPort;
    private final PasswordEncoder passwordEncoder;
    private final TokenService tokenService;

    public boolean authenticate(String userId, String password) {
        try {
            Optional<User> userOptional = userRepositoryPort.findByUserId(userId);
            if (userOptional.isEmpty()) {
                log.warn("사용자 ID가 존재하지 않습니다: {}", userId);
                return false;
            }

            User user = userOptional.get();
            boolean isPasswordValid = passwordEncoder.matches(password, user.getPassword());
            
            if (!isPasswordValid) {
                log.warn("비밀번호가 일치하지 않습니다: {}", userId);
                return false;
            }

            log.info("사용자 인증 성공: {}", userId);
            return true;
            
        } catch (Exception e) {
            log.error("인증 중 오류 발생: {}", e.getMessage());
            return false;
        }
    }
    
    /**
     * 사용자 인증 및 토큰 생성
     */
    public String authenticateAndCreateToken(String userId, String password) {
        log.info("AUTH START - 사용자 인증 및 토큰 생성 시작. userId: {}", userId);
        
        try {
            log.debug("AUTH - 사용자 정보 조회 시작. userId: {}", userId);
            Optional<User> userOptional = userRepositoryPort.findByUserId(userId);
            if (userOptional.isEmpty()) {
                log.warn("AUTH FAILED - 사용자 ID가 존재하지 않습니다: {}", userId);
                return null;
            }

            User user = userOptional.get();
            log.debug("AUTH - 사용자 정보 조회 완료. userId: {}", userId);
            
            log.debug("AUTH - 비밀번호 검증 시작. userId: {}", userId);
            boolean isPasswordValid = passwordEncoder.matches(password, user.getPassword());
            
            if (!isPasswordValid) {
                log.warn("AUTH FAILED - 비밀번호가 일치하지 않습니다: {}", userId);
                return null;
            }
            log.debug("AUTH - 비밀번호 검증 완료. userId: {}", userId);

            // 토큰 생성
            log.debug("AUTH - 토큰 생성 시작. userId: {}", userId);
            String token = tokenService.createToken(userId);
            log.info("AUTH SUCCESS - 사용자 인증 성공 및 토큰 생성 완료. userId: {}", userId);
            
            return token;
            
        } catch (Exception e) {
            log.error("AUTH ERROR - 인증 및 토큰 생성 중 오류 발생. userId: {}, error: {}", userId, e.getMessage(), e);
            return null;
        }
    }
    
    /**
     * 사용자 로그아웃 (토큰 비활성화)
     */
    public void logout(String userId) {
        tokenService.deactivateUserToken(userId);
    }
} 