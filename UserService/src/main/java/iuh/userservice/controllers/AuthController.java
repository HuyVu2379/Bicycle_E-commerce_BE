package iuh.userservice.controllers;

import com.google.api.client.googleapis.auth.oauth2.GoogleAuthorizationCodeTokenRequest;
import com.google.api.client.googleapis.auth.oauth2.GoogleIdToken;
import com.google.api.client.googleapis.auth.oauth2.GoogleIdTokenVerifier;
import com.google.api.client.googleapis.auth.oauth2.GoogleTokenResponse;
import com.google.api.client.googleapis.javanet.GoogleNetHttpTransport;
import com.google.api.client.json.gson.GsonFactory;
import iuh.userservice.dtos.requests.AuthRequest;
import iuh.userservice.dtos.requests.GoogleLoginRequest;
import iuh.userservice.dtos.requests.RefreshTokenRequest;
import iuh.userservice.dtos.requests.RegisterRequest;
import iuh.userservice.dtos.responses.AuthResponse;
import iuh.userservice.dtos.responses.MessageResponse;
import iuh.userservice.dtos.responses.SuccessEntityResponse;
import iuh.userservice.entities.Address;
import iuh.userservice.entities.User;
import iuh.userservice.enums.Role;
import iuh.userservice.exception.errors.UnauthorizedException;
import iuh.userservice.mappers.UserMapper;
import iuh.userservice.repositories.TokenRepository;
import iuh.userservice.services.Impl.AuthenticationServiceImpl;
import iuh.userservice.services.Impl.RandomPhoneNumberGenerator;
import iuh.userservice.services.Impl.UserServiceImpl;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

import java.io.IOException;
import java.security.GeneralSecurityException;
import java.util.Optional;
import java.util.UUID;
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
    @Autowired
    private GoogleIdTokenVerifier googleIdTokenVerifier;
    @Autowired
    private UserMapper userMapper;

    @Value("${spring.security.oauth2.client.registration.google.client-id}")
    private String clientId;

    @Value("${spring.security.oauth2.client.registration.google.client-secret}")
    private String secretId;

    @Value("${spring.security.oauth2.client.registration.google.redirect-uri}")
    private String redirectUri;

    @PostMapping("/login")
    public ResponseEntity<MessageResponse<AuthResponse>> login(@RequestBody AuthRequest loginRequest) {
        AuthResponse authResponse = authenticationService.authenticate(loginRequest);
        System.out.println("Auth response: " + authResponse);
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

    @PostMapping("/google")
    public ResponseEntity<MessageResponse<?>> googleLogin(@RequestBody GoogleLoginRequest request,
                                                          @RequestHeader(value = "Authorization", required = false) String authorizationHeader) {
        System.out.println("Google login request: " + request);

        try {
            System.out.println("Authorization Header: " + authorizationHeader);
            String code = request.getToken();
            System.out.println("Authentication Code: " + code);

            if (code == null) {
                return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                        .body(new MessageResponse<>(HttpStatus.BAD_REQUEST.value(), "ID token is required", false, null));
            }

            // Chuyen tu Code sang Token
            GoogleTokenResponse tokenResponse = new GoogleAuthorizationCodeTokenRequest(
                    GoogleNetHttpTransport.newTrustedTransport(),
                    GsonFactory.getDefaultInstance(),
                    "https://oauth2.googleapis.com/token",
                    clientId,
                    secretId,
                    code,
                    redirectUri
            ).execute();

            System.out.println("Token response: " + tokenResponse.toString());

            String idToken = tokenResponse.getIdToken();

            System.out.println("ID Token: " + idToken);
            GoogleIdToken googleIdToken = googleIdTokenVerifier.verify(idToken);
            if (googleIdToken == null) {
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                        .body(new MessageResponse<>(HttpStatus.UNAUTHORIZED.value(), "Invalid ID token", false, null));
            }

            GoogleIdToken.Payload payload = googleIdToken.getPayload();
            String userId = payload.getSubject();
            String email = payload.get("email") != null ? payload.get("email").toString() : "";
            String fullName = payload.get("name") != null ? payload.get("name").toString() : "Google User";
            String avatar = payload.get("picture") != null ? payload.get("picture").toString() : null;

            Optional<User> existingUser = userService.findUserByEmail(email);
            User user;
            if (existingUser.isEmpty()) {
                Address address = new Address(
                        null,
                        null,
                        null,
                        null,
                        null,
                        null
                );
                String random = RandomPhoneNumberGenerator.generatePhoneNumber();
                RegisterRequest registerRequest = RegisterRequest.builder()
                        .email(email)
                        .fullName(fullName)
                        .avatar(avatar)
                        .phoneNumber(random)
                        .addressId(address.getAddressId())
                        .role(Role.USER)
                        .password(UUID.randomUUID().toString())
                        .googleId(userId)
                        .build();

                try {
                    Optional<User> userOptional = userService.registerUserForGoogle(registerRequest);
                    if (userOptional.isEmpty()) {
                        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                                .body(new MessageResponse<>(HttpStatus.INTERNAL_SERVER_ERROR.value(), "Failed to register Google user", false, null));
                    }
                    user = userOptional.get();
                    System.out.println("User registered: " + user);
                } catch (Exception e) {
                    System.out.println("Error registering user: " + e.getMessage());
                    return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                            .body(new MessageResponse<>(HttpStatus.INTERNAL_SERVER_ERROR.value(), "Failed to register Google user", false, null));
                }
            } else {
                user = existingUser.get();
                String random = RandomPhoneNumberGenerator.generatePhoneNumber();
                 Address address = new Address(
                        null,
                        null,
                        null,
                        null,
                        null,
                        null
                );
                user.setPhoneNumber(random);
                user.setAddressId(address.getAddressId());
                if(user.getGoogleId() == null || user.getGoogleId().isEmpty()){
                    user.setGoogleId(userId);
                }
                userService.updateUser(user);
                System.out.println("User already exists: " + user);
            }

            AuthResponse response = authenticationService.authenticateWithGoogle(email);
            tokenRepository.saveToken(user.getUserId(), response.getRefreshToken(), TimeUnit.DAYS.toMillis(7));

            System.out.println("Auth response after Google login: " + response);

            return ResponseEntity.ok(new MessageResponse<>(HttpStatus.OK.value(), "Login successfully", true, response));
        } catch (GeneralSecurityException e) {
            throw new RuntimeException(e);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }
}
