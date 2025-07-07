package skcc.arch.biz.userrole.controller.response;

import lombok.Builder;
import lombok.Getter;

import java.util.List;

@Getter
public class UserRoleResponse {

    private UserResponse user;
    private List<RoleResponse> roleList;

    @Builder
    public UserRoleResponse(UserResponse user, List<RoleResponse> roleList) {
        this.user = user;
        this.roleList = roleList;
    }
}


