package com.spendwise.spendwise.entity;

import java.time.LocalDateTime;
import jakarta.persistence.*;

@Entity
@Table(name = "app_users", uniqueConstraints = @UniqueConstraint(columnNames = "email"))
public class AppUser {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    @Column(nullable = false, length = 100)
    private String name;
    @Column(nullable = false, unique = true, length = 190)
    private String email;
    @Column(nullable = false, length = 100)
    private String passwordHash;
    @Column(nullable = false, length = 20)
    private String role = "USER";
    @Column(nullable = false, updatable = false)
    private LocalDateTime createdAt;
    public AppUser() {}
    @PrePersist public void onCreate() { createdAt = LocalDateTime.now(); }
    public Long getId(){return id;} public String getName(){return name;} public String getEmail(){return email;}
    public String getPasswordHash(){return passwordHash;} public String getRole(){return role;} public LocalDateTime getCreatedAt(){return createdAt;}
    public void setName(String v){name=v;} public void setEmail(String v){email=v;} public void setPasswordHash(String v){passwordHash=v;} public void setRole(String v){role=v;}
}
