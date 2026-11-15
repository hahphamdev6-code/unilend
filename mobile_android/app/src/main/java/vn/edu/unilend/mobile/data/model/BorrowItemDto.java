package vn.edu.unilend.mobile.data.model;

public class BorrowItemDto {
    private Long id;
    private Long equipmentId;
    private String equipmentCode;
    private String equipmentName;
    private String qrCode;
    private String imageUrl;
    private String conditionBefore;
    private String conditionAfter;
    private String returnStatus;
    private String note;

    public Long getId() { return id; }
    public Long getEquipmentId() { return equipmentId; }
    public String getEquipmentCode() { return equipmentCode; }
    public String getEquipmentName() { return equipmentName; }
    public String getQrCode() { return qrCode; }
    public String getImageUrl() { return imageUrl; }
    public String getConditionBefore() { return conditionBefore; }
    public String getConditionAfter() { return conditionAfter; }
    public String getReturnStatus() { return returnStatus; }
    public String getNote() { return note; }
}
