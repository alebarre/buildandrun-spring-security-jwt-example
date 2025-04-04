package br.com.messageApi.controller;

import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationToken;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

import br.com.messageApi.controller.dto.CreateMessagetDto;
import br.com.messageApi.controller.dto.FeedDto;
import br.com.messageApi.controller.dto.FeedItemDto;
import br.com.messageApi.entities.Role;
import br.com.messageApi.entities.Message;
import br.com.messageApi.repository.MessageRepository;
import br.com.messageApi.repository.UserRepository;

import java.util.UUID;

@RestController
public class MessageController {

    private final MessageRepository messageRepository;
    private final UserRepository userRepository;

    public MessageController(MessageRepository messageRepository,
                           UserRepository userRepository) {
        this.messageRepository = messageRepository;
        this.userRepository = userRepository;
    }

    @GetMapping("/feed")
    public ResponseEntity<FeedDto> feed(@RequestParam(value = "page", defaultValue = "0") int page,
                                        @RequestParam(value = "pageSize", defaultValue = "10") int pageSize) {

        var messages = messageRepository.findAll(
                PageRequest.of(page, pageSize, Sort.Direction.DESC, "creationTimestamp"))
                .map(message ->
                        new FeedItemDto(
                                message.getMessageId(),
                                message.getContent(),
                                message.getUser().getUsername())
                );

        return ResponseEntity.ok(new FeedDto(
                messages.getContent(), page, pageSize, messages.getTotalPages(), messages.getTotalElements()));
    }

    @PostMapping("/messages")
    public ResponseEntity<Void> createmessage(@RequestBody CreateMessagetDto dto, JwtAuthenticationToken token) {
    	
        var user = userRepository.findById(UUID.fromString(token.getName()));

        var message = new Message();
        message.setUser(user.get());
        message.setContent(dto.content());

        messageRepository.save(message);

        return ResponseEntity.ok().build();
    }

    @DeleteMapping("/messages/{id}")
    public ResponseEntity<Void> deletemessage(@PathVariable("id") Long messageId, JwtAuthenticationToken token) {
    	
        var user = userRepository.findById(UUID.fromString(token.getName()));
        
        //Verifica se a mensagem existe
        var message = messageRepository.findById(messageId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND));
        
        //Verifica se o usuário é um Admin.
        var isAdmin = user.get().getRoles()
                .stream()
                .anyMatch(role -> role.getName().equalsIgnoreCase(Role.Values.ADMIN.name()));

        //Se ele for um 'admin' ou o 'user' dono da mensagem, pode deletar. Caso não, retorna um 'forbidden'
        if (isAdmin || message.getUser().getUserId().equals(UUID.fromString(token.getName()))) {
            messageRepository.deleteById(messageId);
        } else {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
        }


        return ResponseEntity.ok().build();
    }
}
