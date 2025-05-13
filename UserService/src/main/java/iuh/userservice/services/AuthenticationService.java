package iuh.userservice.services;

import iuh.userservice.dtos.requests.AuthRequest;
import iuh.userservice.dtos.responses.AuthResponse;
import iuh.userservice.entities.User;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.stereotype.Service;

import java.util.Map;

@Service
public interface AuthenticationService extends UserDetailsService {
    String extractUserId(String token);
    UserDetails loadUserByEmail(String email);
    boolean isTokenValid(String token, UserDetails userDetails);
    AuthResponse refreshToken(String refreshToken);
    boolean validateToken(String token);
    String generateToken(Map<String, Object> extraClaims, User user);
    AuthResponse authenticate(AuthRequest request);
    String generateToken(User user);
    String extractEmail(String token);
    String generateRefreshToken(User user);
    void logout(String token, String userId);
}
