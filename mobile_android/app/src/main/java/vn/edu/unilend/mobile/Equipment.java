package vn.edu.unilend.mobile;

public class Equipment {
    private final long id;
    private final String name;
    private final String category;
    private final String room;
    private final String status; // AVAILABLE, BORROWED, MAINTENANCE
    private final String icon;   // emoji tạm, sau này thay bằng ảnh từ API

    public Equipment(long id, String name, String category, String room,
                     String status, String icon) {
        this.id = id;
        this.name = name;
        this.category = category;
        this.room = room;
        this.status = status;
        this.icon = icon;
    }

    public long getId() { return id; }
    public String getName() { return name; }
    public String getCategory() { return category; }
    public String getRoom() { return room; }
    public String getStatus() { return status; }
    public String getIcon() { return icon; }
}