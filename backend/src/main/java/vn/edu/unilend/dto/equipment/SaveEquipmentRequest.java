package vn.edu.unilend.dto.equipment;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;
import vn.edu.unilend.entity.enums.EquipmentStatus;

import java.time.LocalDate;

@Data
public class SaveEquipmentRequest {
    @NotBlank(message = "Mã thiết bị không được để trống")
    private String equipmentCode;

    private String qrCode;

    @NotBlank(message = "Tên thiết bị không được để trống")
    private String name;

    @NotNull(message = "Danh mục thiết bị không được để trống")
    private Long categoryId;

    private Long roomId;
    private String brand;
    private String modelNumber;
    private String serialNumber;
    private String specifications;
    private String imageUrl;
    private EquipmentStatus status;
    private String conditionNote;
    private LocalDate purchaseDate;
}
