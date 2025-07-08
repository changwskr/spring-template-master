package skcc.arch.biz.menurole.infrastructure.jpa;

import static com.querydsl.core.types.PathMetadataFactory.*;

import com.querydsl.core.types.dsl.*;

import com.querydsl.core.types.PathMetadata;
import javax.annotation.processing.Generated;
import com.querydsl.core.types.Path;
import com.querydsl.core.types.dsl.PathInits;


/**
 * QMenuRoleEntity is a Querydsl query type for MenuRoleEntity
 */
@Generated("com.querydsl.codegen.DefaultEntitySerializer")
public class QMenuRoleEntity extends EntityPathBase<MenuRoleEntity> {

    private static final long serialVersionUID = 15286330L;

    private static final PathInits INITS = PathInits.DIRECT2;

    public static final QMenuRoleEntity menuRoleEntity = new QMenuRoleEntity("menuRoleEntity");

    public final skcc.arch.biz.common.infrastructure.jpa.QBaseEntity _super = new skcc.arch.biz.common.infrastructure.jpa.QBaseEntity(this);

    //inherited
    public final DateTimePath<java.time.LocalDateTime> createdDate = _super.createdDate;

    public final NumberPath<Long> id = createNumber("id", Long.class);

    //inherited
    public final DateTimePath<java.time.LocalDateTime> lastModifiedDate = _super.lastModifiedDate;

    public final skcc.arch.biz.menu.infrastructure.jpa.QMenuEntity menu;

    public final skcc.arch.biz.role.infrastructure.jpa.QRoleEntity role;

    public QMenuRoleEntity(String variable) {
        this(MenuRoleEntity.class, forVariable(variable), INITS);
    }

    public QMenuRoleEntity(Path<? extends MenuRoleEntity> path) {
        this(path.getType(), path.getMetadata(), PathInits.getFor(path.getMetadata(), INITS));
    }

    public QMenuRoleEntity(PathMetadata metadata) {
        this(metadata, PathInits.getFor(metadata, INITS));
    }

    public QMenuRoleEntity(PathMetadata metadata, PathInits inits) {
        this(MenuRoleEntity.class, metadata, inits);
    }

    public QMenuRoleEntity(Class<? extends MenuRoleEntity> type, PathMetadata metadata, PathInits inits) {
        super(type, metadata, inits);
        this.menu = inits.isInitialized("menu") ? new skcc.arch.biz.menu.infrastructure.jpa.QMenuEntity(forProperty("menu"), inits.get("menu")) : null;
        this.role = inits.isInitialized("role") ? new skcc.arch.biz.role.infrastructure.jpa.QRoleEntity(forProperty("role")) : null;
    }

}

