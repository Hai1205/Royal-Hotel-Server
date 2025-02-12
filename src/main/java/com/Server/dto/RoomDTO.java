package com.Server.dto;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.Data;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;

@Data
@JsonInclude(JsonInclude.Include.NON_NULL)
public class RoomDTO {
    private String id;

    private String roomType;

    private BigDecimal roomPrice;

    private List<String> imageUrls;

    private String roomDescription;

    private List<BookingDTO> bookings;

    private Instant createdAt;
}
