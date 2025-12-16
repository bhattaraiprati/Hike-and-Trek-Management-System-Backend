package com.example.treksathi.service;

import com.example.treksathi.enums.ChatRoomType;
import com.example.treksathi.enums.Role;
import com.example.treksathi.model.*;
import com.example.treksathi.record.chat.ChatOutputDTO;
import com.example.treksathi.record.chat.ChatRoomDTO;
import com.example.treksathi.repository.*;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Transactional
@Slf4j
public class ChatRoomService {

    private final ChatRoomRepository chatRoomRepository;
    private final ChatMessageRepository chatMessageRepository;
    private final EventRepository eventRepository;
    private final UserRepository userRepository;
    private final OrganizerRepository organizerRepository;

    public ChatRoomDTO createChatRoom(int eventId, String name, String userEmail) {
        User user = getUserByEmail(userEmail);
        validateOrganizerRole(user);



        Event event = getEventById(eventId);

        ChatRoom chatRoom = buildChatRoom(name, event, user);
        chatRoom = chatRoomRepository.save(chatRoom);

        return mapToChatRoomDTO(chatRoom);
    }

    @Transactional(readOnly = true)
    public List<ChatRoomDTO> getChatRoomDetails(int eventId, String userEmail) {
        User user = getUserByEmail(userEmail);
        Event event = getEventById(eventId);
        Organizer organizer = organizerRepository.findByUser(user);
        ChatRoom room = chatRoomRepository
                .findByOrganizerAndEvent(organizer, event)
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.NOT_FOUND,
                        "No chat room found for this event and organizer"
                ));

        return List.of(mapToChatRoomDTO(room));
    }

    @Transactional(readOnly = true)
    public List<ChatRoomDTO> getUserChatRooms(String userEmail) {
        User user = getUserByEmail(userEmail);
        List<ChatRoom> rooms = chatRoomRepository.findByParticipantsContaining(user);

        if (rooms.isEmpty()) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "No chat rooms found for user");
        }

        return rooms.stream()
                .map(this::mapToChatRoomDTO)
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public List<ChatOutputDTO> getMessages(Long roomId, int limit, String userEmail) {
        User user = getUserByEmail(userEmail);
        ChatRoom room = getChatRoomById(roomId);

        validateUserInRoom(user, room);

        Pageable pageable = PageRequest.of(0, limit);
        List<ChatMessage> messages = chatMessageRepository
                .findByChatRoomOrderByTimestampDesc(room, pageable);

        return messages.stream()
                .map(msg -> mapToChatOutputDTO(msg, roomId))
                .collect(Collectors.toList());
    }

    // Private helper methods

    private User getUserByEmail(String email) {
        return userRepository.findByEmail(email)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "User not found"));
    }

    private Event getEventById(int eventId) {
        return eventRepository.findById(eventId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Event not found"));
    }

    private ChatRoom getChatRoomById(Long roomId) {
        return chatRoomRepository.findById(roomId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Chat room not found"));
    }

    private void validateOrganizerRole(User user) {
        if (user.getRole() != Role.ORGANIZER) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Only organizers can create chat rooms");
        }
    }

    private void validateUserInRoom(User user, ChatRoom room) {
        if (!room.getParticipants().contains(user)) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "User is not a participant of this chat room");
        }
    }

    private ChatRoom buildChatRoom(String name, Event event, User user) {
        ChatRoom chatRoom = new ChatRoom();
        chatRoom.setName(name);
        chatRoom.setType(ChatRoomType.EVENT_GROUP);
        chatRoom.setEvent(event);
        chatRoom.setOrganizer(user.getOrganizer());
        chatRoom.getParticipants().add(user);
        return chatRoom;
    }

    private ChatRoomDTO mapToChatRoomDTO(ChatRoom room) {
        return new ChatRoomDTO(
                (long) room.getId(),
                room.getName(),
                room.getType().name(),
                room.getEvent() != null ? room.getEvent().getTitle() : "General Channel",
                room.getParticipants().size()
        );
    }

    private ChatOutputDTO mapToChatOutputDTO(ChatMessage msg, Long roomId) {
        return new ChatOutputDTO(
                msg.getId(),
                roomId,
                msg.getContent(),
                (long) msg.getSender().getId(),
                msg.getSender().getName(),
                msg.getTimestamp()
        );
    }
}
