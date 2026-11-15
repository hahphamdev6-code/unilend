package vn.edu.unilend.dto.equipment;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import vn.edu.unilend.entity.enums.EquipmentStatus;

import java.time.LocalDate;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class EquipmentDto {
    private Long id;
    private String equipmentCode;
    private String qrCode;
    private String name;
    private Long categoryId;
    private String categoryCode;
    private String categoryName;
    private Long roomId;
    private String roomCode;
    private String building;
    private String brand;
    private String modelNumber;
    private String serialNumber;
    private String specifications;
    private String imageUrl;
    private EquipmentStatus status;
    private String conditionNote;
    private LocalDate purchaseDate;
}
