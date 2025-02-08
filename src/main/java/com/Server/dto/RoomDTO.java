package com.Server.dto;


import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.Data;

import java.math.BigDecimal;
import java.util.List;

@Data
@JsonInclude(JsonInclude.Include.NON_NULL)
public class RoomDTO {

    private String id;
    private String roomType;
    private BigDecimal roomPrice;
    private String imageName;
    private String imageType;
    private byte[] imageData;
    private String roomDescription;
    private List<BookingDTO> bookings;
}
