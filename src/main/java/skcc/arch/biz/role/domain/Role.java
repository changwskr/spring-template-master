package skcc.arch.biz.role.domain;

import lombok.Builder;
import lombok.Getter;
import skcc.arch.biz.menurole.domain.MenuRole;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Objects;

@Getter
@Builder
public class Role {
    private final Long id;
    private final String roleId;
    private final String name;
    private final LocalDateTime createdDate;
    private final LocalDateTime lastModifiedDate;
    private final List<MenuRole> menuRoles;

    // 생성규칙은 굳이 만들지 않음 필요하면 프로젝트 특성에 따라 ID 규칙 채번 등을 넣으면 됨
    // 해당 샘플 프로젝트는 ROLE_ID 및 ROLE_NAME 은 필수값으로 가정하였음

    public static Role update(Role param, Role dbData) {

        return Role.builder()
                .id(param.getId())
                .roleId(param.getRoleId() != null ? param.getRoleId() : dbData.getRoleId())
                .name(param.getName() != null ? param.getName() : dbData.getName())
                .build();
    }

    public static Role defaultRole() {
        return Role.builder().roleId("DEFAULT").name("기본권한").build();
    }

    @Override
    public boolean equals(Object o) {
        if (o == null || getClass() != o.getClass()) return false;
        Role role = (Role) o;
        return Objects.equals(id, role.id) && Objects.equals(roleId, role.roleId) && Objects.equals(name, role.name);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id, roleId, name);
    }
}
