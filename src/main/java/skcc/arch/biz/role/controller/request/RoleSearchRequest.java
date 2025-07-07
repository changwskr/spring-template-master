package skcc.arch.biz.role.controller.request;

import skcc.arch.biz.role.domain.Role;

public record RoleSearchRequest(String roleId, String name) {

    public Role toModel() {
        return Role.builder()
                .roleId(roleId)
                .name(name)
                .build();
    }
}
