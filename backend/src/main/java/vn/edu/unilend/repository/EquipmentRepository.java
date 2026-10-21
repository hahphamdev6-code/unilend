package vn.edu.unilend.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import vn.edu.unilend.entity.Equipment;
import vn.edu.unilend.entity.enums.EquipmentStatus;

import java.util.List;
import java.util.Optional;

@Repository
public interface EquipmentRepository extends JpaRepository<Equipment, Long> {
    Optional<Equipment> findByEquipmentCode(String equipmentCode);
    Optional<Equipment> findByQrCode(String qrCode);
    List<Equipment> findByStatus(EquipmentStatus status);
}
