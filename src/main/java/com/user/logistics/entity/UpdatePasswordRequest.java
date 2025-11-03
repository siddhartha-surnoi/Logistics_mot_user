package com.user.logistics.entity;

public class UpdatePasswordRequest {

    private String email;
    private String currentPassword;
    private String newPassword;
    private String reenterPassword;

    // Getters and setters
    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }
    public String getCurrentPassword() { return currentPassword; }
    public void setCurrentPassword(String currentPassword) { this.currentPassword = currentPassword; }
    public String getNewPassword() { return newPassword; }
    public void setNewPassword(String newPassword) { this.newPassword = newPassword; }
    public String getReenterPassword() { return reenterPassword; }
    public void setReenterPassword(String reenterPassword) { this.reenterPassword = reenterPassword; }

}
