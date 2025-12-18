package com.example.treksathi.Interfaces;

import com.example.treksathi.record.chat.ChatOutputDTO;
import com.example.treksathi.record.chat.ChatRoomDTO;

import java.util.List;

public interface IChatRoomService {
    ChatRoomDTO createChatRoom(int eventId, String name, String userEmail);
    ChatRoomDTO registerParticipantsForEventChat(int eventId, String userEmail);
    List<ChatRoomDTO> getChatRoomDetails(int eventId, String userEmail);
    List<ChatRoomDTO> getUserChatRooms(String userEmail);
    List<ChatOutputDTO> getMessages(Long roomId, int limit, String userEmail);
}
