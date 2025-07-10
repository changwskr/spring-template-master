package skcc.arch.biz.user.service.port.file.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.core.io.InputStreamResource;
import org.springframework.stereotype.Service;
import skcc.arch.app.exception.CustomException;
import skcc.arch.app.exception.ErrorCode;
import skcc.arch.biz.user.service.port.file.domain.FileCreate;
import skcc.arch.biz.user.service.port.file.domain.FileDownload;
import skcc.arch.biz.user.service.port.file.domain.FileModel;
import skcc.arch.biz.user.service.port.file.controller.port.FileServicePort;
import skcc.arch.biz.user.service.port.file.service.port.FileRepositoryPort;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j
public class FileService implements FileServicePort {

    private final UploadPolicies uploadPolicies;
    private final FileRepositoryPort repository;

    /**
     * 단건 파일 저장
     */
    @Override
    public FileModel storeFile(MultipartFile multipartFile, String policyKey) throws IOException {
        log.info("START - FileService.storeFile - fileName: {}, policyKey: {}", 
                 multipartFile.getOriginalFilename(), policyKey);
        
        if (multipartFile.isEmpty()) {
            log.error("파일이 비어 있습니다.");
            throw new IllegalArgumentException("파일이 비어 있습니다.");
        }

        // 정책 정보 가져오기
        UploadPolicies.UploadPolicy policy = getPolicy(policyKey);
        log.info("정책 정보 조회 완료 - maxFileSize: {}, uploadDir: {}", 
                 policy.getMaxFileSize(), policy.getUploadDir());

        // 파일생성 모델
        FileCreate fileCreate = FileCreate.from(multipartFile, policy);
        log.info("파일 생성 모델 생성 완료 - orgName: {}, encName: {}, dirPath: {}", 
                 fileCreate.getOrgName(), fileCreate.getEncName(), fileCreate.getDirPath());

        String fullPath = getFullPath(fileCreate.getDirPath(), fileCreate.getEncName());
        File file = new File(fullPath);
        log.info("전체 파일 경로: {}", file.getAbsolutePath());
        
        // 부모 디렉토리 확인 및 생성
        File parentDir = file.getParentFile();
        log.info("부모 디렉토리 경로: {}", parentDir.getAbsolutePath());
        log.info("부모 디렉토리 존재 여부: {}", parentDir.exists());
        
        if (!parentDir.exists()) {
            log.info("부모 디렉토리가 존재하지 않아 생성을 시도합니다.");
            
            // 디렉토리 생성 시도
            boolean mkdirsResult = parentDir.mkdirs();
            log.info("mkdirs() 결과: {}", mkdirsResult);
            
            // 생성 후 다시 확인
            boolean existsAfterCreation = parentDir.exists();
            log.info("생성 후 디렉토리 존재 여부: {}", existsAfterCreation);
            
            if (!existsAfterCreation) {
                String errorMsg = "폴더 생성 실패: " + parentDir.getAbsolutePath();
                log.error(errorMsg);
                
                // 부모 디렉토리의 부모들도 확인
                File current = parentDir;
                while (current != null) {
                    log.info("디렉토리 체크 - 경로: {}, 존재: {}, 읽기가능: {}, 쓰기가능: {}", 
                             current.getAbsolutePath(), 
                             current.exists(), 
                             current.canRead(), 
                             current.canWrite());
                    current = current.getParentFile();
                }
                
                throw new RuntimeException(errorMsg);
            }
        } else {
            log.info("부모 디렉토리가 이미 존재합니다.");
        }
        
        // 파일 권한 확인
        log.info("파일 저장 전 권한 확인 - 부모 디렉토리 쓰기 가능: {}", parentDir.canWrite());
        
        try {
            multipartFile.transferTo(file);
            log.info("파일 저장 완료: {}", file.getAbsolutePath());
        } catch (IOException e) {
            log.error("파일 저장 중 오류 발생: {}", e.getMessage(), e);
            throw e;
        }

        // DB 저장 정책이 있을경우 DB에 저장한다
        if (policy.isSaveDb()) {
            log.info("DB 저장 정책에 따라 DB에 저장");
            FileModel result = saveToDb(fileCreate);
            log.info("END - FileService.storeFile - DB 저장 완료");
            return result;
        } else {
            log.info("DB 저장하지 않음");
            FileModel result = FileModel.from(fileCreate);
            log.info("END - FileService.storeFile - 파일만 저장 완료");
            return result;
        }
    }

