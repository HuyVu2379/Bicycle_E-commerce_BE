package iuh.userservice.services.Impl;

import iuh.userservice.dtos.requests.RegisterRequest;
import iuh.userservice.entities.User;
import iuh.userservice.enums.Role;
import iuh.userservice.exception.errors.DuplicateUserException;
import iuh.userservice.repositories.UserRepository;
import iuh.userservice.services.UserService;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import java.util.Optional;
@Service
public class UserServiceImpl implements UserService {
    private UserRepository userService;
    private PasswordEncoder passwordEncoder =  new BCryptPasswordEncoder();
    public UserServiceImpl(UserRepository userService) {
        this.userService = userService;
    }

    @Override
    public Optional<User> findUserByEmail(String email) {
        return userService.findByEmail(email);
    }

    @Override
    public Optional<User> registerUser(RegisterRequest registerRequest) {
        if (userService.existsByEmail(registerRequest.getEmail())) {
            throw new DuplicateUserException("Email already exists");
        }
        if (userService.existsByPhoneNumber(registerRequest.getPhoneNumber())) {
            throw new DuplicateUserException("Phone number already exists");
        }
        Role role = registerRequest.getRole() != null ? registerRequest.getRole() : Role.USER;
        User user = User.builder()
                .email(registerRequest.getEmail())
                .phoneNumber(registerRequest.getPhoneNumber())
                .password(passwordEncoder.encode(registerRequest.getPassword()))
                .role(role)
                .fullName(registerRequest.getFullName())
                .gender(registerRequest.getGender())
                .addressId(registerRequest.getAddressId())
                .avatar(registerRequest.getAvatar())
                .dob(registerRequest.getDob())
                .build();
        return Optional.of(userService.save(user));
    }
}
