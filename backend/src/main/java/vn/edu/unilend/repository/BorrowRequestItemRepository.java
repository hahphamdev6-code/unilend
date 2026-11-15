package vn.edu.unilend.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import vn.edu.unilend.entity.BorrowRequestItem;

import java.util.List;

@Repository
public interface BorrowRequestItemRepository extends JpaRepository<BorrowRequestItem, Long> {
    List<BorrowRequestItem> findByBorrowRequestId(Long borrowRequestId);
    List<BorrowRequestItem> findByEquipmentId(Long equipmentId);
}
