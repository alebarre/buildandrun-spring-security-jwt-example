package br.com.messageApi.controller;

import br.com.messageApi.service.MessageService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationToken;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

import br.com.messageApi.dto.CreateMessagetDto;
import br.com.messageApi.dto.FeedDto;
import br.com.messageApi.entities.Role;
import br.com.messageApi.entities.Message;
import br.com.messageApi.repository.MessageRepository;
import br.com.messageApi.repository.UserRepository;

import java.util.UUID;

@RestController
public class MessageController {

    private final MessageRepository messageRepository;
    private final UserRepository userRepository;
    private final MessageService messageService;

    public MessageController(MessageRepository messageRepository,
                             UserRepository userRepository, MessageService messageService) {
        this.messageRepository = messageRepository;
        this.userRepository = userRepository;
        this.messageService = messageService;
    }

    // O método feed é responsável por retornar as mensagens do feed
    // Ele recebe os parâmetros de paginação (page e pageSize) e retorna uma lista de mensagens paginada
    // O método usa o MessageRepository para buscar as mensagens no banco de dados e retorna um FeedDto com as mensagens
    // O FeedDto contém uma lista de FeedItemDto, que representa cada mensagem do feed
    // O FeedItemDto contém o ID da mensagem, o conteúdo da mensagem e o nome do usuário que enviou a mensagem
    // O PageRequest cria uma solicitação de página com os parâmetros de paginação
    // O Sort ordena as mensagens pela data de criação (creationTimestamp) em ordem decrescente
    // O método retorna um ResponseEntity com o FeedDto e o status HTTP 200 (OK)
    // O método também usa o JwtAuthenticationToken para autenticar o usuário que está fazendo a solicitação e se tem a permissão de admin para acessar o feed
    // Se o usuário não tiver a permissão de admin, o método retorna um ResponseEntity com o status HTTP 403 (Forbidden)
    @GetMapping("/feed")
    public FeedDto feed(@RequestParam(value = "page", defaultValue = "0") int page,
                        @RequestParam(value = "pageSize", defaultValue = "10") int pageSize) {

        var retorno = messageService.listarTodasMensagens(page, pageSize);

        return retorno;

    }

    // O método createmessage é responsável por criar uma nova mensagem
    // Ele recebe um CreateMessagetDto com o conteúdo da mensagem e o token JWT do usuário autenticado
    @PostMapping("/messages")
    public ResponseEntity<Void> createmessage(@RequestBody CreateMessagetDto dto, JwtAuthenticationToken token) {
    	
        var retorno = messageService.cadastrarMensagem(dto, token);

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
