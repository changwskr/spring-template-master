package skcc.arch.biz.file.service.port;

import skcc.arch.biz.file.domain.FileModel;
import java.util.List;

public interface FileRepositoryPort {
    FileModel save(FileModel fileModel);
    FileModel findById(Long id);
    List<FileModel> findAll();
    void deleteById(Long id);
}
