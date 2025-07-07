package skcc.arch.biz.userrole.domain;

import lombok.Builder;
import lombok.Getter;
import skcc.arch.biz.role.domain.Role;
import skcc.arch.biz.user.domain.User;

import java.time.LocalDateTime;

@Builder
@Getter
public class UserRole {

    private final Long id;
    private final User user;
    private final Role role;

    private final LocalDateTime createdDate;
    private final LocalDateTime lastModifiedDate;
}
