package skcc.arch.biz.userrole.controller.port;

import skcc.arch.biz.userrole.domain.UserRole;
import skcc.arch.biz.userrole.domain.UserRoleAdd;

import java.util.List;

public interface UserRoleServicePort {

    List<UserRole> updateUserRoleMapping(UserRoleAdd userRoleAdd);
}
