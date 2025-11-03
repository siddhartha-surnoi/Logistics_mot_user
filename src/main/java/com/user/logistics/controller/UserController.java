package com.user.logistics.controller;


import com.user.logistics.entity.LoginRequest;
import com.user.logistics.entity.UpdatePasswordRequest;
import com.user.logistics.entity.User;
import com.user.logistics.repository.UserRepository;
import com.user.logistics.service.*;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

@RestController
@RequestMapping("/user")
public class UserController {
    @Autowired
    private UserService userService;
    @Autowired
    private UserRepository userRepository;
    @Autowired
    private TokenService tokenService;
    @Autowired
    private EmailSenderService emailSenderService;
    @Autowired
    private SmsService smsService;
    @Autowired
    private OTPService otpService;
    @Autowired
    private PasswordEncoder passwordEncoder;

    @PostMapping("/saveUser")
    public User createUser(@RequestBody User userRequest) {
        return userService.createUser(userRequest);
    }

    // GET /users
    @GetMapping("/getAllUsers")
    public List<User> getAllUsers() {
        return userService.getAllUsers();
    }

    // GET /users/{id}
    @GetMapping("/getUser/{id}")
    public User getUserById(@PathVariable Long id) {
        return userService.getUserById(id);
    }

    // PUT /users/{id}
    @PutMapping("/updateUser/{id}")
    public User updateUserById(@PathVariable Long id, @RequestBody User userRequest) {
        return userService.updateUserById(id, userRequest);
    }

