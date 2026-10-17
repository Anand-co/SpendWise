package com.spendwise.spendwise.repository;
import java.math.BigDecimal; import java.time.LocalDate; import java.util.List; import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository; import org.springframework.data.jpa.repository.Query; import org.springframework.data.repository.query.Param;
import com.spendwise.spendwise.entity.Transaction; import com.spendwise.spendwise.entity.TransactionType;
public interface TransactionRepository extends JpaRepository<Transaction,Long> {
 List<Transaction> findAllByUserIdOrderByTransactionDateDesc(Long userId);
 Optional<Transaction> findByIdAndUserId(Long id, Long userId);
 @Query("SELECT COALESCE(SUM(t.amount), 0) FROM Transaction t WHERE t.userId = :userId AND t.type = :type AND t.transactionDate >= :startDate AND t.transactionDate < :endDate")
 BigDecimal calculateTotal(@Param("userId") Long userId,@Param("type") TransactionType type,@Param("startDate") LocalDate startDate,@Param("endDate") LocalDate endDate);
 @Query("SELECT COALESCE(SUM(t.amount), 0) FROM Transaction t WHERE t.userId = :userId AND t.type = :type AND LOWER(t.category) = LOWER(:category) AND t.transactionDate >= :startDate AND t.transactionDate < :endDate")
 BigDecimal calculateCategoryTotal(@Param("userId") Long userId,@Param("type") TransactionType type,@Param("category") String category,@Param("startDate") LocalDate startDate,@Param("endDate") LocalDate endDate);
}
