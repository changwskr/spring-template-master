package skcc.arch.biz.token.controller;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import skcc.arch.app.dto.ApiResponse;
import skcc.arch.app.exception.CustomException;
import skcc.arch.app.exception.ErrorCode;
import skcc.arch.biz.token.domain.Token;
import skcc.arch.biz.token.infrastructure.jpa.TokenRepository;
import skcc.arch.biz.token.service.TokenService;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Controller
@RequestMapping("/token")
@RequiredArgsConstructor
@Slf4j
public class TokenManagementController {
    
    private final TokenService tokenService;
    private final TokenRepository tokenRepository;
    
    /**
     * 토큰 관리 메인 페이지
     */
    @GetMapping("/list")
    public String tokenList(Model model,
                           @PageableDefault(size = 20, sort = "createdDate", direction = Sort.Direction.DESC) Pageable pageable,
                           @RequestParam(value = "userId", required = false) String userId,
                           @RequestParam(value = "isActive", required = false) Boolean isActive) {
        
        log.info("TOKEN MANAGEMENT - 토큰 관리 페이지 접근. userId: {}, isActive: {}", userId, isActive);
        
        try {
            // 토큰 목록 조회
            Page<Token> tokens = getTokensByCondition(userId, isActive, pageable);
            
            // 통계 정보
            long totalTokens = tokenRepository.count();
            long activeTokens = tokenRepository.countByIsActive(true);
            long expiredTokens = tokenRepository.findExpiredTokens(LocalDateTime.now()).size();
            
            model.addAttribute("tokens", tokens);
            model.addAttribute("totalTokens", totalTokens);
            model.addAttribute("activeTokens", activeTokens);
            model.addAttribute("expiredTokens", expiredTokens);
            model.addAttribute("searchUserId", userId);
            model.addAttribute("searchIsActive", isActive);
            
            log.info("TOKEN MANAGEMENT - 토큰 목록 조회 완료. 총 {}개, 활성 {}개, 만료 {}개", totalTokens, activeTokens, expiredTokens);
            
            return "token/list";
            
        } catch (Exception e) {
            log.error("TOKEN MANAGEMENT ERROR - 토큰 목록 조회 중 오류 발생: {}", e.getMessage(), e);
            model.addAttribute("error", "토큰 목록을 불러오는 중 오류가 발생했습니다.");
            return "error/token-error";
        }
    }
    
    /**
     * 토큰 상세 정보 조회 (AJAX)
     */
    @GetMapping("/detail/{tokenId}")
    @ResponseBody
    public ApiResponse<Token> getTokenDetail(@PathVariable Long tokenId) {
        log.info("TOKEN MANAGEMENT - 토큰 상세 조회. tokenId: {}", tokenId);
        
        try {
            Optional<Token> token = tokenRepository.findById(tokenId);
            if (token.isPresent()) {
                log.info("TOKEN MANAGEMENT - 토큰 상세 조회 완료. tokenId: {}", tokenId);
                return ApiResponse.ok(token.get());
            } else {
                log.warn("TOKEN MANAGEMENT - 토큰을 찾을 수 없음. tokenId: {}", tokenId);
                return ApiResponse.fail(new CustomException(ErrorCode.NOT_FOUND_ELEMENT));
            }
        } catch (Exception e) {
            log.error("TOKEN MANAGEMENT ERROR - 토큰 상세 조회 중 오류 발생. tokenId: {}, error: {}", tokenId, e.getMessage(), e);
            return ApiResponse.fail(new CustomException(ErrorCode.INTERNAL_SERVER_ERROR));
        }
    }
    
