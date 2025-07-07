package skcc.arch.biz.menu.infrastructure;

import com.querydsl.core.types.dsl.Expressions;
import com.querydsl.core.types.dsl.NumberExpression;
import com.querydsl.jpa.impl.JPAQueryFactory;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Repository;
import skcc.arch.biz.menu.domain.Menu;
import skcc.arch.biz.menu.infrastructure.jpa.MenuCondition;
import skcc.arch.biz.menu.infrastructure.jpa.MenuConditionBuilder;
import skcc.arch.biz.menu.infrastructure.jpa.MenuEntity;
import skcc.arch.biz.menu.infrastructure.jpa.MenuRepositoryJpa;
import skcc.arch.biz.menu.service.port.MenuRepositoryPort;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

import static skcc.arch.biz.menu.infrastructure.jpa.QMenuEntity.menuEntity;

@Slf4j
@Repository
@RequiredArgsConstructor
public class MenuRepositoryJpaCustomImpl implements MenuRepositoryPort {

    private final MenuRepositoryJpa repository;
    private final JPAQueryFactory queryFactory;

    @Override
    public Menu save(Menu menu) {
        MenuEntity parentEntity = menu.getParentId() != null ? repository.findById(menu.getParentId()).orElse(null)  : null;
        return repository.save(MenuEntity.from(menu, parentEntity)).toModel();
    }

    @Override
    public Optional<Menu> findById(Long id) {

        if (id == null) {
            return Optional.empty();
        }

        List<MenuEntity> treeRawData = repository.findByIdTree(id);
        Menu result = generateMenuTreeById(treeRawData, id);

        return Optional.ofNullable(result);
    }

    @Override
    public Page<Menu> findByCondition(Pageable pageable, Menu search) {
        MenuEntity parentEntity = search.getParentId() != null ? repository.findById(search.getParentId()).orElse(null)  : null;
        MenuCondition condition = MenuCondition.from(search, parentEntity);

        List<Menu> content = queryFactory.selectFrom(menuEntity)
                .where(MenuConditionBuilder.menuCondition(condition))
                .orderBy(menuEntity.menuOrder.asc(), menuEntity.name.asc())
                .offset(pageable.getOffset())
                .limit(pageable.getPageSize())
                .fetch()
                .stream()
                .map(MenuEntity::toModel)
                .toList();

        Long totalCount = queryFactory.select(menuEntity.count())
                .from(menuEntity)
                .where(MenuConditionBuilder.menuCondition(condition))
                .fetchOne();
       return new PageImpl<>(content, pageable, totalCount);
    }

    @Override
    public Menu update(Menu data) {

        MenuEntity parentEntity = getParentEntity(data.getParentId());

        return repository.save(MenuEntity.from(data, parentEntity)).toModel();
    }


    @Override
    public Map<Long, Menu> loadCacheData() {
        Map<Long, Menu> result  = new HashMap<>();

        //최상위 Menu Id
        List<Long> rootMenuIds = queryFactory.select(menuEntity.id)
                .from(menuEntity)
                .where(menuEntity.parent.isNull())
                .orderBy(menuEntity.menuOrder.asc(), menuEntity.name.asc())
                .fetch();

        rootMenuIds.forEach(id -> {
            result.put(id, generateMenuTreeById(repository.findByIdTree(id), id));
        });

        return result;
    }

    @Override
    public void updateSiblingsMenuOrder(Long parentId, Long menuId, int menuOrder) {
        MenuEntity parentEntity = getParentEntity(parentId);

        // 입력메뉴 순서보다 작은 형제들
        queryFactory.update(menuEntity)
                .where(menuEntity.menuOrder.loe(menuOrder)
                        .and(menuEntity.id.ne(menuId))
                        .and(parentEntity != null ? menuEntity.parent.eq(parentEntity) : menuEntity.parent.isNull())
                )
                .set(menuEntity.menuOrder, calcMenuOrder(0))
                .execute();

        // 입력메뉴 순서보다 같거나 큰 형제들
        queryFactory.update(menuEntity)
                .where(menuEntity.menuOrder.goe(menuOrder)
                        .and(menuEntity.id.ne(menuId))
                        .and(parentEntity != null ? menuEntity.parent.eq(parentEntity) : menuEntity.parent.isNull())
                )
                .set(menuEntity.menuOrder, calcMenuOrder(menuOrder))
                .execute();

    }

    @Override
    public int getLastMenuOrder(Long parentId) {
        MenuEntity parentEntity = getParentEntity(parentId);
        Integer maxMenuOrder = queryFactory.select(menuEntity.menuOrder.max())
                .from(menuEntity)
                .where(parentEntity != null ? menuEntity.parent.eq(parentEntity) : menuEntity.parent.isNull())
                .fetchOne();

        return maxMenuOrder == null ? 1 : maxMenuOrder + 1;
    }

    private MenuEntity getParentEntity(Long parentId) {
        MenuEntity parentEntity;
        if(parentId == null) {
            parentEntity =  null;
        } else {
            parentEntity = repository.findById(parentId).orElse(null);
        }
        return parentEntity;
    }

    private NumberExpression<Integer> calcMenuOrder(int addValue) {
        return Expressions.numberTemplate(
                Integer.class, "ROWNUM + {0}" , addValue
        );
    }

    /**
     메뉴 ID 하위로 계층형 데이터를 생성한다
     **/
    private Menu generateMenuTreeById(List<MenuEntity> data, Long id) {
        Map<Long, Menu> menuMap = data.stream()
                .map(MenuEntity::toModel)
                .collect(Collectors.toMap(Menu::getId, menu -> menu, (existing, replacement) -> existing));

        menuMap.values().forEach(menu -> {
            if (menu.getParentId() != null) {
                if(menuMap.get(menu.getParentId()) !=null) {
                    menuMap.get(menu.getParentId()).getChildren().add(menu);
                }
            }
        });
        return menuMap.get(id);
    }
}
