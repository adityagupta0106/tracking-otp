package com.serviceplus.UserManagement.Repository;

import com.serviceplus.UserManagement.Dto.UserCreationHistory;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.time.Instant;
import java.util.Optional;

@Repository
public interface UserCreationHistoryRepository extends JpaRepository<UserCreationHistory, Long> {

    Page<UserCreationHistory> findByCreatedByAndCreatedOnBetweenOrderByCreatedOnDesc(Long userID, Instant from, Instant to, Pageable pageable);

    Page<UserCreationHistory> findByCreatedByOrderByCreatedOnDesc(Long userID, Pageable pageable);

    Optional<UserCreationHistory> findByHistoryIdAndCreatedBy(Long historyId, Long userID);
}
