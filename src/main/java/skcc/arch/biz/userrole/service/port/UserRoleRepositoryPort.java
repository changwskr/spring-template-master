package skcc.arch.biz.userrole.service.port;


import skcc.arch.biz.userrole.domain.UserRole;

import java.util.List;

public interface UserRoleRepositoryPort {

    UserRole save(UserRole userRole);
    List<UserRole> findByUserId(Long userId);
    void deleteByUserIdAndRoleId(Long userId, Long roleId);
    List<UserRole> findByRoleId(Long roleId);
}
