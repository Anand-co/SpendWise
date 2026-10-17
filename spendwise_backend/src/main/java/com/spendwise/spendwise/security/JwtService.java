package com.spendwise.spendwise.security;
import java.util.Date;
import javax.crypto.SecretKey;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.io.Decoders;
import io.jsonwebtoken.security.Keys;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Service;
@Service
public class JwtService {
    private final SecretKey key; private final long expirationMs;
    public JwtService(@Value("${spendwise.jwt.secret}") String secret, @Value("${spendwise.jwt.expiration-ms}") long expirationMs) {
        this.key=Keys.hmacShaKeyFor(Decoders.BASE64.decode(secret)); this.expirationMs=expirationMs;
    }
    public String generateToken(UserDetails user) {
        Date now=new Date();
        return Jwts.builder().subject(user.getUsername()).issuedAt(now).expiration(new Date(now.getTime()+expirationMs)).signWith(key).compact();
    }
    public String extractUsername(String token){return claims(token).getSubject();}
    public boolean isTokenValid(String token, UserDetails user){try{return user.getUsername().equals(extractUsername(token)) && claims(token).getExpiration().after(new Date());}catch(RuntimeException ex){return false;}}
    private Claims claims(String token){return Jwts.parser().verifyWith(key).build().parseSignedClaims(token).getPayload();}
}
