package skcc.arch.biz.user.service.port.file.infrastructure.jpa;

import org.springframework.data.jpa.repository.JpaRepository;

public interface FIleRepositoryJpa extends JpaRepository<FileEntity, Long> {
}
