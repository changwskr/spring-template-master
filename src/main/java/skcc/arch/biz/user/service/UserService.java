package skcc.arch.biz.user.service;


import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import skcc.arch.app.exception.CustomException;
import skcc.arch.app.exception.ErrorCode;
import skcc.arch.app.util.JwtUtil;
import skcc.arch.biz.role.domain.Role;
import skcc.arch.biz.role.service.port.RoleRepositoryPort;
import skcc.arch.biz.user.controller.port.UserServicePort;
import skcc.arch.biz.user.domain.User;
import skcc.arch.biz.user.domain.UserCreate;
import skcc.arch.biz.user.service.port.UserRepositoryPort;
import skcc.arch.biz.userrole.domain.UserRole;
import skcc.arch.biz.userrole.service.port.UserRoleRepositoryPort;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

@Service
@RequiredArgsConstructor
@Slf4j
public class UserService implements UserServicePort {

    private final UserRepositoryPort userRepositoryPort;
    private final PasswordEncoder passwordEncoder;
    private final JwtUtil jwtUtil;
    private final RoleRepositoryPort roleRepositoryPort;
    private final UserRoleRepositoryPort userRoleRepositoryPort;

    // 회원가입 메서드
    @Override
    @Transactional
    public User signUp(UserCreate userCreate) {

        Role defaultRole = roleRepositoryPort.findByRoleId(Role.defaultRole().getRoleId()).orElseGet(() -> roleRepositoryPort.save(Role.defaultRole()));
        // 입력받은 이메일로 회원 존재 점검
        checkUserExistByEmail(userCreate.getEmail());
        User model = User.from(userCreate, passwordEncoder, defaultRole);
        // 기본권한 확인
        User savedModel = userRepositoryPort.save(model);

        return savedModel;
    }

    // 인증
    @Override
    public String authenticate(String email, String rawPassword) {

        User user = userRepositoryPort.findByEmail(email)
                .orElseThrow(() -> new CustomException(ErrorCode.NOT_FOUND_ELEMENT));
        boolean matches = passwordEncoder.matches(rawPassword, user.getPassword());
        if (!matches) {
            throw new CustomException(ErrorCode.NOT_MATCHED_PASSWORD);
        }

        // JWT Token 생성
        Map<String, Object> claims = new HashMap<>();
        claims.put("uid", user.getEmail());
        claims.put("username", user.getUsername());
        claims.put("email", user.getEmail());
        claims.put("role", user.getUserRoles().stream().map(item -> item.getRole().getRoleId()).toArray(String[]::new));

        String token = jwtUtil.generateToken(claims);
        log.info("generated token : {}", token);

        return token;
    }

    // 전체 사용자 조회
    @Override
    public List<User> findAllUsers() {
        return userRepositoryPort.findAll();
    }

    @Override
    public Page<User> findAll(Pageable pageable) {
        return userRepositoryPort.findAll(pageable);
    }

    @Override
    public User getById(Long id) {
        return userRepositoryPort.findById(id)
                .orElseThrow(() -> new CustomException(ErrorCode.NOT_FOUND_ELEMENT));
    }

    @Override
    public Page<User> findAdminUsers(Pageable pageable) {
        log.info("[Service] findAdminUsers : {}", pageable);
        Role adminRole = roleRepositoryPort.findByRoleId("ADMIN").orElseThrow(() -> new CustomException(ErrorCode.NOT_FOUND_ELEMENT));
        List<UserRole> userRolesForAdmin = userRoleRepositoryPort.findByRoleId(adminRole.getId());
        List<Long> adminUserIds = userRolesForAdmin.stream().map(item -> item.getUser().getId()).toList();
        return userRepositoryPort.findAdminUsers(pageable, adminUserIds);
    }

    @Override
    public User updateUserStatus(User user) {

        // 조회
        User findUser = userRepositoryPort.findByEmail(user.getEmail())
                .orElseThrow(() -> new CustomException(ErrorCode.NOT_FOUND_ELEMENT));

        // 상태값 없데이트
        User updateUser = findUser.updateStatus(user.getStatus());
        return userRepositoryPort.updateStatus(updateUser);
    }

    @Override
    public User updateUser(User user) {
        // 조회
        User findUser = userRepositoryPort.findByEmail(user.getEmail())
                .orElseThrow(() -> new CustomException(ErrorCode.NOT_FOUND_ELEMENT));

        User updatedUser = findUser.updateUser(user, passwordEncoder);
        return userRepositoryPort.save(updatedUser);
    }

    // 이메일로 존재여부 체크
    private void checkUserExistByEmail(String email) {
        Optional<User> optionalUser = userRepositoryPort.findByEmail(email);
        if (optionalUser.isPresent()) {
            throw new CustomException(ErrorCode.EXIST_ELEMENT);
        }
    }

}