    // DELETE /users/{id}
    @DeleteMapping("/deleteUser/{id}")
    public void deleteUserById(@PathVariable Long id) {
        userService.deleteUserById(id);
    }

//    @PostMapping("/login")
//    public ResponseEntity<?> login(@RequestBody LoginRequest loginRequest) {
//        if (loginRequest == null || loginRequest.getEmail() == null || loginRequest.getPassword() == null) {
//            return new ResponseEntity<>("Invalid login request", HttpStatus.BAD_REQUEST);
//        }
//
//        User authenticatedUser = userService.login(loginRequest.getEmail(), loginRequest.getPassword());
//        if (authenticatedUser != null) {
////            if (!"ADMIN".equalsIgnoreCase(authenticatedUser.getRole())) {
////                return new ResponseEntity<>("Access Denied: Only admins can log in here.", HttpStatus.FORBIDDEN);
////            }
////            if (userService.isUserLoggedIn(authenticatedUser.getUsersId())) {
////                String activeToken = tokenService.getActiveTokenForUser(authenticatedUser.getUsersId());
////                tokenService.invalidateToken(activeToken);
////                userService.logoutUser(authenticatedUser.getUsersId());
////            }
//
//            String jwtToken = tokenService.generateToken(loginRequest.getEmail());
////            tokenService.setActiveTokenForUser(authenticatedUser.getUsersId(), jwtToken);
//
//
//            JwtResponse response = new JwtResponse(jwtToken, authenticatedUser.getUserId(), authenticatedUser.getName(), authenticatedUser.getEmail(), authenticatedUser.getRole());
//            return new ResponseEntity<>(response, HttpStatus.OK);
//        } else {
//            return new ResponseEntity<>("Invalid credentials", HttpStatus.UNAUTHORIZED);
//        }
//    }

//
//    @PostMapping("/register")
//    public ResponseEntity<?> register(@RequestBody User registrationData) {
//        try {
//            if (userService.existsByEmail(registrationData.getEmail())) {
//                return new ResponseEntity<>("Email is already taken. Please use another email.", HttpStatus.CONFLICT);
//            }
//            switch (registrationData.getRole().toUpperCase()) {
//                case "TRANSPORTER":
//                case "DRIVER":
//                case "MANUFACTURER":
//                    break; // Valid roles
//                default:
//                    return ResponseEntity.badRequest().body("Invalid role specified. Allowed roles: TRANSPORTER, DRIVER, MANUFACTURER.");
//            }
//
//            User registeredUser = userService.registerUser(registrationData);
//            if (registeredUser != null) {
//                sendRegistrationSuccessEmail(registeredUser.getEmail(), registeredUser.getName());
//                return new ResponseEntity<>(registeredUser, HttpStatus.CREATED);
//            } else {
//                return new ResponseEntity<>(HttpStatus.INTERNAL_SERVER_ERROR);
//            }
//        } catch (IllegalArgumentException e) {
//            return new ResponseEntity<>(e.getMessage(), HttpStatus.CONFLICT);
//        } catch (Exception e) {
//            return new ResponseEntity<>(HttpStatus.INTERNAL_SERVER_ERROR);
//        }
//    }

//    @PostMapping("/login")
//    public ResponseEntity<?> login(@RequestBody LoginRequest loginRequest) {
//        if (loginRequest == null || loginRequest.getEmail() == null || loginRequest.getPassword() == null) {
//            return new ResponseEntity<>("Invalid login request", HttpStatus.BAD_REQUEST);
//        }
//
//        User authenticatedUser = userService.login(loginRequest.getEmail(), loginRequest.getPassword());
//        if (authenticatedUser != null) {
//            String jwtToken = tokenService.generateToken(loginRequest.getEmail());
//
//            // Fetch additional role-specific IDs
//            Map<String, Object> additionalDetails = userService.getUserAdditionalDetails(authenticatedUser.getUserId());
//
//            // Prepare response as a Map
//            Map<String, Object> response = new HashMap<>();
//            response.put("token", jwtToken);
//            response.put("userId", authenticatedUser.getUserId());
//            response.put("name", authenticatedUser.getName());
//            response.put("email", authenticatedUser.getEmail());
//            response.put("role", authenticatedUser.getRole());
//            response.putAll(additionalDetails); // Merging additional details into response
//
//            return new ResponseEntity<>(response, HttpStatus.OK);
//        } else {
//            return new ResponseEntity<>("Invalid credentials", HttpStatus.UNAUTHORIZED);
//        }
//    }
//
//
//    @PostMapping("/register")
//    public ResponseEntity<?> register(@RequestBody User registrationData) {
//        try {
//            if (userService.existsByEmail(registrationData.getEmail())) {
//                return new ResponseEntity<>("Email is already taken. Please use another email.", HttpStatus.CONFLICT);
//            }
//            switch (registrationData.getRole().toUpperCase()) {
//                case "TRANSPORTER":
//                case "DRIVER":
//                case "MANUFACTURER":
//                    break; // Valid roles
//                default:
//                    return ResponseEntity.badRequest().body("Invalid role specified. Allowed roles: TRANSPORTER, DRIVER, MANUFACTURER.");
//            }
//
//            User registeredUser = userService.registerUser(registrationData);
//            if (registeredUser != null) {
//                sendRegistrationSuccessEmail(registeredUser.getEmail(), registeredUser.getName());
//                return new ResponseEntity<>(registeredUser, HttpStatus.CREATED);
//            } else {
//                return new ResponseEntity<>(HttpStatus.INTERNAL_SERVER_ERROR);
//            }
//        } catch (IllegalArgumentException e) {
//            return new ResponseEntity<>(e.getMessage(), HttpStatus.CONFLICT);
//        } catch (Exception e) {
//            return new ResponseEntity<>(HttpStatus.INTERNAL_SERVER_ERROR);
//        }
//    }

    private void sendRegistrationSuccessEmail(String to, String name) {
        String subject = "Welcome to LOGISTICS PVT LTD!";
        String body = "Dear " + name + ",\n\n" +
                "Welcome to Logistics Pvt Ltd! We are delighted to have you join our logistics platform. " +
                "Your account has been successfully created, and you can now access our comprehensive logistics services.\n\n" +
                "As a registered member, you have access to:\n" +
                "• Real-time shipment tracking\n" +
                "• Warehouse management tools\n" +
                "• Supply chain optimization\n" +
                "• Delivery scheduling\n" +
                "• Custom logistics solutions\n\n" +
                "If you have any questions or need assistance with our logistics services, our support team is available 24/7.\n\n" +
                "Thank you for choosing Logistics Pvt Ltd. We look forward to being your trusted logistics partner.\n\n" +
                "Best regards,\n" +
                "The Logistics Team\n" +
                "Logistics Pvt Ltd\n" +
                "support@logistics.com\n" +
                "+91-XXX-XXX-XXXX\n" +
                "www.logistics.com";

        emailSenderService.sendEmail(to, subject, body);
    }

    @GetMapping("/drivers")
    public ResponseEntity<List<User>> getAllDrivers() {
        List<User> drivers = userService.getAllDrivers();
        return ResponseEntity.ok(drivers);
    }



