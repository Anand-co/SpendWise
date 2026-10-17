package com.spendwise.spendwise.repository;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import com.spendwise.spendwise.entity.AppUser;
public interface AppUserRepository extends JpaRepository<AppUser, Long> {
    Optional<AppUser> findByEmailIgnoreCase(String email);
    boolean existsByEmailIgnoreCase(String email);
}
