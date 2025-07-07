package skcc.arch.biz.medium.menu.infrastructure;

import com.querydsl.jpa.impl.JPAQueryFactory;
import jakarta.persistence.EntityManager;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.jpa.repository.config.EnableJpaAuditing;
import org.springframework.test.context.jdbc.Sql;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import skcc.arch.biz.menu.controller.request.MenuSearchRequest;
import skcc.arch.biz.menu.domain.Menu;
import skcc.arch.biz.menu.infrastructure.MenuRepositoryJpaCustomImpl;
import skcc.arch.biz.menu.infrastructure.jpa.MenuEntity;
import skcc.arch.biz.menu.infrastructure.jpa.MenuRepositoryJpa;

import java.util.List;
import java.util.Map;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatIterable;

@ExtendWith(SpringExtension.class)
@Sql("/sql/menu-repository-test-data.sql")
@DataJpaTest
@EnableJpaAuditing
@Slf4j
public class MenuRepositoryJpaTest {

    @Autowired
    private MenuRepositoryJpa repository;

    @Autowired
    private EntityManager entityManager;

    private MenuRepositoryJpaCustomImpl repositoryCustomImpl;

    @BeforeEach
    void setUp() {
        JPAQueryFactory jpaQueryFactory = new JPAQueryFactory(entityManager);
        repositoryCustomImpl = new MenuRepositoryJpaCustomImpl(repository, jpaQueryFactory);
    }
    
    @Test
    public void 신규_루트_메뉴순서채번() throws Exception{
        //given
        Long parentId = null;
        int expectedMenuOrder = 4;
        
        //when
        int rootLastMenuOrder = repositoryCustomImpl.getLastMenuOrder(parentId);

        //then
        assertThat(rootLastMenuOrder).isEqualTo(expectedMenuOrder);
    }

    @Test
    public void 신규_자식_메뉴순서채번() throws Exception{
        //given
        Long parentId = 1L;
        int expectedMenuOrder = 4;

        //when
        int rootLastMenuOrder = repositoryCustomImpl.getLastMenuOrder(parentId);

        //then
        assertThat(rootLastMenuOrder).isEqualTo(expectedMenuOrder);
    }

    @Test
    public void ID로_하위계층_모두_조회() throws Exception{

        //given
        Long id = 1L;

        //when
        List<MenuEntity> treeRawData = repository.findByIdTree(id);

        //then
        assertThat(treeRawData.size()).isGreaterThan(1);

    }

    @Test
    public void ID로_하위계층이_포함된_도메인반환() throws Exception{
        //given
        Long id = 1L;

        //when
        Optional<Menu> result = repositoryCustomImpl.findById(id);

        //then
        assertThat(result.isPresent()).isTrue();
        assertThat(result.get().getId()).isEqualTo(id);
        assertThat(result.get().getChildren().size()).isGreaterThan(0);
    }

    @Test
    public void 형제_순서_변경_1에서_2() throws Exception{
        //given
        Long parentId = 1L;
        Long childId1 = 2L;
        Long childId2 = 3L;
        Long childId3 = 4L;
        int changeChildId1Order = 2;

        //when
        repositoryCustomImpl.updateSiblingsMenuOrder(parentId, childId1, changeChildId1Order);

        //then
        int newChild2Order = repositoryCustomImpl.findById(childId2).orElseThrow().getMenuOrder();
        int newChild3Order = repositoryCustomImpl.findById(childId3).orElseThrow().getMenuOrder();

        assertThat(newChild2Order).isEqualTo(1);
        assertThat(newChild3Order).isEqualTo(3);

    }

    @Test
    public void 형제_순서_변경_1에서_3() throws Exception{
        //given
        Long parentId = 1L;
        Long childId1 = 2L;
        Long childId2 = 3L;
        Long childId3 = 4L;
        int changeChildId1Order = 3;

        //when
        repositoryCustomImpl.updateSiblingsMenuOrder(parentId, childId1, changeChildId1Order);

        //then
        int newChild2Order = repositoryCustomImpl.findById(childId2).orElseThrow().getMenuOrder();
        int newChild3Order = repositoryCustomImpl.findById(childId3).orElseThrow().getMenuOrder();

        assertThat(newChild2Order).isEqualTo(1);
        assertThat(newChild3Order).isEqualTo(2);

    }

    @Test
    public void 메뉴_데이터_부모_변경데이터_확인() throws Exception{
        //given
        //부모아이디 1에서 7로
        //순서는 1로
        Menu origin = repository.findById(4L).orElseThrow().toModel();
        Long changeParentId = 7L;
        int changeMenuOrder = 1;

        Menu target = Menu.builder()
                .id(origin.getId())
                .name(origin.getName())
                .menuOrder(changeMenuOrder)
                .menuUrl(origin.getMenuUrl())
                .startDate(origin.getStartDate())
                .endDate(origin.getEndDate())
                .parentId(changeParentId)
                .isDeleted(origin.isDeleted())
                .build();

        //when
        Menu updated = repositoryCustomImpl.update(target);
        Menu parentMenu = repositoryCustomImpl.findById(changeParentId).orElseThrow();


        //then
        assertThat(updated.getParentId()).isEqualTo(changeParentId);
        assertThat(updated.getMenuOrder()).isEqualTo(changeMenuOrder);
        assertThatIterable(parentMenu.getChildren().stream().map(Menu::getId).toList()).contains(updated.getId());
    }
    
    @Test
    public void 메뉴_조건_검색() throws Exception{
        //given
        Menu menuSearch = Menu.builder()
                .name("1-3")
                .build();
        int expectedCount = 3;
        
        //when
        Page<Menu> result = repositoryCustomImpl.findByCondition(PageRequest.of(0, 10), menuSearch);

        //then
        assertThat(result.getTotalElements()).isEqualTo(expectedCount);
    }
    
    @Test
    public void 루트_메뉴_캐시_적재_확인() throws Exception{
        //given
        
        //when
        Map<Long, Menu> rootMenu = repositoryCustomImpl.loadCacheData();
        List<Menu> list = rootMenu.values().stream().toList();

        //then
        assertThat(list.size()).isEqualTo(3);
        assertThat(rootMenu.get(1L).getChildren().size()).isEqualTo(3);

    }

}
