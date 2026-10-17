package com.spendwise.spendwise.service;
import java.math.BigDecimal; import java.math.RoundingMode; import java.util.List; import org.springframework.http.HttpStatus; import org.springframework.stereotype.Service; import org.springframework.web.server.ResponseStatusException;
import com.spendwise.spendwise.dto.SavingsGoalRequest; import com.spendwise.spendwise.entity.SavingsGoal; import com.spendwise.spendwise.repository.AppUserRepository; import com.spendwise.spendwise.repository.SavingsGoalRepository;
@Service public class SavingsGoalService {private final SavingsGoalRepository repository;private final AppUserRepository users;public SavingsGoalService(SavingsGoalRepository repository,AppUserRepository users){this.repository=repository;this.users=users;}
 private Long uid(String email){return users.findByEmailIgnoreCase(email).orElseThrow(()->new ResponseStatusException(HttpStatus.UNAUTHORIZED)).getId();}
 public List<SavingsGoal> getAll(String email){return repository.findAllByUserIdOrderByCreatedAtDesc(uid(email));}
 public SavingsGoal getById(Long id,String email){return repository.findByIdAndUserId(id,uid(email)).orElseThrow(()->new ResponseStatusException(HttpStatus.NOT_FOUND,"Savings goal not found"));}
 public SavingsGoal create(SavingsGoalRequest r,String email){SavingsGoal g=new SavingsGoal();g.setUserId(uid(email));apply(g,r);return repository.save(g);}
 public SavingsGoal update(Long id,SavingsGoalRequest r,String email){SavingsGoal g=getById(id,email);apply(g,r);return repository.save(g);}
 public void delete(Long id,String email){repository.delete(getById(id,email));}
 public BigDecimal getProgressPercent(SavingsGoal g){if(g.getTargetAmount()==null||g.getTargetAmount().signum()==0)return BigDecimal.ZERO;return g.getCurrentAmount().multiply(BigDecimal.valueOf(100)).divide(g.getTargetAmount(),2,RoundingMode.HALF_UP);}
 private void apply(SavingsGoal g,SavingsGoalRequest r){if(r.currentAmount().compareTo(r.targetAmount())>0)throw new ResponseStatusException(HttpStatus.BAD_REQUEST,"currentAmount cannot exceed targetAmount");g.setName(r.name().trim());g.setTargetAmount(r.targetAmount());g.setCurrentAmount(r.currentAmount());g.setDeadline(r.deadline());g.setDescription(r.description());}
}
