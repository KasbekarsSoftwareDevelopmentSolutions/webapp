package com.cloudcomputing.movieRetrievalWebApp.dto.userdto;

import jakarta.validation.constraints.Email;

public class UserUpdateDTO {

    @Email(message = "Email should be valid")
    private String emailAddress;

    private String password;

    private String firstName;

    private String lastName;

    // Getters and Setters

    public String getEmailAddress() {
        return emailAddress;
    }

    public void setEmailAddress(String emailAddress) {
        this.emailAddress = emailAddress;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public String getFirstName() {
        return firstName;
    }

    public void setFirstName(String firstName) {
        this.firstName = firstName;
    }

    public String getLastName() {
        return lastName;
    }

    public void setLastName(String lastName) {
        this.lastName = lastName;
    }
}
