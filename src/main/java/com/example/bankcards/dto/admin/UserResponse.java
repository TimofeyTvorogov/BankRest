package com.example.bankcards.dto.admin;

import java.util.List;

public record UserResponse(Long id, String name, String hash, List<String> roles) {

}
