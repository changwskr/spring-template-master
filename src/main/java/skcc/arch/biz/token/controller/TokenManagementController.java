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
import skcc.arch.biz.token.service.port.TokenManagementPort;

import java.util.Optional;

@Controller
@RequestMapping("/token")
@RequiredArgsConstructor
@Slf4j
public class TokenManagementController {
    
    private final TokenManagementPort tokenManagement;
    
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
            Page<Token> tokens = tokenManagement.findTokensByCondition(userId, isActive, pageable);
            
            // 통계 정보
            TokenManagementPort.TokenStatistics statistics = tokenManagement.getTokenStatistics();
            
            model.addAttribute("tokens", tokens);
            model.addAttribute("totalTokens", statistics.totalTokens());
            model.addAttribute("activeTokens", statistics.activeTokens());
            model.addAttribute("expiredTokens", statistics.expiredTokens());
            model.addAttribute("searchUserId", userId);
            model.addAttribute("searchIsActive", isActive);
            
            log.info("TOKEN MANAGEMENT - 토큰 목록 조회 완료. 총 {}개, 활성 {}개, 만료 {}개", 
                    statistics.totalTokens(), statistics.activeTokens(), statistics.expiredTokens());
            
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
            Optional<Token> token = tokenManagement.findTokenById(tokenId);
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
            tokenManagement.deactivateToken(tokenId);
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
            tokenManagement.deactivateUserToken(userId);
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
            tokenManagement.reactivateToken(tokenId);
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
            tokenManagement.deleteToken(tokenId);
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
            tokenManagement.cleanupExpiredTokens();
            log.info("TOKEN MANAGEMENT SUCCESS - 만료된 토큰 정리 완료");
            return ApiResponse.ok("만료된 토큰이 정리되었습니다.");
            
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
    public ApiResponse<TokenManagementPort.TokenStatistics> getTokenStatistics() {
        log.info("TOKEN MANAGEMENT - 토큰 통계 조회 요청");
        
        try {
            TokenManagementPort.TokenStatistics statistics = tokenManagement.getTokenStatistics();
            
            log.info("TOKEN MANAGEMENT - 토큰 통계 조회 완료. 총: {}, 활성: {}, 비활성: {}, 만료: {}", 
                    statistics.totalTokens(), statistics.activeTokens(), statistics.inactiveTokens(), statistics.expiredTokens());
            
            return ApiResponse.ok(statistics);
            
        } catch (Exception e) {
            log.error("TOKEN MANAGEMENT ERROR - 토큰 통계 조회 중 오류 발생: {}", e.getMessage(), e);
            return ApiResponse.fail(new CustomException(ErrorCode.INTERNAL_SERVER_ERROR));
        }
    }
} 