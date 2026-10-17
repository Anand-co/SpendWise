package com.spendwise.spendwise.service;
import java.math.BigDecimal; import java.time.LocalDate; import java.time.YearMonth; import java.util.List; import org.springframework.http.HttpStatus; import org.springframework.stereotype.Service; import org.springframework.web.server.ResponseStatusException;
import com.spendwise.spendwise.dto.BudgetRequest; import com.spendwise.spendwise.entity.Budget; import com.spendwise.spendwise.entity.TransactionType; import com.spendwise.spendwise.repository.AppUserRepository; import com.spendwise.spendwise.repository.BudgetRepository; import com.spendwise.spendwise.repository.TransactionRepository;
@Service public class BudgetService {
 private final BudgetRepository repository; private final AppUserRepository users; private final TransactionRepository transactions;
 public BudgetService(BudgetRepository repository,AppUserRepository users,TransactionRepository transactions){this.repository=repository;this.users=users;this.transactions=transactions;}
 private Long uid(String email){return users.findByEmailIgnoreCase(email).orElseThrow(()->new ResponseStatusException(HttpStatus.UNAUTHORIZED)).getId();}
 public List<Budget> getAll(String email){return repository.findAllByUserIdOrderByMonthStartDescCategoryAsc(uid(email));}
 public List<Budget> getByMonth(String month,String email){YearMonth ym;try{ym=YearMonth.parse(month);}catch(Exception e){throw new ResponseStatusException(HttpStatus.BAD_REQUEST,"Month must use yyyy-MM format");}return repository.findAllByUserIdAndMonthStartOrderByCategoryAsc(uid(email),ym.atDay(1));}
 public Budget create(BudgetRequest request,String email){Budget b=new Budget();b.setUserId(uid(email));apply(b,request);return repository.save(b);}
 public Budget update(Long id,BudgetRequest request,String email){Budget b=repository.findByIdAndUserId(id,uid(email)).orElseThrow(()->new ResponseStatusException(HttpStatus.NOT_FOUND,"Budget not found"));apply(b,request);return repository.save(b);}
 public void delete(Long id,String email){Long uid=uid(email);Budget b=repository.findByIdAndUserId(id,uid).orElseThrow(()->new ResponseStatusException(HttpStatus.NOT_FOUND,"Budget not found"));repository.delete(b);}
 public BigDecimal getSpent(Budget budget){LocalDate start=budget.getMonthStart(),end=start.plusMonths(1);return transactions.calculateCategoryTotal(budget.getUserId(),TransactionType.EXPENSE,budget.getCategory(),start,end);}
 private void apply(Budget b,BudgetRequest r){if(r.monthStart().getDayOfMonth()!=1)throw new ResponseStatusException(HttpStatus.BAD_REQUEST,"monthStart must be the first day of a month");b.setCategory(r.category().trim());b.setMonthlyLimit(r.monthlyLimit());b.setMonthStart(r.monthStart());}
}
