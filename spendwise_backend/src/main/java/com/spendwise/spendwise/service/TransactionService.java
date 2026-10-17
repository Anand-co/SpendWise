package com.spendwise.spendwise.service;
import java.util.List; import org.springframework.http.HttpStatus; import org.springframework.stereotype.Service; import org.springframework.transaction.annotation.Transactional; import org.springframework.web.server.ResponseStatusException;
import com.spendwise.spendwise.dto.TransactionRequest; import com.spendwise.spendwise.entity.Transaction; import com.spendwise.spendwise.repository.AppUserRepository; import com.spendwise.spendwise.repository.TransactionRepository;
@Service @Transactional public class TransactionService {
 private final TransactionRepository repository; private final AppUserRepository users;
 public TransactionService(TransactionRepository repository,AppUserRepository users){this.repository=repository;this.users=users;}
 private Long uid(String email){return users.findByEmailIgnoreCase(email).orElseThrow(()->new ResponseStatusException(HttpStatus.UNAUTHORIZED)).getId();}
 @Transactional(readOnly=true) public List<Transaction> getAllTransactions(String email){return repository.findAllByUserIdOrderByTransactionDateDesc(uid(email));}
 @Transactional(readOnly=true) public Transaction getTransactionById(Long id,String email){return repository.findByIdAndUserId(id,uid(email)).orElseThrow(()->new ResponseStatusException(HttpStatus.NOT_FOUND,"Transaction not found"));}
 public Transaction createTransaction(TransactionRequest request,String email){Transaction t=new Transaction();t.setUserId(uid(email));copyRequest(request,t);return repository.save(t);}
 public Transaction updateTransaction(Long id,TransactionRequest request,String email){Transaction t=getTransactionById(id,email);copyRequest(request,t);return repository.save(t);}
 public void deleteTransaction(Long id,String email){repository.delete(getTransactionById(id,email));}
 private void copyRequest(TransactionRequest r,Transaction t){t.setTitle(r.title().trim());t.setAmount(r.amount());t.setType(r.type());t.setCategory(r.category().trim());t.setTransactionDate(r.transactionDate());t.setDescription(r.description());}
}
