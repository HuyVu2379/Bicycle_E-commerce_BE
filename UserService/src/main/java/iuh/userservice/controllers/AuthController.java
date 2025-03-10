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
import iuh.userservice.services.Impl.AuthenticationServiceImpl;
import iuh.userservice.services.Impl.UserServiceImpl;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/users/auth")
public class AuthController {
    @Autowired
    private AuthenticationServiceImpl authenticationService;
    @Autowired
    private UserMapper userMapper;
    @Autowired
    private UserServiceImpl userService;

    @PostMapping("/login")
    public ResponseEntity<MessageResponse<AuthResponse>> login(@RequestBody AuthRequest loginRequest) {
        AuthResponse authResponse = authenticationService.authenticate(loginRequest);
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
            authenticationService.logout(jwt, user.getUserId());
            return SuccessEntityResponse.ok("Logged out successfully", null);
        }
        return ResponseEntity.badRequest()
                .body(new MessageResponse<>(HttpStatus.BAD_REQUEST.value(), "Invalid token", false, null));
    }

    @PostMapping("/refresh-token")
    public ResponseEntity<MessageResponse<AuthResponse>> refreshToken(@RequestBody RefreshTokenRequest authRefreshToken) {
        try {
            AuthResponse authResponse = authenticationService.refreshToken(authRefreshToken.getRefreshToken());
            return SuccessEntityResponse.ok("Refresh token successfully", authResponse);
        } catch (Exception e) {
            return ResponseEntity.badRequest()
                    .body(new MessageResponse<>(HttpStatus.BAD_REQUEST.value(), "Invalid token", false, null));
        }
    }

    @GetMapping("/validate-token")
    public ResponseEntity<MessageResponse<Boolean>> validateToken(@RequestParam String token) {
        try {
            boolean isValid = authenticationService.validateToken(token);
            if(isValid){
                return SuccessEntityResponse.ok("Token is valid", isValid);
            }
            else {
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                        .body(new MessageResponse<>(HttpStatus.UNAUTHORIZED.value(), "Token is invalid", false, null));
            }
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(new MessageResponse<>(HttpStatus.UNAUTHORIZED.value(), "Token is invalid", false, null));
        }
    }
}
