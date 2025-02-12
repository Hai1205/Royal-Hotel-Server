package com.Server.service.impl;

import com.Server.dto.Pagination;
import com.Server.dto.Response;
import com.Server.dto.RoomDTO;
import com.Server.entity.Booking;
import com.Server.entity.Room;
import com.Server.exception.OurException;
import com.Server.repo.BookingRepository;
import com.Server.repo.RoomRepository;
import com.Server.service.AwsS3Service;
import com.Server.service.interfac.IRoomService;
import com.Server.utils.Utils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
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
    public Response getAllRooms(int page, int limit, String sort, String order) {
        Response response = new Response();

        try {
            Sort.Direction direction = order.equalsIgnoreCase("asc") ? Sort.Direction.ASC : Sort.Direction.DESC;
            Pageable pageable = PageRequest.of(page - 1, limit, Sort.by(direction, sort));

            Page<Room> roomPage = roomRepository.findAll(pageable);

            List<RoomDTO> roomDTOList = Utils.mapRoomListEntityToRoomListDTO(roomPage.getContent());

            response.setStatusCode(200);
            response.setMessage("Successful");
            response.setPagination(new Pagination(roomPage.getTotalElements(), roomPage.getTotalPages(), page));
            response.setRoomList(roomDTOList);
        } catch (Exception e) {
            response.setStatusCode(500);
            response.setMessage("Error occurred while getting all rooms: " + e.getMessage());
        }

        return response;
    }

    @Override
    public Response deleteRoom(String roomId) {
        Response response = new Response();

        try {
            Room room = roomRepository.findById(roomId).orElseThrow(() -> new OurException("Room Not Found"));

            List<String> imageUrls = room.getImageUrls();
            for (String imageUrl : imageUrls) {
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
            Room room = roomRepository.findById(roomId).orElseThrow(() -> new OurException("Room Not Found"));

            if (roomType != null) room.setRoomType(roomType);
            if (roomPrice != null) room.setRoomPrice(roomPrice);
            if (description != null) room.setRoomDescription(description);

            List<String> savedImageUrls = room.getImageUrls();
            for (String imageUrl : savedImageUrls) {
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
            Room room = roomRepository.findById(roomId).orElseThrow(() -> new OurException("Room Not Found"));
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
    public Response getAvailableRoomsByDateAndType(int page, int limit, String sort, String order, LocalDate checkInDate, LocalDate checkOutDate, String roomType) {
        Response response = new Response();

        try {
            Sort.Direction direction = order.equalsIgnoreCase("asc") ? Sort.Direction.ASC : Sort.Direction.DESC;
            Pageable pageable = PageRequest.of(page - 1, limit, Sort.by(direction, sort));

            List<Booking> bookings = bookingRepository.findBookingsByDateRange(checkInDate, checkOutDate);
            List<String> bookedRoomsId = bookings.stream().map(booking -> booking.getRoom().getId()).toList();
            Page<Room> roomPage = roomRepository.findByRoomTypeLikeAndIdNotIn(roomType, bookedRoomsId, pageable);
            List<RoomDTO> roomDTOList = Utils.mapRoomListEntityToRoomListDTO(roomPage.getContent());

            response.setStatusCode(200);
            response.setMessage("successful");
            response.setPagination(new Pagination(roomPage.getTotalElements(), roomPage.getTotalPages(), page));
            response.setRoomList(roomDTOList);
        } catch (OurException e) {
            response.setStatusCode(404);
            response.setMessage(e.getMessage());
        } catch (Exception e) {
            response.setStatusCode(500);
            response.setMessage("Error occurred while getting available rooms by date range: " + e.getMessage());
        }

        return response;
    }

    @Override
    public Response getAllAvailableRooms(int page, int limit, String sort, String order) {
        Response response = new Response();

        try {
            Sort.Direction direction = order.equalsIgnoreCase("asc") ? Sort.Direction.ASC : Sort.Direction.DESC;

            Pageable pageable = PageRequest.of(page - 1, limit, Sort.by(direction, sort));
            Page<Room> roomPage = roomRepository.findAllAvailableRooms(pageable);
            List<RoomDTO> roomDTOList = Utils.mapRoomListEntityToRoomListDTO(roomPage.getContent());

            response.setStatusCode(200);
            response.setMessage("successful");
            response.setPagination(new Pagination(roomPage.getTotalElements(), roomPage.getTotalPages(), page));
            response.setRoomList(roomDTOList);
        } catch (Exception e) {
            response.setStatusCode(500);
            response.setMessage("Error occurred while getting all available rooms: " + e.getMessage());
        }

        return response;
    }
}