    @PostMapping("/update")
    public ResponseEntity<?> updatePassword(@RequestBody UpdatePasswordRequest request) {
        Map<String, String> response = userService.updatePassword(request);

        if (response.containsKey("error")) {
            return ResponseEntity.badRequest().body(response.get("error"));
        }

        return ResponseEntity.ok(response);
    }




    @PostMapping("/forgot-password")
    public ResponseEntity<?> forgotPassword(@RequestBody Map<String, String> request) {
        String email = request.get("email");

        if (email == null || email.isEmpty()) {
            return ResponseEntity.badRequest().body("Email is required.");
        }

        if (!userService.existsByEmail(email)) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body("No account found with this email.");
        }

        try {
            // Generate a 6-digit OTP
            String otp = otpService.generateOTP(email);

            // Send the OTP to the user's email
            sendOTPEmail(email, otp);

            return ResponseEntity.ok("OTP sent successfully to your email.");
        } catch (Exception e) {
            e.printStackTrace(); // Print detailed error
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Error while sending OTP email.");
        }
    }

    // Verify OTP and Reset Password
    @PostMapping("/reset-password")
    public ResponseEntity<?> resetPassword(@RequestBody Map<String, String> request) {
        String email = request.get("email");
        String otp = request.get("otp");
        String newPassword = request.get("newPassword");

        if (email == null || email.isEmpty()) {
            return ResponseEntity.badRequest().body("Email is required.");
        }
        if (otp == null || otp.isEmpty()) {
            return ResponseEntity.badRequest().body("OTP is required.");
        }
        if (newPassword == null || newPassword.isEmpty()) {
            return ResponseEntity.badRequest().body("New password is required.");
        }

        // Validate the OTP
        if (!otpService.validateOTP(email, otp)) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("Invalid or expired OTP.");
        }

        // Update the password
        userService.updatePassword(email, newPassword);
        otpService.invalidateOTP(email); // Invalidate the OTP after successful reset

        return ResponseEntity.ok("Password reset successful.");
    }

    private void sendOTPEmail(String to, String otp) {
        String subject = "Password Reset OTP";
        String body = "Your OTP for password reset is: " + otp + ". This OTP is valid for 5 minutes.";
        emailSenderService.sendEmail(to, subject, body);
    }
//login with otp

