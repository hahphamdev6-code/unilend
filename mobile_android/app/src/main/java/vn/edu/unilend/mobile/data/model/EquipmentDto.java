package vn.edu.unilend.mobile.data.model;

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
    private String status;
    private String conditionNote;
    private String purchaseDate;

    public Long getId() { return id; }
    public String getEquipmentCode() { return equipmentCode; }
    public String getQrCode() { return qrCode; }
    public String getName() { return name; }
    public Long getCategoryId() { return categoryId; }
    public String getCategoryCode() { return categoryCode; }
    public String getCategoryName() { return categoryName; }
    public Long getRoomId() { return roomId; }
    public String getRoomCode() { return roomCode; }
    public String getBuilding() { return building; }
    public String getBrand() { return brand; }
    public String getModelNumber() { return modelNumber; }
    public String getSerialNumber() { return serialNumber; }
    public String getSpecifications() { return specifications; }
    public String getImageUrl() { return imageUrl; }
    public String getStatus() { return status; }
    public String getConditionNote() { return conditionNote; }
    public String getPurchaseDate() { return purchaseDate; }
}
