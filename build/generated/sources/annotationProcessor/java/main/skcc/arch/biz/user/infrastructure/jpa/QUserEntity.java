package skcc.arch.biz.user.infrastructure.jpa;

import static com.querydsl.core.types.PathMetadataFactory.*;

import com.querydsl.core.types.dsl.*;

import com.querydsl.core.types.PathMetadata;
import javax.annotation.processing.Generated;
import com.querydsl.core.types.Path;
import com.querydsl.core.types.dsl.PathInits;


/**
 * QUserEntity is a Querydsl query type for UserEntity
 */
@Generated("com.querydsl.codegen.DefaultEntitySerializer")
public class QUserEntity extends EntityPathBase<UserEntity> {

    private static final long serialVersionUID = 394830726L;

    public static final QUserEntity userEntity = new QUserEntity("userEntity");

    public final StringPath address = createString("address");

    public final NumberPath<Integer> age = createNumber("age", Integer.class);

    public final StringPath company = createString("company");

    public final DateTimePath<java.time.LocalDateTime> createdDate = createDateTime("createdDate", java.time.LocalDateTime.class);

    public final StringPath email = createString("email");

    public final NumberPath<Long> id = createNumber("id", Long.class);

    public final StringPath job = createString("job");

    public final DateTimePath<java.time.LocalDateTime> lastModifiedDate = createDateTime("lastModifiedDate", java.time.LocalDateTime.class);

    public final StringPath password = createString("password");

    public final EnumPath<skcc.arch.biz.user.domain.UserStatus> status = createEnum("status", skcc.arch.biz.user.domain.UserStatus.class);

    public final StringPath userId = createString("userId");

    public final StringPath username = createString("username");

    public final ListPath<skcc.arch.biz.userrole.infrastructure.jpa.UserRoleEntity, skcc.arch.biz.userrole.infrastructure.jpa.QUserRoleEntity> userRoles = this.<skcc.arch.biz.userrole.infrastructure.jpa.UserRoleEntity, skcc.arch.biz.userrole.infrastructure.jpa.QUserRoleEntity>createList("userRoles", skcc.arch.biz.userrole.infrastructure.jpa.UserRoleEntity.class, skcc.arch.biz.userrole.infrastructure.jpa.QUserRoleEntity.class, PathInits.DIRECT2);

    public QUserEntity(String variable) {
        super(UserEntity.class, forVariable(variable));
    }

    public QUserEntity(Path<? extends UserEntity> path) {
        super(path.getType(), path.getMetadata());
    }

    public QUserEntity(PathMetadata metadata) {
        super(UserEntity.class, metadata);
    }

}

