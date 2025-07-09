package skcc.arch.biz.file.controller;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.core.io.Resource;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;
import skcc.arch.biz.file.controller.port.FileServicePort;
import skcc.arch.biz.file.domain.FileDownload;
import skcc.arch.biz.file.domain.FileModel;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

@Controller
@RequestMapping("/file")
@RequiredArgsConstructor
@Slf4j
public class FileManagementController {

    private final FileServicePort fileServicePort;

    @GetMapping
    public String fileMain() {
        log.info("START - FileManagementController.fileMain");
        String result = "redirect:/file/upload";
        log.info("END - FileManagementController.fileMain");
        return result;
    }

    @GetMapping("/upload")
    public String uploadForm(Model model) {
        log.info("START - FileManagementController.uploadForm");
        model.addAttribute("title", "파일 업로드");
        log.info("END - FileManagementController.uploadForm");
        return "file/upload";
    }

    @PostMapping("/upload")
    public String uploadFile(
            @RequestParam("file") MultipartFile file,
            @RequestParam(value = "policyKey", defaultValue = "defaultPolicy") String policyKey,
            Model model) {

        log.info("START - FileManagementController.uploadFile - policyKey: {}, fileName: {}", 
                 policyKey, file.getOriginalFilename());

        try {
            if (file.isEmpty()) {
                log.warn("업로드할 파일이 비어있음");
                model.addAttribute("error", "업로드할 파일을 선택해주세요.");
                log.info("END - FileManagementController.uploadFile - 파일 없음");
                return "file/upload";
            }

            FileModel uploadedFile = fileServicePort.storeFile(file, policyKey);
            model.addAttribute("success", "파일이 성공적으로 업로드되었습니다.");
            model.addAttribute("uploadedFile", uploadedFile);

            log.info("파일 업로드 완료: {}", uploadedFile.getOrgName());
            log.info("END - FileManagementController.uploadFile - 성공");

        } catch (IOException e) {
            log.error("파일 업로드 중 IOException 발생: {}", e.getMessage(), e);
            model.addAttribute("error", "파일 업로드 중 오류가 발생했습니다: " + e.getMessage());
            log.info("END - FileManagementController.uploadFile - IOException");
        } catch (Exception e) {
            log.error("파일 업로드 중 예상치 못한 오류 발생: {}", e.getMessage(), e);
            model.addAttribute("error", "파일 업로드 중 오류가 발생했습니다.");
            log.info("END - FileManagementController.uploadFile - 예상치 못한 오류");
        }

        return "file/upload";
    }

    @PostMapping("/upload/multiple")
    public String uploadMultipleFiles(
            @RequestParam("files") List<MultipartFile> files,
            @RequestParam(value = "policyKey", defaultValue = "defaultPolicy") String policyKey,
            Model model) {

        log.info("START - FileManagementController.uploadMultipleFiles - policyKey: {}, fileCount: {}", 
                 policyKey, files.size());

        try {
            if (files.isEmpty() || files.stream().allMatch(MultipartFile::isEmpty)) {
                log.warn("업로드할 파일이 없음");
                model.addAttribute("error", "업로드할 파일을 선택해주세요.");
                log.info("END - FileManagementController.uploadMultipleFiles - 파일 없음");
                return "file/upload";
            }

            List<FileModel> uploadedFiles = fileServicePort.storeFiles(files, policyKey);
            model.addAttribute("success", files.size() + "개의 파일이 성공적으로 업로드되었습니다.");
            model.addAttribute("uploadedFiles", uploadedFiles);

            log.info("다중 파일 업로드 완료: {}개", uploadedFiles.size());

        } catch (IOException e) {
            log.error("다중 파일 업로드 중 IOException 발생: {}", e.getMessage(), e);
            model.addAttribute("error", "파일 업로드 중 오류가 발생했습니다: " + e.getMessage());
        } catch (Exception e) {
            log.error("다중 파일 업로드 중 예상치 못한 오류 발생: {}", e.getMessage(), e);
            model.addAttribute("error", "파일 업로드 중 오류가 발생했습니다.");
        }

        log.info("END - FileManagementController.uploadMultipleFiles");
        return "file/upload";
    }

    @GetMapping("/list")
    public String fileList(Model model) {
        log.info("START - FileManagementController.fileList");
        
        try {
            List<FileModel> files = fileServicePort.getAllFiles();
            log.info("파일 목록 조회 완료 - count: {}", files.size());
            
            model.addAttribute("files", files);
            model.addAttribute("title", "파일 목록");
            
            // 헬퍼 메서드를 모델에 추가
            model.addAttribute("fileHelper", new FileHelper());

        } catch (Exception e) {
            log.error("파일 목록 조회 중 오류 발생: {}", e.getMessage(), e);
            model.addAttribute("error", "파일 목록 조회 중 오류가 발생했습니다.");
            model.addAttribute("files", new ArrayList<>());
        }

        log.info("END - FileManagementController.fileList");
        return "file/list";
    }

