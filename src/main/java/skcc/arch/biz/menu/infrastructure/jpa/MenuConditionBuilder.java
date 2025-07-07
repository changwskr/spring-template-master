package skcc.arch.biz.menu.infrastructure.jpa;

import com.querydsl.core.BooleanBuilder;
import com.querydsl.core.types.dsl.BooleanExpression;
import org.springframework.util.ObjectUtils;

import java.time.LocalDate;

import static org.springframework.util.StringUtils.hasText;
import static skcc.arch.biz.menu.infrastructure.jpa.QMenuEntity.menuEntity;

public abstract class MenuConditionBuilder {

    public static BooleanBuilder menuCondition(MenuCondition condition) {
        BooleanBuilder builder = new BooleanBuilder();
        if (condition == null) {
            return builder.and(alwaysTrue());
        }

        return builder
                .and(nameLike(condition.getName()))
                .and(menuUrlLike(condition.getMenuUrl()))
                .and(parentEq(condition.getParent()))
                .and(startDateGoe(condition.getStartDate()))
                .and(endDateGoe(condition.getEndDate()))
                .and(isDeleted(condition.isDeleted()))
                .and(alwaysTrue())
                ;
    }

    private static BooleanExpression nameLike(String menuName) {
        return hasText(menuName) ? menuEntity.name.like("%" + menuName + "%") : null;
    }

    private static BooleanExpression menuUrlLike(String menuName) {
        return hasText(menuName) ? menuEntity.menuUrl.like("%" + menuName + "%") : null;
    }

    private static BooleanExpression parentEq(MenuEntity parent) {
        return ObjectUtils.isEmpty(parent) ? null : menuEntity.parent.eq(parent);
    }

    private static BooleanExpression startDateGoe(LocalDate startDate) {
        return startDate != null ? menuEntity.startDate.goe(startDate) : null;
    }

    private static BooleanExpression endDateGoe(LocalDate endDate) {
        return endDate != null ? menuEntity.endDate.loe(endDate) : null;
    }

    // DEL_YN의 경우 기본값이 FALSE
    private static BooleanExpression isDeleted(Boolean isDeleted) {
        return menuEntity.isDeleted.eq(Boolean.TRUE.equals(isDeleted));
    }

    private static BooleanExpression alwaysTrue() {
        return menuEntity.isNotNull(); // 항상 참인 조건으로 설정.
    }
}
