package com.boostmytool.store.controllers;

import com.boostmytool.store.exception.UnauthorizedException;
import com.boostmytool.store.service.UserService;
import io.permit.sdk.enforcement.User;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController  // Marks this class as a REST controller, meaning it's capable of handling HTTP requests and returning JSON responses
@RequestMapping("/api/users")  // Sets the base URL path for the controller
public class UserController {
    private final UserService userService;

    // Constructor injection of the UserService, which contains the business logic for user operations
    public UserController(UserService userService) {
        this.userService = userService;
    }

    // Handles POST requests to "/api/users/signup" to create a new user
    @PostMapping("/signup")
    public User signup(@RequestBody String key) {
        return userService.signup(key);  // Calls the signup method from UserService to create a new user
    }

    // Handles POST requests to "/api/users/assign-role" to assign a role to the current user
    @PostMapping("/assign-role")
    public void assignRole(HttpServletRequest request, @RequestBody String role) {
        // Retrieves the current user from the HTTP request attributes
        User currentUser = (User) request.getAttribute("user");

        // Throws an UnauthorizedException if no user is logged in
        if (currentUser == null) {
            throw new UnauthorizedException("Not logged in");
        }

        // Calls the assignRole method from UserService to assign the role to the user
        userService.assignRole(currentUser, role);
    }
}
