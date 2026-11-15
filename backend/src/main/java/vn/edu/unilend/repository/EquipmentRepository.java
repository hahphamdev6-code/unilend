package vn.edu.unilend.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
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
    List<Equipment> findByCategoryId(Long categoryId);
    List<Equipment> findByRoomId(Long roomId);

    boolean existsByEquipmentCode(String equipmentCode);
    boolean existsByQrCode(String qrCode);

    @Query("SELECT e FROM Equipment e " +
           "LEFT JOIN FETCH e.category c " +
           "LEFT JOIN FETCH e.room r " +
           "WHERE (:categoryId IS NULL OR c.id = :categoryId) " +
           "AND (:roomId IS NULL OR r.id = :roomId) " +
           "AND (:status IS NULL OR e.status = :status) " +
           "AND (:keyword IS NULL OR LOWER(e.name) LIKE LOWER(CONCAT('%', :keyword, '%')) " +
           "     OR LOWER(e.equipmentCode) LIKE LOWER(CONCAT('%', :keyword, '%')) " +
           "     OR LOWER(e.brand) LIKE LOWER(CONCAT('%', :keyword, '%'))) " +
           "ORDER BY e.id ASC")
    List<Equipment> searchEquipments(
            @Param("keyword") String keyword,
            @Param("categoryId") Long categoryId,
            @Param("roomId") Long roomId,
            @Param("status") EquipmentStatus status
    );
}
