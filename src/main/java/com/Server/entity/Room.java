package com.Server.entity;

import lombok.Data;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.DBRef;
import org.springframework.data.mongodb.core.mapping.Document;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;

@Data
@Document(collection = "rooms")
public class Room {
    @Id
    private String id;

    private String roomType;

    private BigDecimal roomPrice;

    private String roomDescription;

    private List<String> imageUrls;

    @DBRef
    private List<Booking> bookings = new ArrayList<>();

    @CreatedDate
    private Instant createdAt;

    @Override
    public String toString() {
        return "Room{" +
                "id=" + id +
                ", roomType='" + roomType + '\'' +
                ", roomPrice=" + roomPrice +
                ", description='" + roomDescription + '\'' +
                ", createdAt=" + createdAt +
                '}';
    }
}