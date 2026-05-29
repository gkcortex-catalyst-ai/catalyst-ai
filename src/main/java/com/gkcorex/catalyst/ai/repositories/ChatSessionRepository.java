package com.gkcorex.catalyst.ai.repositories;

import com.gkcorex.catalyst.ai.entities.ChatSession;
import com.gkcorex.catalyst.ai.entities.ChatSessionId;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ChatSessionRepository extends JpaRepository<ChatSession, ChatSessionId> {
}
