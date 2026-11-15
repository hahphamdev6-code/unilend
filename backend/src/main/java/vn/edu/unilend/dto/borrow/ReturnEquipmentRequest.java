package vn.edu.unilend.dto.borrow;

import jakarta.validation.constraints.NotNull;
import lombok.Data;
import vn.edu.unilend.entity.enums.ReturnStatus;

import java.util.List;

@Data
public class ReturnEquipmentRequest {
    private List<ItemReturnDetail> items;
    private String note;

    @Data
    public static class ItemReturnDetail {
        @NotNull(message = "ID thiết bị không được để trống")
        private Long equipmentId;

        @NotNull(message = "Trạng thái trả không được để trống")
        private ReturnStatus returnStatus;

        private String conditionAfter;
        private String note;
    }
}
