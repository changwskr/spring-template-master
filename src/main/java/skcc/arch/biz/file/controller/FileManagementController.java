package skcc.arch.biz.file.controller;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.core.io.Resource;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
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
        return "redirect:/file/upload";
    }

    @GetMapping("/upload")
    public String uploadForm(Model model) {
        model.addAttribute("title", "파일 업로드");
        return "file/upload";
    }

    @PostMapping("/upload")
    public String uploadFile(
            @RequestParam("file") MultipartFile file,
            @RequestParam(value = "policyKey", defaultValue = "defaultPolicy") String policyKey,
            Model model) {
        
        try {
            if (file.isEmpty()) {
                model.addAttribute("error", "업로드할 파일을 선택해주세요.");
                return "file/upload";
            }
            
            FileModel uploadedFile = fileServicePort.storeFile(file, policyKey);
            model.addAttribute("success", "파일이 성공적으로 업로드되었습니다.");
            model.addAttribute("uploadedFile", uploadedFile);
            
            log.info("파일 업로드 완료: {}", uploadedFile.getOrgName());
            
        } catch (IOException e) {
            log.error("파일 업로드 중 오류 발생: {}", e.getMessage());
            model.addAttribute("error", "파일 업로드 중 오류가 발생했습니다: " + e.getMessage());
        } catch (Exception e) {
            log.error("파일 업로드 중 예상치 못한 오류 발생: {}", e.getMessage());
            model.addAttribute("error", "파일 업로드 중 오류가 발생했습니다.");
        }
        
        return "file/upload";
    }

    @PostMapping("/upload/multiple")
    public String uploadMultipleFiles(
            @RequestParam("files") List<MultipartFile> files,
            @RequestParam(value = "policyKey", defaultValue = "defaultPolicy") String policyKey,
            Model model) {
        
        try {
            if (files.isEmpty() || files.stream().allMatch(MultipartFile::isEmpty)) {
                model.addAttribute("error", "업로드할 파일을 선택해주세요.");
                return "file/upload";
            }
            
            List<FileModel> uploadedFiles = fileServicePort.storeFiles(files, policyKey);
            model.addAttribute("success", files.size() + "개의 파일이 성공적으로 업로드되었습니다.");
            model.addAttribute("uploadedFiles", uploadedFiles);
            
            log.info("다중 파일 업로드 완료: {}개", uploadedFiles.size());
            
        } catch (IOException e) {
            log.error("다중 파일 업로드 중 오류 발생: {}", e.getMessage());
            model.addAttribute("error", "파일 업로드 중 오류가 발생했습니다: " + e.getMessage());
        } catch (Exception e) {
            log.error("다중 파일 업로드 중 예상치 못한 오류 발생: {}", e.getMessage());
            model.addAttribute("error", "파일 업로드 중 오류가 발생했습니다.");
        }
        
        return "file/upload";
    }

    @GetMapping("/list")
    public String fileList(Model model) {
        try {
            // 실제 구현에서는 파일 목록을 데이터베이스에서 조회해야 합니다
            // 현재는 임시로 빈 목록을 전달합니다
            List<FileModel> files = new ArrayList<>();
            model.addAttribute("files", files);
            model.addAttribute("title", "파일 목록");
            
        } catch (Exception e) {
            log.error("파일 목록 조회 중 오류 발생: {}", e.getMessage());
            model.addAttribute("error", "파일 목록 조회 중 오류가 발생했습니다.");
            model.addAttribute("files", new ArrayList<>());
        }
        
        return "file/list";
    }

    @PostMapping("/download")
    public ResponseEntity<Resource> downloadFile(
            @RequestParam("fileName") String fileName,
            @RequestParam("storedName") String storedName) {
        
        try {
            FileDownload fileDownload = fileServicePort.getFileDownload(
                FileModel.builder()
                    .orgName(fileName)
                    .encName(storedName)
                    .build()
            );
            
            if (fileDownload != null) {
                return ResponseEntity.ok()
                    .header("Content-Disposition", "attachment; filename=\"" + fileDownload.fileName() + "\"")
                    .header("Content-Type", "application/octet-stream")
                    .body(fileDownload.resource());
            } else {
                return ResponseEntity.notFound().build();
            }
            
        } catch (Exception e) {
            log.error("파일 다운로드 중 오류 발생: {}", e.getMessage());
            return ResponseEntity.internalServerError().build();
        }
    }
} 