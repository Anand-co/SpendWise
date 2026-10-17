package com.spendwise.spendwise.controller;
import java.util.List; import jakarta.validation.Valid; import org.springframework.http.HttpStatus; import org.springframework.security.core.Authentication; import org.springframework.web.bind.annotation.*;
import com.spendwise.spendwise.dto.TransactionRequest; import com.spendwise.spendwise.entity.Transaction; import com.spendwise.spendwise.service.TransactionService;
@RestController @RequestMapping("/api/transactions") public class TransactionController {
 private final TransactionService service; public TransactionController(TransactionService service){this.service=service;}
 @GetMapping public List<Transaction> getAllTransactions(Authentication a){return service.getAllTransactions(a.getName());}
 @GetMapping("/{id}") public Transaction getTransactionById(@PathVariable Long id,Authentication a){return service.getTransactionById(id,a.getName());}
 @PostMapping @ResponseStatus(HttpStatus.CREATED) public Transaction createTransaction(@Valid @RequestBody TransactionRequest request,Authentication a){return service.createTransaction(request,a.getName());}
 @PutMapping("/{id}") public Transaction updateTransaction(@PathVariable Long id,@Valid @RequestBody TransactionRequest request,Authentication a){return service.updateTransaction(id,request,a.getName());}
 @DeleteMapping("/{id}") @ResponseStatus(HttpStatus.NO_CONTENT) public void deleteTransaction(@PathVariable Long id,Authentication a){service.deleteTransaction(id,a.getName());}
}
