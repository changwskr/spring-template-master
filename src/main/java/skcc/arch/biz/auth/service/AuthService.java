package skcc.arch.biz.auth.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import skcc.arch.biz.user.domain.User;
import skcc.arch.biz.user.service.port.UserRepositoryPort;

import java.util.Optional;

@Service
@RequiredArgsConstructor
@Slf4j
public class AuthService {

    private final UserRepositoryPort userRepositoryPort;
    private final PasswordEncoder passwordEncoder;

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
} 