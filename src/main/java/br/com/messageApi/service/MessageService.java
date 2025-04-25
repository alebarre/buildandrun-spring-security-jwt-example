package br.com.messageApi.service;

import br.com.messageApi.dto.CreateMessagetDto;
import br.com.messageApi.dto.FeedDto;
import br.com.messageApi.dto.FeedItemDto;
import br.com.messageApi.entities.Message;
import br.com.messageApi.repository.MessageRepository;
import br.com.messageApi.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.http.ResponseEntity;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationToken;
import org.springframework.stereotype.Service;

import java.util.UUID;

@Service
public class MessageService {

    @Autowired
    private MessageRepository messageRepository;

    @Autowired
    private UserRepository userRepository;

    public FeedDto listarTodasMensagens(int page, int pageSize) {

        try{
            var messages = messageRepository.findAll(
                            PageRequest.of(page, pageSize, Sort.Direction.DESC, "creationTimestamp"))
                    .map(message ->
                            new FeedItemDto(
                                    message.getMessageId(),
                                    message.getContent(),
                                    message.getUser().getUsername())
                    );

            // Retorna o FeedDto com as mensagens paginadas, contendo a lista de mensagens, número da página atual, tamanho da página, total de páginas
            // e o total de elementos.
            return ResponseEntity.ok(new FeedDto(
                    messages.getContent(), page, pageSize, messages.getTotalPages(), messages.getTotalElements())).getBody();
        } catch (RuntimeException e) {
            throw new RuntimeException(e.getCause());
        }

    }

    public ResponseEntity<FeedDto> cadastrarMensagem(CreateMessagetDto dto, JwtAuthenticationToken token) {

        try {
            var user = userRepository.findById(UUID.fromString(token.getName()));

            var message = new Message();

            if (user.isPresent()) {
                message.setUser(user.get());
                message.setContent(dto.content());
            }

            messageRepository.save(message);

            return ResponseEntity.ok().build();
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

}
