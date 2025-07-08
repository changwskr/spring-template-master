package skcc.arch.biz.menu.infrastructure.jpa;

import static com.querydsl.core.types.PathMetadataFactory.*;

import com.querydsl.core.types.dsl.*;

import com.querydsl.core.types.PathMetadata;
import javax.annotation.processing.Generated;
import com.querydsl.core.types.Path;
import com.querydsl.core.types.dsl.PathInits;


/**
 * QMenuEntity is a Querydsl query type for MenuEntity
 */
@Generated("com.querydsl.codegen.DefaultEntitySerializer")
public class QMenuEntity extends EntityPathBase<MenuEntity> {

    private static final long serialVersionUID = -591496018L;

    private static final PathInits INITS = PathInits.DIRECT2;

    public static final QMenuEntity menuEntity = new QMenuEntity("menuEntity");

    public final skcc.arch.biz.common.infrastructure.jpa.QBaseEntity _super = new skcc.arch.biz.common.infrastructure.jpa.QBaseEntity(this);

    public final ListPath<MenuEntity, QMenuEntity> children = this.<MenuEntity, QMenuEntity>createList("children", MenuEntity.class, QMenuEntity.class, PathInits.DIRECT2);

    //inherited
    public final DateTimePath<java.time.LocalDateTime> createdDate = _super.createdDate;

    public final DatePath<java.time.LocalDate> endDate = createDate("endDate", java.time.LocalDate.class);

    public final NumberPath<Long> id = createNumber("id", Long.class);

    public final BooleanPath isDeleted = createBoolean("isDeleted");

    //inherited
    public final DateTimePath<java.time.LocalDateTime> lastModifiedDate = _super.lastModifiedDate;

    public final NumberPath<Integer> menuOrder = createNumber("menuOrder", Integer.class);

    public final StringPath menuUrl = createString("menuUrl");

    public final StringPath name = createString("name");

    public final QMenuEntity parent;

    public final DatePath<java.time.LocalDate> startDate = createDate("startDate", java.time.LocalDate.class);

    public QMenuEntity(String variable) {
        this(MenuEntity.class, forVariable(variable), INITS);
    }

    public QMenuEntity(Path<? extends MenuEntity> path) {
        this(path.getType(), path.getMetadata(), PathInits.getFor(path.getMetadata(), INITS));
    }

    public QMenuEntity(PathMetadata metadata) {
        this(metadata, PathInits.getFor(metadata, INITS));
    }

    public QMenuEntity(PathMetadata metadata, PathInits inits) {
        this(MenuEntity.class, metadata, inits);
    }

    public QMenuEntity(Class<? extends MenuEntity> type, PathMetadata metadata, PathInits inits) {
        super(type, metadata, inits);
        this.parent = inits.isInitialized("parent") ? new QMenuEntity(forProperty("parent"), inits.get("parent")) : null;
    }

}

