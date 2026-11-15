package vn.edu.unilend.entity;

import jakarta.persistence.*;
import lombok.*;
import vn.edu.unilend.entity.enums.RoomStatus;
import vn.edu.unilend.entity.enums.RoomType;

@Entity
@Table(name = "rooms")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Room {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "room_code", nullable = false, unique = true, length = 30)
    private String roomCode;

    @Column(nullable = false, length = 50)
    private String building;

    @Enumerated(EnumType.STRING)
    @Column(name = "room_type", nullable = false, length = 20)
    private RoomType roomType;

    private Integer capacity;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private RoomStatus status;

    @PrePersist
    protected void onCreate() {
        if (roomType == null) {
            roomType = RoomType.CLASSROOM;
        }
        if (capacity == null) {
            capacity = 0;
        }
        if (status == null) {
            status = RoomStatus.AVAILABLE;
        }
    }
}
