package br.com.messageApi.entities;

import jakarta.persistence.*;
import org.springframework.security.crypto.password.PasswordEncoder;

import br.com.messageApi.controller.dto.LoginRequest;

import java.util.Set;
import java.util.UUID;

@Entity
@Table(name = "tb_users")
public class User {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @Column(name = "user_id")
    private UUID userId;

    @Column(unique = true)
    private String username;
    
    private String password;

    //Configuração para ação em casacata - Será trazido do banco de dados a role, por isso o EAGER
    @ManyToMany(cascade = CascadeType.ALL, fetch = FetchType.EAGER)
    @JoinTable(
            name = "tb_users_roles",//nome da tabela de junção
            joinColumns = @JoinColumn(name = "user_id"), //nome da coluna com a chave do usuário
            inverseJoinColumns = @JoinColumn(name = "role_id") //id da Role
    )
    private Set<Role> roles;


    public UUID getUserId() {
        return userId;
    }

    public void setUserId(UUID userId) {
        this.userId = userId;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public Set<Role> getRoles() {
        return roles;
    }

    public void setRoles(Set<Role> roles) {
        this.roles = roles;
    }

    // Método para verificar se o login está correto, comparando a senha informada com a senha do banco de dados
    // A senha sem criptografia vem no LoginRequest e a senha criptografada vem do banco de dados
    // O método PasswordEncoder.matches() compara a senha informada com a senha criptografada armazenada no banco de dados e retorna um boolean indicando se as senhas são iguais ou não
    public boolean isLoginCorrect(LoginRequest loginRequest, PasswordEncoder passwordEncoder) {
        return passwordEncoder.matches(loginRequest.password(), this.password);
    }
}
