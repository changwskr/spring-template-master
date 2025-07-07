package skcc.arch.biz.menu.service;

import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import skcc.arch.app.exception.CustomException;
import skcc.arch.app.exception.ErrorCode;
import skcc.arch.biz.common.constants.CacheGroup;
import skcc.arch.biz.common.service.MyCacheService;
import skcc.arch.biz.menu.controller.port.MenuServicePort;
import skcc.arch.biz.menu.domain.Menu;
import skcc.arch.biz.menu.service.port.MenuRepositoryPort;
import skcc.arch.biz.menurole.domain.MenuRole;
import skcc.arch.biz.menurole.service.port.MenuRoleRepository;
import skcc.arch.biz.role.domain.Role;
import skcc.arch.biz.role.service.port.RoleRepositoryPort;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

@Service
@RequiredArgsConstructor
public class MenuService implements MenuServicePort {

    private final MenuRepositoryPort menuRepositoryPort;
    private final MyCacheService myCacheService;
    private final RoleRepositoryPort roleRepositoryPort;
    private final MenuRoleRepository menuRoleRepository;

    @Override
    @Transactional
    public Menu save(Menu param) {

        // 순번이 없을 경우 마지막 순번 채번하여 세팅
        int lastMenuOrder = param.getMenuOrder() < 1 ? menuRepositoryPort.getLastMenuOrder(param.getParentId()) : param.getMenuOrder();
        Menu target = Menu.createMenu(param, lastMenuOrder);

        // 저장
        Menu saved = menuRepositoryPort.save(target);

        // 형제 순서 업데이트
        menuRepositoryPort.updateSiblingsMenuOrder(param.getParentId(), saved.getId(), param.getMenuOrder());

        return saved;
    }

    @Override
    public Page<Menu> findByCondition(Pageable pageable, Menu menu) {
        return menuRepositoryPort.findByCondition(pageable, menu);
    }

    @Override
    @SuppressWarnings("unchecked")
    public Menu findByMenuId(Long menuId) {
        return menuRepositoryPort.findById(menuId).orElseThrow(
                () -> new CustomException(ErrorCode.NOT_FOUND_ELEMENT));
    }

    @Override
    @Transactional
    public Menu update(Menu param) {

        Menu dbData = menuRepositoryPort.findById(param.getId()).orElseThrow(
                () -> new CustomException(ErrorCode.NOT_FOUND_ELEMENT));

        // 권한이 존재하는지 확인
        validRoleExist(param.getRoleList());

        // 기존 메뉴권한맵핑 삭제
        menuRoleRepository.deleteByMenuId(param.getId());

        // 신규 메뉴권한맵핑 추가
        for (Role role : param.getRoleList()) {
            Role dbRole = roleRepositoryPort.findByRoleId(role.getRoleId()).orElseThrow(() -> new CustomException(ErrorCode.NOT_FOUND_ELEMENT));
            MenuRole menuRoleData = MenuRole.builder()
                    .menu(dbData)
                    .role(dbRole)
                    .build();
            menuRoleRepository.save(menuRoleData);
        }

        // 업데이트 요청 값이 없을 경우 기존 그대로 유지
        Menu target = Menu.updateMenu(param, dbData);

        // 순서를 입력받았을때
        if (param.getMenuOrder() > 0) {
            // 형제 순서 업데이트
            menuRepositoryPort.updateSiblingsMenuOrder(target.getParentId(), param.getId(), target.getMenuOrder());
        }

        // 실제 대상 업데이트
        return menuRepositoryPort.update(target);
    }

    @Override
    public List<Menu> getRootMenuList(boolean dbSelect) {

        // 1. DB 조회 요청일 경우
        if (dbSelect) {
            Map<Long, Menu> dbMenu = menuRepositoryPort.loadCacheData();
            return new ArrayList<>(dbMenu.values());
        }

        // 2. 캐시 조회
        Map<Long, Menu> cacheMenu = (Map<Long, Menu>) myCacheService.get(CacheGroup.MENU, "ROOT", Map.class);
        // 2-1. 캐시 데이터가 없을 경우
        if (cacheMenu.isEmpty()) {
            // 2-1-1. DB 조회
            Map<Long, Menu> dbMenu = menuRepositoryPort.loadCacheData();
            if (dbMenu.isEmpty()) {
                return new ArrayList<>();
            }
            // 2-1-2. 데이터가 있으면 캐시데이터 추가후 반환
            myCacheService.put(CacheGroup.MENU, "ROOT", dbMenu);
            return new ArrayList<>(dbMenu.values());
        }
        // 2-2. 캐시 데이터 반환
        return new ArrayList<>(cacheMenu.values());
    }

    private void validRoleExist(List<Role> roleList) {

        if (roleList == null || roleList.isEmpty()) {
            return;
        }
        for (Role role : roleList) {
            roleRepositoryPort.findByRoleId(role.getRoleId()).orElseThrow(() -> new CustomException(ErrorCode.NOT_FOUND_ELEMENT));
        }
    }
}
