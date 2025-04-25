package br.com.messageApi.dto;

public record ResponseOtpDTO(String token, String otp, String message) {
}
