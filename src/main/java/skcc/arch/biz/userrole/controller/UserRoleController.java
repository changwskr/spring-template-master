package skcc.arch.biz.userrole.controller;

import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import skcc.arch.app.dto.ApiResponse;
import skcc.arch.biz.userrole.controller.response.RoleResponse;
import skcc.arch.biz.userrole.controller.response.UserResponse;
import skcc.arch.biz.userrole.controller.response.UserRoleResponse;
import skcc.arch.biz.userrole.domain.UserRole;
import skcc.arch.biz.userrole.domain.UserRoleAdd;
import skcc.arch.biz.userrole.service.UserRoleService;

import java.util.List;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/userrole")
public class UserRoleController {

    private final UserRoleService userRoleService;

    @PostMapping
    public ApiResponse<UserRoleResponse> updateUserRoleMapping(@RequestBody UserRoleAdd userRoleAdd) {
        List<UserRole> userRoles = userRoleService.updateUserRoleMapping(userRoleAdd);

        UserResponse userResponse = UserResponse.fromUser(userRoles.getFirst().getUser());
        List<RoleResponse> roleResponses = userRoles.stream()
                .filter(item -> item.getRole() != null)
                .map(item -> RoleResponse.fromRole(item.getRole().getRoleId(), item.getRole().getName())).toList();

        return ApiResponse.ok( UserRoleResponse.builder()
                .user(userResponse)
                .roleList(roleResponses)
                .build());
    }

}
