package skcc.arch.biz.user.infrastructure.mybatis;

import lombok.*;
import skcc.arch.biz.user.domain.User;
import skcc.arch.biz.user.domain.UserStatus;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

/**
 * MyBatis User Dto
 */

@Getter
@Setter
@NoArgsConstructor
@Builder
@AllArgsConstructor
public class UserDto {

    private Long id;
    private String email;
    private String password;
    private String username;
    private List<skcc.arch.biz.userrole.domain.UserRole> userRoles  = new ArrayList<>();
    private UserStatus status;
    private LocalDateTime createdDate;
    private LocalDateTime lastModifiedDate;


    public static UserDto from (User user){
        if (user == null) {
            return null;
        }
        UserDto userDto = UserDto.builder()
                .id(user.getId())
                .email(user.getEmail())
                .password(user.getPassword())
                .username(user.getUsername())
                .userRoles(new ArrayList<>())
                .status(user.getStatus())
                .createdDate(user.getCreatedDate())
                .lastModifiedDate(user.getLastModifiedDate())
                .build();

        userDto.getUserRoles().addAll(user.getUserRoles());

        return userDto;
    }

    public User toModel() {
        User model = User.builder()
                .id(id)
                .email(email)
                .password(password)
                .username(username)
                .status(status)
                .createdDate(createdDate)
                .lastModifiedDate(lastModifiedDate)
                .build();
        model.getUserRoles().addAll(userRoles);
        return model;
    }

}
