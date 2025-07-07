package skcc.arch.biz.role.controller.request;

import jakarta.validation.constraints.NotNull;
import skcc.arch.biz.role.domain.Role;

public record RoleCreateRequest (
        @NotNull String roleId,
        @NotNull String name) {

    public Role toModel() {
        return Role.builder()
                .roleId(roleId)
                .name(name)
                .build();
    }
}
