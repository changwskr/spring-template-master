package skcc.arch.biz.userrole.infrastructure.jpa;

import static com.querydsl.core.types.PathMetadataFactory.*;

import com.querydsl.core.types.dsl.*;

import com.querydsl.core.types.PathMetadata;
import javax.annotation.processing.Generated;
import com.querydsl.core.types.Path;
import com.querydsl.core.types.dsl.PathInits;


/**
 * QUserRoleEntity is a Querydsl query type for UserRoleEntity
 */
@Generated("com.querydsl.codegen.DefaultEntitySerializer")
public class QUserRoleEntity extends EntityPathBase<UserRoleEntity> {

    private static final long serialVersionUID = -1745836782L;

    private static final PathInits INITS = PathInits.DIRECT2;

    public static final QUserRoleEntity userRoleEntity = new QUserRoleEntity("userRoleEntity");

    public final skcc.arch.biz.common.infrastructure.jpa.QBaseEntity _super = new skcc.arch.biz.common.infrastructure.jpa.QBaseEntity(this);

    //inherited
    public final DateTimePath<java.time.LocalDateTime> createdDate = _super.createdDate;

    public final NumberPath<Long> id = createNumber("id", Long.class);

    //inherited
    public final DateTimePath<java.time.LocalDateTime> lastModifiedDate = _super.lastModifiedDate;

    public final skcc.arch.biz.role.infrastructure.jpa.QRoleEntity role;

    public final skcc.arch.biz.user.infrastructure.jpa.QUserEntity user;

    public QUserRoleEntity(String variable) {
        this(UserRoleEntity.class, forVariable(variable), INITS);
    }

    public QUserRoleEntity(Path<? extends UserRoleEntity> path) {
        this(path.getType(), path.getMetadata(), PathInits.getFor(path.getMetadata(), INITS));
    }

    public QUserRoleEntity(PathMetadata metadata) {
        this(metadata, PathInits.getFor(metadata, INITS));
    }

    public QUserRoleEntity(PathMetadata metadata, PathInits inits) {
        this(UserRoleEntity.class, metadata, inits);
    }

    public QUserRoleEntity(Class<? extends UserRoleEntity> type, PathMetadata metadata, PathInits inits) {
        super(type, metadata, inits);
        this.role = inits.isInitialized("role") ? new skcc.arch.biz.role.infrastructure.jpa.QRoleEntity(forProperty("role")) : null;
        this.user = inits.isInitialized("user") ? new skcc.arch.biz.user.infrastructure.jpa.QUserEntity(forProperty("user")) : null;
    }

}

