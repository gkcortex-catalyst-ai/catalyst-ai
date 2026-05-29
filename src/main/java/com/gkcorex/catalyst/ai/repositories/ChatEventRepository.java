package com.gkcorex.catalyst.ai.repositories;

import com.gkcorex.catalyst.ai.entities.ChatEvent;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ChatEventRepository extends JpaRepository<ChatEvent, Long> {
}
