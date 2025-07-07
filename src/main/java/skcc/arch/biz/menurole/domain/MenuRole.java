package skcc.arch.biz.menurole.domain;

import lombok.Builder;
import lombok.Getter;
import skcc.arch.biz.menu.domain.Menu;
import skcc.arch.biz.role.domain.Role;

import java.time.LocalDateTime;

@Getter
@Builder
public class MenuRole {
    private final Long id;
    private final Menu menu;
    private final Role role;
    private final LocalDateTime createdDate;
    private final LocalDateTime lastModifiedDate;
}
