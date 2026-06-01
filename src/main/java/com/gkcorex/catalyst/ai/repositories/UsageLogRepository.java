package com.gkcorex.catalyst.ai.repositories;

import com.gkcorex.catalyst.ai.entities.UsageLog;
import java.time.LocalDate;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface UsageLogRepository extends JpaRepository<UsageLog, Long> {
  Optional<UsageLog> findByUserIdAndDate(Long userId, LocalDate today);
}
