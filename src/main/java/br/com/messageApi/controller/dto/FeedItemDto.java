package br.com.messageApi.controller.dto;

public record FeedItemDto(long messageId, String content, String username) {
}
