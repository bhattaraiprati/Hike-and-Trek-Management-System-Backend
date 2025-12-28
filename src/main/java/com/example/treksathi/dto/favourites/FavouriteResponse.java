package com.example.treksathi.dto.favourites;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class FavouriteResponse {
    private Integer id;
    private Integer eventId;
    private LocalDateTime addedAt;
    private String message;
}
