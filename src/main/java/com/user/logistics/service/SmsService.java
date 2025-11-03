//package com.example.Logistics.service;
//
//import com.twilio.Twilio;
//import com.twilio.rest.api.v2010.account.Message;
//import org.springframework.stereotype.Service;
//
//@Service
//public class SmsService {
//
//    private static final String ACCOUNT_SID = "AC2f679abae4232c397d3f801669da2684";
//    private static final String AUTH_TOKEN = "b4eb70d4ab229028175a2d1b0fa5e9d6";
//    private static final String TWILIO_PHONE_NUMBER = "7793907422";
//
//    public SmsService() {
//        Twilio.init(ACCOUNT_SID, AUTH_TOKEN);
//    }
//
//    public void sendSms(String toPhoneNumber, String message) {
//        try {
//            Message sms = Message.creator(
//                    new com.twilio.type.PhoneNumber(toPhoneNumber),
//                    new com.twilio.type.PhoneNumber(TWILIO_PHONE_NUMBER),
//                    message
//            ).create();
//            System.out.println("SMS sent successfully. SID: " + sms.getSid());
//        } catch (Exception e) {
//            System.err.println("Failed to send SMS: " + e.getMessage());
//        }
//    }
//}


package com.user.logistics.service;

import com.google.i18n.phonenumbers.NumberParseException;
import com.google.i18n.phonenumbers.PhoneNumberUtil;
import com.google.i18n.phonenumbers.Phonenumber;
import com.twilio.Twilio;
import com.twilio.rest.api.v2010.account.Message;
import org.springframework.stereotype.Service;

@Service
public class SmsService {

    private static final String ACCOUNT_SID = "AC2f679abae4232c397d3f801669da2684";
    private static final String AUTH_TOKEN = "b4eb70d4ab229028175a2d1b0fa5e9d6";
    private static final String TWILIO_PHONE_NUMBER = "+16087057704"; // Verified Twilio number in E.164 format
    private static final String DEFAULT_REGION = "IN"; // Default region for India

    public SmsService() {
        Twilio.init(ACCOUNT_SID, AUTH_TOKEN);
    }

    public void sendSms(String toPhoneNumber, String message) {
        try {
            // Format the phone number
            String formattedPhoneNumber = formatPhoneNumber(toPhoneNumber);
            if (formattedPhoneNumber == null) {
                throw new IllegalArgumentException("Invalid phone number format.");
            }

            // Send the SMS
            Message sms = Message.creator(
                    new com.twilio.type.PhoneNumber(formattedPhoneNumber),
                    new com.twilio.type.PhoneNumber(TWILIO_PHONE_NUMBER),
                    message
            ).create();

            System.out.println("SMS sent successfully. SID: " + sms.getSid());
        } catch (Exception e) {
            System.err.println("Failed to send SMS: " + e.getMessage());
        }
    }

    private String formatPhoneNumber(String phoneNumber) {
        PhoneNumberUtil phoneNumberUtil = PhoneNumberUtil.getInstance();
        try {
            Phonenumber.PhoneNumber parsedNumber = phoneNumberUtil.parse(phoneNumber, DEFAULT_REGION);
            if (phoneNumberUtil.isValidNumber(parsedNumber)) {
                return phoneNumberUtil.format(parsedNumber, PhoneNumberUtil.PhoneNumberFormat.E164);
            }
        } catch (NumberParseException e) {
            System.err.println("Failed to parse phone number: " + e.getMessage());
        }
        return null;
    }
}
