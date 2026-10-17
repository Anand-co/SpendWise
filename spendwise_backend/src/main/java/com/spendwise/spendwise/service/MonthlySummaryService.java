package com.spendwise.spendwise.service;
import java.math.BigDecimal; import java.time.LocalDate; import java.time.YearMonth; import org.springframework.http.HttpStatus; import org.springframework.stereotype.Service; import org.springframework.transaction.annotation.Transactional; import org.springframework.web.server.ResponseStatusException;
import com.spendwise.spendwise.dto.MonthlySummaryResponse; import com.spendwise.spendwise.entity.TransactionType; import com.spendwise.spendwise.repository.AppUserRepository; import com.spendwise.spendwise.repository.TransactionRepository;
@Service public class MonthlySummaryService {
 private final TransactionRepository repository; private final AppUserRepository users;
 public MonthlySummaryService(TransactionRepository repository,AppUserRepository users){this.repository=repository;this.users=users;}
 @Transactional(readOnly=true) public MonthlySummaryResponse getMonthlySummary(YearMonth month,String email){Long uid=users.findByEmailIgnoreCase(email).orElseThrow(()->new ResponseStatusException(HttpStatus.UNAUTHORIZED)).getId();LocalDate start=month.atDay(1),end=month.plusMonths(1).atDay(1);BigDecimal income=repository.calculateTotal(uid,TransactionType.INCOME,start,end);BigDecimal expenses=repository.calculateTotal(uid,TransactionType.EXPENSE,start,end);return new MonthlySummaryResponse(month,income,expenses,income.subtract(expenses));}
}
