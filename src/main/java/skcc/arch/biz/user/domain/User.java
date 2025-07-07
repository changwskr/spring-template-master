package skcc.arch.biz.user.domain;

import lombok.Builder;
import lombok.Getter;
import org.springframework.security.crypto.password.PasswordEncoder;
import skcc.arch.biz.role.domain.Role;
import skcc.arch.biz.userrole.domain.UserRole;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Getter
public class User {

    private final Long id;
    private final String email;
    private final String password;
    private final String username;
    private final List<skcc.arch.biz.userrole.domain.UserRole> userRoles  = new ArrayList<>();
    private final UserStatus status;
    private final LocalDateTime createdDate;
    private final LocalDateTime lastModifiedDate;

    @Builder
    public User(Long id, String email, String password, String username, UserStatus status, LocalDateTime createdDate, LocalDateTime lastModifiedDate) {
        this.id = id;
        this.email = email;
        this.password = password;
        this.username = username;
        this.status = status;
        this.createdDate = createdDate;
        this.lastModifiedDate = lastModifiedDate;
    }

    /**
     * 사용자를 생성할때만 사용
     *
     * @param userCreate 사용자 생성 모델
     * @param passwordEncoder 비밀번호 생성 구현체
     * @return 사용자 모델
     */
    public static User from (UserCreate userCreate, PasswordEncoder passwordEncoder) {
        User newUser = User.builder()
                .email(userCreate.getEmail())
                .username(userCreate.getUsername())
                .password(passwordEncoder.encode(userCreate.getPassword()))
                .status(UserStatus.PENDING)
                // JPA의 경우 BaseEntity에 처리
                .createdDate(LocalDateTime.now())
                .lastModifiedDate(LocalDateTime.now())
                .build();
        newUser.addDefaultRole();
        return newUser;
    }

    /**
     * 사용자를 생성할때만 사용
     *
     * @param userCreate 사용자 생성 모델
     * @param passwordEncoder 비밀번호 생성 구현체
     * @param defaultRole 기본권한
     * @return 사용자 모델
     */
    public static User from (UserCreate userCreate, PasswordEncoder passwordEncoder, Role defaultRole) {
        User newUser = User.builder()
                .email(userCreate.getEmail())
                .username(userCreate.getUsername())
                .password(passwordEncoder.encode(userCreate.getPassword()))
                .status(UserStatus.PENDING)
                // JPA의 경우 BaseEntity에 처리
                .createdDate(LocalDateTime.now())
                .lastModifiedDate(LocalDateTime.now())
                .build();
        newUser.addUserRole(defaultRole);
        return newUser;
    }

    /**
     * 상태 값을 변경할때 사용
     * @param requestStatus
     * @return
     */
    public User updateStatus(UserStatus requestStatus) {

        if (requestStatus == null) {
            throw new IllegalArgumentException("requestStatus is null");
        }

        if (status == requestStatus) {
            throw new IllegalStateException("status is same");
        }

        User target = User.builder()
                .id(id)
                .email(email)
                .username(username)
                .password(password)
                .status(requestStatus)
                .build();

        target.getUserRoles().clear();
        target.getUserRoles().addAll(userRoles);
        return target;
    }

    /**
     * 사용자정보 업데이트
     */
    public User updateUser(User updateUser, PasswordEncoder passwordEncoder) {
        User userToUpdate = User.builder()
                .id(id)
                .email(email)
                .username(updateUser.getUsername() != null ? updateUser.getUsername() : username)
                .password(updateUser.getPassword() != null ? passwordEncoder.encode(updateUser.getPassword()) : password)
                .status(updateUser.getStatus() != null ? updateUser.getStatus() : status)
                .build();

        userToUpdate.getUserRoles().clear();
        userToUpdate.getUserRoles().addAll(userRoles);
        return userToUpdate;
    }

    public void addUserRole(Role role) {
        this.userRoles.add(UserRole.builder()
                .role(role)
                .user(this)
                .build());
    }

    public void addDefaultRole() {
        this.userRoles.add(UserRole.builder()
                .role(Role.defaultRole())
                .user(this)
                .build());
    }

    public List<Role> getRoles() {
        return this.userRoles.stream().map(UserRole::getRole).toList();
    }
}
