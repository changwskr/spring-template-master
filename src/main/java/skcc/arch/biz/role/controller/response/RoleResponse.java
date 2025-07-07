package skcc.arch.biz.role.controller.response;

import lombok.Builder;
import skcc.arch.biz.role.domain.Role;

@Builder
public record RoleResponse (Long id, String roleId, String name) {

    public static RoleResponse from(Role model) {
        return RoleResponse.builder()
                .id(model.getId())
                .roleId(model.getRoleId())
                .name(model.getName())
                .build();
    }
}
