package br.com.messageApi.controller;

import java.time.Instant;
import java.util.stream.Collectors;

import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.oauth2.jwt.JwtClaimsSet;
import org.springframework.security.oauth2.jwt.JwtEncoder;
import org.springframework.security.oauth2.jwt.JwtEncoderParameters;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;	

import br.com.messageApi.dto.LoginRequest;
import br.com.messageApi.dto.LoginResponse;
import br.com.messageApi.entities.Role;
import br.com.messageApi.repository.UserRepository;

@RestController
public class TokenController {

    private final JwtEncoder jwtEncoder;
    private final UserRepository userRepository;
    private BCryptPasswordEncoder passwordEncoder;

    public TokenController(JwtEncoder jwtEncoder,
                           UserRepository userRepository,
                           BCryptPasswordEncoder passwordEncoder) {
        this.jwtEncoder = jwtEncoder;
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
    }


    // O método login é responsável por autenticar o usuário e gerar o token JWT
    // O método recebe um LoginRequest com o nome de usuário e a senha do usuário
    // O método verifica se o usuário existe no banco de dados e se a senha está correta
    // Se o usuário e a senha estiverem corretos, o método gera um token JWT com as informações do usuário e retorna o token para o cliente
    @PostMapping("/login")
    public ResponseEntity<LoginResponse> login(@RequestBody LoginRequest loginRequest) {

        var user = userRepository.findByUsername(loginRequest.username());

        if (user.isEmpty() || !user.get().isLoginCorrect(loginRequest, passwordEncoder)) {
            throw new BadCredentialsException("user or password is invalid!");
        }

        // Caso chegue até aqui seignifica que o usuário e a senha estão corretos
        // Então vamos gerar o token JWT para o usuário
        // O token JWT é gerado com as informações do usuário, como o ID do usuário e os papéis (roles) do usuário
        // Configurar os claims do token JWT 

        var now = Instant.now();
        var expiresIn = 1000L;

        // Scopes
        // O scope é uma lista de permissões que o usuário tem no sistema
        // Através do 'stream'faz o 'map' que pega no nome da role e depois um collect e um join através de espaços
        // O resultado é uma string com os nomes das roles separados por espaço
        // Exemplo: "ROLE_USER ROLE_ADMIN"
        var scopes = user.get().getRoles()
                .stream()
                .map(Role::getName)
                .collect(Collectors.joining(" "));
        // Claims
        // Claims são as informações que serão armazenadas no token JWT
        var claims = JwtClaimsSet.builder()
                .issuer("message-api-backend")
                .subject(user.get().getUserId().toString())
                .issuedAt(now)
                .expiresAt(now.plusSeconds(expiresIn))
                .claim("scope", scopes)
                .build();
        
        System.out.println("Issuer: " + claims.getClaims().get("iss"));
        System.out.println("Role: " + claims.getClaims().get("scope"));
        
        // O token JWT é gerado através dos claims gerados acima. Informações do usuário, como o ID do usuário e os papéis (roles) do usuário
        var jwtValue = jwtEncoder.encode(JwtEncoderParameters.from(claims)).getTokenValue();

        return ResponseEntity.ok(new LoginResponse(jwtValue, expiresIn));
    }
}
