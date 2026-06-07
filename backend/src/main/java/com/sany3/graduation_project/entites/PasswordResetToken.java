package com.sany3.graduation_project.entites;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;

import java.time.LocalDateTime;

@Entity
@Table(name = "password_reset_tokens")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@ToString(exclude = "user")
@EqualsAndHashCode(exclude = "user")
public class PasswordResetToken {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "user_id", nullable = false,
            foreignKey = @ForeignKey(name = "fk_password_reset_user"))
    private User user;

    @Column(nullable = false, length = 6)
    private String code;

    @Column(name = "reset_token", length = 255)
    private String resetToken;

    @Column(nullable = false)
    @Builder.Default
    private Boolean used = false;

    @Column(nullable = false)
    @Builder.Default
    private Integer attempts = 0;

    @Column(name = "expires_at", nullable = false)
    private LocalDateTime expiresAt;

    @CreationTimestamp
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    public boolean isExpired() {
        return LocalDateTime.now().isAfter(expiresAt);
    }

    public void incrementAttempts() {
        this.attempts++;
    }
}
