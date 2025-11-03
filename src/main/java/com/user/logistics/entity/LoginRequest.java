package com.user.logistics.entity;

public class LoginRequest {

    private String email;
    private String password;
    private String phoneNumber;
    private String identifier; // Either email or phone number

    private String otp;

    public LoginRequest() {

    }

    public LoginRequest(String email, String password, String phoneNumber, String identifier, String otp) {
        this.email = email;
        this.password = password;
        this.phoneNumber = phoneNumber;
        this.identifier = identifier;
        this.otp = otp;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public String getOtp() {
        return otp;
    }

    public void setOtp(String otp) {
        this.otp = otp;
    }

    public String getPhoneNumber() {
        return phoneNumber;
    }

    public void setPhoneNumber(String phoneNumber) {
        this.phoneNumber = phoneNumber;
    }

    public String getIdentifier() {
        return identifier;
    }

    public void setIdentifier(String identifier) {
        this.identifier = identifier;
    }
}