    /**
     * 다건 파일 저장
     */
    @Override
    public List<FileModel> storeFiles(List<MultipartFile> multipartFiles, String policy) throws IOException {
        List<FileModel> storeFileModelResult = new ArrayList<>();
        for (MultipartFile multipartFile : multipartFiles) {
            if (!multipartFile.isEmpty()) {
                storeFileModelResult.add(storeFile(multipartFile, policy));
            }
        }
        return storeFileModelResult;
    }

    /**
     * 파일 다운로드
     */
    @Override
    public FileDownload getFileDownload(FileModel fileModel) {
        log.info("START - FileService.getFileDownload - id: {}, dirPath: {}, orgName: {}", 
                 fileModel.getId(), fileModel.getDirPath(), fileModel.getOrgName());
        
        try {
            FileDownload result = null;
            
            if(fileModel.getDirPath() != null && fileModel.getOrgName() != null) {
                log.info("파일 경로로 다운로드 시도 - dirPath: {}, orgName: {}", 
                         fileModel.getDirPath(), fileModel.getOrgName());
                result = getDownloadFileByFilepath(getFullPath(fileModel.getDirPath(), fileModel.getOrgName()));
            } else if(fileModel.getId() != 0) {
                log.info("파일 ID로 다운로드 시도 - id: {}", fileModel.getId());
                result = getDownloadFileByFileId(fileModel.getId());
            } else {
                log.warn("다운로드할 파일 정보가 불충분함 - id: {}, dirPath: {}, orgName: {}", 
                         fileModel.getId(), fileModel.getDirPath(), fileModel.getOrgName());
            }
            
            if (result != null) {
                log.info("END - FileService.getFileDownload - 성공, fileName: {}", result.fileName());
            } else {
                log.warn("END - FileService.getFileDownload - 파일을 찾을 수 없음");
            }
            
            return result;
            
        } catch (Exception e) {
            log.error("FileService.getFileDownload 오류 발생: {}", e.getMessage(), e);
            log.info("END - FileService.getFileDownload - 오류");
            throw e;
        }
    }

    /**
     * 파일 경로로 다운로드
     */
    public FileDownload getDownloadFileByFilepath(String filePath) {
        log.info("START - FileService.getDownloadFileByFilepath - filePath: {}", filePath);
        
        try {
            Path path = Paths.get(filePath);
            boolean fileExists = Files.exists(path);
            log.info("파일 존재 여부 확인 - 경로: {}, 존재: {}", path.toAbsolutePath(), fileExists);
            
            if (!fileExists) {
                log.error("파일이 존재하지 않음: {}", path.toAbsolutePath());
            }
            
            InputStreamResource resource;
            try {
                resource = new InputStreamResource(Files.newInputStream(path));
                log.info("파일 스트림 생성 성공 - 파일크기: {} bytes", Files.size(path));
            } catch (IOException e) {
                log.error("파일 스트림 생성 실패: {}, 오류: {}", filePath, e.getMessage());
                log.info("END - FileService.getDownloadFileByFilepath - 파일 없음");
                throw new CustomException(ErrorCode.NOT_FOUND_FILE);
            }
            
            String fileName = path.getFileName().toString();
            FileDownload result = new FileDownload(fileName, resource);
            
            log.info("END - FileService.getDownloadFileByFilepath - 성공, fileName: {}", fileName);
            return result;
            
        } catch (CustomException e) {
            // CustomException은 이미 로깅됨
            throw e;
        } catch (Exception e) {
            log.error("FileService.getDownloadFileByFilepath 예상치 못한 오류 발생: {}", e.getMessage(), e);
            log.info("END - FileService.getDownloadFileByFilepath - 오류");
            throw e;
        }
    }

