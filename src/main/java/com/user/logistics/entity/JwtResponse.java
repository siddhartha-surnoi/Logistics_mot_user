package com.user.logistics.entity;

public class JwtResponse {
    private String token;
    private long id;
    private String name;
    private String email;
    private String role;
//    private List<Device> devices; // List of devices the user is logged in from



    public JwtResponse(String token, long id, String name, String email, String role) {
        this.token = token;
        this.id = id;
        this.name = name;
        this.email = email;
//        this.devices = devices;

        this.role = role;
    }

    public String getToken() {
        return token;
    }

    public void setToken(String token) {
        this.token = token;
    }

    public long getId() {
        return id;
    }

    public void setId(long id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getRole() {
        return role;
    }

    public void setRole(String role) {
        this.role = role;
    }
}


