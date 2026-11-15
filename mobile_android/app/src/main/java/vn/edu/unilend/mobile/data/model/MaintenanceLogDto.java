package vn.edu.unilend.mobile.data.model;

public class MaintenanceLogDto {
    private Long id;
    private Long equipmentId;
    private String equipmentCode;
    private String equipmentName;
    private Long reportedById;
    private String reportedByName;
    private Long handledById;
    private String handledByName;
    private String issueDescription;
    private String severity;
    private String status;
    private String resolvedAt;
    private String createdAt;

    public Long getId() { return id; }
    public Long getEquipmentId() { return equipmentId; }
    public String getEquipmentCode() { return equipmentCode; }
    public String getEquipmentName() { return equipmentName; }
    public Long getReportedById() { return reportedById; }
    public String getReportedByName() { return reportedByName; }
    public Long getHandledById() { return handledById; }
    public String getHandledByName() { return handledByName; }
    public String getIssueDescription() { return issueDescription; }
    public String getSeverity() { return severity; }
    public String getStatus() { return status; }
    public String getResolvedAt() { return resolvedAt; }
    public String getCreatedAt() { return createdAt; }
}
