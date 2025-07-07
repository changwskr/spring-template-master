package skcc.arch.biz.userrole.infrastructure;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;
import skcc.arch.biz.userrole.domain.UserRole;
import skcc.arch.biz.userrole.infrastructure.jpa.UserRoleEntity;
import skcc.arch.biz.userrole.infrastructure.jpa.UserRoleRepositoryJpa;
import skcc.arch.biz.userrole.service.port.UserRoleRepositoryPort;

import java.util.List;

@Repository
@RequiredArgsConstructor
public class UserRoleRepositoryJpaCustomImpl implements UserRoleRepositoryPort {

    private final UserRoleRepositoryJpa repository;

    @Override
    public UserRole save(UserRole userRole) {
        return repository.save(UserRoleEntity.from(userRole)).toModel();
    }

    @Override
    public List<UserRole> findByUserId(Long userId) {
        return repository.findByUserId(userId).stream().map(UserRoleEntity::toModel).toList();
    }

    @Override
    public void deleteByUserIdAndRoleId(Long userId, Long roleId) {
        repository.deleteByUserIdAndRoleId(userId, roleId);
    }

    @Override
    public List<UserRole> findByRoleId(Long roleId) {
        return repository.findByRoleId(roleId).stream().map(UserRoleEntity::toModel).toList();
    }
}
