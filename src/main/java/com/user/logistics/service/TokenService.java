package com.user.logistics.service;



import com.user.logistics.entity.User;
import com.user.logistics.repository.UserRepository;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.ExpiredJwtException;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import jakarta.transaction.Transactional;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
//import org.springframework.security.core.userdetails.User;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;

@Service
public class TokenService {

    @Autowired
    private UserRepository userRepository;
//    private static final String SECRET_KEY = "Zxs07E2JjXAFfS8GPuCCyOvHEWDG5GujHhxCmvdrPmI";

    @Value("${security.jwt.secret}")
    private String SECRET_KEY;
    private static final long EXPIRATION_TIME = 86400000L; // 1 day
    private final Map<String, String> resetTokens = new ConcurrentHashMap<>();
    private final Map<String, Date> invalidatedTokens = new ConcurrentHashMap<>();

    public String generateToken(String email) {
        Date now = new Date();
        Date expiryDate = new Date(now.getTime() + EXPIRATION_TIME);
        return Jwts.builder()
                .setSubject(email)
                .setIssuedAt(now)
                .setExpiration(expiryDate)
                .signWith(SignatureAlgorithm.HS256, SECRET_KEY)
                .compact();
    }

//    public boolean validateToken(String token, User userDetails) {
//        final String email = getEmailFromToken(token);
//        return (email.equals(userDetails.getEmail()) && !isTokenExpired(token) && !isTokenInvalidated(token));
//    }

    public String validateTokenAndGetSubject(String token) {
        return Jwts.parser()
                .setSigningKey(SECRET_KEY)
                .parseClaimsJws(token)
                .getBody()
                .getSubject();
    }


    public Authentication validateTokenAndGetAuthentication(String token) {
        try {
            Claims claims = Jwts.parser()
                    .setSigningKey(SECRET_KEY)
                    .parseClaimsJws(token)
                    .getBody();

            String username = claims.getSubject();
            List<String> roles = (List<String>) claims.get("roles");


            List<SimpleGrantedAuthority> authorities = roles.stream()
                    .map(SimpleGrantedAuthority::new)
                    .collect(Collectors.toList());


            return new UsernamePasswordAuthenticationToken(username, null, authorities);

        } catch (ExpiredJwtException e) {
            throw new RuntimeException("Token has expired");
        } catch (Exception e) {
            throw new RuntimeException("Error parsing JWT token");
        }
    }

//    public String getEmailFromToken(String token) {
//        return Jwts.parser()
//                .setSigningKey(SECRET_KEY)
//                .parseClaimsJws(token)
//                .getBody()
//                .getSubject();
//    }

    private boolean isTokenExpired(String token) {
        final Date expiration = Jwts.parser()
                .setSigningKey(SECRET_KEY)
                .parseClaimsJws(token)
                .getBody()
                .getExpiration();
        return expiration.before(new Date());
    }
//    public void invalidateToken(String token) {
//        if (token != null) {
//            Date expiration = Jwts.parser()
//                    .setSigningKey(SECRET_KEY)
//                    .parseClaimsJws(token)
//                    .getBody()
//                    .getExpiration();
//            invalidatedTokens.put(token, expiration);
//        }
//    }
//    public boolean isTokenInvalidated(String token) {
//        return invalidatedTokens.containsKey(token);
//    }
//
//    // Get active token for a user
//    public String getActiveTokenForUser(Long userId) {
//        return userActiveTokens.get(userId);
//    }
//
//    // Set active token for a user
//    public void setActiveTokenForUser(Long userId, String token) {
//        userActiveTokens.put(userId, token);
//    }
//    public void removeActiveTokenForUser(Long userId) {
//        userActiveTokens.remove(userId);
//    }


    public String generateToken(String email, List<String> roles) {
        Date now = new Date();
        Date expiryDate = new Date(now.getTime() + EXPIRATION_TIME);

        return Jwts.builder()
                .setSubject(email)
                .claim("roles", roles)
                .setIssuedAt(now)
                .setExpiration(expiryDate)
                .signWith(SignatureAlgorithm.HS256, SECRET_KEY)
                .compact();
    }

    // Generate Password Reset Token
//    public String generateResetToken(String email) {
//        String token = UUID.randomUUID().toString();
//        resetTokens.put(token, email); // Map reset token to email
//        return token;
//    }

    // Validate Reset Token
//    public String validateResetToken(String token) {
//        if (!resetTokens.containsKey(token)) {
//            throw new RuntimeException("Invalid or expired token");
//        }
//        return resetTokens.get(token);
//    }

//    public boolean validateResetToken(String token) {
//        return resetTokens.containsKey(token);
//    }
//
//    // Invalidate Reset Token
//    public void invalidateResetToken(String token) {
//        resetTokens.remove(token);
//    }


    // Validate JWT Token
    public boolean validateToken(String token) {
        try {
            if (invalidatedTokens.containsKey(token)) {
                throw new RuntimeException("Token has been invalidated.");
            }
            Jwts.parser().setSigningKey(SECRET_KEY).parseClaimsJws(token);
            return true;
        } catch (ExpiredJwtException e) {
            throw new RuntimeException("Token has expired.");
        } catch (Exception e) {
            throw new RuntimeException("Invalid token.");
        }
    }

//    public String validateTokenAndGetSubject(String token) {
//        return Jwts.parser()
//                .setSigningKey(SECRET_KEY)
//                .parseClaimsJws(token)
//                .getBody()
//                .getSubject();
//    }

//    public Authentication validateTokenAndGetAuthentication(String token) {
//        Claims claims = Jwts.parser()
//                .setSigningKey(SECRET_KEY)
//                .parseClaimsJws(token)
//                .getBody();
//
//        String username = claims.getSubject();
//        List<String> roles = (List<String>) claims.get("roles");
//
//        List<SimpleGrantedAuthority> authorities = roles.stream()
//                .map(SimpleGrantedAuthority::new)
//                .collect(Collectors.toList());
//
//        return new UsernamePasswordAuthenticationToken(username, null, authorities);
//    }

    public void invalidateToken(String token) {
        Date expiration = Jwts.parser()
                .setSigningKey(SECRET_KEY)
                .parseClaimsJws(token)
                .getBody()
                .getExpiration();
        invalidatedTokens.put(token, expiration);
    }


    // Generate a unique reset token with expiration time
    @Transactional
    public String generateResetToken(String email) {
        String resetToken = UUID.randomUUID().toString();
        LocalDateTime expiry = LocalDateTime.now().plusHours(1);

        userRepository.saveResetToken(email, resetToken, expiry);
        return resetToken;
    }

    // Validate the reset token
    public boolean validateResetToken(String token) {
        User user = userRepository.findByResetToken(token);
        return user != null && user.getTokenExpiry().isAfter(LocalDateTime.now());
    }

    // Retrieve email from the token
    public String getEmailFromToken(String token) {
        User user = userRepository.findByResetToken(token);
        return (user != null) ? user.getEmail() : null;
    }

    // Invalidate the used reset token
    public void invalidateResetToken(String token) {
        userRepository.invalidateResetToken(token);
    }


}
