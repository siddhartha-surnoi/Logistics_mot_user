package com.user.logistics.service;

import org.springframework.stereotype.Service;

import java.security.SecureRandom;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Service
public class OTPService {

    private final Map<String, OTPDetails> otpStore = new ConcurrentHashMap<>();
    private final int OTP_VALIDITY_DURATION = 5 * 60 * 1000; // 5 minutes in milliseconds
    private static final SecureRandom secureRandom = new SecureRandom();
    private static final int OTP_LENGTH = 6;
    // Generate and store OTP
//    public String generateOTP(String email) {
//        String otp = String.valueOf(new Random().nextInt(999999)).substring(0, 6); // Generate 6-digit OTP
//        otpStore.put(email, new OTPDetails(otp, System.currentTimeMillis()));
//        return otp;
//    }

    public String generateOTP(String email) {
        StringBuilder otp = new StringBuilder(OTP_LENGTH);
        for (int i = 0; i < OTP_LENGTH; i++) {
            otp.append(secureRandom.nextInt(10)); // Generates a random digit (0-9)
        }

        otpStore.put(email, new OTPDetails(otp.toString(), System.currentTimeMillis()));
        return otp.toString();
    }
    // Store OTP (This is the missing method you need)
    public void storeOtp(String email, String otp) {
        otpStore.put(email, new OTPDetails(otp, System.currentTimeMillis()));
    }
    // Validate OTP
    public boolean validateOTP(String email, String otp) {
        if (!otpStore.containsKey(email)) {
            return false; // No OTP generated for this email
        }

        OTPDetails otpDetails = otpStore.get(email);
        if (otpDetails == null || !otpDetails.getOtp().equals(otp)) {
            return false; // OTP mismatch
        }

        // Check if OTP has expired
        if (System.currentTimeMillis() - otpDetails.getTimestamp() > OTP_VALIDITY_DURATION) {
            otpStore.remove(email);
            return false; // OTP expired
        }

        return true;
    }



    // Invalidate OTP
    public void invalidateOTP(String email) {
        otpStore.remove(email);
    }

    // Inner class to store OTP details
    private static class OTPDetails {
        private final String otp;
        private final long timestamp;

        public OTPDetails(String otp, long timestamp) {
            this.otp = otp;
            this.timestamp = timestamp;
        }

        public String getOtp() {
            return otp;
        }

        public long getTimestamp() {
            return timestamp;
        }
    }

}
