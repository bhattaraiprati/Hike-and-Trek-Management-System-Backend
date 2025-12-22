package com.example.treksathi.service;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import com.example.treksathi.Interfaces.IChatRoomService;
import com.example.treksathi.enums.ChatRoomType;
import com.example.treksathi.enums.Role;
import com.example.treksathi.exception.ChatRoomNotFoundException;
import com.example.treksathi.exception.EventNotFoundException;
import com.example.treksathi.exception.NotFoundException;
import com.example.treksathi.exception.UnauthorizedException;
import com.example.treksathi.exception.UsernameNotFoundException;
import com.example.treksathi.model.ChatMessage;
import com.example.treksathi.model.ChatRoom;
import com.example.treksathi.model.Event;
import com.example.treksathi.model.EventRegistration;
import com.example.treksathi.model.Organizer;
import com.example.treksathi.model.User;
import com.example.treksathi.record.chat.ChatOutputDTO;
import com.example.treksathi.record.chat.ChatRoomDTO;
import com.example.treksathi.repository.ChatMessageRepository;
import com.example.treksathi.repository.ChatRoomRepository;
import com.example.treksathi.repository.EventRegistrationRepository;
import com.example.treksathi.repository.EventRepository;
import com.example.treksathi.repository.OrganizerRepository;
import com.example.treksathi.repository.UserRepository;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Service
@RequiredArgsConstructor
@Transactional
@Slf4j
public class ChatRoomService  implements IChatRoomService {

    private final ChatRoomRepository chatRoomRepository;
    private final ChatMessageRepository chatMessageRepository;
    private final EventRepository eventRepository;
    private final EventRegistrationRepository eventRegistrationRepository;
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

    public ChatRoomDTO registerParticipantsForEventChat(int eventId, String userEmail) {
        User user = getUserByEmail(userEmail);
        Event event = getEventById(eventId);

        Optional<List<EventRegistration>> registrationsOpt = eventRegistrationRepository.findByUserId(user.getId());
        boolean isRegistered = registrationsOpt
                .map(list -> list.stream().anyMatch(reg -> reg.getEvent().getId() == eventId))
                .orElse(false);

        if (!isRegistered) {
            throw new ChatRoomNotFoundException("Please register to this event");
        }

        ChatRoom chatRoom = chatRoomRepository.findByEvent(event).orElseThrow(() ->
                new ChatRoomNotFoundException("No chat room found for this event"));


        if (!chatRoom.getParticipants().contains(user)) {
            chatRoom.getParticipants().add(user);
            chatRoom = chatRoomRepository.save(chatRoom);
        }

        return mapToChatRoomDTO(chatRoom);
    }


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

    public List<ChatRoomDTO> getUserChatRooms(String userEmail) {
        User user = getUserByEmail(userEmail);
        List<ChatRoom> rooms = chatRoomRepository.findByParticipantsContaining(user);

        if (rooms.isEmpty()) {
            throw new ChatRoomNotFoundException("Please join a chat room to view messages.");
        }

        return rooms.stream()
                .map(this::mapToChatRoomDTO)
                .collect(Collectors.toList());
    }

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
                .orElseThrow(() -> new UsernameNotFoundException("User not found"));
    }

    private Event getEventById(int eventId) {
        return eventRepository.findById(eventId)
                .orElseThrow(() -> new EventNotFoundException("Event not found"));
    }

    private ChatRoom getChatRoomById(Long roomId) {
        return chatRoomRepository.findById(roomId.intValue())
                .orElseThrow(() -> new NotFoundException("Chat room not found"));
    }

    private void validateOrganizerRole(User user) {
        if (user.getRole() != Role.ORGANIZER) {
            throw new UnauthorizedException("Only organizers can create chat rooms");
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
