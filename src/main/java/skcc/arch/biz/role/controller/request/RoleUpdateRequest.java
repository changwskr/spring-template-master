package skcc.arch.biz.role.controller.request;

import org.thymeleaf.util.StringUtils;
import skcc.arch.biz.role.domain.Role;

public record RoleUpdateRequest (Long id, String roleId, String name) {

    public Role toModel() {

        if (id == null) {
            throw new IllegalArgumentException("id is required.");
        }

        // 업데이트항목 2가지가 모두 null 인 경우 예외
        if (StringUtils.isEmpty(roleId) && StringUtils.isEmpty(name)) {
            throw new IllegalArgumentException("roleId or name is required.");
        }

        return Role.builder()
                .id(id)
                .roleId(roleId)
                .name(name)
                .build();
    }
}
