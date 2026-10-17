package com.spendwise.spendwise.service;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;
import com.spendwise.spendwise.repository.AppUserRepository;
@Service
public class CustomUserDetailsService implements UserDetailsService {
    private final AppUserRepository users;
    public CustomUserDetailsService(AppUserRepository users){this.users=users;}
    @Override public UserDetails loadUserByUsername(String email) throws UsernameNotFoundException {
        var u=users.findByEmailIgnoreCase(email).orElseThrow(()->new UsernameNotFoundException("User not found"));
        return User.withUsername(u.getEmail()).password(u.getPasswordHash()).roles(u.getRole()).build();
    }
}
