package br.com.messageApi.dto;

public record LoginResponse(String accessToken, Long expiresIn) {
}
