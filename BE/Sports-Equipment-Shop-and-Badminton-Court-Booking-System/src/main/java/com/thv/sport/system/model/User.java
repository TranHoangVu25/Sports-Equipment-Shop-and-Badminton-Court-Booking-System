package com.thv.sport.system.model;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;

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

    @Column(name = "gender")
    private String gender;

    @Column(name = "location")
    private String location;
}

