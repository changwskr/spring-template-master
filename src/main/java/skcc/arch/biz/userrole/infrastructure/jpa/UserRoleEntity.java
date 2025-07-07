package skcc.arch.biz.userrole.infrastructure.jpa;

import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import skcc.arch.biz.common.infrastructure.jpa.BaseEntity;
import skcc.arch.biz.role.infrastructure.jpa.RoleEntity;
import skcc.arch.biz.user.infrastructure.jpa.UserEntity;
import skcc.arch.biz.userrole.domain.UserRole;

@Entity
@Table(name = "user_roles")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class UserRoleEntity extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private UserEntity user;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "role_id", nullable = false)
    private RoleEntity role;

    @Builder
    public UserRoleEntity(Long id, UserEntity user, RoleEntity role) {
        this.id = id;
        this.user = user;
        this.role = role;
    }

    public static UserRoleEntity from(UserRole model) {
        return UserRoleEntity.builder()
                .user(UserEntity.from(model.getUser()))
                .role(RoleEntity.from(model.getRole()))
                .build();
    }

    public UserRole toModel() {
        return UserRole.builder()
                .id(id)
                .user(user.toModel())
                .role(role.toModel())
                .createdDate(super.getCreatedDate())
                .lastModifiedDate(super.getLastModifiedDate())
                .build();
    }

}