    /**
     * 토큰 비활성화 (AJAX)
     */
    @PostMapping("/deactivate/{tokenId}")
    @ResponseBody
    public ApiResponse<String> deactivateToken(@PathVariable Long tokenId) {
        log.info("TOKEN MANAGEMENT - 토큰 비활성화 요청. tokenId: {}", tokenId);
        
        try {
            tokenService.deactivateToken(tokenId);
            log.info("TOKEN MANAGEMENT SUCCESS - 토큰 비활성화 완료. tokenId: {}", tokenId);
            return ApiResponse.ok("토큰이 성공적으로 비활성화되었습니다.");
            
        } catch (CustomException e) {
            log.error("TOKEN MANAGEMENT ERROR - 토큰 비활성화 실패. tokenId: {}, error: {}", tokenId, e.getMessage());
            return ApiResponse.fail(e);
        } catch (Exception e) {
            log.error("TOKEN MANAGEMENT ERROR - 토큰 비활성화 중 예상치 못한 오류 발생. tokenId: {}, error: {}", tokenId, e.getMessage(), e);
            return ApiResponse.fail(new CustomException(ErrorCode.INTERNAL_SERVER_ERROR));
        }
    }
    
    /**
     * 사용자 토큰 전체 비활성화 (AJAX)
     */
    @PostMapping("/deactivate-user/{userId}")
    @ResponseBody
    public ApiResponse<String> deactivateUserTokens(@PathVariable String userId) {
        log.info("TOKEN MANAGEMENT - 사용자 토큰 전체 비활성화 요청. userId: {}", userId);
        
        try {
            tokenService.deactivateUserToken(userId);
            log.info("TOKEN MANAGEMENT SUCCESS - 사용자 토큰 전체 비활성화 완료. userId: {}", userId);
            return ApiResponse.ok("사용자의 모든 토큰이 비활성화되었습니다.");
            
        } catch (Exception e) {
            log.error("TOKEN MANAGEMENT ERROR - 사용자 토큰 비활성화 중 오류 발생. userId: {}, error: {}", userId, e.getMessage(), e);
            return ApiResponse.fail(new CustomException(ErrorCode.INTERNAL_SERVER_ERROR));
        }
    }
    
    /**
     * 토큰 재활성화 (AJAX)
     */
    @PostMapping("/reactivate/{tokenId}")
    @ResponseBody
    public ApiResponse<String> reactivateToken(@PathVariable Long tokenId) {
        log.info("TOKEN MANAGEMENT - 토큰 재활성화 요청. tokenId: {}", tokenId);
        
        try {
            tokenService.reactivateToken(tokenId);
            log.info("TOKEN MANAGEMENT SUCCESS - 토큰 재활성화 완료. tokenId: {}", tokenId);
            return ApiResponse.ok("토큰이 성공적으로 재활성화되었습니다.");
            
        } catch (CustomException e) {
            log.error("TOKEN MANAGEMENT ERROR - 토큰 재활성화 실패. tokenId: {}, error: {}", tokenId, e.getMessage());
            return ApiResponse.fail(e);
        } catch (Exception e) {
            log.error("TOKEN MANAGEMENT ERROR - 토큰 재활성화 중 예상치 못한 오류 발생. tokenId: {}, error: {}", tokenId, e.getMessage(), e);
            return ApiResponse.fail(new CustomException(ErrorCode.INTERNAL_SERVER_ERROR));
        }
    }
    
    /**
     * 토큰 삭제 (AJAX)
     */
    @DeleteMapping("/delete/{tokenId}")
    @ResponseBody
    public ApiResponse<String> deleteToken(@PathVariable Long tokenId) {
        log.info("TOKEN MANAGEMENT - 토큰 삭제 요청. tokenId: {}", tokenId);
        
        try {
            tokenService.deleteToken(tokenId);
            log.info("TOKEN MANAGEMENT SUCCESS - 토큰 삭제 완료. tokenId: {}", tokenId);
            return ApiResponse.ok("토큰이 성공적으로 삭제되었습니다.");
            
        } catch (CustomException e) {
            log.error("TOKEN MANAGEMENT ERROR - 토큰 삭제 실패. tokenId: {}, error: {}", tokenId, e.getMessage());
            return ApiResponse.fail(e);
        } catch (Exception e) {
            log.error("TOKEN MANAGEMENT ERROR - 토큰 삭제 중 예상치 못한 오류 발생. tokenId: {}, error: {}", tokenId, e.getMessage(), e);
            return ApiResponse.fail(new CustomException(ErrorCode.INTERNAL_SERVER_ERROR));
        }
    }
    
