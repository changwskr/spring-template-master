package skcc.arch.biz.small.menu;

import org.assertj.core.api.AssertionsForClassTypes;
import org.junit.jupiter.api.Test;
import skcc.arch.biz.menu.domain.Menu;

import java.time.LocalDate;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;


class MenuTest {
    
    @Test
    public void 신규_생성_기본값_확인_이름_URL() throws Exception{
        //given
        Menu existOrder = Menu.builder()
                .menuOrder(2)
                .build();
        Menu nonExistOrder = Menu.builder()
                .build();
        int genMenuOrder = 3;

        //when
        Menu existOrderCreatedMenu = Menu.createMenu(existOrder, genMenuOrder);
        Menu nonExistOrderCreatedMenu = Menu.createMenu(nonExistOrder, genMenuOrder);

        //then
        assertThat(existOrderCreatedMenu.getName()).isEqualTo("UNKNOWN");
        assertThat(existOrderCreatedMenu.getMenuUrl()).isEqualTo("/unknown");
        assertThat(existOrderCreatedMenu.getParentId()).isNull();
        assertThat(existOrderCreatedMenu.isDeleted()).isFalse();
        assertThat(existOrderCreatedMenu.getStartDate()).isEqualTo(LocalDate.now());
        assertThat(existOrderCreatedMenu.getEndDate()).isEqualTo(LocalDate.of(9999,12,31));
        assertThat(existOrderCreatedMenu.getMenuOrder()).isEqualTo(2);
        assertThat(nonExistOrderCreatedMenu.getMenuOrder()).isEqualTo(genMenuOrder);
    }
    
    @Test
    public void 메뉴정보_수정시_본인ID와_부모ID가_동일할_경우_에러발생() throws Exception{
        //given
        Menu menu = Menu.builder()
                .parentId(1L)
                .build();
        Menu dbMenu = Menu.builder()
                .id(1L)
                .build();
        String errMsg = "자기 자신은 부모가 될 수 없습니다.";
        
        //when & then
        IllegalStateException exception = assertThrows(IllegalStateException.class, () -> Menu.updateMenu(menu, dbMenu));
        AssertionsForClassTypes.assertThat(exception.getMessage()).isEqualTo(errMsg);
        
    }
    
    @Test
    public void 메뉴정보_수정시_부모ID가_자식의ID목록에_포함될_경우_에러발생() throws Exception{
        //given
        Menu menu = Menu.builder()
                .id(1L)
                .parentId(2L)
                .build();

        Menu dbMenu = Menu.builder()
                .id(1L)
                .build();
        dbMenu.getChildren().add(Menu.builder().id(2L).build());

        String errMsg = "자기 자식을 부모로 선택 할 수 없습니다. (순환참조 방지)";
        
        //when & then
        IllegalStateException exception = assertThrows(IllegalStateException.class, () -> Menu.updateMenu(menu, dbMenu));
        AssertionsForClassTypes.assertThat(exception.getMessage()).isEqualTo(errMsg);

    }

}