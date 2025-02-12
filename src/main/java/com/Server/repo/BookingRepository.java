package com.Server.repo;

import com.Server.entity.Booking;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.data.mongodb.repository.Query;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

public interface BookingRepository extends MongoRepository<Booking, String> {
    Optional<Booking> findByBookingConfirmationCode(String confirmationCode);

    @Query("{ 'checkInDate': { $lte: ?1 }, 'checkOutDate': { $gte: ?0 } }")
    List<Booking> findBookingsByDateRange(LocalDate checkInDate,  LocalDate checkOutDate);

    Page<Booking> findAll(Pageable pageable);

    Page<Booking> findByUserId(String userId, Pageable pageable);
}
