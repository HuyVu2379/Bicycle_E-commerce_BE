package iuh.userservice.controllers;

import iuh.userservice.dtos.requests.AuthRequest;
import iuh.userservice.dtos.requests.RegisterRequest;
import iuh.userservice.dtos.requests.UpdateAvatarRequest;
import iuh.userservice.dtos.responses.AuthResponse;
import iuh.userservice.dtos.responses.MessageResponse;
import iuh.userservice.dtos.responses.SuccessEntityResponse;
import iuh.userservice.dtos.responses.UserResponse;
import iuh.userservice.entities.Address;
import iuh.userservice.entities.User;
import iuh.userservice.exception.errors.UserNotFoundException;
import iuh.userservice.mappers.AddressMapper;
import iuh.userservice.mappers.UserMapper;
import iuh.userservice.services.AddressService;
import iuh.userservice.services.Impl.AuthenticationServiceImpl;
import iuh.userservice.services.Impl.UserServiceImpl;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.Optional;

@RestController
@RequestMapping("/api/v1/users")
public class UserController {
    @Autowired
    private UserServiceImpl userService;
    @Autowired
    private UserMapper userMapper;
    @Autowired
    private AuthenticationServiceImpl authenticationService;
    @Autowired
    private AddressService addressService;
    private static final Logger logger = LoggerFactory.getLogger(UserController.class);

    @PostMapping("/register")
    public ResponseEntity<MessageResponse<AuthResponse>> register(@RequestBody RegisterRequest registerRequest) {
        userService.registerUser(registerRequest);
        AuthRequest authRequest = userMapper.RegisterRequestToAuthRequest(registerRequest);
        AuthResponse authResponse = authenticationService.authenticate(authRequest);
        return SuccessEntityResponse.created("Register successfully", authResponse);
    }

    @PutMapping("/updateAvatar")
    public ResponseEntity<MessageResponse<Boolean>> register(@RequestBody UpdateAvatarRequest updateAvatarRequest) {
        boolean result = userService.updateAvatar(updateAvatarRequest);
        if (result == true) {
            return SuccessEntityResponse.ok("Update avatar successfully", true);
        } else {
            return ResponseEntity.badRequest()
                    .body(new MessageResponse<>(HttpStatus.BAD_REQUEST.value(), "Failed to update avatar", false, null));
        }
    }

    @PostMapping("/update")
    public ResponseEntity<MessageResponse<UserResponse>> update(@RequestBody User userRequest) {
        try {
            System.out.println("check user request: " + userRequest);
            Optional<User> existingUserOpt = userService.findUserByEmail(userRequest.getEmail());
            if (existingUserOpt.isEmpty()) {
                return ResponseEntity.status(HttpStatus.NOT_FOUND)
                        .body(new MessageResponse<>(HttpStatus.NOT_FOUND.value(),
                                "Không tìm thấy người dùng với ID: " + userRequest.getUserId(),
                                false, null));
            }
            User existingUser = existingUserOpt.get();
            existingUser.setFullName(userRequest.getFullName());
            existingUser.setDob(userRequest.getDob());
            existingUser.setAddressId(userRequest.getAddressId());
            existingUser.setAvatar(userRequest.getAvatar());
            int phone = userService.existsByPhoneNumber(userRequest.getPhoneNumber());
            if (userRequest.getPhoneNumber() == existingUser.getPhoneNumber()) {
                existingUser.setPhoneNumber(userRequest.getPhoneNumber());
            } else {
                if (phone >= 2) {
                    return ResponseEntity.status(HttpStatus.CONFLICT)
                            .body(new MessageResponse<>(HttpStatus.CONFLICT.value(),
                                    "Số điện thoại đã tồn tại", false, null));
                }
                existingUser.setPhoneNumber(userRequest.getPhoneNumber());
            }
            Optional<Address> address = addressService.getAddressByUserId(existingUser.getUserId());
            Optional<User> updatedUser = userService.updateUser(existingUser);
            UserResponse userResponse = userMapper.UserToUserResponse(updatedUser.get());
            userResponse.setAddress(address.get());
            return SuccessEntityResponse.ok("Cập nhật người dùng thành công", userResponse);
        } catch (Exception e) {
            throw e;
        }
    }

    @GetMapping("/{userId}")
    public ResponseEntity<MessageResponse<UserResponse>> getUserById(@PathVariable String userId) {
        try {
            System.out.println("check userId: " + userId);
            Optional<User> userOptional = userService.findUserById(userId);
            if (userOptional.isEmpty()) {
                throw new UserNotFoundException("Không tìm thấy người dùng với ID: " + userId);
            }
            User user = userOptional.get();

            Optional<Address> addressOptional = addressService.getAddressByUserId(userId);
            Address address = addressOptional.orElse(null);

            UserResponse userResponse = null;
            userResponse = UserResponse.builder().fullName(user.getFullName())
                    .gender(user.getGender() != null ? user.getGender().toString() : null)
                    .address(address)
                    .dob(user.getDob())
                    .avatar(user.getAvatar())
                    .phoneNumber(user.getPhoneNumber())
                    .email(user.getEmail())
                    .userId(user.getUserId())
                    .build();
            if (user == null) {
                throw new UserNotFoundException("Không tìm thấy người dùng với ID: " + userId);
            }
            return SuccessEntityResponse.ok("Lấy thông tin người dùng thành công", userResponse);
        } catch (Exception e) {
            throw e;
        }
    }

}
