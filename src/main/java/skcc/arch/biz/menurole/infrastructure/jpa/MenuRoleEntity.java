package skcc.arch.biz.menurole.infrastructure.jpa;

import jakarta.persistence.*;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import skcc.arch.biz.common.infrastructure.jpa.BaseEntity;
import skcc.arch.biz.menu.infrastructure.jpa.MenuEntity;
import skcc.arch.biz.menurole.domain.MenuRole;
import skcc.arch.biz.role.infrastructure.jpa.RoleEntity;

@Entity
@Table(name = "menu_roles")
@NoArgsConstructor(access = lombok.AccessLevel.PROTECTED)
@Getter
public class MenuRoleEntity extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "menu_id", nullable = false)
    private MenuEntity menu;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "role_id", nullable = false)
    private RoleEntity role;

    @Builder
    public MenuRoleEntity(Long id, MenuEntity menu, RoleEntity role) {
        this.id = id;
        this.menu = menu;
        this.role = role;
    }

    public static MenuRoleEntity from (MenuRole model) {
        return MenuRoleEntity.builder()
                .id(model.getId())
                .role(RoleEntity.from(model.getRole()))
                .menu(MenuEntity.from(model.getMenu(), null))
                .build();
    }

    public MenuRole toModel() {
        return MenuRole.builder()
                .id(id)
                .menu(menu.toModel())
                .role(role.toModel())
                .build();
    }

}
