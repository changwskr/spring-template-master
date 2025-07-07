package skcc.arch.biz.role.infrastructure.jpa;

import org.springframework.data.jpa.repository.JpaRepository;

public interface RoleRepositoryJpa extends JpaRepository<RoleEntity, Long> {
}
