package com.spendwise.spendwise.service;
import org.springframework.http.HttpStatus;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;
import com.spendwise.spendwise.dto.*;
import com.spendwise.spendwise.entity.AppUser;
import com.spendwise.spendwise.repository.AppUserRepository;
import com.spendwise.spendwise.security.JwtService;
@Service
public class AuthService {
 private final AppUserRepository users; private final PasswordEncoder encoder; private final AuthenticationManager manager; private final UserDetailsService detailsService; private final JwtService jwt;
 public AuthService(AppUserRepository users, PasswordEncoder encoder, AuthenticationManager manager, UserDetailsService detailsService, JwtService jwt){this.users=users;this.encoder=encoder;this.manager=manager;this.detailsService=detailsService;this.jwt=jwt;}
 @Transactional public AuthResponse register(RegisterRequest request){String email=request.email().trim().toLowerCase();if(users.existsByEmailIgnoreCase(email))throw new ResponseStatusException(HttpStatus.CONFLICT,"Email is already registered");AppUser u=new AppUser();u.setName(request.name().trim());u.setEmail(email);u.setPasswordHash(encoder.encode(request.password()));u.setRole("USER");u=users.save(u);UserDetails details=detailsService.loadUserByUsername(u.getEmail());return response(u,jwt.generateToken(details));}
 public AuthResponse login(LoginRequest request){String email=request.email().trim().toLowerCase();try{manager.authenticate(new UsernamePasswordAuthenticationToken(email,request.password()));}catch(BadCredentialsException ex){throw new ResponseStatusException(HttpStatus.UNAUTHORIZED,"Invalid email or password");}AppUser u=users.findByEmailIgnoreCase(email).orElseThrow(()->new ResponseStatusException(HttpStatus.UNAUTHORIZED,"Invalid email or password"));return response(u,jwt.generateToken(detailsService.loadUserByUsername(email)));}
 public UserResponse me(String email){AppUser u=users.findByEmailIgnoreCase(email).orElseThrow(()->new ResponseStatusException(HttpStatus.NOT_FOUND,"User not found"));return new UserResponse(u.getId(),u.getName(),u.getEmail(),u.getCreatedAt());}
 private AuthResponse response(AppUser u,String token){return new AuthResponse(token,"Bearer",u.getId(),u.getName(),u.getEmail());}
}
