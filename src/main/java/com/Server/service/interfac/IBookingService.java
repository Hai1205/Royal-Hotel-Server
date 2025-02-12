package com.Server.service.interfac;

import com.Server.dto.Response;
import com.Server.entity.Booking;

public interface IBookingService {
    Response saveBooking(String rooId, String userId, Booking bookingRequest);

    Response findBookingByConfirmationCode(String confirmationCode);

    Response getAllBookings(int page, int limit, String sort, String order);

    Response cancelBooking(String bookingId);

    Response getUserBookings(int page, int limit, String sort, String order, String userId);
}
