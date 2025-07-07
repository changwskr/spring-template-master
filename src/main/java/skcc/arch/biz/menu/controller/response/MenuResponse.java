package skcc.arch.biz.menu.controller.response;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.Builder;
import lombok.Getter;
import skcc.arch.biz.menu.domain.Menu;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

@Getter
@Builder
public class MenuResponse {

    private Long id;
    private String name;
    private Long parentId;
    @JsonInclude(JsonInclude.Include.NON_NULL)
    private List<MenuResponse> children;
    private int menuOrder;
    private String menuUrl;
    private boolean isDeleted;
    private LocalDate startDate;
    private LocalDate endDate;
    private LocalDateTime createdDate;
    private LocalDateTime lastModifiedDate;

    public static MenuResponse from(Menu menu) {
        return MenuResponse.builder()
                .id(menu.getId())
                .name(menu.getName())
                .parentId(menu.getParentId())
                .children(menu.getChildren() != null ? menu.getChildren().stream().map(MenuResponse::from).toList() : null)
                .menuOrder(menu.getMenuOrder())
                .menuUrl(menu.getMenuUrl())
                .isDeleted(menu.isDeleted())
                .startDate(menu.getStartDate())
                .endDate(menu.getEndDate())
                .createdDate(menu.getCreatedDate())
                .lastModifiedDate(menu.getLastModifiedDate())
                .build();
    }

}
