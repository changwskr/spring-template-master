package skcc.arch.biz.role.infrastructure.jpa;

import lombok.Builder;
import lombok.Getter;
import skcc.arch.biz.role.domain.Role;

@Getter
@Builder
public class RoleCondition {

    private Long id;
    private String roleId;
    private String name;

    public static RoleCondition from(Role role) {
        return RoleCondition.builder()
                .id(role.getId())
                .roleId(role.getRoleId())
                .name(role.getName())
                .build();
    }


}
