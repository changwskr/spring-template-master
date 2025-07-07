package skcc.arch.biz.menu.infrastructure.jpa;


import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import skcc.arch.biz.common.infrastructure.jpa.BaseEntity;
import skcc.arch.biz.menu.domain.Menu;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "menus") // 데이터베이스 테이블 이름
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED) // JPA Proxy용 기본 생성자
public class MenuEntity extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY) // 기본 키 자동 생성
    private Long id;

    @Column(name = "name", nullable = false, length = 255)
    private String name;

    // 부모 메뉴와의 관계 (자체 참조)
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "parent_id") // 부모 메뉴 ID를 참조
    private MenuEntity parent;

    // 자식 메뉴와의 관계 (양방향 매핑)
    @OneToMany(mappedBy = "parent", cascade = CascadeType.ALL)
    @OrderBy("menuOrder ASC") // 쿼리 실행 시 정렬 기준
    private List<MenuEntity> children = new ArrayList<>();

    @Column(name = "menu_order", nullable = false)
    private int menuOrder;

    @Column(name = "menu_url", length = 512)
    private String menuUrl;

    @Column(name = "is_deleted", nullable = false)
    private boolean isDeleted;

    @Column(name = "start_date")
    private LocalDate startDate;

    @Column(name = "end_date")
    private LocalDate endDate;

    @Builder
    public MenuEntity(Long id, String name, MenuEntity parent, List<MenuEntity> children, int menuOrder, String menuUrl, boolean isDeleted, LocalDate startDate, LocalDate endDate) {
        this.id = id;
        this.name = name;
        this.parent = parent;
        this.children = children;
        this.menuOrder = menuOrder;
        this.menuUrl = menuUrl;
        this.isDeleted = isDeleted;
        this.startDate = startDate;
        this.endDate = endDate;
    }

    // JPA Repository 에서만 사용
    public static MenuEntity from(Menu menu, MenuEntity parent) {
        if (menu == null) return null;

        return MenuEntity.builder()
                .id(menu.getId())
                .name(menu.getName())
                .parent(parent)
                .menuUrl(menu.getMenuUrl())
                .menuOrder(menu.getMenuOrder())
                .isDeleted(menu.isDeleted())
                .startDate(menu.getStartDate())
                .endDate(menu.getEndDate())
                .build();
    }

    // Model은 자식 생성하지 않음
    public Menu toModel() {
        return Menu.builder()
                .id(this.id)
                .name(this.name)
                .parentId(this.parent != null ? this.parent.getId() : null)
                .menuUrl(this.menuUrl)
                .menuOrder(this.menuOrder)
                .isDeleted(this.isDeleted)
                .startDate(this.startDate)
                .endDate(this.endDate)
                .createdDate(super.getCreatedDate())
                .lastModifiedDate(super.getLastModifiedDate())
                .build();
    }
}

