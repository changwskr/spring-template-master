package skcc.arch.biz.userrole.controller.response;

import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import skcc.arch.biz.user.domain.User;
import skcc.arch.biz.user.domain.UserStatus;

@Getter
@NoArgsConstructor
public class UserResponse {

    // 응답값으로 필요한 정보만 세팅
    private Long id;
    private String email;
    private String username;
    private UserStatus status;

    @Builder
    public UserResponse(Long id, String email, String username, UserStatus status) {
        this.id = id;
        this.email = email;
        this.username = username;
        this.status = status;
    }

    public static UserResponse fromUser(User user) {
        return UserResponse.builder()
                .id(user.getId())
                .email(user.getEmail())
                .username(user.getUsername())
                .status(user.getStatus())
                .build();
    }
}
