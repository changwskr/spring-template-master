package skcc.arch.biz.file.service;

import lombok.Getter;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

import jakarta.annotation.PostConstruct;
import java.util.HashMap;
import java.util.Map;
import java.io.File;

@Getter
@Setter
@Configuration
@ConfigurationProperties(prefix = "file")
@Slf4j
public class UploadPolicies {

    private Map<String, UploadPolicy> uploadPolices;

    @PostConstruct
    public void init() {
        if (uploadPolices == null) {
            log.warn("파일 업로드 정책이 설정되지 않아 기본 정책을 사용합니다");
            uploadPolices = new HashMap<>();
            
            UploadPolicy defaultPolicy = new UploadPolicy();
            defaultPolicy.setMaxFileSize(10 * 1024 * 1024); // 10MB
            
            // 현재 작업 디렉토리 기준으로 uploads 폴더 설정
            String currentDir = System.getProperty("user.dir");
            String uploadDir = currentDir + File.separator + "uploads";
            log.info("기본 업로드 디렉토리 설정: {}", uploadDir);
            
            defaultPolicy.setUploadDir(uploadDir);
            defaultPolicy.setAllowedExtensions(new String[]{"jpg", "jpeg", "png", "gif", "pdf", "doc", "docx", "txt", "xlsx"});
            defaultPolicy.setSaveDb(true);
            
            uploadPolices.put("defaultPolicy", defaultPolicy);
        }
    }

    @Getter
    @Setter
    public static class UploadPolicy {
        private long maxFileSize;
        private String uploadDir;
        private String[] allowedExtensions;
        private boolean saveDb;
    }
}