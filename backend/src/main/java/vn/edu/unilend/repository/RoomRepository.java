package vn.edu.unilend.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import vn.edu.unilend.entity.Room;
import vn.edu.unilend.entity.enums.RoomStatus;
import vn.edu.unilend.entity.enums.RoomType;

import java.util.List;
import java.util.Optional;

@Repository
public interface RoomRepository extends JpaRepository<Room, Long> {
    Optional<Room> findByRoomCode(String roomCode);
    List<Room> findByRoomType(RoomType roomType);
    List<Room> findByStatus(RoomStatus status);
}
