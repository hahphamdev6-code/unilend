package vn.edu.unilend.dto.equipment;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import vn.edu.unilend.entity.enums.RoomStatus;
import vn.edu.unilend.entity.enums.RoomType;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class RoomDto {
    private Long id;
    private String roomCode;
    private String building;
    private RoomType roomType;
    private Integer capacity;
    private RoomStatus status;
}
