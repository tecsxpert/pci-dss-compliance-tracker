package com.internship.tool.dto;

import java.io.Serializable;

public class UserDTO implements Serializable {

    private static final long serialVersionUID = 1L;

    private String name;
    private String email;

    // Default constructor
    public UserDTO() {
    }

    // Parameterized constructor
    public UserDTO(String name, String email) {
        this.name = name;
        this.email = email;
    }

    // Getters
    public String getName() {
        return name;
    }

    public String getEmail() {
        return email;
    }

    // Setters
    public void setName(String name) {
        this.name = name;
    }

    public void setEmail(String email) {
        this.email = email;
    }
}