package br.com.messageApi.config;

import io.jsonwebtoken.*;
import com.nimbusds.jose.jwk.JWK;
import com.nimbusds.jose.jwk.JWKSet;
import com.nimbusds.jose.jwk.RSAKey;
import com.nimbusds.jose.jwk.source.ImmutableJWKSet;
import io.jsonwebtoken.security.Keys;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.config.Customizer;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.oauth2.jwt.JwtDecoder;
import org.springframework.security.oauth2.jwt.JwtEncoder;
import org.springframework.security.oauth2.jwt.NimbusJwtDecoder;
import org.springframework.security.oauth2.jwt.NimbusJwtEncoder;
import org.springframework.security.web.SecurityFilterChain;

import javax.crypto.SecretKey;
import java.security.interfaces.RSAPrivateKey;
import java.security.interfaces.RSAPublicKey;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.Date;
import java.util.Map;

@Configuration
@EnableWebSecurity // A anotação @EnableWebSecurity ativa a configuração de segurança da aplicação, permitindo que o Spring Security proteja os endpoints da API
@EnableMethodSecurity // Habilita a segurança em métodos, permitindo o uso de anotações como @PreAuthorize e @PostAuthorize
public class SecurityConfig {

    @Value("${jwt.public.key}")
    private RSAPublicKey publicKey;

    @Value("${jwt.private.key}")
    private RSAPrivateKey privateKey;

    private static final SecretKey SECRET_KEY = Keys.secretKeyFor(SignatureAlgorithm.HS256);
    private static final long OTP_EXPIRATION_MINUTES = 5;

    // Configuração de segurança da aplicação Spring Security
    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {

        http
                .authorizeHttpRequests(authorize -> authorize
                        .requestMatchers(HttpMethod.POST, "/esqueci-a-senha/**").permitAll()
                        .requestMatchers(HttpMethod.POST, "/validate").permitAll()
                        .requestMatchers(HttpMethod.POST, "/users").permitAll()
                        .requestMatchers(HttpMethod.POST, "/login").permitAll()
                        .anyRequest().authenticated())
                .csrf(csrf -> csrf.disable()) // Desabilita o CSRF, pois não é necessário para APIs REST, em ambiente de produção deve ser habilitado
                .cors(cors -> cors.disable()) // Desabilita o CORS apenas localmente
                .oauth2ResourceServer(oauth2 -> oauth2.jwt(Customizer.withDefaults())) // Configura o servidor de recursos OAuth2 para usar JWT
                .sessionManagement(session -> session.sessionCreationPolicy(SessionCreationPolicy.STATELESS)); // Define a política de sessão como sem estado (stateless), ou seja, não armazena informações de sessão no servidor

        return http.build();
    }

    // Configuração do JWT Decoder, que é responsável por decodificar o token JWT com a biblioteca Nimbus
    // O JWT Decoder é usado para validar o token JWT recebido na requisição e extrair as informações contidas nele
    @Bean
    public JwtDecoder jwtDecoder() {
        return NimbusJwtDecoder.withPublicKey(publicKey).build();
    }


    // Configuração do JWT Encoder, que é responsável por criar o token JWT
    // O JWT Encoder é usado para assinar o token JWT com a chave privada, garantindo que o token seja autêntico e não tenha sido alterado
    @Bean
    public JwtEncoder jwtEncoder() {
        // O JWK é usado para assinar o token JWT e garantir sua autenticidade
        JWK jwk = new RSAKey.Builder(this.publicKey).privateKey(privateKey).build(); // Cria um JWK (JSON Web Key) com a chave pública e privada

        // O JWKSet é usado para armazenar um conjunto de chaves públicas e privadas, permitindo que o servidor valide e assine tokens JWT
        var jwks = new ImmutableJWKSet<>(new JWKSet(jwk));// Cria um JWKSet (JSON Web Key Set) com o JWK criado anteriormente

        return new NimbusJwtEncoder(jwks);// Cria um JwtEncoder com o JWKSet criado anteriormente
    }

    // Configuração do BCryptPasswordEncoder, que é responsável por codificar senhas usando o algoritmo BCrypt
    // O BCryptPasswordEncoder é usado para garantir que as senhas sejam armazenadas de forma segura no banco de dados
    @Bean
    public BCryptPasswordEncoder bCryptPasswordEncoder() {
        return new BCryptPasswordEncoder();
    }

    //Configuração do token JWT que fará parte do "esquecia a senha"
    //Envio de token para o email e validaçao na plataforma para alteração de senha
    public static String createToken(String email, String otp) {

        Instant now = Instant.now();
        Instant expiration = now.plus(OTP_EXPIRATION_MINUTES, ChronoUnit.MINUTES);

        return Jwts.builder()
                .claim("email", email)
                .claim("otp", otp)
                .setIssuedAt(Date.from(now))
                .setExpiration(Date.from(expiration))
                .signWith(SECRET_KEY)
                .compact();
    }

    public static Map<String, Object> parseToken(String token) {
        Jws<Claims> jws = Jwts.parserBuilder()
                .setSigningKey(SECRET_KEY)
                .build()
                .parseClaimsJws(token);

        return jws.getBody();
    }

    public static boolean isTokenValid(String token) {
        try {
            Jwts.parserBuilder()
                    .setSigningKey(SECRET_KEY)
                    .build()
                    .parseClaimsJws(token);
            return true;
        } catch (JwtException e) {
            return false;
        }
    }

}
