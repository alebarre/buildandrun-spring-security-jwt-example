package br.com.messageApi.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import br.com.messageApi.entities.Role;
import br.com.messageApi.entities.User;

import java.util.UUID;

@Repository
public interface RoleRepository extends JpaRepository<Role, Long> {
    Role findByName(String name);
}
