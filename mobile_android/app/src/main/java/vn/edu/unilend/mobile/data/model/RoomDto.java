package vn.edu.unilend.mobile.data.model;

public class RoomDto {
    private Long id;
    private String roomCode;
    private String building;
    private String roomType;
    private Integer capacity;
    private String status;

    public Long getId() { return id; }
    public String getRoomCode() { return roomCode; }
    public String getBuilding() { return building; }
    public String getRoomType() { return roomType; }
    public Integer getCapacity() { return capacity; }
    public String getStatus() { return status; }
}
