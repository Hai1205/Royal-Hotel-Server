package com.Server.service.impl;

import com.Server.dto.Response;
import com.Server.dto.RoomDTO;
import com.Server.model.Booking;
import com.Server.model.Room;
import com.Server.exception.OurException;
import com.Server.repo.BookingRepository;
import com.Server.repo.RoomRepository;
import com.Server.service.AwsS3Service;
import com.Server.service.interfac.IRoomService;
import com.Server.utils.Utils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

@Service
public class RoomService implements IRoomService {
    @Autowired
    private RoomRepository roomRepository;

    @Autowired
    private BookingRepository bookingRepository;

    @Autowired
    private AwsS3Service awsS3Service;

    @Override
    public Response addNewRoom(List<MultipartFile> photos, String roomType, BigDecimal roomPrice, String description) {
        Response response = new Response();

        try {
            List<String> imageUrls = new ArrayList<>();

            for (MultipartFile photo : photos) {
                String imageUrl = awsS3Service.saveImageToS3(photo);
                imageUrls.add(imageUrl);
            }

            Room room = new Room();
            room.setImageUrls(imageUrls);
            room.setRoomPrice(roomPrice);
            room.setRoomType(roomType);
            room.setRoomDescription(description);

            Room savedRoom = roomRepository.save(room);
            RoomDTO roomDTO = Utils.mapRoomEntityToRoomDTO(savedRoom);

            response.setStatusCode(200);
            response.setMessage("successful");
            response.setRoom(roomDTO);
        } catch (Exception e) {
            response.setStatusCode(500);
            response.setMessage("Error occurred while saving a room: " + e.getMessage());
        }

        return response;
    }

    @Override
    public List<String> getAllRoomTypes() {
        return roomRepository.findDistinctRoomType();
    }

    @Override
    public Response getAllRooms() {
        Response response = new Response();

        try {
            List<Room> roomList = roomRepository.findAll();
            List<RoomDTO> roomDTOList = Utils.mapRoomListEntityToRoomListDTO(roomList);

            response.setStatusCode(200);
            response.setMessage("successful");
            response.setRoomList(roomDTOList);
        } catch (Exception e) {
            response.setStatusCode(500);
            response.setMessage("Error occurred while getting  all room: " + e.getMessage());
        }

        return response;
    }

    @Override
    public Response deleteRoom(String roomId) {
        Response response = new Response();

        try {
            Room room = roomRepository.findById(roomId).orElseThrow(() -> new OurException("Room Not Found"));

            List<String> imageUrls = room.getImageUrls();
            for(String imageUrl : imageUrls){
                awsS3Service.deleteImageFromS3(imageUrl);
            }

            roomRepository.deleteById(roomId);

            response.setStatusCode(200);
            response.setMessage("successful");
        } catch (OurException e) {
            response.setStatusCode(404);
            response.setMessage(e.getMessage());
        } catch (Exception e) {
            response.setStatusCode(500);
            response.setMessage("Error occurred while deleting  a room: " + e.getMessage());
        }
        return response;
    }

    @Override
    public Response updateRoom(String roomId, String description, String roomType, BigDecimal roomPrice, List<MultipartFile> photos) {
        Response response = new Response();

        try {
            Room room = roomRepository.findById(roomId).orElseThrow(()-> new OurException("Room Not Found"));

            if (roomType != null) room.setRoomType(roomType);
            if (roomPrice != null) room.setRoomPrice(roomPrice);
            if (description != null) room.setRoomDescription(description);

            List<String> savedImageUrls = room.getImageUrls();
            for(String imageUrl : savedImageUrls){
                awsS3Service.deleteImageFromS3(imageUrl);
            }

            if (photos != null && !photos.isEmpty()) {
                List<String> imageUrls = new ArrayList<>();
                for (MultipartFile photo : photos) {
                    String imageUrl = awsS3Service.saveImageToS3(photo);
                    imageUrls.add(imageUrl);
                }

                room.setImageUrls(imageUrls);
            }

            Room updatedRoom = roomRepository.save(room);
            RoomDTO roomDTO = Utils.mapRoomEntityToRoomDTO(updatedRoom);

            response.setStatusCode(200);
            response.setMessage("successful");
            response.setRoom(roomDTO);
        } catch (OurException e) {
            response.setStatusCode(404);
            response.setMessage(e.getMessage());
        } catch (Exception e) {
            response.setStatusCode(500);
            response.setMessage("Error occurred while updating  a room: " + e.getMessage());
        }

        return response;
    }

    @Override
    public Response getRoomById(String roomId) {
        Response response = new Response();

        try {
            Room room = roomRepository.findById(roomId).orElseThrow(()-> new OurException("Room Not Found"));
            RoomDTO roomDTO = Utils.mapRoomEntityToRoomDTOPlusBookings(room);

            response.setStatusCode(200);
            response.setMessage("successful");
            response.setRoom(roomDTO);
        } catch (OurException e) {
            response.setStatusCode(404);
            response.setMessage(e.getMessage());
        } catch (Exception e) {
            response.setStatusCode(500);
            response.setMessage("Error occurred while getting  a room by id: " + e.getMessage());
        }

        return response;
    }

    @Override
    public Response getAvailableRoomsByDateAndType(LocalDate checkInDate, LocalDate checkOutDate, String roomType) {
        Response response = new Response();

        try {
            List<Booking> bookings = bookingRepository.findBookingsByDateRange(checkInDate, checkOutDate);
            List<String> bookedRoomsId = bookings.stream().map(booking -> booking.getRoom().getId()).toList();

            List<Room> availableRooms = roomRepository.findByRoomTypeLikeAndIdNotIn(roomType, bookedRoomsId);
            List<RoomDTO> roomDTOList = Utils.mapRoomListEntityToRoomListDTO(availableRooms);

            response.setStatusCode(200);
            response.setMessage("successful");
            response.setRoomList(roomDTOList);
        } catch (OurException e) {
            response.setStatusCode(404);
            response.setMessage(e.getMessage());
        } catch (Exception e) {
            response.setStatusCode(500);
            response.setMessage("Error occurred while getting  available rooms by date range: " + e.getMessage());
        }

        return response;
    }

    @Override
    public Response getAllAvailableRooms() {
        Response response = new Response();

        try {
            List<Room> roomList = roomRepository.findAllAvailableRooms();
            List<RoomDTO> roomDTOList = Utils.mapRoomListEntityToRoomListDTO(roomList);

            response.setStatusCode(200);
            response.setMessage("successful");
            response.setRoomList(roomDTOList);
        }catch (Exception e) {
            response.setStatusCode(500);
            response.setMessage("Error occurred while getting  all available rooms: " + e.getMessage());
        }

        return response;
    }
}
