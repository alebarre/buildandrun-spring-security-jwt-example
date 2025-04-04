package br.com.messageApi.controller.dto;

public record LoginResponse(String accessToken, Long expiresIn) {
}
