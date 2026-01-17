package com.example.treksathi.dto.event;

import lombok.Data;
import lombok.AllArgsConstructor;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class EventStatsDTO {
    private long totalEvents;
    private long activeEvents;
    private long completedEvents;
    private long cancelledEvents;
}
