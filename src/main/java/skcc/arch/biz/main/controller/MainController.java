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

import java.util.List;

@Controller
@RequestMapping("/")
@RequiredArgsConstructor
public class MainController {

    private final MenuService menuService;
    private final MenuInitializationService menuInitializationService;

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