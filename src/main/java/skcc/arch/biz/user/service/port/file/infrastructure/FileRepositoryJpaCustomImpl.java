package skcc.arch.biz.user.service.port.file.infrastructure;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;
import skcc.arch.biz.user.service.port.file.domain.FileModel;
import skcc.arch.biz.user.service.port.file.infrastructure.jpa.FIleRepositoryJpa;
import skcc.arch.biz.user.service.port.file.infrastructure.jpa.FileEntity;
import skcc.arch.biz.user.service.port.file.service.port.FileRepositoryPort;
import lombok.extern.slf4j.Slf4j;

import java.util.List;

@Repository
@RequiredArgsConstructor
@Slf4j
public class FileRepositoryJpaCustomImpl implements FileRepositoryPort {

    private final FIleRepositoryJpa repository;

    @Override
    public FileModel save(FileModel fileModel) {
        log.info("START - FileRepositoryJpaCustomImpl.save - orgName: {}", fileModel.getOrgName());
        
        try {
            FileEntity savedEntity = repository.save(FileEntity.from(fileModel));
            log.info("END - FileRepositoryJpaCustomImpl.save - id: {}", savedEntity.getId());
            return savedEntity.toModel();
        } catch (Exception e) {
            log.error("FileRepositoryJpaCustomImpl.save 오류 발생: {}", e.getMessage(), e);
            log.info("END - FileRepositoryJpaCustomImpl.save - 오류");
            throw e;
        }
    }

    @Override
    public FileModel findById(Long id) {
        log.info("START - FileRepositoryJpaCustomImpl.findById - id: {}", id);
        
        try {
            FileEntity entity = repository.findById(id).orElse(null);
            FileModel result = entity == null ? null : entity.toModel();
            log.info("END - FileRepositoryJpaCustomImpl.findById - found: {}", result != null);
            return result;
        } catch (Exception e) {
            log.error("FileRepositoryJpaCustomImpl.findById 오류 발생: {}", e.getMessage(), e);
            log.info("END - FileRepositoryJpaCustomImpl.findById - 오류");
            throw e;
        }
    }

    @Override
    public List<FileModel> findAll() {
        log.info("START - FileRepositoryJpaCustomImpl.findAll");
        
        try {
            List<FileEntity> entities = repository.findAll();
            List<FileModel> result = entities.stream()
                    .map(FileEntity::toModel)
                    .toList();
            log.info("END - FileRepositoryJpaCustomImpl.findAll - count: {}", result.size());
            return result;
        } catch (Exception e) {
            log.error("FileRepositoryJpaCustomImpl.findAll 오류 발생: {}", e.getMessage(), e);
            log.info("END - FileRepositoryJpaCustomImpl.findAll - 오류");
            throw e;
        }
    }

    @Override
    public void deleteById(Long id) {
        log.info("START - FileRepositoryJpaCustomImpl.deleteById - id: {}", id);
        
        try {
            repository.deleteById(id);
            log.info("END - FileRepositoryJpaCustomImpl.deleteById - 성공");
        } catch (Exception e) {
            log.error("FileRepositoryJpaCustomImpl.deleteById 오류 발생: {}", e.getMessage(), e);
            log.info("END - FileRepositoryJpaCustomImpl.deleteById - 오류");
            throw e;
        }
    }
}
