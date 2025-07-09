package skcc.arch.biz.menu.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import skcc.arch.biz.menu.domain.Menu;
import skcc.arch.biz.menu.service.port.MenuRepositoryPort;

import java.time.LocalDate;
import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j
public class MenuInitializationService {

    private final MenuRepositoryPort menuRepositoryPort;

    public void initializeMenus() {
        // 기존 메뉴 모두 삭제하고 다시 생성
        log.info("기존 메뉴 삭제 시작");
        deleteAllMenus();
        log.info("메뉴 초기화 시작");
        createInitialMenus();
        log.info("메뉴 초기화 완료");
    }

    private void deleteAllMenus() {
        List<Menu> allMenus = menuRepositoryPort.findAll();
        for (Menu menu : allMenus) {
            menuRepositoryPort.deleteById(menu.getId());
        }
    }

    private void createInitialMenus() {
        // 1단계: 메인 메뉴들
        Menu 수신 = createMenu("수신", null, 1, "/receive");
        Menu 여신 = createMenu("여신", null, 2, "/credit");
        Menu 공통 = createMenu("공통", null, 3, "/common");

        // 2단계: 서브 메뉴들
        Menu 수신업무2 = createMenu("수신업무 2", 수신.getId(), 1, "/receive/business2");
        Menu 여신업무2 = createMenu("여신업무 2", 여신.getId(), 1, "/credit/business2");
        Menu 공통업무 = createMenu("공통업무", 공통.getId(), 1, "/common/business");

        // 3단계: 세부 메뉴들
        // 수신 메뉴
        Menu 입금 = createMenu("입금", 수신업무2.getId(), 1, "/receive/business2/deposit");
        Menu 지급 = createMenu("지급", 수신업무2.getId(), 2, "/receive/business2/payment");
        
        // 여신 메뉴
        Menu 여신사후 = createMenu("여신사후", 여신업무2.getId(), 1, "/credit/business2/after");
        Menu 대출 = createMenu("대출", 여신업무2.getId(), 2, "/credit/business2/loan");
        
        // 공통 메뉴
        Menu 사용자관리 = createMenu("사용자관리", 공통업무.getId(), 1, "/user");
        Menu 로그인관리 = createMenu("로그인관리", 공통업무.getId(), 2, "/auth/login-management");
        Menu 거래내역 = createMenu("거래내역", 공통업무.getId(), 3, "/common/transaction-log");
        Menu 코드관리 = createMenu("코드관리", 공통업무.getId(), 4, "/code/list");
        Menu 파일관리 = createMenu("파일관리", 공통업무.getId(), 5, "/file/upload");
        Menu 로그관리 = createMenu("로그관리", 공통업무.getId(), 6, "/log/system");
    }

    private Menu createMenu(String name, Long parentId, int order, String url) {
        Menu menu = Menu.builder()
                .name(name)
                .parentId(parentId)
                .menuOrder(order)
                .menuUrl(url)
                .isDeleted(false)
                .startDate(LocalDate.now())
                .endDate(LocalDate.of(9999, 12, 31))
                .build();
        
        return menuRepositoryPort.save(menu);
    }
} 