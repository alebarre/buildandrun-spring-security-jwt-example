package br.com.messageApi.controller;

import br.com.messageApi.config.SecurityConfig;
import br.com.messageApi.dto.ResponseOtpDTO;
import br.com.messageApi.entities.User;
import br.com.messageApi.repository.UserRepository;
import br.com.messageApi.service.OTPService;
import io.jsonwebtoken.JwtException;
import io.micrometer.common.util.StringUtils;
import jakarta.mail.MessagingException;
import jakarta.persistence.NoResultException;
import org.apache.coyote.Response;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

import java.util.HashMap;
import java.util.Map;

@RestController
public class OTPController {

    private final UserRepository userRepository;
    private final OTPService otpService;

    public OTPController(UserRepository userRepository, OTPService otpService) {
        this.userRepository = userRepository;
        this.otpService = otpService;
    }

    @Transactional
    @PostMapping("/esqueci-a-senha/{usuario}")
    public ResponseOtpDTO esqueciASenha(@PathVariable("usuario") String usuario) throws MessagingException {
        if (StringUtils.isEmpty(usuario)) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "O campo 'email' é obrigatório e não pode estar vazio");
        }

        User emailEncontrado = userRepository.findByUsername(usuario)
                .orElseThrow(() ->
                        new NoResultException(HttpStatus.NO_CONTENT + "Usuário "+ usuario + " não encontrado!")
                );

        if (emailEncontrado != null || StringUtils.isEmpty(emailEncontrado.getUsername())) {
            return otpService.sendEmail(emailEncontrado.getUsername());
        }

        return null;

    }

    @PostMapping("/validate")
    public Map<String, Object> validateOtp(
            @RequestParam String token,
            @RequestParam String userEnteredOtp) {

        try {
            return otpService.validateOtp(token, userEnteredOtp);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

}
