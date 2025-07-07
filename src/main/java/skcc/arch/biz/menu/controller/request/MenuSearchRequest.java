package skcc.arch.biz.menu.controller.request;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import skcc.arch.biz.menu.domain.Menu;

import java.time.LocalDate;

@Getter
@Setter
@NoArgsConstructor
public class MenuSearchRequest {

    private String name;
    private Long parentId;
    private int menuOrder;
    private String menuUrl;
    private boolean isDeleted;
    private LocalDate startDate;
    private LocalDate endDate;

    public Menu toModel() {
        return Menu.builder()
                .name(name)
                .parentId(parentId)
                .menuOrder(menuOrder)
                .menuUrl(menuUrl)
                .isDeleted(isDeleted)
                .startDate(startDate)
                .endDate(endDate)
                .build();
    }

}
