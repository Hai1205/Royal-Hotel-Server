package com.Server.service.impl;

import com.Server.dto.*;
import com.Server.entity.*;
import com.Server.exception.OurException;
import com.Server.repo.*;
import com.Server.service.interfac.IBookingService;
import com.Server.utils.Utils;
import jakarta.mail.MessagingException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

@Service
public class BookingService implements IBookingService {
    @Autowired
    private BookingRepository bookingRepository;

    @Autowired
    private RoomRepository roomRepository;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private EmailService emailService;

    @Override
    public Response saveBooking(String rooId, String userId, Booking bookingRequest) {
        Response response = new Response();

        try {
            if (bookingRequest.getCheckOutDate().isBefore(bookingRequest.getCheckInDate())) {
                throw new IllegalArgumentException("Check in date must come before check out date");
            }

            Room room = roomRepository.findById(rooId).orElseThrow(() -> new OurException("Room Not Found"));
            User user = userRepository.findById(userId).orElseThrow(() -> new OurException("User Not Found"));

            List<Booking> existingBookings = room.getBookings();
            if (!roomIsAvailable(bookingRequest, existingBookings)) {
                throw new OurException("Room not Available for the selected date range");
            }

            String bookingConfirmationCode = Utils.generateRandomConfirmationCode(10);
            bookingRequest.setRoom(room);
            bookingRequest.setUser(user);
            bookingRequest.setBookingConfirmationCode(bookingConfirmationCode);

            Booking savedBooking = bookingRepository.save(bookingRequest);
            List<Booking> userBookings = user.getBookings();
            userBookings.add(savedBooking);
            user.setBookings(userBookings);
            userRepository.save(user);

            List<Booking> roomBookings = room.getBookings();
            roomBookings.add(savedBooking);
            room.setBookings(roomBookings);
            roomRepository.save(room);

            String subject = "Xác nhận đặt phòng thành công!";
            String templateName = "mail-confirm-password";
            Map<String, Object> variables = new HashMap<>();
            variables.put("email", user.getEmail());
            variables.put("orderAmount", bookingRequest.getCost());
            variables.put("orderCode", bookingConfirmationCode);
            emailService.sendHtmlEmail(user.getEmail(), subject, templateName, variables);

            response.setStatusCode(200);
            response.setMessage("successful");
            response.setBookingConfirmationCode(bookingConfirmationCode);
        } catch (MessagingException e) {
            response.setStatusCode(403);
            response.setMessage(e.getMessage());
        } catch (OurException e) {
            response.setStatusCode(404);
            response.setMessage(e.getMessage());
        } catch (Exception e) {
            response.setStatusCode(500);
            response.setMessage("Error saving a  booking " + e.getMessage());
        }

        return response;
    }

    @Override
    public Response findBookingByConfirmationCode(String confirmationCode) {
        Response response = new Response();

        try {
            Booking booking = bookingRepository.findByBookingConfirmationCode(confirmationCode).orElseThrow(() -> new OurException("Booking Not Found"));
            BookingDTO bookingDTO = Utils.mapBookingEntityToBookingDTOPlusBookedRooms(booking, true);

            response.setMessage("successful");
            response.setStatusCode(200);
            response.setBooking(bookingDTO);
        } catch (OurException e) {
            response.setStatusCode(404);
            response.setMessage(e.getMessage());
        } catch (Exception e) {
            response.setStatusCode(500);
            response.setMessage("Error getting booking by confirmation code " + e.getMessage());
        }

        return response;
    }

    @Override
    public Response getAllBookings(int page, int limit, String sort, String order) {
        Response response = new Response();

        try {
            Sort.Direction direction = order.equalsIgnoreCase("asc") ? Sort.Direction.ASC : Sort.Direction.DESC;
            Pageable pageable = PageRequest.of(page - 1, limit, Sort.by(direction, sort));
            Page<Booking> bookingPage = bookingRepository.findAll(pageable);

            List<BookingDTO> bookingDTOList = Utils.mapBookingListEntityToBookingListDTO(bookingPage.getContent());

            response.setStatusCode(200);
            response.setMessage("successful");
            response.setPagination(new Pagination(bookingPage.getTotalElements(), bookingPage.getTotalPages(), page));
            response.setBookingList(bookingDTOList);
        } catch (Exception e) {
            response.setStatusCode(500);
            response.setMessage("Error getting all bookings: " + e.getMessage());
        }

        return response;
    }

    @Override
    public Response getUserBookings(int page, int limit, String sort, String order, String userId) {
        Response response = new Response();

        try {
            Sort.Direction direction = order.equalsIgnoreCase("asc") ? Sort.Direction.ASC : Sort.Direction.DESC;
            Pageable pageable = PageRequest.of(page - 1, limit, Sort.by(direction, sort));

            Page<Booking> bookingPage = bookingRepository.findByUserId(userId, pageable);
            List<BookingDTO> bookingDTOList = Utils.mapBookingListEntityToBookingListDTO(bookingPage.getContent());

            response.setStatusCode(200);
            response.setMessage("successful");
            response.setPagination(new Pagination(bookingPage.getTotalElements(), bookingPage.getTotalPages(), page));
            response.setBookingList(bookingDTOList);
        } catch (Exception e) {
            response.setStatusCode(500);
            response.setMessage("Error getting all bookings: " + e.getMessage());
        }

        return response;
    }

    @Override
    public Response cancelBooking(String bookingId) {
        Response response = new Response();

        try {
            Booking booking = bookingRepository.findById(bookingId).orElseThrow(() -> new OurException("Booking Not Found"));

            User user = booking.getUser();
            if (user != null) {
                user.getBookings().removeIf(b -> b.getId().equals(bookingId));
                userRepository.save(user);
            }

            Room room = booking.getRoom();
            if (room != null) {
                room.getBookings().removeIf(b -> b.getId().equals(bookingId));
                roomRepository.save(room);
            }

            bookingRepository.deleteById(bookingId);

            response.setMessage("successful");
            response.setStatusCode(200);
        } catch (OurException e) {
            response.setStatusCode(404);
            response.setMessage(e.getMessage());
        } catch (Exception e) {
            response.setStatusCode(500);
            response.setMessage("Error cancelling a booking " + e.getMessage());
        }

        return response;
    }

    private boolean roomIsAvailable(Booking bookingRequest, List<Booking> existingBookings) {
        return existingBookings.stream()
                .noneMatch(existingBooking ->
                        bookingRequest.getCheckInDate().equals(existingBooking.getCheckInDate())
                                || bookingRequest.getCheckOutDate().isBefore(existingBooking.getCheckOutDate())
                                || (bookingRequest.getCheckInDate().isAfter(existingBooking.getCheckInDate())
                                && bookingRequest.getCheckInDate().isBefore(existingBooking.getCheckOutDate()))
                                || (bookingRequest.getCheckInDate().isBefore(existingBooking.getCheckInDate())

                                && bookingRequest.getCheckOutDate().equals(existingBooking.getCheckOutDate()))
                                || (bookingRequest.getCheckInDate().isBefore(existingBooking.getCheckInDate())

                                && bookingRequest.getCheckOutDate().isAfter(existingBooking.getCheckOutDate()))

                                || (bookingRequest.getCheckInDate().equals(existingBooking.getCheckOutDate())
                                && bookingRequest.getCheckOutDate().equals(existingBooking.getCheckInDate()))

                                || (bookingRequest.getCheckInDate().equals(existingBooking.getCheckOutDate())
                                && bookingRequest.getCheckOutDate().equals(bookingRequest.getCheckInDate()))
                );
    }
}
