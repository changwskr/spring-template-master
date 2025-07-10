package skcc.arch.biz.token.domain;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.LocalDateTime;

@Entity
@Table(name = "tokens", indexes = {
    @Index(name = "idx_token_value", columnList = "token_value", unique = true),
    @Index(name = "idx_user_id", columnList = "user_id"),
    @Index(name = "idx_user_id_active", columnList = "user_id, is_active"),
    @Index(name = "idx_expires_at", columnList = "expires_at")
})
@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Token {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @Column(name = "user_id", nullable = false)
    private String userId;
    
    @Column(name = "token_value", nullable = false, length = 500)
    private String tokenValue;
    
    @Column(name = "expires_at", nullable = false)
    private LocalDateTime expiresAt;
    
    @Column(name = "is_active", nullable = false)
    @Builder.Default
    private Boolean isActive = true;
    
    @CreationTimestamp
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;
    
    @UpdateTimestamp
    @Column(name = "updated_at", nullable = false)
    private LocalDateTime updatedAt;
    
    // 토큰 비활성화
    public void deactivate() {
        this.isActive = false;
    }
    
    // 토큰 재활성화
    public void reactivate() {
        this.isActive = true;
    }
    
    // 토큰 만료 여부 확인
    public boolean isExpired() {
        return LocalDateTime.now().isAfter(expiresAt);
    }
    
    // 토큰 유효성 확인
    public boolean isValid() {
        return isActive && !isExpired();
    }
} 