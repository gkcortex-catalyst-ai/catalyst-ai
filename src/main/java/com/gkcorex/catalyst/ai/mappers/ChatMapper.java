package com.gkcorex.catalyst.ai.mappers;

import com.gkcorex.catalyst.ai.dtos.chat.ChatResponse;
import com.gkcorex.catalyst.ai.entities.ChatMessage;
import java.util.List;
import org.mapstruct.Mapper;

@Mapper(componentModel = "spring")
public interface ChatMapper {

  List<ChatResponse> mapChatMessagesToChatResponses(List<ChatMessage> chatMessages);
}
