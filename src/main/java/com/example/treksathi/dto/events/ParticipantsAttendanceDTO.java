package com.example.treksathi.dto.events;

import lombok.Data;
import lombok.Getter;
import lombok.Setter;

@Data
@Getter
@Setter
public class ParticipantsAttendanceDTO {
    private int participantId;
    private String attendanceStatus;

}
