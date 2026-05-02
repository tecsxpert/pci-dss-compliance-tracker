package com.internship.tool.controller;

import com.internship.tool.service.UserService;
import com.internship.tool.dto.UserDTO;

import jakarta.validation.Valid;   // ✅ validation import

import org.springframework.web.bind.annotation.*;
import java.util.List;

@RestController
public class TestController {

    private final UserService userService;

    public TestController(UserService userService) {
        this.userService = userService;
    }

    // ✅ GET
    @GetMapping("/users")
    public List<UserDTO> getUsers() {
        return userService.getAllUsers();
    }

    // ✅ POST (with validation)
    @PostMapping("/users")
    public UserDTO createUser(@Valid @RequestBody UserDTO userDTO) {
        return userService.saveUser(userDTO);
    }

    // ✅ DELETE
    @DeleteMapping("/users/{id}")
    public String deleteUser(@PathVariable Long id) {
        userService.deleteUser(id);
        return "User deleted";
    }

    // ✅ UPDATE (ADD VALID HERE ALSO)
    @PutMapping("/users/{id}")
    public UserDTO updateUser(@PathVariable Long id,
                             @Valid @RequestBody UserDTO userDTO) {
        return userService.updateUser(id, userDTO);
    }
}