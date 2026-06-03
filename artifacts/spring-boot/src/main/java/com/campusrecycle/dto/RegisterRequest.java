package com.campusrecycle.dto;

public class RegisterRequest {
    private String email;
    private String password;
    private String name;
    private String usn; // 🌟 ADD THIS FIELD

    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }

    public String getPassword() { return password; }
    public void setPassword(String password) { this.password = password; }

    public String getName() { return name; }
    public void setName(String name) { this.name = name; }

    public String getUsn() { return usn; } // 🌟 ADD THIS GETTER
    public void setUsn(String usn) { this.usn = usn; } // 🌟 ADD THIS SETTER
}