package com.spendwise.spendwise.controller;
import java.math.BigDecimal; import java.util.List; import java.util.Map; import jakarta.validation.Valid; import org.springframework.http.HttpStatus; import org.springframework.security.core.Authentication; import org.springframework.web.bind.annotation.*; import com.spendwise.spendwise.dto.SavingsGoalRequest; import com.spendwise.spendwise.entity.SavingsGoal; import com.spendwise.spendwise.service.SavingsGoalService;
@RestController @RequestMapping("/api/savings-goals") public class SavingsGoalController {private final SavingsGoalService service;public SavingsGoalController(SavingsGoalService service){this.service=service;}
 @GetMapping public List<SavingsGoal> getAll(Authentication a){return service.getAll(a.getName());}
 @GetMapping("/{id}") public SavingsGoal getById(@PathVariable Long id,Authentication a){return service.getById(id,a.getName());}
 @GetMapping("/progress") public List<Map<String,Object>> progress(Authentication a){return service.getAll(a.getName()).stream().map(g->{BigDecimal pct=service.getProgressPercent(g);return Map.<String,Object>of("id",g.getId(),"name",g.getName(),"targetAmount",g.getTargetAmount(),"currentAmount",g.getCurrentAmount(),"remaining",g.getTargetAmount().subtract(g.getCurrentAmount()),"progressPercentage",pct,"completed",g.getCurrentAmount().compareTo(g.getTargetAmount())>=0,"deadline",g.getDeadline()==null?"":g.getDeadline());}).toList();}
 @PostMapping @ResponseStatus(HttpStatus.CREATED) public SavingsGoal create(@Valid @RequestBody SavingsGoalRequest request,Authentication a){return service.create(request,a.getName());}
 @PutMapping("/{id}") public SavingsGoal update(@PathVariable Long id,@Valid @RequestBody SavingsGoalRequest request,Authentication a){return service.update(id,request,a.getName());}
 @DeleteMapping("/{id}") @ResponseStatus(HttpStatus.NO_CONTENT) public void delete(@PathVariable Long id,Authentication a){service.delete(id,a.getName());}
}
