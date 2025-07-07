package skcc.arch.biz.menu.domain;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import skcc.arch.biz.role.domain.Role;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Getter
@AllArgsConstructor
@Builder
public class Menu {

    private final Long id;
    private final String name;
    private final Long parentId;
    private final int menuOrder;
    private final String menuUrl;
    private final boolean isDeleted;
    private final List<Menu> children = new ArrayList<>();
    private final LocalDate startDate;
    private final LocalDate endDate;
    private final LocalDateTime createdDate;
    private final LocalDateTime lastModifiedDate;
    private final List<Role> roleList = new ArrayList<>();


    // 신규 메뉴 생성시 사용
    public static Menu createMenu(Menu param, int lastMenuOrder) {
        return Menu.builder()
                .name(param.getName() != null ? param.getName() : "UNKNOWN")
                .menuOrder(param.getMenuOrder() > 0 ? param.getMenuOrder() : lastMenuOrder)
                .parentId(param.getParentId() != null ? param.getParentId() : null)
                .menuUrl(param.getMenuUrl() != null ? param.getMenuUrl() : "/unknown")
                .startDate(param.getStartDate() != null ? param.getStartDate() : LocalDate.now())
                .endDate(param.getEndDate() != null ? param.getEndDate() : LocalDate.of(9999,12,31))
                .build();
    }

    public static Menu updateMenu(Menu param, Menu dbData) {

        // 부모ID가 자신이 될 수 없고 자식의 ID가 될수 없다
        if(param.getParentId().equals(dbData.getId())) {
            throw new IllegalStateException("자기 자신은 부모가 될 수 없습니다.");
        }

        dbData.getChildren().forEach(child -> {
            if(child.getId().equals(param.getParentId())) {
                throw new IllegalStateException("자기 자식을 부모로 선택 할 수 없습니다. (순환참조 방지)");
            }
        });

        return Menu.builder()
                .id(param.getId())
                .name(param.getName() != null ? param.getName() : dbData.getName())
                .menuOrder(param.getMenuOrder() > 0 ? param.getMenuOrder() : dbData.getMenuOrder())
                .parentId(param.getParentId() != null ? param.getParentId() : dbData.getParentId())
                .menuUrl(param.getMenuUrl() != null ? param.getMenuUrl() : dbData.getMenuUrl())
                .startDate(param.getStartDate() != null ? param.getStartDate() : dbData.getStartDate())
                .endDate(param.getEndDate() != null ? param.getEndDate() : dbData.getEndDate())
                .build();
    }
}
