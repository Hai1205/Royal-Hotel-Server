package com.Server.repo;

import com.Server.entity.Room;
import org.springframework.data.domain.Page;
import org.springframework.data.mongodb.repository.Aggregation;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.data.mongodb.repository.Query;
import org.springframework.data.domain.Pageable;
import java.util.List;

public interface RoomRepository extends MongoRepository<Room, String> {
    @Aggregation("{ $group: { _id: '$roomType'} }")
    List<String> findDistinctRoomType();

    Page<Room> findAll(Pageable pageable);

    @Query("{ 'bookings': {$size: 0 } }")
    Page<Room> findAllAvailableRooms(Pageable pageable);

    Page<Room> findByRoomTypeLikeAndIdNotIn(String roomType, List<String> bookedRoomsId, Pageable pageable);
}
