package com.example.treksathi.controller;

import com.example.treksathi.record.chat.ChatOutputDTO;
import com.example.treksathi.record.chat.ChatRoomDTO;
import com.example.treksathi.service.ChatRoomService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/chat")
@RequiredArgsConstructor
@Slf4j
public class ChatRoomRestController {

    private final ChatRoomService chatRoomService;

    @PostMapping("/rooms/create/{eventId}")
    public ResponseEntity<ChatRoomDTO> createChatRoom(
            @PathVariable int eventId,
            @RequestParam String name,
            Authentication auth) {
        return ResponseEntity.ok(chatRoomService.createChatRoom(eventId, name, auth.getName()));
    }

//    @GetMapping("/rooms/{eventId}")
//    public ResponseEntity<String> getOrganizerChatRooms(
//            @PathVariable int eventId) {
//        return ResponseEntity.ok("test success");
//    }

    @GetMapping("/rooms/{eventId}")
    public ResponseEntity<List<ChatRoomDTO>> getOrganizerChatRooms(
            @PathVariable int eventId,
            Authentication auth) {
        log.info("Fetching chat rooms for event ID: {} by user: {}", eventId, auth.getName());
        return ResponseEntity.ok(chatRoomService.getChatRoomDetails(eventId, auth.getName()));
    }
    @GetMapping("/rooms")
    public List<ChatRoomDTO> getUserChatRooms(Authentication auth) {
        return chatRoomService.getUserChatRooms(auth.getName());
    }

    @GetMapping("/rooms/{roomId}/messages")
    public List<ChatOutputDTO> getMessages(
            @PathVariable Long roomId,
            @RequestParam(defaultValue = "50") int limit,
            Authentication auth) {
        return chatRoomService.getMessages(roomId, limit, auth.getName());
    }
}
