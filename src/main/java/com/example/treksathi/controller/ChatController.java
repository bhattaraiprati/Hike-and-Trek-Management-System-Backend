package com.example.treksathi.controller;


import com.example.treksathi.model.ChatMessage;
import com.example.treksathi.model.ChatRoom;
import com.example.treksathi.model.User;
import com.example.treksathi.record.chat.ChatMessageDTO;
import com.example.treksathi.record.chat.ChatOutputDTO;
import com.example.treksathi.repository.ChatMessageRepository;
import com.example.treksathi.repository.ChatRoomRepository;
import com.example.treksathi.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.messaging.handler.annotation.DestinationVariable;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;

@Controller
@RequiredArgsConstructor
public class ChatController {

    private final SimpMessagingTemplate messagingTemplate;
    private final ChatMessageRepository chatMessageRepository;
    private final ChatRoomRepository chatRoomRepository;
    private final UserRepository userRepository;


    @MessageMapping("/chat.send/{chatRoomId}")
    @Transactional
    public void sendMessage(@DestinationVariable Long chatRoomId,
                            @Payload ChatMessageDTO messageDTO,
                            Authentication authentication) {

        User sender = userRepository.findByEmail(authentication.getName())
                .orElseThrow(() -> new RuntimeException("User not found"));

        // Validate user is in the room (optional but recommended)
        if (!chatRoomRepository.existsByIdAndParticipantsContaining(chatRoomId, sender)) {
            throw new RuntimeException("Not authorized to send message in this room");
        }

        ChatRoom chatRoom = chatRoomRepository.getReferenceById(chatRoomId);

        ChatMessage chatMessage = new ChatMessage();
        chatMessage.setChatRoom(chatRoom);
        chatMessage.setSender(sender);
        chatMessage.setContent(messageDTO.content());
        chatMessage.setTimestamp(LocalDateTime.now());

        chatMessage = chatMessageRepository.save(chatMessage);

        ChatOutputDTO output = new ChatOutputDTO(
                chatMessage.getId(),
                chatRoomId,
                chatMessage.getContent(),
                (long) sender.getId(),
                sender.getName(),
                chatMessage.getTimestamp()
        );

        // Broadcast to all subscribers of this room
        messagingTemplate.convertAndSend("/topic/room/" + chatRoomId, output);
    }

    @MessageMapping("/chat.join/{chatRoomId}")
    public void joinRoom(@DestinationVariable Long chatRoomId, Authentication authentication) {
        // Broadcast user joined (optional)
        messagingTemplate.convertAndSend("/topic/room/" + chatRoomId + "/presence",
                authentication.getName() + " joined");
    }

    @MessageMapping("/chat.leave/{chatRoomId}")
    public void leaveRoom(@DestinationVariable Long chatRoomId, Authentication authentication) {
        messagingTemplate.convertAndSend("/topic/room/" + chatRoomId + "/presence",
                authentication.getName() + " left");
    }
}
