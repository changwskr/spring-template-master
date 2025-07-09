package skcc.arch.biz.log.controller;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import skcc.arch.biz.log.service.LogService;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

@Controller
@RequestMapping("/log")
@RequiredArgsConstructor
@Slf4j
public class LogManagementController {

    private final LogService logService;

    @GetMapping
    public String logMain() {
        return "redirect:/log/system";
    }

    @GetMapping("/system")
    public String systemLog(
            @RequestParam(required = false) String level,
            @RequestParam(required = false) String logger,
            @RequestParam(required = false) String startDate,
            @RequestParam(required = false) String endDate,
            Model model) {
        
        try {
            // 실제 구현에서는 로그 파일을 읽거나 데이터베이스에서 로그를 조회해야 합니다
            // 현재는 시연용 더미 데이터를 생성합니다
            List<Map<String, Object>> logs = generateDummyLogs();
            
            // 필터링 로직 (실제로는 서비스 레이어에서 처리)
            if (level != null && !level.isEmpty()) {
                logs = logs.stream()
                        .filter(log -> log.get("level").toString().equalsIgnoreCase(level))
                        .toList();
            }
            
            if (logger != null && !logger.isEmpty()) {
                logs = logs.stream()
                        .filter(log -> log.get("logger").toString().toLowerCase().contains(logger.toLowerCase()))
                        .toList();
            }
            
            model.addAttribute("logs", logs);
            model.addAttribute("level", level);
            model.addAttribute("logger", logger);
            model.addAttribute("startDate", startDate);
            model.addAttribute("endDate", endDate);
            
        } catch (Exception e) {
            log.error("시스템 로그 조회 중 오류 발생: {}", e.getMessage());
            model.addAttribute("error", "시스템 로그 조회 중 오류가 발생했습니다.");
            model.addAttribute("logs", new ArrayList<>());
        }
        
        return "log/system";
    }

    @GetMapping("/transaction")
    public String transactionLog(
            @RequestParam(required = false) String userId,
            @RequestParam(required = false) String action,
            @RequestParam(required = false) String startDate,
            @RequestParam(required = false) String endDate,
            Model model) {
        
        try {
            // 실제 구현에서는 거래 내역을 데이터베이스에서 조회해야 합니다
            // 현재는 시연용 더미 데이터를 생성합니다
            List<Map<String, Object>> transactions = generateDummyTransactions();
            
            // 필터링 로직
            if (userId != null && !userId.isEmpty()) {
                transactions = transactions.stream()
                        .filter(tx -> tx.get("userId").toString().toLowerCase().contains(userId.toLowerCase()))
                        .toList();
            }
            
            if (action != null && !action.isEmpty()) {
                transactions = transactions.stream()
                        .filter(tx -> tx.get("action").toString().toLowerCase().contains(action.toLowerCase()))
                        .toList();
            }
            
            model.addAttribute("transactions", transactions);
            model.addAttribute("userId", userId);
            model.addAttribute("action", action);
            model.addAttribute("startDate", startDate);
            model.addAttribute("endDate", endDate);
            
        } catch (Exception e) {
            log.error("거래 내역 조회 중 오류 발생: {}", e.getMessage());
            model.addAttribute("error", "거래 내역 조회 중 오류가 발생했습니다.");
            model.addAttribute("transactions", new ArrayList<>());
        }
        
        return "log/transaction";
    }

    @PostMapping("/test")
    @ResponseBody
    public String testLog(@RequestParam String level, @RequestParam String message) {
        try {
            switch (level.toLowerCase()) {
                case "debug":
                    log.debug("테스트 DEBUG 로그: {}", message);
                    break;
                case "info":
                    log.info("테스트 INFO 로그: {}", message);
                    break;
                case "warn":
                    log.warn("테스트 WARN 로그: {}", message);
                    break;
                case "error":
                    log.error("테스트 ERROR 로그: {}", message);
                    break;
                default:
                    log.info("테스트 로그: {}", message);
            }
            return "로그가 성공적으로 생성되었습니다.";
        } catch (Exception e) {
            return "로그 생성 중 오류가 발생했습니다: " + e.getMessage();
        }
    }

    @GetMapping("/download")
    public String downloadLogs(
            @RequestParam(required = false) String startDate,
            @RequestParam(required = false) String endDate,
            Model model) {
        
        try {
            // 실제 구현에서는 로그 파일을 압축하여 다운로드 제공
            model.addAttribute("message", "로그 다운로드 기능은 추후 구현 예정입니다.");
            return "log/system";
        } catch (Exception e) {
            log.error("로그 다운로드 중 오류 발생: {}", e.getMessage());
            model.addAttribute("error", "로그 다운로드 중 오류가 발생했습니다.");
            return "log/system";
        }
    }

    private List<Map<String, Object>> generateDummyLogs() {
        List<Map<String, Object>> logs = new ArrayList<>();
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
        
        for (int i = 0; i < 20; i++) {
            Map<String, Object> log = Map.of(
                "timestamp", LocalDateTime.now().minusMinutes(i * 15).format(formatter),
                "level", i % 4 == 0 ? "ERROR" : i % 3 == 0 ? "WARN" : i % 2 == 0 ? "INFO" : "DEBUG",
                "logger", i % 3 == 0 ? "skcc.arch.biz.user.service.UserService" : 
                         i % 3 == 1 ? "skcc.arch.biz.auth.controller.LoginController" : 
                         "skcc.arch.biz.file.service.FileService",
                "message", i % 4 == 0 ? "데이터베이스 연결 오류 발생" : 
                          i % 3 == 0 ? "사용자 로그인 시도: admin" : 
                          i % 2 == 0 ? "파일 업로드 완료: test.pdf" : 
                          "일반 처리 로그 메시지",
                "thread", "http-nio-8080-exec-" + (i % 10 + 1)
            );
            logs.add(log);
        }
        
        return logs;
    }

    private List<Map<String, Object>> generateDummyTransactions() {
        List<Map<String, Object>> transactions = new ArrayList<>();
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
        
        for (int i = 0; i < 15; i++) {
            Map<String, Object> transaction = Map.of(
                "id", "TXN" + String.format("%06d", i + 1),
                "timestamp", LocalDateTime.now().minusHours(i * 2).format(formatter),
                "userId", i % 3 == 0 ? "admin" : i % 3 == 1 ? "user01" : "manager",
                "action", i % 4 == 0 ? "로그인" : i % 4 == 1 ? "파일업로드" : i % 4 == 2 ? "사용자등록" : "코드수정",
                "details", i % 4 == 0 ? "시스템 로그인" : 
                          i % 4 == 1 ? "문서파일 업로드 (2.5MB)" : 
                          i % 4 == 2 ? "신규 사용자 등록: test" + i : 
                          "시스템 코드 수정",
                "ipAddress", "192.168.1." + (100 + i % 50),
                "status", i % 10 == 0 ? "실패" : "성공"
            );
            transactions.add(transaction);
        }
        
        return transactions;
    }
} 