package br.com.messageApi.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import br.com.messageApi.entities.Message;

@Repository
public interface MessageRepository extends JpaRepository<Message, Long> {

}
