package com.spendwise.spendwise.controller;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;
import com.spendwise.spendwise.dto.*;
import com.spendwise.spendwise.service.AuthService;
@RestController @RequestMapping("/api/auth")
public class AuthController {
 private final AuthService service; public AuthController(AuthService service){this.service=service;}
 @PostMapping("/register") @ResponseStatus(HttpStatus.CREATED) public AuthResponse register(@Valid @RequestBody RegisterRequest request){return service.register(request);}
 @PostMapping("/login") public AuthResponse login(@Valid @RequestBody LoginRequest request){return service.login(request);}
 @GetMapping("/me") public UserResponse me(Authentication authentication){return service.me(authentication.getName());}
}
