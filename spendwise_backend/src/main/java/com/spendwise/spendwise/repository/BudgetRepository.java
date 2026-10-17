package com.spendwise.spendwise.repository;
import java.time.LocalDate; import java.util.List; import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository; import com.spendwise.spendwise.entity.Budget;
public interface BudgetRepository extends JpaRepository<Budget,Long> {
 List<Budget> findAllByUserIdOrderByMonthStartDescCategoryAsc(Long userId);
 List<Budget> findAllByUserIdAndMonthStartOrderByCategoryAsc(Long userId, LocalDate monthStart);
 Optional<Budget> findByIdAndUserId(Long id, Long userId);
 boolean existsByIdAndUserId(Long id, Long userId);
}
