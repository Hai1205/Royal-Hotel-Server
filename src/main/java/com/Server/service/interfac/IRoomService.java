package com.Server.service.interfac;

import com.Server.dto.Response;
import org.springframework.web.multipart.MultipartFile;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

public interface IRoomService {

    Response addNewRoom(List<MultipartFile> photos, String roomType, BigDecimal roomPrice, String description);

    List<String> getAllRoomTypes();

    Response getAllRooms(int page, int limit, String sort, String order);

    Response deleteRoom(String roomId);

    Response updateRoom(String roomId, String description, String roomType, BigDecimal roomPrice, List<MultipartFile> photos);

    Response getRoomById(String roomId);

    Response getAvailableRoomsByDateAndType(int page, int limit, String sort, String order, LocalDate checkInDate, LocalDate checkOutDate, String roomType);

    Response getAllAvailableRooms(int page, int limit, String sort, String order);
}