    /**
     * 만료된 토큰 정리 (AJAX)
     */
    @PostMapping("/cleanup-expired")
    @ResponseBody
    public ApiResponse<String> cleanupExpiredTokens() {
        log.info("TOKEN MANAGEMENT - 만료된 토큰 정리 요청");
        
        try {
            LocalDateTime now = LocalDateTime.now();
            List<Token> expiredTokens = tokenRepository.findExpiredTokens(now);
            int expiredCount = expiredTokens.size();
            
            if (expiredCount > 0) {
                tokenService.cleanupExpiredTokens();
                log.info("TOKEN MANAGEMENT SUCCESS - 만료된 토큰 정리 완료. 삭제된 토큰 수: {}", expiredCount);
                return ApiResponse.ok(String.format("만료된 토큰 %d개가 정리되었습니다.", expiredCount));
            } else {
                log.info("TOKEN MANAGEMENT - 정리할 만료된 토큰이 없음");
                return ApiResponse.ok("정리할 만료된 토큰이 없습니다.");
            }
            
        } catch (Exception e) {
            log.error("TOKEN MANAGEMENT ERROR - 만료된 토큰 정리 중 오류 발생: {}", e.getMessage(), e);
            return ApiResponse.fail(new CustomException(ErrorCode.INTERNAL_SERVER_ERROR));
        }
    }
    
    /**
     * 토큰 통계 조회 (AJAX)
     */
    @GetMapping("/statistics")
    @ResponseBody
    public ApiResponse<TokenStatistics> getTokenStatistics() {
        log.info("TOKEN MANAGEMENT - 토큰 통계 조회 요청");
        
        try {
            long totalTokens = tokenRepository.count();
            long activeTokens = tokenRepository.countByIsActive(true);
            long inactiveTokens = tokenRepository.countByIsActive(false);
            long expiredTokens = tokenRepository.findExpiredTokens(LocalDateTime.now()).size();
            
            TokenStatistics statistics = TokenStatistics.builder()
                    .totalTokens(totalTokens)
                    .activeTokens(activeTokens)
                    .inactiveTokens(inactiveTokens)
                    .expiredTokens(expiredTokens)
                    .build();
            
            log.info("TOKEN MANAGEMENT - 토큰 통계 조회 완료. 총: {}, 활성: {}, 비활성: {}, 만료: {}", 
                    totalTokens, activeTokens, inactiveTokens, expiredTokens);
            
            return ApiResponse.ok(statistics);
            
        } catch (Exception e) {
            log.error("TOKEN MANAGEMENT ERROR - 토큰 통계 조회 중 오류 발생: {}", e.getMessage(), e);
            return ApiResponse.fail(new CustomException(ErrorCode.INTERNAL_SERVER_ERROR));
        }
    }
    
    /**
     * 조건에 따른 토큰 조회
     */
    private Page<Token> getTokensByCondition(String userId, Boolean isActive, Pageable pageable) {
        if (userId != null && !userId.trim().isEmpty() && isActive != null) {
            return tokenRepository.findByUserIdContainingAndIsActive(userId.trim(), isActive, pageable);
        } else if (userId != null && !userId.trim().isEmpty()) {
            return tokenRepository.findByUserIdContaining(userId.trim(), pageable);
        } else if (isActive != null) {
            return tokenRepository.findByIsActive(isActive, pageable);
        } else {
            return tokenRepository.findAll(pageable);
        }
    }
    
    /**
     * 토큰 통계 DTO
     */
    @lombok.Data
    @lombok.Builder
    @lombok.AllArgsConstructor
    @lombok.NoArgsConstructor
    public static class TokenStatistics {
        private long totalTokens;
        private long activeTokens;
        private long inactiveTokens;
        private long expiredTokens;
    }
} 