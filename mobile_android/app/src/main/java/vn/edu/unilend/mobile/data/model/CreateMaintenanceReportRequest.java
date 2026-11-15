package vn.edu.unilend.mobile.data.model;

public class CreateMaintenanceReportRequest {
    private final Long equipmentId;
    private final String issueDescription;
    private final String severity;

    public CreateMaintenanceReportRequest(Long equipmentId, String issueDescription, String severity) {
        this.equipmentId = equipmentId;
        this.issueDescription = issueDescription;
        this.severity = severity;
    }
}
