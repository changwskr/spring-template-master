package skcc.arch.biz.user.service.port.file.controller.port;

import org.springframework.web.multipart.MultipartFile;
import skcc.arch.biz.user.service.port.file.domain.FileDownload;
import skcc.arch.biz.user.service.port.file.domain.FileModel;

import java.io.IOException;
import java.util.List;

public interface FileServicePort {

    FileModel storeFile(MultipartFile multipartFile, String policyKey) throws IOException;
    List<FileModel> storeFiles(List<MultipartFile> multipartFiles, String policyKey) throws IOException;
    FileDownload getFileDownload(FileModel fileModel);
    List<FileModel> getAllFiles();
    void deleteFile(Long id);
}
