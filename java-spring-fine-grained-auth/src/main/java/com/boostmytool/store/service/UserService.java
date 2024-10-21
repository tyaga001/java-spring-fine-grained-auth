package com.boostmytool.store.service;

import com.boostmytool.store.exception.ForbiddenAccessException;
import com.boostmytool.store.exception.UnauthorizedException;
import io.permit.sdk.Permit;
import io.permit.sdk.api.PermitApiError;
import io.permit.sdk.api.PermitContextError;
import io.permit.sdk.enforcement.Resource;
import io.permit.sdk.enforcement.User;
import org.springframework.stereotype.Service;

import java.io.IOException;

@Service  // Marks this class as a Spring service, making it eligible for component scanning and dependency injection
public class UserService {
    private final Permit permit;

    // Constructor injection of the Permit SDK instance
    public UserService(Permit permit) {
        this.permit = permit;
    }

    // Simulates user login by creating and returning a Permit User object based on the provided key
    public Object login(String key) {
        return new User.Builder(key).build();
    }

    // Handles user signup by creating a new Permit User object and syncing it with the Permit service
    public User signup(String key) {
        var user = new User.Builder(key).build();
        try {
            permit.api.users.sync(user);  // Syncs the newly created user with the Permit service
        } catch (PermitContextError | PermitApiError | IOException e) {
            throw new RuntimeException("Failed to create user", e);  // Throws runtime exception if user creation fails
        }
        return user;
    }

    // Assigns a specific role to a user in the "default" environment
    public void assignRole(User user, String role) {
        try {
            permit.api.users.assignRole(user.getKey(), role, "default");  // Assigns a role to the user
        } catch (PermitApiError | PermitContextError | IOException e) {
            throw new RuntimeException("Failed to assign role to user", e);  // Throws runtime exception if role assignment fails
        }
    }

    // Checks if a user is authorized to perform a certain action on a given resource
    public void authorize(User user, String action, Resource resource) {
        if (user == null) {
            throw new UnauthorizedException("Not logged in");  // Throws an exception if the user is not logged in
        }
        try {
            var permitted = permit.check(user, action, resource);  // Checks if the user is permitted to perform the action
            if (!permitted) {
                throw new ForbiddenAccessException("Access denied");  // Throws a forbidden exception if authorization fails
            }
        } catch (PermitApiError | IOException e) {
            throw new RuntimeException("Failed to authorize user", e);  // Throws runtime exception if authorization process fails
        }
    }
}
