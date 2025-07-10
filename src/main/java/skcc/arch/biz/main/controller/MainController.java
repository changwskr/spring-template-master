package skcc.arch.biz.main.controller;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;
import skcc.arch.biz.menu.domain.Menu;
import skcc.arch.biz.menu.service.MenuInitializationService;
import skcc.arch.biz.menu.service.MenuService;
import skcc.arch.biz.token.service.TokenService;
import skcc.arch.app.exception.CustomException;
import org.springframework.web.bind.annotation.RequestBody;
import skcc.arch.biz.user.service.port.UserRepositoryPort;
import skcc.arch.biz.user.domain.User;
import org.springframework.security.crypto.password.PasswordEncoder;
import java.util.Optional;

import java.util.List;

@Controller
@RequestMapping("/")
@RequiredArgsConstructor
public class MainController {

    private final MenuService menuService;
    private final MenuInitializationService menuInitializationService;
    private final TokenService tokenService;
    private final UserRepositoryPort userRepositoryPort;
    private final PasswordEncoder passwordEncoder;

    @GetMapping("/main")
    public String main(Model model) {
        List<Menu> menus = menuService.getMenuHierarchy();
        model.addAttribute("menus", menus);
        
        // 디버깅을 위해 메뉴 URL 출력
        printMenuDebugInfo(menus);
        
        return "main";
    }

    @GetMapping("/")
    public String index() {
        return "redirect:/auth/login";
    }

    @PostMapping("/api/refresh-menus")
    @ResponseBody
    public String refreshMenus() {
        try {
            menuInitializationService.initializeMenus();
            return "메뉴가 성공적으로 새로고침되었습니다.";
        } catch (Exception e) {
            return "메뉴 새로고침 중 오류가 발생했습니다: " + e.getMessage();
        }
    }

    @GetMapping("/api/debug-menus")
    @ResponseBody
    public String debugMenus() {
        try {
            List<Menu> menus = menuService.getMenuHierarchy();
            StringBuilder sb = new StringBuilder();
            sb.append("현재 메뉴 구조:\n");
            
            for (Menu menu : menus) {
                sb.append("1단계: ").append(menu.getName()).append(" (URL: ").append(menu.getMenuUrl()).append(")\n");
                if (menu.getChildren() != null) {
                    for (Menu submenu : menu.getChildren()) {
                        sb.append("  2단계: ").append(submenu.getName()).append(" (URL: ").append(submenu.getMenuUrl()).append(")\n");
                        if (submenu.getChildren() != null) {
                            for (Menu subitem : submenu.getChildren()) {
                                sb.append("    3단계: ").append(subitem.getName()).append(" (URL: ").append(subitem.getMenuUrl()).append(")\n");
                            }
                        }
                    }
                }
            }
            return sb.toString();
        } catch (Exception e) {
            return "메뉴 디버깅 중 오류가 발생했습니다: " + e.getMessage();
        }
    }
    
