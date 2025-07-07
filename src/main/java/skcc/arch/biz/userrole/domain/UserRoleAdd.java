package skcc.arch.biz.userrole.domain;

import lombok.Builder;
import lombok.Getter;
import skcc.arch.biz.role.domain.Role;

import java.util.List;

@Builder
@Getter
public class UserRoleAdd {
    private Long userId;
    private List<Role> roleList;
}
