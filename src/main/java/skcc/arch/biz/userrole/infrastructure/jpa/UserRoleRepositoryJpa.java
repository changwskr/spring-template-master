package skcc.arch.biz.userrole.infrastructure.jpa;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface UserRoleRepositoryJpa extends JpaRepository<UserRoleEntity, Long> {
    List<UserRoleEntity> findByUserId(Long userId);
    void deleteByUserIdAndRoleId(Long userId, Long roleId);
    List<UserRoleEntity> findByRoleId(Long roleId);
}
