package vn.edu.unilend.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import vn.edu.unilend.entity.MaintenanceLog;
import vn.edu.unilend.entity.enums.MaintenanceStatus;

import java.util.List;

@Repository
public interface MaintenanceLogRepository extends JpaRepository<MaintenanceLog, Long> {
    List<MaintenanceLog> findByEquipmentIdOrderByCreatedAtDesc(Long equipmentId);
    List<MaintenanceLog> findByReportedByIdOrderByCreatedAtDesc(Long reportedById);
    List<MaintenanceLog> findByStatusOrderByCreatedAtDesc(MaintenanceStatus status);
    List<MaintenanceLog> findAllByOrderByCreatedAtDesc();
}
