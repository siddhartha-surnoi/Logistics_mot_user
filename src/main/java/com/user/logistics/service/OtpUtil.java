//package com.example.logistics.service;
//
//import java.util.Random;
//
//public class OtpUtil {
//    public static String generateOtp(int length) {
//        Random random = new Random();
//        StringBuilder otp = new StringBuilder();
//        for (int i = 0; i < length; i++) {
//            otp.append(random.nextInt(10)); // Generates a random digit (0-9)
//        }
//        return otp.toString();
//    }
//}
package com.user.logistics.service;

import java.security.SecureRandom;

public class OtpUtil {
    private static final SecureRandom secureRandom = new SecureRandom();

    public static String generateOtp(int length) {
        if (length <= 0) {
            throw new IllegalArgumentException("OTP length must be greater than 0");
        }

        StringBuilder otp = new StringBuilder(length);
        for (int i = 0; i < length; i++) {
            otp.append(secureRandom.nextInt(10)); // Generates a random digit (0-9)
        }
        return otp.toString();
    }
}
