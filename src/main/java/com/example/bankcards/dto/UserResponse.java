package com.example.bankcards.dto;

import java.util.List;

public record UserResponse(Long id, String name, String hash, List<String> roles) {

}
