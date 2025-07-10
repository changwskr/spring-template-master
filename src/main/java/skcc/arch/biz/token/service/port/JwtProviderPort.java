package skcc.arch.biz.token.service.port;

import java.util.Map;

/**
 * JWT 제공자 아웃바운드 포트
 * 도메인이 JWT 토큰 생성/검증 기능을 사용하기 위한 계약 정의
 */
public interface JwtProviderPort {
    
    /**
     * JWT 토큰 생성
     * @param claims 토큰에 포함될 클레임 정보
     * @return 생성된 JWT 토큰 문자열
     */
    String generateToken(Map<String, Object> claims);
    
    /**
     * JWT 토큰 검증 및 사용자 ID 추출
     * @param token JWT 토큰 문자열
     * @return 토큰에서 추출한 사용자 ID
     * @throws RuntimeException 토큰이 유효하지 않은 경우
     */
    String validateTokenAndExtractUID(String token);
    
    /**
     * JWT 토큰 유효성 검증만 수행
     * @param token JWT 토큰 문자열
     * @return 토큰이 유효한지 여부
     */
    boolean isTokenValid(String token);
} 