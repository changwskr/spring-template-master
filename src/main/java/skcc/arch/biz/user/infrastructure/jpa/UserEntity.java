package skcc.arch.biz.user.infrastructure.jpa;

import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import skcc.arch.biz.common.infrastructure.jpa.BaseEntity;
import skcc.arch.biz.role.infrastructure.jpa.RoleEntity;
import skcc.arch.biz.user.domain.User;
import skcc.arch.biz.user.domain.UserStatus;
import skcc.arch.biz.userrole.infrastructure.jpa.UserRoleEntity;

import java.util.ArrayList;
import java.util.List;

@Getter
@Entity
@Table(name = "users")
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class UserEntity extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE)
    private Long id;

    private String email;

    private String password;

    private String username;

    @OneToMany(mappedBy = "user", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<UserRoleEntity> userRoles = new ArrayList<>();

    @Enumerated(EnumType.STRING)
    private UserStatus status;

    public static UserEntity from(User user) {
        UserEntity userEntity = new UserEntity();
        userEntity.id = user.getId();
        userEntity.email = user.getEmail();
        userEntity.password = user.getPassword();
        userEntity.username = user.getUsername();
        userEntity.status = user.getStatus();
        user.getRoles().forEach(role -> {
            RoleEntity roleEntity = RoleEntity.from(role);
            userEntity.userRoles.add(UserRoleEntity.builder().role(roleEntity).user(userEntity).build());
        });
        return userEntity;
    }

    public User toModel() {
        return User.builder()
                .id(id)
                .email(email)
                .password(password)
                .username(username)
                .status(status)
                .createdDate(super.getCreatedDate())
                .lastModifiedDate(super.getLastModifiedDate())
                .build();
    }

}