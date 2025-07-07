package skcc.arch.biz.mock;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import skcc.arch.biz.role.domain.Role;
import skcc.arch.biz.role.service.port.RoleRepositoryPort;

import java.util.Optional;

public class FakeRoleRepositoryPort implements RoleRepositoryPort {
    @Override
    public Role save(Role Role) {
        return null;
    }

    @Override
    public Optional<Role> findById(Long id) {
        return Optional.empty();
    }

    @Override
    public Page<Role> findByCondition(Pageable pageable, Role search) {
        return null;
    }

    @Override
    public Role update(Role Role) {
        return null;
    }

    @Override
    public Optional<Role> findByRoleId(String roleId) {
        return Optional.empty();
    }
}