//    @PostMapping("/login")
//    public ResponseEntity<?> login(@RequestBody LoginRequest loginRequest) {
//        if (loginRequest == null ||
//                (loginRequest.getEmail() == null && loginRequest.getPhoneNumber() == null) ||
//                loginRequest.getPassword() == null) {
//            return new ResponseEntity<>("Invalid login request", HttpStatus.BAD_REQUEST);
//        }
//
//        User authenticatedUser = null;
//
//        if (loginRequest.getEmail() != null) {
//            authenticatedUser = userService.loginByEmail(loginRequest.getEmail(), loginRequest.getPassword());
//        } else if (loginRequest.getPhoneNumber() != null) {
//            authenticatedUser = userService.loginByPhoneNumber(loginRequest.getPhoneNumber(), loginRequest.getPassword());
//        }
//
//        if (authenticatedUser != null) {
//            String jwtToken = tokenService.generateToken(
//                    loginRequest.getEmail() != null ? loginRequest.getEmail() : loginRequest.getPhoneNumber()
//            );
//            JwtResponse response = new JwtResponse(jwtToken, authenticatedUser.getUserId(), authenticatedUser.getName(), authenticatedUser.getEmail(), authenticatedUser.getRole());
//            return new ResponseEntity<>(response, HttpStatus.OK);
//        } else {
//            return new ResponseEntity<>("Invalid credentials", HttpStatus.UNAUTHORIZED);
//        }
//    }


    @PostMapping("/register")
    public ResponseEntity<?> register(@RequestBody User registrationData) {
        try {
            // Check if email or phone number is already in use
            if (userService.existsByEmail(registrationData.getEmail()) ||
                    userService.existsByPhoneNumber(registrationData.getPhoneNumber())) {
                return ResponseEntity.status(HttpStatus.CONFLICT).body("Email or phone number already in use.");
            }

            // Register the user
            User registeredUser = userService.registerUser(registrationData);
            if (registeredUser != null) {
                // Send SMS Notification
                smsService.sendSms(
                        registeredUser.getPhoneNumber(),
                        "Welcome to Logistics Pvt Ltd! Your account has been successfully registered."
                );

                // Send Email Notification
                emailSenderService.sendEmail(
                        registeredUser.getEmail(),
                        "Welcome to Logistics Pvt Ltd!",
                        "Dear " + registeredUser.getName() + ",\n\nYour account has been successfully created."
                );

                // Return the response
                return ResponseEntity.status(HttpStatus.CREATED).body(registeredUser);
            } else {
                return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
            }
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(e.getMessage());
        }
    }
    @PostMapping("/login")
    public ResponseEntity<?> login(@RequestBody LoginRequest loginRequest) {
        if (loginRequest == null ||
                (loginRequest.getEmail() == null && loginRequest.getPhoneNumber() == null) ||
                loginRequest.getPassword() == null) {
            return new ResponseEntity<>("Invalid login request", HttpStatus.BAD_REQUEST);
        }

        User authenticatedUser = null;

        if (loginRequest.getEmail() != null) {
            authenticatedUser = userService.loginByEmail(loginRequest.getEmail(), loginRequest.getPassword());
        } else if (loginRequest.getPhoneNumber() != null) {
            authenticatedUser = userService.loginByPhoneNumber(loginRequest.getPhoneNumber(), loginRequest.getPassword());
        }

        if (authenticatedUser != null) {
            // Generate OTP
            String otp = otpService.generateOTP(authenticatedUser.getEmail());

            // Send OTP to Email and Phone
            if (authenticatedUser.getEmail() != null) {
                sendLoginOTPEmail(authenticatedUser.getEmail(), otp);
            }

            if (authenticatedUser.getPhoneNumber() != null) {
                smsService.sendSms(
                        authenticatedUser.getPhoneNumber(),
                        "Your OTP for login is: " + otp
                );
            }

            // Store OTP temporarily for later validation
            otpService.storeOtp(authenticatedUser.getEmail(), otp);

            return new ResponseEntity<>("OTP sent to email and phone. Please verify to proceed.", HttpStatus.OK);
        } else {
            return new ResponseEntity<>("Invalid credentials", HttpStatus.UNAUTHORIZED);
        }
    }

    @PostMapping("/verify-login-otp")
    public ResponseEntity<?> verifyLoginOtp(@RequestBody Map<String, String> request) {
        String email = request.get("email");
        String otp = request.get("otp");

        if (email == null || email.isEmpty()) {
            return ResponseEntity.badRequest().body("Email is required.");
        }
        if (otp == null || otp.isEmpty()) {
            return ResponseEntity.badRequest().body("OTP is required.");
        }

        // Validate the OTP
        if (!otpService.validateOTP(email, otp)) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("Invalid or expired OTP.");
        }

        // Generate JWT token and return the response
        User authenticatedUser = userService.findUserByEmail(email);
        String jwtToken = tokenService.generateToken(
                authenticatedUser.getEmail() != null ? authenticatedUser.getEmail() : authenticatedUser.getPhoneNumber()
        );

        // Fetch additional role-specific IDs
//        Map<String, Object> additionalDetails = userService.getUserAdditionalDetails(authenticatedUser.getUserId());

        // Prepare response as a Map
        Map<String, Object> response = new HashMap<>();
        response.put("token", jwtToken);
        response.put("userId", authenticatedUser.getUserId());
        response.put("name", authenticatedUser.getName());
        response.put("email", authenticatedUser.getEmail());
        response.put("role", authenticatedUser.getRole());
//        response.putAll(additionalDetails); // Merging additional details into response

        otpService.invalidateOTP(email); // Invalidate OTP after successful verification

        return new ResponseEntity<>(response, HttpStatus.OK);
    }

//    @PostMapping("/resend-otp")
//    public ResponseEntity<String> resendOtp(@RequestBody Map<String, String> request) {
//        String email = request.get("email");
//        if (email == null || email.isEmpty()) {
//            return ResponseEntity.badRequest().body("Email is required");
//        }
//
//        boolean otpSent = otpService.resendOtp(email);
//        if (otpSent) {
//            return ResponseEntity.ok("OTP resent successfully");
//        } else {
//            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Failed to resend OTP");
//        }
//    }

    private void sendLoginOTPEmail(String to, String otp) {
        String subject = "Login OTP";
        String body = "Your OTP for login is: " + otp + ". This OTP is valid for 5 minutes.";
        emailSenderService.sendEmail(to, subject, body);
    }
