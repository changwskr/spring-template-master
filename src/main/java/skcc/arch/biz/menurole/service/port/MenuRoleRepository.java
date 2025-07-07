package skcc.arch.biz.menurole.service.port;

import skcc.arch.biz.menurole.domain.MenuRole;

import java.util.List;

public interface MenuRoleRepository {
    MenuRole save(MenuRole model);
    List<MenuRole> findByMenuId(Long menuId);
    void deleteByMenuId(Long menuId);
}
