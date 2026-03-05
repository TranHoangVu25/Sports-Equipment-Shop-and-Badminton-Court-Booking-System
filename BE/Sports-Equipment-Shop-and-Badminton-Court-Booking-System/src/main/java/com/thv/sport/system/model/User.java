package com.thv.sport.system.model;

import jakarta.persistence.*;
import lombok.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "\"user\"")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class User {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "user_id")
    private Long userId;

    @Column(name = "email", length = 255, nullable = false)
    private String email;

    @Column(name = "full_name", length = 255, nullable = false)
    private String fullName;

    @Column(name = "encrypted_password", length = 255, nullable = false)
    private String encryptedPassword;

    @Column(name = "confirmation_token", length = 255)
    private String confirmationToken;

    @Column(name = "confirmed_at")
    private LocalDateTime confirmedAt;

    @Column(name = "confirmation_sent_at")
    private LocalDateTime confirmationSentAt;

    @Column(name = "last_sign_in_at")
    private LocalDateTime lastSignInAt;

    @Column(name = "dob", length = 255)
    private String dob;

    @Column(name = "jti", length = 255)
    private String jti;

    @Column(name = "phone_number", length = 255)
    private String phoneNumber;

    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "role_id", nullable = false)
    private Role role;
}

