package skcc.arch.biz.menurole.infrastructure.jpa;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;
import skcc.arch.biz.menurole.domain.MenuRole;
import skcc.arch.biz.menurole.service.port.MenuRoleRepository;

import java.util.List;

@Repository
@RequiredArgsConstructor
public class MenuRoleRepositoryJpaCustomImpl implements MenuRoleRepository {

    private final MenuRoleRepositoryJpa repository;

    @Override
    public MenuRole save(MenuRole model) {
        return repository.save(MenuRoleEntity.from(model)).toModel();
    }

    @Override
    public List<MenuRole> findByMenuId(Long menuId) {
        return repository.findByMenuId(menuId)
                .stream().map(MenuRoleEntity::toModel).toList();
    }

    @Override
    public void deleteByMenuId(Long menuId) {
        repository.deleteByMenuId(menuId);
    }
}
