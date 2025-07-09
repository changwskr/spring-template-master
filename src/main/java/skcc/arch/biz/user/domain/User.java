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
    private final String userId;        // 요구사항: USERID
    private final String address;       // 요구사항: 주소
    private final String job;           // 요구사항: 직업
    private final Integer age;          // 요구사항: 나이
    private final String company;       // 요구사항: 회사
    private final List<skcc.arch.biz.userrole.domain.UserRole> userRoles  = new ArrayList<>();
    private final UserStatus status;
    private final LocalDateTime createdDate;
    private final LocalDateTime lastModifiedDate;

    @Builder
    public User(Long id, String email, String password, String username, String userId, String address, String job, Integer age, String company, UserStatus status, LocalDateTime createdDate, LocalDateTime lastModifiedDate) {
        this.id = id;
        this.email = email;
        this.password = password;
        this.username = username;
        this.userId = userId;
        this.address = address;
        this.job = job;
        this.age = age;
        this.company = company;
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
                .userId(userCreate.getUserId())
                .address(userCreate.getAddress())
                .job(userCreate.getJob())
                .age(userCreate.getAge())
                .company(userCreate.getCompany())
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
                .userId(userCreate.getUserId())
                .address(userCreate.getAddress())
                .job(userCreate.getJob())
                .age(userCreate.getAge())
                .company(userCreate.getCompany())
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
                .userId(userId)
                .address(address)
                .job(job)
                .age(age)
                .company(company)
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
                .userId(updateUser.getUserId() != null ? updateUser.getUserId() : userId)
                .address(updateUser.getAddress() != null ? updateUser.getAddress() : address)
                .job(updateUser.getJob() != null ? updateUser.getJob() : job)
                .age(updateUser.getAge() != null ? updateUser.getAge() : age)
                .company(updateUser.getCompany() != null ? updateUser.getCompany() : company)
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
