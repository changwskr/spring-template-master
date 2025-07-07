package skcc.arch.biz.userrole.controller.response;

import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
public class RoleResponse {
    private String roleId;
    private String name;

    @Builder
    public RoleResponse(String roleId, String name) {
        this.roleId = roleId;
        this.name = name;
    }

    public static RoleResponse fromRole(String roleId, String name) {
        return RoleResponse.builder()
                .roleId(roleId)
                .name(name)
                .build();
    }
}
