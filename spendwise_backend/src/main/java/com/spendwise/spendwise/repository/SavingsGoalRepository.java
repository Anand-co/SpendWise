package com.spendwise.spendwise.repository;
import java.util.List; import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository; import com.spendwise.spendwise.entity.SavingsGoal;
public interface SavingsGoalRepository extends JpaRepository<SavingsGoal,Long> {
 List<SavingsGoal> findAllByUserIdOrderByCreatedAtDesc(Long userId);
 Optional<SavingsGoal> findByIdAndUserId(Long id, Long userId);
 boolean existsByIdAndUserId(Long id, Long userId);
}
