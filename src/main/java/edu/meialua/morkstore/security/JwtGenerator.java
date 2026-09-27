package edu.meialua.morkstore.security;

import edu.meialua.morkstore.adapters.in.User;
import edu.meialua.morkstore.adapters.in.repositories.UserRepository;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.ExpiredJwtException;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import io.jsonwebtoken.security.Keys;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.authentication.AuthenticationCredentialsNotFoundException;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Component;

import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.stream.Collectors;

@Component
public class JwtGenerator {

    private final UserRepository userRepository;
    private final SecretKey signingKey;
    private final long expirationMs;

    @Autowired
    public JwtGenerator(UserRepository userRepository,
                        @Value("${app.jwt.secret}") String secret,
                        @Value("${app.jwt.expiration-ms}") long expirationMs) {
        this.userRepository = userRepository;
        this.signingKey = Keys.hmacShaKeyFor(secret.getBytes(StandardCharsets.UTF_8));
        this.expirationMs = expirationMs;
    }

    public String generateToken(Authentication authentication) {
        String username = authentication.getName();
        Date now = new Date();
        Date expiry = new Date(now.getTime() + expirationMs);

        // 1) Busca sua entidade real no banco
        edu.meialua.morkstore.adapters.in.User domainUser =
                userRepository.findByUserName(username)
                        .orElseThrow(() -> new RuntimeException("Usuário não encontrado no DB"));

        // 2) Monta as claims
        Map<String, Object> claims = new HashMap<>();
        claims.put("id", domainUser.getId());
        claims.put("email", domainUser.getEmail());
        claims.put("telephone", domainUser.getTelephone());
        claims.put("address", domainUser.getAddress());
        claims.put("roles", authentication.getAuthorities()
                .stream()
                .map(GrantedAuthority::getAuthority)
                .collect(Collectors.toList()));

        // 3) Gera o token
        return Jwts.builder()
                .setClaims(claims)
                .setSubject(username)
                .setIssuedAt(now)
                .setExpiration(expiry)
                .signWith(signingKey, SignatureAlgorithm.HS256)
                .compact();
    }

    public String getUsernameFromJWT(String token) {
        Claims claims = Jwts
                .parser()
                .setSigningKey(signingKey)
                .build()
                .parseClaimsJws(token)
                .getBody();

        return claims.getSubject();
    }

    public boolean validadeToken(String token) {
        try {
            Jwts
                    .parser()
                    .setSigningKey(signingKey)
                    .build()
                    .parseClaimsJws(token);
            return true;
        } catch (ExpiredJwtException ex) {
            // Propagamos separadamente para que o filtro consiga distinguir
            // "token expirado" de "token inválido" e sinalizar isso ao front,
            // que usa essa distinção para deslogar o usuário automaticamente.
            throw ex;
        } catch (Exception ex) {
            throw new AuthenticationCredentialsNotFoundException("JWT inválido", ex);
        }
    }
}
