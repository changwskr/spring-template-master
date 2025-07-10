package skcc.arch.biz.auth.controller;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import skcc.arch.biz.auth.service.AuthService;
import skcc.arch.biz.menu.service.MenuInitializationService;

import jakarta.servlet.http.HttpSession;

@Controller
@RequestMapping("/auth")
@RequiredArgsConstructor
@Slf4j
public class LoginController {

    private final AuthService authService;
    private final MenuInitializationService menuInitializationService;

    @GetMapping("/login")
    public String loginPage(Model model) {
        return "login";
    }

    @GetMapping("/login-management")
    public String loginManagement(Model model) {
        model.addAttribute("title", "로그인 관리");
        model.addAttribute("menuName", "로그인 관리");
        return "common/login-management";
    }

    @PostMapping("/login")
    public String login(@RequestParam String userId, 
                       @RequestParam String password, 
                       Model model,
                       HttpSession session) {
        log.info("LOGIN START - 사용자 로그인 시도. userId: {}", userId);
        
        try {
            String token = authService.authenticateAndCreateToken(userId, password);
            if (token != null) {
                // 세션에 로그인 정보 저장
                session.setAttribute("userId", userId);
                session.setAttribute("isAuthenticated", true);
                log.info("LOGIN SUCCESS - 세션에 로그인 정보 저장 완료. userId: {}", userId);
                
                // 메뉴 초기화
                menuInitializationService.initializeMenus();
                log.debug("LOGIN - 메뉴 초기화 완료. userId: {}", userId);
                
                // 토큰을 모델에 추가
                model.addAttribute("token", token);
                model.addAttribute("userId", userId);
                
                log.info("LOGIN END - 로그인 성공. userId: {}, redirectTo: login-success", userId);
                return "login-success";
            } else {
                log.warn("LOGIN FAILED - 인증 실패. userId: {}", userId);
                model.addAttribute("error", "사용자 ID 또는 비밀번호가 잘못되었습니다.");
                return "login";
            }
        } catch (Exception e) {
            log.error("LOGIN ERROR - 로그인 중 예외 발생. userId: {}, error: {}", userId, e.getMessage(), e);
            model.addAttribute("error", "로그인 중 오류가 발생했습니다.");
            return "login";
        }
    }

    @GetMapping("/logout")
    public String logout(HttpSession session) {
        // 세션에서 사용자 ID 가져오기
        String userId = (String) session.getAttribute("userId");
        if (userId != null) {
            // 토큰 비활성화
            authService.logout(userId);
        }
        
        // 세션 무효화
        session.invalidate();
        return "redirect:/auth/login";
    }
} 