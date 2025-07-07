package skcc.arch.biz.menurole.infrastructure.jpa;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface MenuRoleRepositoryJpa extends JpaRepository<MenuRoleEntity, Long> {

    List<MenuRoleEntity> findByMenuId(Long menuId);
    void deleteByMenuId(Long menuId);
}
