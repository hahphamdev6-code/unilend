package vn.edu.unilend.dto.borrow;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import vn.edu.unilend.entity.enums.ReturnStatus;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class BorrowItemDto {
    private Long id;
    private Long equipmentId;
    private String equipmentCode;
    private String equipmentName;
    private String qrCode;
    private String imageUrl;
    private String conditionBefore;
    private String conditionAfter;
    private ReturnStatus returnStatus;
    private String note;
}