    /**
     * 파일 id 정보로 파일을 찾는다.
     * 저장한 파일명은 오리지널 파일명이다
     */
    public FileDownload getDownloadFileByFileId(long id) {
        log.info("START - FileService.getDownloadFileByFileId - id: {}", id);
        
        try {
            log.info("DB에서 파일 정보 조회 시작 - id: {}", id);
            FileModel result = repository.findById(id);
            
            if (result == null) {
                log.warn("DB에서 파일을 찾을 수 없음 - id: {}", id);
                log.info("END - FileService.getDownloadFileByFileId - 파일 없음");
                throw new CustomException(ErrorCode.NOT_FOUND_FILE);
            }
            
            log.info("DB에서 파일 정보 조회 성공 - orgName: {}, encName: {}, dirPath: {}, size: {}", 
                     result.getOrgName(), result.getEncName(), result.getDirPath(), result.getSize());
            
            String filePath = getFullPath(result.getDirPath(), result.getEncName());
            log.info("실제 파일 경로 생성: {}", filePath);
            
            FileDownload downloadFileByFilepath = getDownloadFileByFilepath(filePath);
            FileDownload finalResult = new FileDownload(result.getOrgName(), downloadFileByFilepath.resource());
            
            log.info("원본 파일명으로 다운로드 준비 완료 - 원본파일명: {}, 저장파일명: {}", 
                     result.getOrgName(), result.getEncName());
            log.info("END - FileService.getDownloadFileByFileId - 성공");
            return finalResult;
            
        } catch (CustomException e) {
            // CustomException은 이미 로깅됨
            throw e;
        } catch (Exception e) {
            log.error("FileService.getDownloadFileByFileId 예상치 못한 오류 발생: {}", e.getMessage(), e);
            log.info("END - FileService.getDownloadFileByFileId - 오류");
            throw e;
        }
    }

    /**
     * 파일정보를 DB에 저장한다
     */
    private FileModel saveToDb(FileCreate createRequest) {
        return repository.save(FileModel.from(createRequest));
    }

    /**
     * 업로드 정책 정보를 가져온다
     */
    private UploadPolicies.UploadPolicy getPolicy(String policyKey) {
        UploadPolicies.UploadPolicy uploadPolicy = uploadPolicies.getUploadPolices().get(policyKey);
        if (uploadPolicy == null) {
            throw new IllegalStateException("정책 정보가 없습니다. policyKey :" + policyKey );
        }
        return uploadPolicy;
    }

    private String getFullPath(String dirPath, String filename) {
        return dirPath + "/" + filename;
    }

    @Override
    public List<FileModel> getAllFiles() {
        log.info("START - FileService.getAllFiles");
        
        try {
            List<FileModel> files = repository.findAll();
            log.info("END - FileService.getAllFiles - count: {}", files.size());
            return files;
        } catch (Exception e) {
            log.error("FileService.getAllFiles 오류 발생: {}", e.getMessage(), e);
            log.info("END - FileService.getAllFiles - 오류");
            throw e;
        }
    }

    @Override
    public void deleteFile(Long id) {
        log.info("START - FileService.deleteFile - id: {}", id);
        
        try {
            // 1. DB에서 파일 정보 조회
            FileModel fileModel = repository.findById(id);
            if (fileModel == null) {
                log.warn("파일을 찾을 수 없음 - id: {}", id);
                throw new IllegalArgumentException("파일을 찾을 수 없습니다. ID: " + id);
            }
            
            log.info("삭제할 파일 정보 - orgName: {}, encName: {}, dirPath: {}", 
                     fileModel.getOrgName(), fileModel.getEncName(), fileModel.getDirPath());
            
            // 2. 물리적 파일 삭제
            String filePath = getFullPath(fileModel.getDirPath(), fileModel.getEncName());
            File physicalFile = new File(filePath);
            
            if (physicalFile.exists()) {
                log.info("물리적 파일 삭제 시도: {}", physicalFile.getAbsolutePath());
                boolean deleted = physicalFile.delete();
                if (deleted) {
                    log.info("물리적 파일 삭제 성공: {}", physicalFile.getAbsolutePath());
                } else {
                    log.warn("물리적 파일 삭제 실패: {}", physicalFile.getAbsolutePath());
                    // 물리적 파일 삭제 실패해도 DB 레코드는 삭제하도록 진행
                }
            } else {
                log.warn("물리적 파일이 존재하지 않음: {}", physicalFile.getAbsolutePath());
            }
            
            // 3. DB에서 파일 레코드 삭제
            repository.deleteById(id);
            log.info("DB 레코드 삭제 완료 - id: {}", id);
            
            log.info("END - FileService.deleteFile - 성공");
            
        } catch (Exception e) {
            log.error("FileService.deleteFile 오류 발생: {}", e.getMessage(), e);
            log.info("END - FileService.deleteFile - 오류");
            throw e;
        }
    }
}
