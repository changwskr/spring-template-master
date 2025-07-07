package skcc.arch.biz.userrole.service;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import skcc.arch.app.exception.CustomException;
import skcc.arch.app.exception.ErrorCode;
import skcc.arch.biz.role.domain.Role;
import skcc.arch.biz.role.service.port.RoleRepositoryPort;
import skcc.arch.biz.user.domain.User;
import skcc.arch.biz.user.service.port.UserRepositoryPort;
import skcc.arch.biz.userrole.controller.port.UserRoleServicePort;
import skcc.arch.biz.userrole.domain.UserRole;
import skcc.arch.biz.userrole.domain.UserRoleAdd;
import skcc.arch.biz.userrole.service.port.UserRoleRepositoryPort;

import java.util.ArrayList;
import java.util.List;

@Service
@RequiredArgsConstructor
public class UserRoleService implements UserRoleServicePort {

    private final UserRoleRepositoryPort repositoryPort;
    private final UserRepositoryPort userRepositoryPort;
    private final RoleRepositoryPort roleRepositoryPort;

    @Transactional
    public List<UserRole> updateUserRoleMapping(UserRoleAdd param) {

        final Long userId = param.getUserId();
        // 유효한 사용자 체크
        User user = validUser(userId);

        // 유효한 권한 체크
        List<Role> paramRoleList = validRoleList(param.getRoleList());

        // 사용자 맵핑 권한 목록 조회
        List<UserRole> roleMappings = repositoryPort.findByUserId(userId);

        List<Role> mappedRoleList = roleMappings.stream().map(UserRole::getRole).toList();

        // 삭제할 권한 판별
        mappedRoleList.stream().filter(item -> !paramRoleList.contains(item))
                .forEach(role -> repositoryPort.deleteByUserIdAndRoleId(userId, role.getId()));

        // 추가할 권한 판별
        paramRoleList.stream().filter(item -> !mappedRoleList.contains(item))
                .forEach(role -> repositoryPort.save(UserRole.builder().user(user).role(role).build()));

        List<UserRole> result = repositoryPort.findByUserId(userId);
        if( result.isEmpty() ) {
            return List.of(UserRole.builder().user(user).build());
        }
        return result;
    }

    private User validUser(Long userId) {
        return userRepositoryPort.findById(userId).orElseThrow(
                () -> new CustomException(ErrorCode.NOT_FOUND_ELEMENT));
    }


    private List<Role> validRoleList(List<Role> roleList) {

        List<Role> validRoleList = new ArrayList<>();

        if (roleList == null || roleList.isEmpty()) {
            return validRoleList;
        }
        for (Role role : roleList) {
            Role roleResult = roleRepositoryPort.findByRoleId(role.getRoleId()).orElseThrow(() -> new CustomException(ErrorCode.NOT_FOUND_ELEMENT));
            validRoleList.add(roleResult);
        }
        return validRoleList;
    }
}
