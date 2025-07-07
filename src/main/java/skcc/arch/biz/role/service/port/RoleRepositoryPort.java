package skcc.arch.biz.role.service.port;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import skcc.arch.biz.role.domain.Role;

import java.util.Optional;

public interface RoleRepositoryPort {

    Role save(Role Role);
    Optional<Role> findById(Long id);
    Page<Role> findByCondition(Pageable pageable, Role search);
    Role update(Role Role);
    Optional<Role> findByRoleId(String roleId);
}
