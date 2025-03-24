package iuh.userservice.controllers;

import iuh.userservice.dtos.requests.AuthRequest;
import iuh.userservice.dtos.requests.RefreshTokenRequest;
import iuh.userservice.dtos.requests.RegisterRequest;
import iuh.userservice.dtos.responses.AuthResponse;
import iuh.userservice.dtos.responses.MessageResponse;
import iuh.userservice.dtos.responses.SuccessEntityResponse;
import iuh.userservice.entities.User;
import iuh.userservice.exception.errors.UnauthorizedException;
import iuh.userservice.mappers.UserMapper;
import iuh.userservice.repositories.TokenRepository;
import iuh.userservice.services.Impl.AuthenticationServiceImpl;
import iuh.userservice.services.Impl.UserServiceImpl;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

import java.util.Date;
import java.util.concurrent.TimeUnit;

@RestController
@RequestMapping("/api/v1/auth")
public class AuthController {
    @Autowired
    private AuthenticationServiceImpl authenticationService;
    @Autowired
    private UserServiceImpl userService;
    @Autowired
    private TokenRepository tokenRepository;

    @PostMapping("/login")
    public ResponseEntity<MessageResponse<AuthResponse>> login(@RequestBody AuthRequest loginRequest) {
        AuthResponse authResponse = authenticationService.authenticate(loginRequest);
        tokenRepository.saveToken(authResponse.getUserId(), authResponse.getRefreshToken(), TimeUnit.DAYS.toMillis(7));
        return SuccessEntityResponse.ok("Login successfully", authResponse);
    }

    @PostMapping("/logout")
    public ResponseEntity<MessageResponse<Void>> logout(HttpServletRequest request,
                                                        @AuthenticationPrincipal UserDetails userDetails) {
        String authHeader = request.getHeader("Authorization");
        if (authHeader != null && authHeader.startsWith("Bearer ")) {
            String jwt = authHeader.substring(7);
            User user = userService.findUserByEmail(userDetails.getUsername())
                    .orElseThrow(() -> new UnauthorizedException("User not found"));
            tokenRepository.deleteToken(user.getUserId());
            authenticationService.logout(jwt, user.getUserId());
            return SuccessEntityResponse.ok("Logged out successfully", null);
        }
        return ResponseEntity.badRequest()
                .body(new MessageResponse<>(HttpStatus.BAD_REQUEST.value(), "Invalid token", false, null));
    }

    @PostMapping("/refresh-token")
    public ResponseEntity<MessageResponse<AuthResponse>> refreshToken(@RequestBody RefreshTokenRequest authRefreshToken) {
        try {
            String userId = authenticationService.extractUserId(authRefreshToken.getRefreshToken());
            System.out.println("User ID: " + userId);
            String storedRefreshToken = tokenRepository.getToken(userId);
            System.out.println("Stored refresh token: " + storedRefreshToken);
            if (storedRefreshToken == null
                    || !storedRefreshToken.equals(authRefreshToken.getRefreshToken())
                    || !authenticationService.validateToken(authRefreshToken.getRefreshToken())) {
                throw new UnauthorizedException("Invalid refresh token");
            }
            AuthResponse authResponse = authenticationService.refreshToken(authRefreshToken.getRefreshToken());
            String newRefreshToken = authResponse.getRefreshToken();
            tokenRepository.saveToken(userId, newRefreshToken, TimeUnit.DAYS.toMillis(7));
            return SuccessEntityResponse.ok("Refresh token successfully", authResponse);
        } catch (Exception e) {
            throw e;
        }
    }

    @GetMapping("/validate-token")
    public ResponseEntity<MessageResponse<Boolean>> validateToken(@RequestParam String token) {
        try {
            boolean isValid = authenticationService.validateToken(token);
            if (isValid) {
                return SuccessEntityResponse.ok("Token is valid", isValid);
            } else {
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                        .body(new MessageResponse<>(HttpStatus.UNAUTHORIZED.value(), "Token is invalid", false, null));
            }
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(new MessageResponse<>(HttpStatus.UNAUTHORIZED.value(), "Token is invalid", false, null));
        }
    }
}
