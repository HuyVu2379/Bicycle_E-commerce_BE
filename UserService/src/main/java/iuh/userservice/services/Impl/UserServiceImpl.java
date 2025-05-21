package iuh.userservice.services.Impl;

import iuh.userservice.dtos.requests.RegisterRequest;
import iuh.userservice.dtos.requests.UpdateAvatarRequest;
import iuh.userservice.entities.User;
import iuh.userservice.enums.Role;
import iuh.userservice.exception.errors.DuplicateUserException;
import iuh.userservice.repositories.UserRepository;
import iuh.userservice.services.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.Optional;

@Service
@RequiredArgsConstructor
public class UserServiceImpl implements UserService {
    private UserRepository userService;
    private PasswordEncoder passwordEncoder;

    @Autowired
    public UserServiceImpl(UserRepository userService, PasswordEncoder passwordEncoder) {
        this.userService = userService;
        this.passwordEncoder = passwordEncoder;
    }

    @Override
    public Optional<User> findUserByEmail(String email) {
        return userService.findByEmail(email);
    }

    @Override
    public Optional<User> findUserById(String id) {
        return userService.findById(id);
    }

    @Override
    public Optional<User> registerUser(RegisterRequest registerRequest) {
        if (userService.existsByEmail(registerRequest.getEmail())) {
            throw new DuplicateUserException("Email already exists");
        }
        if (userService.existsUserByPhoneNumber(registerRequest.getPhoneNumber()) >= 1) {
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

    @Override
    public Optional<User> registerUserForGoogle(RegisterRequest registerRequest) {
        System.out.println("Bắt đầu lưu user Google: " + registerRequest.getEmail());
        if (userService.existsByEmail(registerRequest.getEmail())) {
            return userService.findByEmail(registerRequest.getEmail());
        }
        Role role = registerRequest.getRole() != null ? registerRequest.getRole() : Role.USER;
        User user = User.builder()
                .email(registerRequest.getEmail())
                .phoneNumber(registerRequest.getPhoneNumber())
                .password(registerRequest.getPassword())
                .role(role)
                .fullName(registerRequest.getFullName())
                .gender(null)
                .addressId(registerRequest.getAddressId())
                .avatar(registerRequest.getAvatar())
                .dob(null)
                .build();

        User savedUser = userService.save(user);
        System.out.println("User Google đã lưu xong: " + savedUser);
        return Optional.of(savedUser);
    }

    @Override
    public Optional<User> updateUser(User user) {
        return Optional.of(userService.save(user));
    }

    @Override
    public Boolean existsByEmail(String email) {
        return userService.existsByEmail(email);
    }

    @Override
    public int existsByPhoneNumber(String phoneNumber) {
        return userService.existsUserByPhoneNumber(phoneNumber);
    }

    @Override
    public boolean updateAvatar(UpdateAvatarRequest updateAvatarRequest) {
        try {
            int result = userService.updateAvatar(updateAvatarRequest.getAvatarUrl(), updateAvatarRequest.getUserId());
            if (result > 0) {
                return true;
            }
            return false;
        } catch (Exception e) {
            System.out.println("Error: " + e);
        }
        return false;
    }
}