    @PostMapping("/api/debug-token")
    @ResponseBody
    public String debugToken(@RequestBody TokenDebugRequest request) {
        StringBuilder sb = new StringBuilder();
        sb.append("=== 토큰 서버 검증 결과 ===\n\n");
        
        try {
            String tokenValue = request.getToken();
            if (tokenValue == null || tokenValue.trim().isEmpty()) {
                return "❌ 토큰 값이 비어있습니다.";
            }
            
            sb.append("토큰 길이: ").append(tokenValue.length()).append(" 문자\n");
            sb.append("토큰 시작: ").append(tokenValue.substring(0, Math.min(50, tokenValue.length()))).append("...\n\n");
            
            try {
                // 토큰 검증 시도
                String userId = tokenService.validateTokenAndExtractUserId(tokenValue);
                sb.append("✅ 토큰 검증 성공!\n");
                sb.append("추출된 사용자 ID: ").append(userId).append("\n");
                
                // 토큰 상세 정보 조회
                sb.append("\n--- 토큰 상세 정보 ---\n");
                boolean isValid = tokenService.validateToken(tokenValue);
                sb.append("토큰 유효성: ").append(isValid ? "✅ 유효" : "❌ 무효").append("\n");
                
            } catch (CustomException e) {
                sb.append("❌ 토큰 검증 실패 (CustomException)\n");
                sb.append("에러 코드: ").append(e.getErrorCode().getCode()).append("\n");
                sb.append("에러 메시지: ").append(e.getMessage()).append("\n");
                sb.append("HTTP 상태: ").append(e.getErrorCode().getStatus()).append("\n\n");
                
                sb.append("--- 가능한 원인 ---\n");
                switch (e.getErrorCode().getCode()) {
                    case 90006:
                        sb.append("• 토큰이 만료되었습니다. 새로 로그인하세요.\n");
                        break;
                    case 90008:
                        sb.append("• JWT 토큰 형식이 잘못되었습니다.\n");
                        sb.append("• 시크릿 키가 변경되었을 수 있습니다.\n");
                        break;
                    case 90010:
                        sb.append("• 데이터베이스에서 토큰을 찾을 수 없습니다.\n");
                        sb.append("• 토큰이 삭제되었거나 초기화되었을 수 있습니다.\n");
                        break;
                    case 90011:
                        sb.append("• 토큰이 수동으로 비활성화되었습니다.\n");
                        break;
                    default:
                        sb.append("• 알 수 없는 토큰 오류입니다.\n");
                }
                
            } catch (Exception e) {
                sb.append("❌ 예상치 못한 오류 발생\n");
                sb.append("오류 타입: ").append(e.getClass().getSimpleName()).append("\n");
                sb.append("오류 메시지: ").append(e.getMessage()).append("\n");
            }
            
            sb.append("\n--- 권장 조치 ---\n");
            sb.append("1. 로그아웃 후 다시 로그인\n");
            sb.append("2. 브라우저 localStorage 확인\n");
            sb.append("3. 토큰 관리 페이지에서 토큰 상태 확인\n");
            
            return sb.toString();
            
        } catch (Exception e) {
            return "토큰 디버깅 중 오류가 발생했습니다: " + e.getMessage();
        }
    }
    
    @GetMapping("/api/debug-users")
    @ResponseBody
    public String debugUsers() {
        StringBuilder sb = new StringBuilder();
        sb.append("=== 시스템 사용자 목록 ===\n\n");
        
        try {
            List<User> users = userRepositoryPort.findAll();
            
            if (users.isEmpty()) {
                sb.append("❌ 데이터베이스에 사용자가 없습니다!\n");
                sb.append("초기 사용자 데이터를 추가해야 합니다.\n\n");
                sb.append("--- 권장 조치 ---\n");
                sb.append("1. SQL로 기본 사용자 추가:\n");
                sb.append("   INSERT INTO USER (user_id, email, password, username) VALUES\n");
                sb.append("   ('admin', 'admin@test.com', '$2a$10$encrypted_password', '관리자');\n\n");
                sb.append("2. 또는 회원가입 API 사용\n");
            } else {
                sb.append("총 ").append(users.size()).append("명의 사용자가 등록되어 있습니다.\n\n");
                
                for (int i = 0; i < users.size(); i++) {
                    User user = users.get(i);
                    sb.append("--- 사용자 #").append(i + 1).append(" ---\n");
                    sb.append("ID: ").append(user.getId()).append("\n");
                    sb.append("사용자 ID: ").append(user.getUserId()).append("\n");
                    sb.append("이메일: ").append(user.getEmail()).append("\n");
                    sb.append("사용자명: ").append(user.getUsername()).append("\n");
                    sb.append("비밀번호 설정: ").append(user.getPassword() != null ? "✅ 있음" : "❌ 없음").append("\n");
                    sb.append("\n");
                }
                
                sb.append("--- 로그인 테스트 가능한 사용자 ID ---\n");
                for (User user : users) {
                    if (user.getUserId() != null && !user.getUserId().trim().isEmpty()) {
                        sb.append("• ").append(user.getUserId()).append("\n");
                    }
                }
            }
            
            return sb.toString();
            
        } catch (Exception e) {
            return "사용자 목록 조회 중 오류가 발생했습니다: " + e.getMessage();
        }
    }
    