//

    @PostMapping("/logout")
    public ResponseEntity<?> logout(HttpServletRequest request) {
        // Extract JWT token from the Authorization header
        String token = request.getHeader("Authorization");
        if (token == null || !token.startsWith("Bearer ")) {
            return new ResponseEntity<>("Token is missing or invalid", HttpStatus.BAD_REQUEST);
        }

        // Remove the "Bearer " prefix
        token = token.substring(7);

        // Optional: Invalidate the token on the server-side (e.g., using a blacklist mechanism)
        tokenService.invalidateToken(token);  // Assuming invalidateToken stores invalidated tokens

        return new ResponseEntity<>("Successfully logged out", HttpStatus.OK);
    }

    @PostMapping("/fcm-token")
    public ResponseEntity<String> saveFcmToken(@RequestBody User user) {
        Optional<User> existingUser = userRepository.findById(user.getUserId());

        if (existingUser.isPresent()) {
            User updatedUser = existingUser.get();
            updatedUser.setFcmToken(user.getFcmToken());
            userRepository.save(updatedUser);
            return ResponseEntity.ok("FCM Token updated successfully!");
        } else {
            return ResponseEntity.badRequest().body("User not found.");
        }
    }

//    @GetMapping("/{userId}")
//    public ResponseEntity<Map<String, Object>> getUserDetails(@PathVariable Long userId) {
//        Map<String, Object> userDetails = userService.getUserDetails(userId);
//        if (userDetails.isEmpty()) {
//            return ResponseEntity.notFound().build();
//        }
//        return ResponseEntity.ok(userDetails);
//    }

    @PostMapping("/resend-otp")
    public ResponseEntity<?> resendOtp(@RequestBody Map<String, String> request) {
        String emailOrPhone = request.get("emailOrPhone");

        if (emailOrPhone == null || emailOrPhone.isEmpty()) {
            return ResponseEntity.badRequest().body("Email or phone number is required.");
        }

        User user = userService.findUserByEmailOrPhone(emailOrPhone);

        if (user == null) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body("User not found.");
        }

        // Generate a new OTP
        String newOtp = otpService.generateOTP(user.getEmail());

        // Send OTP via email if available
        if (user.getEmail() != null) {
            sendLoginOTPEmail(user.getEmail(), newOtp);
        }

        // Send OTP via SMS if phone number is available
        if (user.getPhoneNumber() != null) {
            smsService.sendSms(
                    user.getPhoneNumber(),
                    "Your new OTP for login is: " + newOtp
            );
        }

        // Store the new OTP
        otpService.storeOtp(user.getEmail(), newOtp);

        return ResponseEntity.ok("New OTP has been sent to email/phone.");
    }

    @PostMapping("/update-password")
    public ResponseEntity<?> updatePassword(@RequestBody Map<String, String> request) {
        String email = request.get("email");
        String oldPassword = request.get("oldPassword");
        String newPassword = request.get("newPassword");
        String confirmPassword = request.get("confirmPassword");

        // Basic validations
        if (email == null || email.isEmpty()) {
            return ResponseEntity.badRequest().body("Email is required.");
        }
        if (oldPassword == null || oldPassword.isEmpty()) {
            return ResponseEntity.badRequest().body("Old password is required.");
        }
        if (newPassword == null || newPassword.isEmpty()) {
            return ResponseEntity.badRequest().body("New password is required.");
        }
        if (confirmPassword == null || confirmPassword.isEmpty()) {
            return ResponseEntity.badRequest().body("Confirm password is required.");
        }

        if (!newPassword.equals(confirmPassword)) {
            return ResponseEntity.badRequest().body("New password and confirm password do not match.");
        }

        // Fetch user by email
        Optional<User> userOptional = userService.findByEmail(email);
        if (userOptional.isEmpty()) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body("User not found.");
        }

        User user = userOptional.get();

        // Check old password match
        if (!passwordEncoder.matches(oldPassword, user.getPassword())) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("Old password is incorrect.");
        }

        // Update new password
        user.setPassword(passwordEncoder.encode(newPassword));
        userService.save(user);

        return ResponseEntity.ok("Password updated successfully.");
    }

}
