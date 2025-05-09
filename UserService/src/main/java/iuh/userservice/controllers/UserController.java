package iuh.userservice.controllers;

import iuh.userservice.dtos.requests.AuthRequest;
import iuh.userservice.dtos.requests.RegisterRequest;
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

    @PostMapping(value = "/upload/update", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<MessageResponse<UserResponse>> update(
            @RequestPart("userData") User userRequest,
            @RequestPart(value = "avatar", required = false) MultipartFile avatarFile) {
        try {
            System.out.println("Check userRequest to upload: " + userRequest);
            Optional<User> existingUserOpt = userService.findUserByEmail(userRequest.getEmail());
            System.out.println("check existingUserOpt: " + existingUserOpt.get());
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
            existingUser.setGender(userRequest.getGender());

            // Xử lý upload ảnh nếu có file
            if (avatarFile != null && !avatarFile.isEmpty()) {
                // Nếu đang dùng Cloudinary Gateway Filter, lấy thông tin từ header
                String cloudinaryUrl = userRequest.getAvatar(); // Hoặc giữ nguyên giá trị hiện có
                // Nếu cần upload trực tiếp từ controller
                // String cloudinaryUrl = cloudinaryService.uploadImage(avatarFile);

                existingUser.setAvatar(cloudinaryUrl);
            } else if (userRequest.getAvatar() != null) {
                // Nếu không có file mới nhưng có URL avatar trong userRequest
                existingUser.setAvatar(userRequest.getAvatar());
            }

            if (existingUser.getPhoneNumber() != userRequest.getPhoneNumber()) {
                existingUser.setPhoneNumber(userRequest.getPhoneNumber());
            }

            Optional<User> updatedUser = userService.updateUser(existingUser);
            Address address = addressService.getAddressByUserId(userRequest.getUserId()).orElse(null);
            UserResponse userResponse = userMapper.UserToUserResponse(updatedUser.get());
            userResponse.setAddress(address);

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
