package skcc.arch.biz.mock;

import skcc.arch.biz.userrole.domain.UserRole;
import skcc.arch.biz.userrole.service.port.UserRoleRepositoryPort;

import java.util.List;

public class FakeUserRoleRepositoryPort implements UserRoleRepositoryPort {
    @Override
    public UserRole save(UserRole userRole) {
        return null;
    }

    @Override
    public List<UserRole> findByUserId(Long userId) {
        return List.of();
    }

    @Override
    public void deleteByUserIdAndRoleId(Long userId, Long roleId) {

    }

    @Override
    public List<UserRole> findByRoleId(Long roleId) {
        return List.of();
    }
}
