package skcc.arch.biz.menu.infrastructure.jpa;

import lombok.Builder;
import lombok.Getter;
import skcc.arch.biz.menu.domain.Menu;

import java.time.LocalDate;

@Getter
@Builder
public class MenuCondition {

    private Long id;
    private String name;
    private MenuEntity parent;
    private int menuOrder;
    private String menuUrl;
    private boolean isDeleted;
    private LocalDate startDate;
    private LocalDate endDate;

    public static MenuCondition from(Menu menu, MenuEntity parent) {
        return MenuCondition.builder()
                .id(menu.getId())
                .name(menu.getName())
                .menuOrder(menu.getMenuOrder())
                .menuUrl(menu.getMenuUrl())
                .parent(parent)
                .isDeleted(menu.isDeleted())
                .startDate(menu.getStartDate())
                .endDate(menu.getEndDate())
                .build();
    }
}
