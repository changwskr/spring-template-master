package skcc.arch.app.filter;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.util.AntPathMatcher;
import org.springframework.web.filter.OncePerRequestFilter;
import skcc.arch.app.dto.ApiResponse;
import skcc.arch.app.exception.CustomException;
import skcc.arch.app.exception.ErrorCode;
import skcc.arch.app.util.HttpResponseUtil;
import skcc.arch.app.util.JwtUtil;
import skcc.arch.biz.user.service.CustomUserDetailService;
import skcc.arch.biz.token.service.TokenService;

import java.io.IOException;
import java.util.List;

/**
 * JwtRequestFilter는 {@link OncePerRequestFilter}를 확장한 커스텀 필터로,
 * 애플리케이션에서 들어오는 요청을 처리하여 JWT 토큰을 검증하고 
 * Security Context를 설정하는 역할을 합니다.
 *
 * 이 필터는 다음 단계를 수행합니다:
 * 1. HTTP 요청의 Authorization 헤더에서 JWT 토큰을 추출합니다.
 *    토큰은 "Bearer "로 시작해야 합니다.
 * 2. {@link JwtUtil#validateTokenAndExtractUID(String)}을 사용하여 토큰 및 UID(사용자 식별자)를 추출합니다.
 * 3. 추출된 UID를 기반으로 {@link CustomUserDetailService}에서 사용자 정보를 로드합니다.
 * 4. 사용자 정보가 유효하다면, {@link UsernamePasswordAuthenticationToken}을 생성하고
 *    이를 Security Context에 설정하여 인증과 권한 관리를 처리합니다.
 *
 * 이 필터는 이후 요청이 인증된 사용자 정보와 Security Context를 통해 권한을 활용할 수 있도록 보장합니다.
 *
 * 필터는 요청을 처리한 후 체인의 다음 필터를 호출합니다.
 */

@RequiredArgsConstructor
@Slf4j
public class JwtRequestFilter extends OncePerRequestFilter {

    private final CustomUserDetailService customUserDetailService;
    private final JwtUtil jwtUtil;
    private final TokenService tokenService;
    private final List<String> authWhitelist;
    private final AntPathMatcher antPathMatcher;

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain chain)
            throws ServletException, IOException {

        String requestPath = request.getRequestURI();
        log.debug("JWT FILTER START - JWT 토큰 필터 시작. path: {}", requestPath);

        // 요청 경로가 화이트리스트에 포함되어 있을경우 JWT Token 검증 Skip
        if (isWhitelisted(requestPath)) {
            log.debug("JWT FILTER - 화이트리스트 경로, 토큰 검증 스킵. path: {}", requestPath);
            chain.doFilter(request, response);
            return;
        }

        final String authorizationHeader = request.getHeader("Authorization");
        String uid = null;
        String token = null;

        // 1. HTTP 요청의 Authorization 헤더에서 JWT 토큰 추출
        if (authorizationHeader != null && authorizationHeader.startsWith("Bearer ")) {
            token = authorizationHeader.substring(7);
            log.debug("JWT FILTER - Authorization 헤더에서 토큰 추출 완료. path: {}", requestPath);
        } else {
            // 2. Authorization 헤더가 없으면 파라미터에서 토큰 추출 시도 (GET/POST 모두 지원)
            String paramToken = request.getParameter("authToken");
            if (paramToken != null && !paramToken.trim().isEmpty()) {
                token = paramToken.trim();
                log.debug("JWT FILTER - URL/폼 파라미터에서 토큰 추출 완료. method: {}, path: {}", 
                        request.getMethod(), requestPath);
            }
        }

        if (token != null) {
            log.debug("JWT FILTER - 토큰 발견 (길이: {}). method: {}, path: {}", 
                    token.length(), request.getMethod(), requestPath);

            try {
                // 3. 토큰 검증 및 사용자 ID 추출 (성능 최적화)
                uid = tokenService.validateTokenAndExtractUserId(token);
                log.debug("JWT FILTER - 토큰 검증 및 사용자 ID 추출 완료. userId: {}, method: {}, path: {}", 
                        uid, request.getMethod(), requestPath);
                
                if (uid != null && SecurityContextHolder.getContext().getAuthentication() == null) {
                    try {
                        // 4. UID로 사용자 정보 조회 (userId 기반 조회)
                        log.debug("JWT FILTER - 사용자 정보 조회 시작. 추출된 userId: '{}', method: {}, path: {}", 
                                uid, request.getMethod(), requestPath);
                        UserDetails userDetails = customUserDetailService.loadUserByUserId(uid);
                        if (userDetails != null) {
                            // 5. Security 인증 토큰 생성
                            UsernamePasswordAuthenticationToken authToken =
                                    new UsernamePasswordAuthenticationToken(userDetails, null, userDetails.getAuthorities());
                            // 6. Security Context에 인증 정보 설정
                            SecurityContextHolder.getContext().setAuthentication(authToken);
                            log.debug("JWT FILTER SUCCESS - Security Context 인증 설정 완료. userId: {}, method: {}, path: {}", 
                                    uid, request.getMethod(), requestPath);
                        } else {
                            log.warn("JWT FILTER - UserDetails가 null입니다. userId: {}, method: {}, path: {}", 
                                    uid, request.getMethod(), requestPath);
                        }
                    } catch (CustomException e) {
                        log.error("JWT FILTER ERROR - 사용자 조회 실패. userId: '{}', method: {}, path: {}, errorCode: {}, message: {}", 
                                uid, request.getMethod(), requestPath, e.getErrorCode().getCode(), e.getMessage());
                        throw e; // 상위로 예외 전파
                    }
                }
            } catch (CustomException e) {
                log.error("JWT FILTER ERROR - 커스텀 예외 발생. method: {}, path: {}, error: {}", 
                        request.getMethod(), requestPath, e.getMessage());
                ApiResponse<Void> failResponse = ApiResponse.fail(e);
                HttpResponseUtil.writeResponseBody(response,failResponse);
                return;
            } catch (Exception e) {
                log.error("JWT FILTER ERROR - 예상치 못한 예외 발생. method: {}, path: {}, error: {}", 
                        request.getMethod(), requestPath, e.getMessage(), e);
                ApiResponse<Void> failResponse = ApiResponse.fail(new CustomException(ErrorCode.JWT_INVALID));
                HttpResponseUtil.writeResponseBody(response,failResponse);
                return;
            }
        } else {
            log.warn("JWT FILTER FAILED - Authorization 헤더와 파라미터 모두에서 토큰을 찾을 수 없음. method: {}, path: {}", 
                    request.getMethod(), requestPath);
            ApiResponse<Void> failResponse = ApiResponse.fail(new CustomException(ErrorCode.JWT_NOT_FOUND));
            HttpResponseUtil.writeResponseBody(response,failResponse);
            return;
        }

        log.debug("JWT FILTER END - JWT 토큰 필터 완료. userId: {}, path: {}", uid, requestPath);
        chain.doFilter(request, response);
    }

    // 화이트리스트 경로 매칭 로직
    private boolean isWhitelisted(String path) {
        return authWhitelist.stream()
                .anyMatch(whitelistPath -> antPathMatcher.match(whitelistPath, path));
    }
}