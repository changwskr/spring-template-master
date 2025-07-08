package skcc.arch.biz.role.infrastructure.jpa;

import static com.querydsl.core.types.PathMetadataFactory.*;

import com.querydsl.core.types.dsl.*;

import com.querydsl.core.types.PathMetadata;
import javax.annotation.processing.Generated;
import com.querydsl.core.types.Path;
import com.querydsl.core.types.dsl.PathInits;


/**
 * QRoleEntity is a Querydsl query type for RoleEntity
 */
@Generated("com.querydsl.codegen.DefaultEntitySerializer")
public class QRoleEntity extends EntityPathBase<RoleEntity> {

    private static final long serialVersionUID = 198386524L;

    public static final QRoleEntity roleEntity = new QRoleEntity("roleEntity");

    public final skcc.arch.biz.common.infrastructure.jpa.QBaseEntity _super = new skcc.arch.biz.common.infrastructure.jpa.QBaseEntity(this);

    //inherited
    public final DateTimePath<java.time.LocalDateTime> createdDate = _super.createdDate;

    public final NumberPath<Long> id = createNumber("id", Long.class);

    //inherited
    public final DateTimePath<java.time.LocalDateTime> lastModifiedDate = _super.lastModifiedDate;

    public final ListPath<skcc.arch.biz.menurole.infrastructure.jpa.MenuRoleEntity, skcc.arch.biz.menurole.infrastructure.jpa.QMenuRoleEntity> menuRoles = this.<skcc.arch.biz.menurole.infrastructure.jpa.MenuRoleEntity, skcc.arch.biz.menurole.infrastructure.jpa.QMenuRoleEntity>createList("menuRoles", skcc.arch.biz.menurole.infrastructure.jpa.MenuRoleEntity.class, skcc.arch.biz.menurole.infrastructure.jpa.QMenuRoleEntity.class, PathInits.DIRECT2);

    public final StringPath name = createString("name");

    public final StringPath roleId = createString("roleId");

    public QRoleEntity(String variable) {
        super(RoleEntity.class, forVariable(variable));
    }

    public QRoleEntity(Path<? extends RoleEntity> path) {
        super(path.getType(), path.getMetadata());
    }

    public QRoleEntity(PathMetadata metadata) {
        super(RoleEntity.class, metadata);
    }

}

