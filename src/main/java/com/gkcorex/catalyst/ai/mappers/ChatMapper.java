package com.gkcorex.catalyst.ai.mappers;

import com.gkcorex.catalyst.ai.dtos.chat.ChatResponse;
import com.gkcorex.catalyst.ai.entities.ChatMessage;
import org.mapstruct.Mapper;

import java.util.List;

@Mapper(componentModel = "spring")
public interface ChatMapper {

    List<ChatResponse> mapChatMessagesToChatResponses(List<ChatMessage> chatMessages);

}
