package skcc.arch.biz.menu.infrastructure.jpa;


import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface MenuRepositoryJpa extends JpaRepository<MenuEntity, Long> {
    @Query(value = """
    WITH RECURSIVE menu_tree (id, name, parent_id, menu_order, menu_url, start_date, end_date, created_date, last_modified_date, is_deleted, depth, menu_order_path) AS (
         SELECT id,
                name,
                parent_id,
                menu_order,
                menu_url,
                start_date,
                end_date,
                created_date,
                last_modified_date,
                is_deleted,
                1 AS depth,
                CAST(menu_order AS VARCHAR) AS menu_order_path
         FROM menus
         WHERE id = ?
    
         UNION ALL
    
         SELECT child.id,
                child.name,
                child.parent_id,
                child.menu_order,
                child.menu_url,
                child.start_date,
                child.end_date,
                child.created_date,
                child.last_modified_date,
                child.is_deleted,
                ct.depth + 1 AS depth,
                ct.menu_order_path || '-' || CAST(child.menu_order AS VARCHAR) AS menu_order_path
         FROM menus child
                  INNER JOIN menu_tree ct ON child.parent_id = ct.id
     )
     SELECT id, name, parent_id, menu_order, menu_url
          , start_date
          , end_date
          , created_date
          , last_modified_date
            , is_deleted
    FROM menu_tree
    ORDER BY menu_order_path
    """, nativeQuery = true
    )
    List<MenuEntity> findByIdTree(@Param("id") Long id);

}
