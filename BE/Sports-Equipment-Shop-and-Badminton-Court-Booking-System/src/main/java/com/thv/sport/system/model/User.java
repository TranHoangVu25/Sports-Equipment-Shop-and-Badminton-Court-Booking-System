package com.thv.sport.system.model;

import jakarta.persistence.*;
import lombok.*;
import java.time.LocalDateTime;
import java.util.List;

@Entity
@Table(name = "\"user\"")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class User {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "user_id")
    private Long userId;

    @Column(name = "email", nullable = false)
    private String email;

    @Column(name = "full_name", nullable = false)
    private String fullName;

    @Column(name = "encrypted_password", nullable = false)
    private String encryptedPassword;

    @Column(name = "confirmation_token")
    private String confirmationToken;

    @Column(name = "confirmed_at")
    private LocalDateTime confirmedAt;

    @Column(name = "confirmation_sent_at")
    private LocalDateTime confirmationSentAt;

    @Column(name = "last_sign_in_at")
    private LocalDateTime lastSignInAt;

    @Column(name = "locked_at")
    private LocalDateTime lockedAt;

    @Column(name = "dob")
    private String dob;

    @Column(name = "jti")
    private String jti;

    @Column(name = "phone_number")
    private String phoneNumber;

    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    @Column(name = "confirm_forgot_token")
    private String confirmForgot;

    @Column(name = "confirm_forgot_expired")
    private LocalDateTime confirmForgotExpired;

    @Column(name = "created_at")
    private LocalDateTime createdAt;

    @Column(name = "role", nullable = false)
    private String role;

    // Relationship
    @OneToMany(mappedBy = "user", cascade = CascadeType.ALL, orphanRemoval = true)
//    @JsonManagedReference
    private List<Address> addresses;
}

