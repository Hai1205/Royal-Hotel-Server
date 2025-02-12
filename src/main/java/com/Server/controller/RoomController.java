package com.Server.controller;


import com.Server.dto.Response;
import com.Server.service.interfac.IRoomService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

@RestController
@RequestMapping("/rooms")
public class RoomController {
    @Autowired
    private IRoomService roomService;

    @PostMapping("/add")
    @PreAuthorize("hasAuthority('ADMIN')")
    public ResponseEntity<Response> addNewRoom(
            @RequestParam(value = "photos", required = false) List<MultipartFile> photos,
            @RequestParam(value = "roomType", required = false) String roomType,
            @RequestParam(value = "roomPrice", required = false) BigDecimal roomPrice,
            @RequestParam(value = "roomDescription", required = false) String roomDescription
    ) {
        if (photos == null || photos.isEmpty() || roomType == null || roomPrice == null) {
            Response response = new Response();
            response.setStatusCode(400);
            response.setMessage("Please Provide values for all fields (photos, roomType, roomPrice)");

            return ResponseEntity.status(response.getStatusCode()).body(response);
        }
        Response response = roomService.addNewRoom(photos, roomType, roomPrice, roomDescription);

        return ResponseEntity.status(response.getStatusCode()).body(response);
    }

    @GetMapping("/all")
    public ResponseEntity<Response> getAllRooms(
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "6") int limit,
            @RequestParam(defaultValue = "createdAt") String sort,
            @RequestParam(defaultValue = "asc") String order)
    {
        Response response = roomService.getAllRooms(page, limit, sort, order);

        return ResponseEntity.status(response.getStatusCode()).body(response);
    }

    @GetMapping("/types")
    public List<String> getRoomTypes() {
        return roomService.getAllRoomTypes();
    }

    @GetMapping("/room-by-id/{roomId}")
    public ResponseEntity<Response> getRoomByID(@PathVariable("roomId") String roomId) {
        Response response = roomService.getRoomById(roomId);

        return ResponseEntity.status(response.getStatusCode()).body(response);
    }

    @GetMapping("/all-available-rooms")
    public ResponseEntity<Response> getAvailableRooms() {
        Response response = roomService.getAllAvailableRooms();

        return ResponseEntity.status(response.getStatusCode()).body(response);
    }

    @GetMapping("/available-rooms-by-date-and-type")
    public ResponseEntity<Response> getAvailableRoomsByDateAndType(
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate checkInDate,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate checkOutDate,
            @RequestParam(required = false) String roomType)
    {
        if (checkInDate == null || checkOutDate == null || roomType.isBlank()) {
            Response response = new Response();
            response.setStatusCode(400);
            response.setMessage("All fields are required(checkInDate,checkOutDate,roomType )");

            return ResponseEntity.status(response.getStatusCode()).body(response);
        }
        Response response = roomService.getAvailableRoomsByDateAndType(checkInDate, checkOutDate, roomType);

        return ResponseEntity.status(response.getStatusCode()).body(response);
    }

    @PutMapping("/update/{roomId}")
    @PreAuthorize("hasAuthority('ADMIN')")
    public ResponseEntity<Response> updateRoom(
            @PathVariable String roomId,
            @RequestParam(value = "photo", required = false) List<MultipartFile> photos,
            @RequestParam(value = "roomType", required = false) String roomType,
            @RequestParam(value = "roomPrice", required = false) BigDecimal roomPrice,
            @RequestParam(value = "roomDescription", required = false) String roomDescription) {
        Response response = roomService.updateRoom(roomId, roomDescription, roomType, roomPrice, photos);

        return ResponseEntity.status(response.getStatusCode()).body(response);
    }

    @DeleteMapping("/delete/{roomId}")
    @PreAuthorize("hasAuthority('ADMIN')")
    public ResponseEntity<Response> deleteRoom(@PathVariable String roomId) {
        Response response = roomService.deleteRoom(roomId);

        return ResponseEntity.status(response.getStatusCode()).body(response);
    }
}