    @PostMapping("/download")
    public ResponseEntity<Resource> downloadFile(@RequestParam("fileId") Long fileId) {
        log.info("START - FileManagementController.downloadFile - fileId: {}", fileId);

        try {
            FileModel fileModel = FileModel.builder().id(fileId).build();
            FileDownload fileDownload = fileServicePort.getFileDownload(fileModel);

            if (fileDownload != null) {
                log.info("파일 다운로드 성공: {}", fileDownload.fileName());
                log.info("END - FileManagementController.downloadFile - 성공");
                return ResponseEntity.ok()
                    .header("Content-Disposition", "attachment; filename=\"" + fileDownload.fileName() + "\"")
                    .header("Content-Type", "application/octet-stream")
                    .body(fileDownload.resource());
            } else {
                log.warn("파일을 찾을 수 없음 - fileId: {}", fileId);
                log.info("END - FileManagementController.downloadFile - 파일 없음");
                return ResponseEntity.notFound().build();
            }

        } catch (Exception e) {
            log.error("파일 다운로드 중 오류 발생: {}", e.getMessage(), e);
            log.info("END - FileManagementController.downloadFile - 오류");
            return ResponseEntity.internalServerError().build();
        }
    }

    @PostMapping("/delete/{id}")
    public String deleteFile(@PathVariable Long id, RedirectAttributes redirectAttributes) {
        log.info("START - FileManagementController.deleteFile - id: {}", id);
        
        try {
            fileServicePort.deleteFile(id);
            log.info("파일 삭제 성공 - id: {}", id);
            redirectAttributes.addFlashAttribute("success", "파일이 성공적으로 삭제되었습니다.");
            log.info("END - FileManagementController.deleteFile - 성공");
            
        } catch (IllegalArgumentException e) {
            log.error("파일을 찾을 수 없음 - id: {}, error: {}", id, e.getMessage());
            redirectAttributes.addFlashAttribute("error", "파일을 찾을 수 없습니다.");
            log.info("END - FileManagementController.deleteFile - 파일 없음");
            
        } catch (Exception e) {
            log.error("파일 삭제 중 오류 발생 - id: {}, error: {}", id, e.getMessage(), e);
            redirectAttributes.addFlashAttribute("error", "파일 삭제 중 오류가 발생했습니다.");
            log.info("END - FileManagementController.deleteFile - 오류");
        }
        
        return "redirect:/file/list";
    }

    @GetMapping("/test-korean-log")
    @ResponseBody
    public String testKoreanLog() {
        log.info("=== 한글 로그 테스트 시작 ===");
        log.debug("디버그: 파일 업로드가 완료되었습니다");
        log.info("정보: 사용자가 파일을 다운로드했습니다");
        log.warn("경고: 파일 크기가 제한을 초과했습니다");
        log.error("오류: 파일을 찾을 수 없습니다");
        log.info("=== 한글 로그 테스트 종료 ===");
        return "한글 로그 테스트 완료";
    }

    // FileHelper 클래스를 FileManagementController 내부의 static 클래스로 정의
    public static class FileHelper {
        
        public String getFileIcon(String fileName) {
            if (fileName == null) return "📄";
            
            String extension = getExtension(fileName).toLowerCase();
            
            switch(extension) {
                case "pdf": return "📄";
                case "doc":
                case "docx": return "📝";
                case "xls":
                case "xlsx": return "📊";
                case "ppt":
                case "pptx": return "📋";
                case "jpg":
                case "jpeg":
                case "png":
                case "gif":
                case "bmp": return "🖼️";
                case "mp4":
                case "avi":
                case "mov": return "🎥";
                case "mp3":
                case "wav":
                case "flac": return "🎵";
                case "zip":
                case "rar":
                case "7z": return "📦";
                case "txt": return "📄";
                default: return "📄";
            }
        }
        
        public String getFileType(String fileName) {
            if (fileName == null) return "기타 파일";
            
            String extension = getExtension(fileName).toLowerCase();
            
            switch(extension) {
                case "pdf": return "PDF 문서";
                case "doc":
                case "docx": return "Word 문서";
                case "xls":
                case "xlsx": return "Excel 스프레드시트";
                case "ppt":
                case "pptx": return "PowerPoint 프레젠테이션";
                case "jpg":
                case "jpeg":
                case "png":
                case "gif":
                case "bmp": return "이미지 파일";
                case "mp4":
                case "avi":
                case "mov": return "비디오 파일";
                case "mp3":
                case "wav":
                case "flac": return "오디오 파일";
                case "zip":
                case "rar":
                case "7z": return "압축 파일";
                case "txt": return "텍스트 파일";
                default: return "기타 파일";
            }
        }
        
        public String formatFileSize(long bytes) {
            if (bytes == 0) return "0 Bytes";
            
            String[] sizes = {"Bytes", "KB", "MB", "GB"};
            int i = (int) Math.floor(Math.log(bytes) / Math.log(1024));
            double size = bytes / Math.pow(1024, i);
            
            return String.format("%.2f %s", size, sizes[i]);
        }
        
        private String getExtension(String fileName) {
            int lastDot = fileName.lastIndexOf('.');
            return lastDot > 0 ? fileName.substring(lastDot + 1) : "";
        }
    }
}