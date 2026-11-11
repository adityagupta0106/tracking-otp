package com.serviceplus.UserManagement.Kafka.repository;

import com.serviceplus.UserManagement.Kafka.entity.KafkaOutboxEvent;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface KafkaOutboxRepository extends JpaRepository<KafkaOutboxEvent, String> {

    List<KafkaOutboxEvent> findTop100ByStatusOrderByCreatedAtAsc(String status);

    List<KafkaOutboxEvent> findTop300ByStatusOrderByCreatedAtAsc(String outboxStatusPending);
}