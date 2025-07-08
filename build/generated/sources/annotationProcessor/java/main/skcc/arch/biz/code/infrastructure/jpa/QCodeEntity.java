package skcc.arch.biz.code.infrastructure.jpa;

import static com.querydsl.core.types.PathMetadataFactory.*;

import com.querydsl.core.types.dsl.*;

import com.querydsl.core.types.PathMetadata;
import javax.annotation.processing.Generated;
import com.querydsl.core.types.Path;
import com.querydsl.core.types.dsl.PathInits;


/**
 * QCodeEntity is a Querydsl query type for CodeEntity
 */
@Generated("com.querydsl.codegen.DefaultEntitySerializer")
public class QCodeEntity extends EntityPathBase<CodeEntity> {

    private static final long serialVersionUID = -1508008246L;

    private static final PathInits INITS = PathInits.DIRECT2;

    public static final QCodeEntity codeEntity = new QCodeEntity("codeEntity");

    public final skcc.arch.biz.common.infrastructure.jpa.QBaseEntity _super = new skcc.arch.biz.common.infrastructure.jpa.QBaseEntity(this);

    public final ListPath<CodeEntity, QCodeEntity> child = this.<CodeEntity, QCodeEntity>createList("child", CodeEntity.class, QCodeEntity.class, PathInits.DIRECT2);

    public final StringPath code = createString("code");

    public final StringPath codeName = createString("codeName");

    //inherited
    public final DateTimePath<java.time.LocalDateTime> createdDate = _super.createdDate;

    public final BooleanPath delYn = createBoolean("delYn");

    public final StringPath description = createString("description");

    public final NumberPath<Long> id = createNumber("id", Long.class);

    //inherited
    public final DateTimePath<java.time.LocalDateTime> lastModifiedDate = _super.lastModifiedDate;

    public final QCodeEntity parentCode;

    public final NumberPath<Integer> seq = createNumber("seq", Integer.class);

    public QCodeEntity(String variable) {
        this(CodeEntity.class, forVariable(variable), INITS);
    }

    public QCodeEntity(Path<? extends CodeEntity> path) {
        this(path.getType(), path.getMetadata(), PathInits.getFor(path.getMetadata(), INITS));
    }

    public QCodeEntity(PathMetadata metadata) {
        this(metadata, PathInits.getFor(metadata, INITS));
    }

    public QCodeEntity(PathMetadata metadata, PathInits inits) {
        this(CodeEntity.class, metadata, inits);
    }

    public QCodeEntity(Class<? extends CodeEntity> type, PathMetadata metadata, PathInits inits) {
        super(type, metadata, inits);
        this.parentCode = inits.isInitialized("parentCode") ? new QCodeEntity(forProperty("parentCode"), inits.get("parentCode")) : null;
    }

}

