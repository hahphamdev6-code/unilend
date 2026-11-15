package vn.edu.unilend.service;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import vn.edu.unilend.dto.maintenance.CreateMaintenanceReportRequest;
import vn.edu.unilend.dto.maintenance.MaintenanceLogDto;
import vn.edu.unilend.dto.maintenance.ResolveMaintenanceRequest;
import vn.edu.unilend.entity.Equipment;
import vn.edu.unilend.entity.MaintenanceLog;
import vn.edu.unilend.entity.User;
import vn.edu.unilend.entity.enums.EquipmentStatus;
import vn.edu.unilend.entity.enums.MaintenanceStatus;
import vn.edu.unilend.entity.enums.Severity;
import vn.edu.unilend.repository.EquipmentRepository;
import vn.edu.unilend.repository.MaintenanceLogRepository;
import vn.edu.unilend.repository.UserRepository;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class MaintenanceService {

    private final MaintenanceLogRepository maintenanceLogRepository;
    private final EquipmentRepository equipmentRepository;
    private final UserRepository userRepository;

    @Transactional
    public MaintenanceLogDto reportIssue(String userCode, CreateMaintenanceReportRequest request) {
        User reporter = userRepository.findByUserCode(userCode)
                .orElseThrow(() -> new IllegalArgumentException("Không tìm thấy người báo cáo"));
        Equipment equipment = equipmentRepository.findById(request.getEquipmentId())
                .orElseThrow(() -> new IllegalArgumentException("Không tìm thấy thiết bị ID: " + request.getEquipmentId()));

        Severity severity = request.getSeverity() != null ? request.getSeverity() : Severity.MEDIUM;
        MaintenanceLog log = MaintenanceLog.builder()
                .equipment(equipment)
                .reportedBy(reporter)
                .issueDescription(request.getIssueDescription().trim())
                .severity(severity)
                .status(MaintenanceStatus.OPEN)
                .repairCost(BigDecimal.ZERO)
                .build();

        if (severity == Severity.HIGH || severity == Severity.CRITICAL) {
            equipment.setStatus(EquipmentStatus.MAINTENANCE);
            equipmentRepository.save(equipment);
        }

        return toDto(maintenanceLogRepository.save(log));
    }

    @Transactional(readOnly = true)
    public List<MaintenanceLogDto> getMyReports(String userCode) {
        User reporter = userRepository.findByUserCode(userCode)
                .orElseThrow(() -> new IllegalArgumentException("Không tìm thấy người dùng"));
        return maintenanceLogRepository.findByReportedByIdOrderByCreatedAtDesc(reporter.getId())
                .stream().map(this::toDto).collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public List<MaintenanceLogDto> getAllReports(MaintenanceStatus status) {
        List<MaintenanceLog> list = (status != null)
                ? maintenanceLogRepository.findByStatusOrderByCreatedAtDesc(status)
                : maintenanceLogRepository.findAllByOrderByCreatedAtDesc();
        return list.stream().map(this::toDto).collect(Collectors.toList());
    }

    @Transactional
    public MaintenanceLogDto resolveReport(Long id, String staffUserCode, ResolveMaintenanceRequest request) {
        MaintenanceLog log = maintenanceLogRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Không tìm thấy báo cáo sự cố ID: " + id));
        User handler = userRepository.findByUserCode(staffUserCode)
                .orElseThrow(() -> new IllegalArgumentException("Không tìm thấy kỹ thuật viên"));

        log.setHandledBy(handler);
        log.setStatus(request.getStatus());
        if (request.getRepairCost() != null) {
            log.setRepairCost(request.getRepairCost());
        }

        Equipment equipment = log.getEquipment();
        if (request.getStatus() == MaintenanceStatus.RESOLVED) {
            log.setResolvedAt(LocalDateTime.now());
            equipment.setStatus(EquipmentStatus.AVAILABLE);
            if (request.getConditionNote() != null && !request.getConditionNote().isBlank()) {
                equipment.setConditionNote(request.getConditionNote().trim());
            } else {
                equipment.setConditionNote("Đã sửa chữa, hoạt động tốt");
            }
        } else if (request.getStatus() == MaintenanceStatus.IN_PROGRESS) {
            equipment.setStatus(EquipmentStatus.MAINTENANCE);
        } else if (request.getStatus() == MaintenanceStatus.UNREPAIRABLE) {
            log.setResolvedAt(LocalDateTime.now());
            equipment.setStatus(EquipmentStatus.BROKEN);
        }
        equipmentRepository.save(equipment);

        return toDto(maintenanceLogRepository.save(log));
    }

    public MaintenanceLogDto toDto(MaintenanceLog log) {
        return MaintenanceLogDto.builder()
                .id(log.getId())
                .equipmentId(log.getEquipment().getId())
                .equipmentCode(log.getEquipment().getEquipmentCode())
                .equipmentName(log.getEquipment().getName())
                .reportedById(log.getReportedBy().getId())
                .reportedByName(log.getReportedBy().getFullName())
                .handledById(log.getHandledBy() != null ? log.getHandledBy().getId() : null)
                .handledByName(log.getHandledBy() != null ? log.getHandledBy().getFullName() : null)
                .issueDescription(log.getIssueDescription())
                .severity(log.getSeverity())
                .status(log.getStatus())
                .repairCost(log.getRepairCost())
                .resolvedAt(log.getResolvedAt())
                .createdAt(log.getCreatedAt())
                .build();
    }
}
