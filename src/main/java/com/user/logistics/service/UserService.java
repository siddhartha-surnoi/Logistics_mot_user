package com.user.logistics.service;


import com.user.logistics.entity.UpdatePasswordRequest;
import com.user.logistics.entity.User;

import com.user.logistics.exception.UserNotFoundException;
import com.user.logistics.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

@Service
public class UserService {

    @Autowired
    private UserRepository userRepository;

//    @Autowired
//    private TokenService tokenService;

    @Autowired
    private PasswordEncoder passwordEncoder;
    @Autowired
    private EmailSenderService emailSenderService;

//    @Autowired
//    private DriverRepository driverRepository;
//
//    @Autowired
//    private TransporterRepository transporterRepository;
//
//    @Autowired
//    private ManufacturerRepository manufacturerRepository;
//
//    @Autowired
//    private ProfileSettingsRepository profileSettingsRepository;

    public User createUser(User userRequest) {
        // If password should be encoded, do here: userRequest.setPassword(passwordEncoder.encode(userRequest.getPassword()));
        return userRepository.save(userRequest);
    }

    public List<User> getAllUsers() {
        return userRepository.findAll();
    }

    public User getUserById(Long id) {
        return userRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("User not found with id: " + id));
    }

    public User updateUserById(Long id, User userRequest) {
        User existing = getUserById(id);

        // Update only fields that are not null in the request
        if (userRequest.getName() != null) existing.setName(userRequest.getName());
        if (userRequest.getEmail() != null) existing.setEmail(userRequest.getEmail());
        if (userRequest.getPassword() != null) existing.setPassword(userRequest.getPassword());
        if (userRequest.getRole() != null) existing.setRole(userRequest.getRole());
        return userRepository.save(existing);
    }

    public void deleteUserById(Long id) {
        User existing = getUserById(id);
        userRepository.delete(existing);
    }
    public boolean existsByEmail(String email) {
        return userRepository.existsByEmail(email);
    }

    public List<User> getAllDrivers() {
        return userRepository.findByRole("DRIVER");
    }
    public User registerUser(User user) {
//        return saveUser(user);
        user.setPassword(passwordEncoder.encode(user.getPassword()));
        return userRepository.save(user);
    }

//
//    public User login(String email, String password) {
//        Optional<User> userOpt = userRepository.findByEmail(email);
//        if (userOpt.isPresent()) {
//            User user = userOpt.get();
//            if (user.getPassword().equals(password)) {
//                return user;
//            }
//            if (passwordEncoder.matches(password, user.getPassword())) {
//                return user;
//            }
//        }
//        return null;
//    }


//    public User login(String email, String password) {
//        Optional<User> userOpt = userRepository.findByEmail(email);
//        if (userOpt.isPresent()) {
//            User user = userOpt.get();
//            if (user.getPassword().equals(password)) {
//                return user;
//            }
//            if (passwordEncoder.matches(password, user.getPassword())) {
//                return user;
//            }
//        }
//        return null;
//    }

    public User login(String email, String password) {
        Optional<User> userOpt = userRepository.findByEmail(email);

        if (userOpt.isPresent()) {
            User user = userOpt.get();
            System.out.println("User found: " + user.getEmail());
            System.out.println("Stored password: " + user.getPassword());
            System.out.println("Entered password: " + password);

            // If the password is stored as plain text, use direct comparison
            if (password.equals(user.getPassword())) {
                System.out.println("Login successful (Plaintext password match)");
                return user;
            }

            // If passwords are hashed, use passwordEncoder.matches()
            if (passwordEncoder.matches(password, user.getPassword())) {
                System.out.println("Login successful (BCrypt password match)");
                return user;
            }

            System.out.println("Password does not match");
        } else {
            System.out.println("User not found for email: " + email);
        }

        return null;
    }


//    public Map<String, Object> getUserAdditionalDetails(Long userId) {
//        Map<String, Object> details = new HashMap<>();
//
//        Optional<Driver> driverOpt = driverRepository.findByUserUserId(userId);
//        if (driverOpt.isPresent()) {
//            details.put("driverId", driverOpt.get().getDriverId());
//        }
//
//        Optional<Manufacturer> manufacturerOpt = manufacturerRepository.findByUserUserId(userId);
//        if (manufacturerOpt.isPresent()) {
//            details.put("manufacturerId", manufacturerOpt.get().getManufacturerId());
//        }
//
//        Optional<Transporter> transporterOpt = transporterRepository.findByUserUserId(userId);
//        if (transporterOpt.isPresent()) {
//            details.put("transporterId", transporterOpt.get().getTransporterId());
//        }
//
//        return details;
//    }



    public boolean isUserLoggedIn(Long userId) {
        Optional<User> userOptional = userRepository.findById(userId);

        if (userOptional.isPresent()) {
            User user = userOptional.get();
//            return user.getOnlineStatus() == User.OnlineStatus.ONLINE;
        }

        return false;
    }

    //    public void logoutUser(Long userId) {
