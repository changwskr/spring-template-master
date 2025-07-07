package skcc.arch.biz.menu.controller.port;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import skcc.arch.biz.menu.domain.Menu;

import java.util.List;

public interface MenuServicePort {

    Menu save(Menu menu);
    Page<Menu> findByCondition(Pageable pageable, Menu menu);
    Menu findByMenuId(Long menuId);
    Menu update(Menu menu);
    List<Menu> getRootMenuList(boolean dbSelect);
}
