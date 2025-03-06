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
    public UserDetails loadUserByEmail(String email);
    public boolean isTokenValid(String token, UserDetails userDetails);
    public AuthResponse refreshToken(String refreshToken);
    public boolean validateToken(String token);
    public String generateToken(Map<String, Object> extraClaims, User user);
    public AuthResponse authenticate(AuthRequest request);
    public String generateToken(User user);
    public String extractEmail(String token);
    public String generateRefreshToken(User user);
    public void logout(String token, String userId);
}
