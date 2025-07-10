package skcc.arch.biz.token.infrastructure.jwt;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import skcc.arch.app.util.JwtUtil;
import skcc.arch.biz.token.service.port.JwtProviderPort;

import java.util.Map;

/**
 * JWT 제공자 어댑터
 * JwtProviderPort를 구현하여 JWT 기반 토큰 생성/검증 제공
 */
@Component
@RequiredArgsConstructor
public class JwtProviderAdapter implements JwtProviderPort {
    
    private final JwtUtil jwtUtil;
    
    @Override
    public String generateToken(Map<String, Object> claims) {
        return jwtUtil.generateToken(claims);
    }
    
    @Override
    public String validateTokenAndExtractUID(String token) {
        return jwtUtil.validateTokenAndExtractUID(token);
    }
    
    @Override
    public boolean isTokenValid(String token) {
        try {
            jwtUtil.validateTokenAndExtractUID(token);
            return true;
        } catch (Exception e) {
            return false;
        }
    }
} 