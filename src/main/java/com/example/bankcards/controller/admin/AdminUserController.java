package com.example.bankcards.controller.admin;


import com.example.bankcards.dto.UserRequest;
import com.example.bankcards.dto.UserResponse;
import com.example.bankcards.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

import static org.springframework.http.HttpStatus.CREATED;

@RestController
@RequestMapping("/api/admin")
public class AdminUserController {

    @Autowired
    UserService userService;

    @GetMapping("/users/{id}")
    public ResponseEntity<UserResponse> getUserById(@PathVariable("id") Long id) {
        return ResponseEntity.ok(userService.findUserById(id));
    }

    @GetMapping("/users")
    public ResponseEntity<List<UserResponse>> getUsers() {
        return ResponseEntity.ok(userService.findAllUsers());
    }

    @PostMapping("/users")
    public ResponseEntity<UserResponse> postUser(@RequestBody UserRequest request) {
        var createdUserDto = userService.createUser(request);
        return ResponseEntity.status(CREATED).body(createdUserDto);
    }

    @DeleteMapping("/users/{id}")
    public ResponseEntity<UserResponse> deleteUser(@PathVariable("id") Long id) {
        userService.deleteUser(id);
        return ResponseEntity.noContent().build();
    }

//    @PutMapping("/users/{id}")
//    public ResponseEntity<Void> updateUser(@PathVariable("id") Long id, @RequestBody User user) {
//
//    }
//
//    @PatchMapping("/users/{id}")
//    public ResponseEntity<Void> patchUser(@PathVariable("id") Long id) {
//
//    }


}
