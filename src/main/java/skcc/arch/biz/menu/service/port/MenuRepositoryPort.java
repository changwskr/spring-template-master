package skcc.arch.biz.menu.service.port;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import skcc.arch.biz.menu.domain.Menu;

import java.util.Map;
import java.util.Optional;

public interface MenuRepositoryPort {

    Menu save(Menu menu);
    Optional<Menu> findById(Long id);
    Page<Menu> findByCondition(Pageable pageable, Menu search);
    Menu update(Menu Menu);
    Map<Long, Menu> loadCacheData();
    void updateSiblingsMenuOrder(Long parentId, Long menuId, int menuOrder);
    int getLastMenuOrder(Long parentId);
}
