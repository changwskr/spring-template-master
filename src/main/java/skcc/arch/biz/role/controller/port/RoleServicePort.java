package skcc.arch.biz.role.controller.port;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import skcc.arch.biz.role.domain.Role;

public interface RoleServicePort {

    Role save(Role role);
    Role findById(Long id);
    Page<Role> findByCondition(Pageable pageable, Role search);
    Role update(Role role);
}
