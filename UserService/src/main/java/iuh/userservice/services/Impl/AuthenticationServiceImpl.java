package iuh.userservice.services.Impl;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import io.jsonwebtoken.io.Decoders;
import io.jsonwebtoken.security.Keys;
import iuh.userservice.dtos.requests.AuthRequest;
import iuh.userservice.dtos.responses.AuthResponse;
import iuh.userservice.entities.User;
import iuh.userservice.exception.errors.UnauthorizedException;
import iuh.userservice.exception.errors.UserNotFoundException;
import iuh.userservice.repositories.TokenRepository;
import iuh.userservice.repositories.UserRepository;
import iuh.userservice.services.AuthenticationService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.security.Key;
import java.util.Collection;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.function.Function;

@Service
@RequiredArgsConstructor
@Slf4j
public class AuthenticationServiceImpl implements AuthenticationService {
    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final TokenRepository tokenRepository;
    @Value("${jwt.secretKey}")
    private String secretKey;

    @Value("${jwt.expiration}")
    private long jwtExpiration;

    @Value("${jwt.refresh-token.expiration}")
    private long refreshExpiration;

    @Override
    public AuthResponse authenticate(AuthRequest request) {
        User user = userRepository.findByEmail(request.getEmail())
                .orElseThrow(() -> new UnauthorizedException("User not found with email: " + request.getEmail()));

        if (!passwordEncoder.matches(request.getPassword(), user.getPassword())) {
            throw new UnauthorizedException("Invalid password");
        }
        String accessToken = generateToken(user);
        String refreshToken = generateRefreshToken(user);

        return AuthResponse.builder()
                .accessToken(accessToken)
                .refreshToken(refreshToken)
                .userId(user.getUserId())
                .email(user.getEmail())
                .role(user.getRole())
                .build();
    }

    @Override
    public AuthResponse authenticateWithGoogle(String email) {
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new UnauthorizedException("User not found with email: " + email));

        String accessToken = generateToken(user);
        String refreshToken = generateRefreshToken(user);

        return AuthResponse.builder()
                .accessToken(accessToken)
                .refreshToken(refreshToken)
                .userId(user.getUserId())
                .email(user.getEmail())
                .role(user.getRole())
                .build();
    }

    @Override
    public String generateToken(User user) {
        return generateToken(new HashMap<>(), user);
    }

    @Override
    public String generateToken(
            Map<String, Object> extraClaims,
            User user
    ) {
        return buildToken(extraClaims, user, jwtExpiration);
    }

    @Override
    public String generateRefreshToken(User user) {
        return buildToken(new HashMap<>(), user, refreshExpiration);
    }

    @Override
    public AuthResponse refreshToken(String refreshToken) {
        if (tokenRepository.isTokenBlacklisted(refreshToken)) {
            throw new UnauthorizedException("Invalid refresh token");
        }
        String userEmail;
        try {
            userEmail = extractEmail(refreshToken);
        } catch (Exception e) {
            throw new UnauthorizedException("Invalid refresh token");
        }
        User user = userRepository.findByEmail(userEmail)
                .orElseThrow(() -> new UnauthorizedException("User not found with email: " + userEmail));
        if (!isTokenValid(refreshToken, loadUserByEmail(userEmail))) {
            throw new UnauthorizedException("Invalid refresh token");
        }
        Date expiration = extractExpiration(refreshToken);
        long ttl = expiration.getTime() - System.currentTimeMillis();
        if (ttl > 0) {
            tokenRepository.blacklistToken(refreshToken, ttl);
        }
        String accessToken = generateToken(user);
        String newRefreshToken = generateRefreshToken(user);
        tokenRepository.saveToken(user.getUserId(), accessToken, jwtExpiration);
        tokenRepository.saveToken(user.getUserId() + ":refresh", newRefreshToken, refreshExpiration);
        return AuthResponse.builder()
                .accessToken(accessToken)
                .refreshToken(newRefreshToken)
                .userId(user.getUserId())
                .email(user.getEmail())
                .role(user.getRole())
                .build();
    }

    @Override
    public boolean validateToken(String token) {
        if (tokenRepository.isTokenBlacklisted(token)) {
            return false;
        }
        try {
            String userEmail = extractEmail(token);
            UserDetails userDetails = loadUserByEmail(userEmail);
            return isTokenValid(token, userDetails);
        } catch (Exception e) {
            return false;
        }
    }

    private String buildToken(
            Map<String, Object> extraClaims,
            User user,
            long expiration
    ) {
        return Jwts
                .builder()
                .setClaims(extraClaims)
                .setSubject(user.getEmail())
                .setIssuedAt(new Date(System.currentTimeMillis()))
                .setExpiration(new Date(System.currentTimeMillis() + expiration))
                .claim("userId", user.getUserId())
                .claim("role", user.getRole().name())
                .signWith(getSignInKey(), SignatureAlgorithm.HS256)
                .compact();
    }

    @Override
    public boolean isTokenValid(String token, UserDetails userDetails) {
        if (tokenRepository.isTokenBlacklisted(token)) {
            return false;
        }
        final String username = extractEmail(token);
        return (username.equals(userDetails.getUsername())) && !isTokenExpired(token);
    }

    @Override
    public void logout(String token, String userId) {
        Date expiration = extractExpiration(token);
        long ttl = expiration.getTime() - System.currentTimeMillis();
        if (ttl > 0) {
            tokenRepository.blacklistToken(token, ttl);
        }
        tokenRepository.deleteToken(userId);
        tokenRepository.deleteToken(userId + ":refresh");
    }

    @Override
    public String extractUserId(String token) {
        return extractClaim(token, claims -> claims.get("userId", String.class));
    }

    @Override
    public String extractEmail(String token) {
        return extractClaim(token, Claims::getSubject);
    }

    public <T> T extractClaim(String token, Function<Claims, T> claimsResolver) {
        final Claims claims = extractAllClaims(token);
        return claimsResolver.apply(claims);
    }


    private boolean isTokenExpired(String token) {
        return extractExpiration(token).before(new Date());
    }

    private Date extractExpiration(String token) {
        return extractClaim(token, Claims::getExpiration);
    }


    private Claims extractAllClaims(String token) {
        return Jwts
                .parserBuilder()
                .setSigningKey(getSignInKey())
                .build()
                .parseClaimsJws(token)
                .getBody();
    }

    private Key getSignInKey() {
        byte[] keyBytes = Decoders.BASE64.decode(secretKey);
        return Keys.hmacShaKeyFor(keyBytes);
    }

    @Override
    public UserDetails loadUserByEmail(String email) throws UserNotFoundException {
        Optional<User> userOptional = userRepository.findByEmail(email);

        if (userOptional.isEmpty()) {
            throw new UsernameNotFoundException("User not found with email: " + email);
        }

        User user = userOptional.get();

        return new org.springframework.security.core.userdetails.User(
                user.getEmail(),
                user.getPassword(),
                getAuthorities(user)
        );
    }

    private Collection<? extends GrantedAuthority> getAuthorities(User user) {
        return Collections.singletonList(new SimpleGrantedAuthority("ROLE_" + user.getRole().name()));
    }

    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        return loadUserByEmail(username);
    }
}