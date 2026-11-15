package vn.edu.unilend.service;

import lombok.RequiredArgsConstructor;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import vn.edu.unilend.dto.borrow.BorrowItemDto;
import vn.edu.unilend.dto.borrow.BorrowRequestDto;
import vn.edu.unilend.dto.borrow.CreateBorrowRequest;
import vn.edu.unilend.dto.borrow.ReturnEquipmentRequest;
import vn.edu.unilend.entity.BorrowRequest;
import vn.edu.unilend.entity.BorrowRequestItem;
import vn.edu.unilend.entity.Equipment;
import vn.edu.unilend.entity.Room;
import vn.edu.unilend.entity.User;
import vn.edu.unilend.entity.enums.BorrowStatus;
import vn.edu.unilend.entity.enums.EquipmentStatus;
import vn.edu.unilend.entity.enums.ReturnStatus;
import vn.edu.unilend.entity.enums.Role;
import vn.edu.unilend.repository.BorrowRequestRepository;
import vn.edu.unilend.repository.EquipmentRepository;
import vn.edu.unilend.repository.RoomRepository;
import vn.edu.unilend.repository.UserRepository;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class BorrowService {

    private final BorrowRequestRepository borrowRequestRepository;
    private final EquipmentRepository equipmentRepository;
    private final RoomRepository roomRepository;
    private final UserRepository userRepository;

    @Transactional
    public BorrowRequestDto createBorrowRequest(String userCode, CreateBorrowRequest request) {
        if (!request.getExpectedReturnTime().isAfter(request.getBorrowTime())) {
            throw new IllegalArgumentException("Thời gian dự kiến trả phải sau thời gian nhận thiết bị");
        }

        User borrower = userRepository.findByUserCode(userCode)
                .orElseThrow(() -> new IllegalArgumentException("Không tìm thấy thông tin người mượn"));

        Room usageRoom = null;
        if (request.getUsageRoomId() != null) {
            usageRoom = roomRepository.findById(request.getUsageRoomId())
                    .orElseThrow(() -> new IllegalArgumentException("Không tìm thấy phòng sử dụng ID: " + request.getUsageRoomId()));
        }

        List<Equipment> equipments = equipmentRepository.findAllById(request.getEquipmentIds());
        if (equipments.size() != request.getEquipmentIds().size()) {
            throw new IllegalArgumentException("Một hoặc nhiều thiết bị không tồn tại");
        }

        for (Equipment equipment : equipments) {
            if (equipment.getStatus() != EquipmentStatus.AVAILABLE) {
                throw new IllegalStateException("Thiết bị '" + equipment.getName() + "' hiện không sẵn sàng cho mượn");
            }
        }

        String requestCode = generateRequestCode();
        BorrowRequest borrowRequest = BorrowRequest.builder()
                .requestCode(requestCode)
                .borrower(borrower)
                .usageRoom(usageRoom)
                .purpose(request.getPurpose().trim())
                .borrowTime(request.getBorrowTime())
                .expectedReturnTime(request.getExpectedReturnTime())
                .status(BorrowStatus.PENDING)
                .note(request.getNote())
                .build();

        for (Equipment equipment : equipments) {
            BorrowRequestItem item = BorrowRequestItem.builder()
                    .borrowRequest(borrowRequest)
                    .equipment(equipment)
                    .conditionBefore(equipment.getConditionNote())
                    .returnStatus(ReturnStatus.PENDING)
                    .build();
            borrowRequest.getItems().add(item);
        }

        BorrowRequest saved = borrowRequestRepository.save(borrowRequest);
        return toBorrowRequestDto(saved);
    }

    @Transactional(readOnly = true)
    public List<BorrowRequestDto> getMyBorrowRequests(String userCode, BorrowStatus status) {
        User user = userRepository.findByUserCode(userCode)
                .orElseThrow(() -> new IllegalArgumentException("Không tìm thấy người dùng"));

        List<BorrowRequest> list = (status != null)
                ? borrowRequestRepository.findByBorrowerIdAndStatusInOrderByCreatedAtDesc(user.getId(), List.of(status))
                : borrowRequestRepository.findByBorrowerIdOrderByCreatedAtDesc(user.getId());

        return list.stream().map(this::toBorrowRequestDto).collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public List<BorrowRequestDto> getAllBorrowRequests(BorrowStatus status) {
        List<BorrowRequest> list = (status != null)
                ? borrowRequestRepository.findByStatusOrderByCreatedAtDesc(status)
                : borrowRequestRepository.findAllByOrderByCreatedAtDesc();
        return list.stream().map(this::toBorrowRequestDto).collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public BorrowRequestDto getBorrowRequestById(Long id, String userCode) {
        BorrowRequest request = borrowRequestRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Không tìm thấy phiếu mượn ID: " + id));
        User currentUser = userRepository.findByUserCode(userCode)
                .orElseThrow(() -> new IllegalArgumentException("Không tìm thấy người dùng"));

        boolean isStaff = currentUser.getRole() == Role.ADMIN || currentUser.getRole() == Role.TECHNICIAN;
        if (!isStaff && !request.getBorrower().getId().equals(currentUser.getId())) {
            throw new AccessDeniedException("Bạn không có quyền xem phiếu mượn này");
        }
        return toBorrowRequestDto(request);
    }

    @Transactional
    public BorrowRequestDto cancelBorrowRequest(Long id, String userCode) {
        BorrowRequest request = borrowRequestRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Không tìm thấy phiếu mượn ID: " + id));

        if (!request.getBorrower().getUserCode().equals(userCode)) {
            throw new AccessDeniedException("Bạn chỉ có thể hủy phiếu mượn của chính mình");
        }
        if (request.getStatus() != BorrowStatus.PENDING && request.getStatus() != BorrowStatus.APPROVED) {
            throw new IllegalStateException("Chỉ có thể hủy phiếu đang chờ duyệt hoặc đã duyệt chưa nhận");
        }

        if (request.getStatus() == BorrowStatus.APPROVED) {
            for (BorrowRequestItem item : request.getItems()) {
                Equipment eq = item.getEquipment();
                if (eq.getStatus() == EquipmentStatus.RESERVED) {
                    eq.setStatus(EquipmentStatus.AVAILABLE);
                    equipmentRepository.save(eq);
                }
            }
        }

        request.setStatus(BorrowStatus.CANCELLED);
        return toBorrowRequestDto(borrowRequestRepository.save(request));
    }

    @Transactional
    public BorrowRequestDto extendBorrowRequest(Long id, String userCode, int hours) {
        BorrowRequest request = borrowRequestRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Không tìm thấy phiếu mượn ID: " + id));

        if (!request.getBorrower().getUserCode().equals(userCode)) {
            throw new AccessDeniedException("Bạn chỉ có thể gia hạn phiếu mượn của chính mình");
        }
        if (request.getStatus() != BorrowStatus.BORROWING) {
            throw new IllegalStateException("Chỉ có thể gia hạn cho phiếu đang mượn");
        }

        int addHours = hours > 0 ? hours : 24;
        request.setExpectedReturnTime(request.getExpectedReturnTime().plusHours(addHours));
        String existingNote = request.getNote() != null ? request.getNote() + " | " : "";
        request.setNote(existingNote + "Gia hạn thêm " + addHours + "h");
        return toBorrowRequestDto(borrowRequestRepository.save(request));
    }

    @Transactional
    public BorrowRequestDto approveBorrowRequest(Long id, String staffUserCode) {
        BorrowRequest request = borrowRequestRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Không tìm thấy phiếu mượn ID: " + id));
        if (request.getStatus() != BorrowStatus.PENDING) {
            throw new IllegalStateException("Phiếu mượn không ở trạng thái chờ duyệt");
        }

        User approver = userRepository.findByUserCode(staffUserCode)
                .orElseThrow(() -> new IllegalArgumentException("Không tìm thấy thông tin người duyệt"));

        for (BorrowRequestItem item : request.getItems()) {
            Equipment eq = item.getEquipment();
            if (eq.getStatus() != EquipmentStatus.AVAILABLE) {
                throw new IllegalStateException("Thiết bị '" + eq.getName() + "' không còn sẵn sàng để duyệt");
            }
            eq.setStatus(EquipmentStatus.RESERVED);
            equipmentRepository.save(eq);
        }

        request.setApprover(approver);
        request.setStatus(BorrowStatus.APPROVED);
        return toBorrowRequestDto(borrowRequestRepository.save(request));
    }

    @Transactional
    public BorrowRequestDto rejectBorrowRequest(Long id, String staffUserCode, String reason) {
        BorrowRequest request = borrowRequestRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Không tìm thấy phiếu mượn ID: " + id));
        if (request.getStatus() != BorrowStatus.PENDING) {
            throw new IllegalStateException("Phiếu mượn không ở trạng thái chờ duyệt");
        }

        User approver = userRepository.findByUserCode(staffUserCode)
                .orElseThrow(() -> new IllegalArgumentException("Không tìm thấy thông tin người duyệt"));

        request.setApprover(approver);
        request.setStatus(BorrowStatus.REJECTED);
        request.setRejectReason(reason != null && !reason.isBlank() ? reason.trim() : "Không đáp ứng điều kiện mượn");
        return toBorrowRequestDto(borrowRequestRepository.save(request));
    }

    @Transactional
    public BorrowRequestDto handoverEquipments(Long id) {
        BorrowRequest request = borrowRequestRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Không tìm thấy phiếu mượn ID: " + id));
        if (request.getStatus() != BorrowStatus.APPROVED) {
            throw new IllegalStateException("Chỉ có thể bàn giao thiết bị cho phiếu đã duyệt");
        }

        for (BorrowRequestItem item : request.getItems()) {
            Equipment eq = item.getEquipment();
            eq.setStatus(EquipmentStatus.BORROWED);
            equipmentRepository.save(eq);
        }

        request.setActualPickupTime(LocalDateTime.now());
        request.setStatus(BorrowStatus.BORROWING);
        return toBorrowRequestDto(borrowRequestRepository.save(request));
    }

    @Transactional
    public BorrowRequestDto returnEquipments(Long id, ReturnEquipmentRequest returnBody) {
        BorrowRequest request = borrowRequestRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Không tìm thấy phiếu mượn ID: " + id));
        if (request.getStatus() != BorrowStatus.BORROWING && request.getStatus() != BorrowStatus.OVERDUE) {
            throw new IllegalStateException("Phiếu mượn không ở trạng thái đang mượn hoặc quá hạn");
        }

        Map<Long, ReturnEquipmentRequest.ItemReturnDetail> detailMap = Map.of();
        if (returnBody != null && returnBody.getItems() != null) {
            detailMap = returnBody.getItems().stream()
                    .collect(Collectors.toMap(
                            ReturnEquipmentRequest.ItemReturnDetail::getEquipmentId,
                            d -> d,
                            (a, b) -> b
                    ));
        }

        for (BorrowRequestItem item : request.getItems()) {
            Equipment eq = item.getEquipment();
            ReturnEquipmentRequest.ItemReturnDetail detail = detailMap.get(eq.getId());

            ReturnStatus rStatus = (detail != null && detail.getReturnStatus() != null)
                    ? detail.getReturnStatus()
                    : ReturnStatus.RETURNED_GOOD;

            item.setReturnStatus(rStatus);
            item.setConditionAfter(detail != null && detail.getConditionAfter() != null
                    ? detail.getConditionAfter()
                    : "Bình thường");
            if (detail != null && detail.getNote() != null) {
                item.setNote(detail.getNote());
            }

            if (rStatus == ReturnStatus.RETURNED_GOOD) {
                eq.setStatus(EquipmentStatus.AVAILABLE);
            } else if (rStatus == ReturnStatus.RETURNED_DAMAGED) {
                eq.setStatus(EquipmentStatus.BROKEN);
                eq.setConditionNote(item.getConditionAfter());
            } else if (rStatus == ReturnStatus.LOST) {
                eq.setStatus(EquipmentStatus.LOST);
            }
            equipmentRepository.save(eq);
        }

        if (returnBody != null && returnBody.getNote() != null && !returnBody.getNote().isBlank()) {
            request.setNote(returnBody.getNote().trim());
        }
        request.setActualReturnTime(LocalDateTime.now());
        request.setStatus(BorrowStatus.RETURNED);
        return toBorrowRequestDto(borrowRequestRepository.save(request));
    }

    private String generateRequestCode() {
        String datePart = LocalDate.now().format(DateTimeFormatter.ofPattern("yyyyMMdd"));
        String prefix = "PM-" + datePart + "-";
        long count = borrowRequestRepository.countByRequestCodePrefix(prefix);
        return prefix + String.format("%03d", count + 1);
    }

    public BorrowRequestDto toBorrowRequestDto(BorrowRequest br) {
        List<BorrowItemDto> itemDtos = br.getItems().stream()
                .map(item -> {
                    Equipment eq = item.getEquipment();
                    return BorrowItemDto.builder()
                            .id(item.getId())
                            .equipmentId(eq.getId())
                            .equipmentCode(eq.getEquipmentCode())
                            .equipmentName(eq.getName())
                            .qrCode(eq.getQrCode())
                            .imageUrl(eq.getImageUrl())
                            .conditionBefore(item.getConditionBefore())
                            .conditionAfter(item.getConditionAfter())
                            .returnStatus(item.getReturnStatus())
                            .note(item.getNote())
                            .build();
                })
                .collect(Collectors.toList());

        return BorrowRequestDto.builder()
                .id(br.getId())
                .requestCode(br.getRequestCode())
                .borrowerId(br.getBorrower().getId())
                .borrowerCode(br.getBorrower().getUserCode())
                .borrowerName(br.getBorrower().getFullName())
                .approverId(br.getApprover() != null ? br.getApprover().getId() : null)
                .approverName(br.getApprover() != null ? br.getApprover().getFullName() : null)
                .usageRoomId(br.getUsageRoom() != null ? br.getUsageRoom().getId() : null)
                .usageRoomCode(br.getUsageRoom() != null ? br.getUsageRoom().getRoomCode() : null)
                .purpose(br.getPurpose())
                .borrowTime(br.getBorrowTime())
                .expectedReturnTime(br.getExpectedReturnTime())
                .actualPickupTime(br.getActualPickupTime())
                .actualReturnTime(br.getActualReturnTime())
                .status(br.getStatus())
                .rejectReason(br.getRejectReason())
                .note(br.getNote())
                .createdAt(br.getCreatedAt())
                .items(itemDtos)
                .build();
    }
}
