package skcc.arch.biz.menu.controller.request;

import jakarta.validation.constraints.Min;
import lombok.Builder;
import lombok.Getter;
import skcc.arch.biz.menu.domain.Menu;

import java.time.LocalDate;

@Getter
@Builder
public class MenuCreateRequest {

    private String name;
    private Long parentId;
    @Min( value = 1)
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
