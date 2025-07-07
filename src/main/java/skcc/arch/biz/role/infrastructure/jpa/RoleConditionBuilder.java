package skcc.arch.biz.role.infrastructure.jpa;

import com.querydsl.core.BooleanBuilder;
import com.querydsl.core.types.dsl.BooleanExpression;

import static org.springframework.util.StringUtils.hasText;
import static skcc.arch.biz.role.infrastructure.jpa.QRoleEntity.roleEntity;

public abstract class RoleConditionBuilder {

    public static BooleanBuilder roleCondition(RoleCondition condition) {
        BooleanBuilder builder = new BooleanBuilder();
        if (condition == null) {
            return builder.and(alwaysTrue());
        }

        return builder
                .and(roleIdLike(condition.getRoleId()))
                .and(nameLike(condition.getName()))
                .and(alwaysTrue())
                ;
    }

    private static BooleanExpression roleIdLike(String roleId) {
        return hasText(roleId) ? roleEntity.roleId.like("%" + roleId + "%") : null;
    }

    private static BooleanExpression nameLike(String name) {
        return hasText(name) ? roleEntity.name.like("%" + name + "%") : null;
    }

    private static BooleanExpression alwaysTrue() {
        return roleEntity.isNotNull(); // 항상 참인 조건으로 설정.
    }

}
