package skcc.arch.biz.role.infrastructure.jpa;

import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import skcc.arch.biz.common.infrastructure.jpa.BaseEntity;
import skcc.arch.biz.menurole.infrastructure.jpa.MenuRoleEntity;
import skcc.arch.biz.role.domain.Role;

import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "roles")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class RoleEntity extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "role_id", nullable = false, length = 255)
    private String roleId;

    @Column(name = "name", nullable = false, length = 255)
    private String name;

    @OneToMany(mappedBy = "role", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<MenuRoleEntity> menuRoles = new ArrayList<>();

    @Builder
    public RoleEntity(Long id, String roleId, String name) {
        this.id = id;
        this.roleId = roleId;
        this.name = name;
    }

    // 생성시 사용
    public static RoleEntity from(Role role) {
        return RoleEntity.builder()
                .id(role.getId())
                .roleId(role.getRoleId())
                .name(role.getName())
                .build();
    }

    // 모델로 변환
    public Role toModel() {
        return Role.builder()
                .id(id)
                .roleId(roleId)
                .name(name)
                .createdDate(super.getCreatedDate())
                .lastModifiedDate(super.getLastModifiedDate())
                .build();
    }

}
