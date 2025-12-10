package com.example.bankcards.controller.admin;


import com.example.bankcards.dto.admin.UserRequest;
import com.example.bankcards.dto.admin.UserResponse;
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
    public ResponseEntity<List<UserResponse>> getUsers(
            @RequestParam(name = "page",required = false, defaultValue = "0") Integer page,
            @RequestParam(name = "size",required = false, defaultValue = "10") Integer size
    ) {
        return ResponseEntity.ok(userService.findAllUsers(page, size));
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

    @PutMapping("/users/{id}")
    public ResponseEntity<UserResponse> updateUser(@PathVariable("id") Long id, @RequestBody UserRequest updateRequest) {
        return ResponseEntity.ok( userService.updateUser(id,updateRequest));
    }


//todo implement @patch
}