    @PostMapping("/api/create-test-user")
    @ResponseBody
    public String createTestUser() {
        StringBuilder sb = new StringBuilder();
        sb.append("=== 테스트 사용자 생성 ===\n\n");
        
        try {
            // 기존에 admin 사용자가 있는지 확인
            Optional<User> existingUser = userRepositoryPort.findByUserId("admin");
            if (existingUser.isPresent()) {
                sb.append("❌ 'admin' 사용자가 이미 존재합니다.\n");
                sb.append("기존 사용자 정보:\n");
                User user = existingUser.get();
                sb.append("• 사용자 ID: ").append(user.getUserId()).append("\n");
                sb.append("• 이메일: ").append(user.getEmail()).append("\n");
                sb.append("• 사용자명: ").append(user.getUsername()).append("\n\n");
                sb.append("이 사용자로 로그인을 시도해보세요:\n");
                sb.append("• 사용자 ID: admin\n");
                sb.append("• 비밀번호: admin123");
                return sb.toString();
            }
            
            // 새 테스트 사용자 생성
            String encodedPassword = passwordEncoder.encode("admin123");
            
            User testUser = User.builder()
                    .userId("admin")
                    .email("admin@test.com")
                    .username("관리자")
                    .password(encodedPassword)
                    .build();
            
            User savedUser = userRepositoryPort.save(testUser);
            
            sb.append("✅ 테스트 사용자가 성공적으로 생성되었습니다!\n\n");
            sb.append("--- 생성된 사용자 정보 ---\n");
            sb.append("• ID: ").append(savedUser.getId()).append("\n");
            sb.append("• 사용자 ID: ").append(savedUser.getUserId()).append("\n");
            sb.append("• 이메일: ").append(savedUser.getEmail()).append("\n");
            sb.append("• 사용자명: ").append(savedUser.getUsername()).append("\n");
            sb.append("• 비밀번호: admin123 (암호화되어 저장됨)\n\n");
            
            sb.append("--- 로그인 방법 ---\n");
            sb.append("1. 로그인 페이지로 이동: /auth/login\n");
            sb.append("2. 다음 정보로 로그인:\n");
            sb.append("   • 사용자 ID: admin\n");
            sb.append("   • 비밀번호: admin123\n\n");
            
            sb.append("이제 JWT 토큰 기반 인증을 테스트할 수 있습니다!");
            
            return sb.toString();
            
        } catch (Exception e) {
            sb.append("❌ 테스트 사용자 생성 중 오류 발생:\n");
            sb.append(e.getMessage());
            return sb.toString();
        }
    }
    
    // 토큰 디버깅 요청 DTO
    public static class TokenDebugRequest {
        private String token;
        
        public String getToken() {
            return token;
        }
        
        public void setToken(String token) {
            this.token = token;
        }
    }

    private void printMenuDebugInfo(List<Menu> menus) {
        System.out.println("=== 메뉴 디버깅 정보 ===");
        for (Menu menu : menus) {
            System.out.println("1단계: " + menu.getName() + " (URL: " + menu.getMenuUrl() + ")");
            if (menu.getChildren() != null) {
                for (Menu submenu : menu.getChildren()) {
                    System.out.println("  2단계: " + submenu.getName() + " (URL: " + submenu.getMenuUrl() + ")");
                    if (submenu.getChildren() != null) {
                        for (Menu subitem : submenu.getChildren()) {
                            System.out.println("    3단계: " + subitem.getName() + " (URL: " + subitem.getMenuUrl() + ")");
                        }
                    }
                }
            }
        }
        System.out.println("========================");
    }

    // 거래 로그 화면
    @GetMapping("/common/transaction-log")
    public String transactionLog(Model model) {
        model.addAttribute("title", "거래 로그");
        model.addAttribute("menuName", "거래 로그");
        return "common/transaction-log";
    }

    // 수신 메뉴
    @GetMapping("/receive/business2/deposit")
    public String depositManagement(Model model) {
        model.addAttribute("title", "입금 업무");
        model.addAttribute("menuName", "입금");
        return "receive/deposit";
    }

    @GetMapping("/receive/business2/payment")
    public String paymentManagement(Model model) {
        model.addAttribute("title", "지급 업무");
        model.addAttribute("menuName", "지급");
        return "receive/payment";
    }

    // 여신 메뉴
    @GetMapping("/credit/business2/after")
    public String creditAfterManagement(Model model) {
        model.addAttribute("title", "여신사후 업무");
        model.addAttribute("menuName", "여신사후");
        return "credit/after";
    }

    @GetMapping("/credit/business2/loan")
    public String loanManagement(Model model) {
        model.addAttribute("title", "대출 업무");
        model.addAttribute("menuName", "대출");
        return "credit/loan";
    }
} 