package vn.edu.unilend.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import vn.edu.unilend.entity.BorrowRequest;
import vn.edu.unilend.entity.enums.BorrowStatus;

import java.util.List;
import java.util.Optional;

@Repository
public interface BorrowRequestRepository extends JpaRepository<BorrowRequest, Long> {

    Optional<BorrowRequest> findByRequestCode(String requestCode);

    List<BorrowRequest> findByBorrowerIdOrderByCreatedAtDesc(Long borrowerId);

    List<BorrowRequest> findByBorrowerIdAndStatusInOrderByCreatedAtDesc(Long borrowerId, List<BorrowStatus> statuses);

    List<BorrowRequest> findByStatusOrderByCreatedAtDesc(BorrowStatus status);

    List<BorrowRequest> findAllByOrderByCreatedAtDesc();

    @Query("SELECT COUNT(br) FROM BorrowRequest br WHERE br.requestCode LIKE CONCAT(:prefix, '%')")
    long countByRequestCodePrefix(@Param("prefix") String prefix);
}
