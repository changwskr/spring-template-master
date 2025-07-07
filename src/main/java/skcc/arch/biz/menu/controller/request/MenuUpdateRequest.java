package skcc.arch.biz.menu.controller.request;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import skcc.arch.biz.menu.domain.Menu;
import skcc.arch.biz.role.domain.Role;

import java.time.LocalDate;
import java.util.List;

@Getter
@Setter
@NoArgsConstructor
public class MenuUpdateRequest {
    private Long id;
    private String name;
    private Long parentId;
    private int menuOrder;
    private String menuUrl;
    private boolean isDeleted;
    private LocalDate startDate;
    private LocalDate endDate;
    private List<String> roleIds;

    public Menu toModel() {
        Menu build = Menu.builder()
                .id(id)
                .name(name)
                .parentId(parentId)
                .menuOrder(menuOrder)
                .menuUrl(menuUrl)
                .isDeleted(isDeleted)
                .startDate(startDate)
                .endDate(endDate)
                .build();

        if (roleIds != null && !roleIds.isEmpty()) {
            for (String roleId : roleIds) {
                build.getRoleList().add(Role.builder().roleId(roleId).build());
            }
        }

        return build;
    }

}
