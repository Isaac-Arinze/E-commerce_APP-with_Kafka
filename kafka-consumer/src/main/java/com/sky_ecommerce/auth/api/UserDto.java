package com.sky_ecommerce.auth.api;

import java.time.Instant;

public class UserDto {
    private String id;
    private String email;
    private String status;
    private String firstName;
    private String lastName;
    private Instant createdAt;
    private Instant updatedAt;

    public UserDto() {
    }

    public UserDto(String id, String email, String status, String firstName, String lastName, Instant createdAt, Instant updatedAt) {
        this.id = id;
        this.email = email;
        this.status = status;
        this.firstName = firstName;
        this.lastName = lastName;
        this.createdAt = createdAt;
        this.updatedAt = updatedAt;
    }

    public String getId() {
        return id;
    }

    public UserDto setId(String id) {
        this.id = id;
        return this;
    }

    public String getEmail() {
        return email;
    }

    public UserDto setEmail(String email) {
        this.email = email;
        return this;
    }

    public String getStatus() {
        return status;
    }

    public UserDto setStatus(String status) {
        this.status = status;
        return this;
    }

    public String getFirstName() {
        return firstName;
    }

    public UserDto setFirstName(String firstName) {
        this.firstName = firstName;
        return this;
    }

    public String getLastName() {
        return lastName;
    }

    public UserDto setLastName(String lastName) {
        this.lastName = lastName;
        return this;
    }

    public Instant getCreatedAt() {
        return createdAt;
    }

    public UserDto setCreatedAt(Instant createdAt) {
        this.createdAt = createdAt;
        return this;
    }

    public Instant getUpdatedAt() {
        return updatedAt;
    }

    public UserDto setUpdatedAt(Instant updatedAt) {
        this.updatedAt = updatedAt;
        return this;
    }
}