//        updateUserOnlineStatus(userId, User.OnlineStatus.OFFLINE);
//        // Optionally, invalidate any tokens associated with this user
//        String token = getTokenByUserId(userId);
//        if (token != null) {
//            tokenService.invalidateToken(token);
//        }
//    }
    public String getTokenByUserId(Long userId) {
        // Retrieve the token associated with this userId, implementation depends on your setup
        // For example, you might store tokens in a database or in memory
        return null;
    }

    public void updateUserOnlineStatus(Long userId, User.OnlineStatus status) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new UserNotFoundException("User not found with id " + userId));

        user.setOnlineStatus(status);
//        if (status == User.OnlineStatus.OFFLINE) {
//            user.setLastSeen(LocalDateTime.now()); // Update lastSeen to the current time
//        } else {
//            user.setLastSeen(null); // Reset last seen when online
//        }

        userRepository.save(user);
    }
    public void updatePassword(String email, String newPassword) {
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new IllegalArgumentException("User not found."));
        user.setPassword(passwordEncoder.encode(newPassword)); // Ensure password encoding
        userRepository.save(user);
    }

public boolean existsByPhoneNumber(String phoneNumber) {
    return userRepository.existsByPhoneNumber(phoneNumber);
}

    public Map<String, String> updatePassword(UpdatePasswordRequest request) {
        Map<String, String> response = new HashMap<>();

        // Validate user
        Optional<User> userOptional = userRepository.findByEmail(request.getEmail());
        if (userOptional.isEmpty()) {
            response.put("error", "User not found");
            return response;
        }

        User user = userOptional.get();

        // Validate current password
        if (!passwordEncoder.matches(request.getCurrentPassword(), user.getPassword())) {
            response.put("error", "Current password is incorrect");
            return response;
        }

        // Validate new password and re-enter password match
        if (!request.getNewPassword().equals(request.getReenterPassword())) {
            response.put("error", "New passwords do not match");
            return response;
        }

        // Update password
        user.setPassword(passwordEncoder.encode(request.getNewPassword()));
        userRepository.save(user);

        response.put("message", "Password updated successfully");
        return response;
    }

    public User loginByEmail(String email, String password) {
        Optional<User> userOpt = userRepository.findByEmail(email);
        if (userOpt.isPresent()) {
            User user = userOpt.get();
            if (passwordEncoder.matches(password, user.getPassword())) {
                return user;
            }
        }
        return null;
    }

    public User loginByPhoneNumber(String phoneNumber, String password) {
        Optional<User> userOpt = userRepository.findByPhoneNumber(phoneNumber);
        if (userOpt.isPresent()) {
            User user = userOpt.get();
            if (passwordEncoder.matches(password, user.getPassword())) {
                return user;
            }
        }
        return null;
    }
    public User findUserByEmail(String email) {
        Optional<User> userOpt = userRepository.findByEmail(email);
        if (userOpt.isPresent()) {
            return userOpt.get();
        }
        return null;
    }




//    public Map<String, Object> getUserDetails(Long userId) {
//        Map<String, Object> response = new HashMap<>();
//
//        Optional<User> userOptional = userRepository.findById(userId);
//        if (userOptional.isPresent()) {
//            User user = userOptional.get();
//            response.put("user", user);
//
//            // Check if the user exists in Driver table
//            driverRepository.findByUser(user).ifPresent(driver -> {
//                driver.setUser(user); // Ensure user is linked
//                response.put("driver", driver);
//            });
//
//            // Check if the user exists in Manufacturer table
//            manufacturerRepository.findByUser(user).ifPresent(manufacturer -> {
//                manufacturer.setUser(user);
//                response.put("manufacturer", manufacturer);
//            });
//
//            // Check if the user exists in Transporter table
//            transporterRepository.findByUser(user).ifPresent(transporter -> {
//                transporter.setUser(user);
//                response.put("transporter", transporter);
//            });
//
//            // Check if the user has Profile Settings
//            profileSettingsRepository.findByUser(user).ifPresent(profileSettings -> {
//                profileSettings.setUser(user);
//                response.put("profileSettings", profileSettings);
//            });
//
//        }

//        return response;
//    }
    public User findUserByEmailOrPhone(String emailOrPhone) {
        if (emailOrPhone.contains("@")) {
            return userRepository.findByEmail(emailOrPhone).orElse(null);
        } else {
            return userRepository.findByPhoneNumber(emailOrPhone).orElse(null);
        }
    }

    public Optional<User> findByEmail(String email) {
        return userRepository.findByEmail(email);
    }

    public void save(User user) {
        userRepository.save(user);
    }

}
