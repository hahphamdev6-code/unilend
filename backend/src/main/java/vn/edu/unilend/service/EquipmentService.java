package vn.edu.unilend.service;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import vn.edu.unilend.dto.equipment.CategoryDto;
import vn.edu.unilend.dto.equipment.EquipmentDto;
import vn.edu.unilend.dto.equipment.RoomDto;
import vn.edu.unilend.dto.equipment.SaveEquipmentRequest;
import vn.edu.unilend.entity.Category;
import vn.edu.unilend.entity.Equipment;
import vn.edu.unilend.entity.Room;
import vn.edu.unilend.entity.enums.EquipmentStatus;
import vn.edu.unilend.repository.CategoryRepository;
import vn.edu.unilend.repository.EquipmentRepository;
import vn.edu.unilend.repository.RoomRepository;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class EquipmentService {

    private final EquipmentRepository equipmentRepository;
    private final CategoryRepository categoryRepository;
    private final RoomRepository roomRepository;

    @Transactional(readOnly = true)
    public List<CategoryDto> getAllCategories() {
        return categoryRepository.findAll().stream()
                .map(this::toCategoryDto)
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public List<RoomDto> getAllRooms() {
        return roomRepository.findAll().stream()
                .map(this::toRoomDto)
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public List<EquipmentDto> searchEquipments(String keyword, Long categoryId, Long roomId, EquipmentStatus status) {
        String cleanKeyword = (keyword != null && !keyword.isBlank()) ? keyword.trim() : null;
        return equipmentRepository.searchEquipments(cleanKeyword, categoryId, roomId, status)
                .stream()
                .map(this::toEquipmentDto)
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public EquipmentDto getEquipmentById(Long id) {
        Equipment equipment = equipmentRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Không tìm thấy thiết bị ID: " + id));
        return toEquipmentDto(equipment);
    }

    @Transactional(readOnly = true)
    public EquipmentDto getEquipmentByQrCode(String qrCode) {
        Equipment equipment = equipmentRepository.findByQrCode(qrCode.trim())
                .orElseThrow(() -> new IllegalArgumentException("Không tìm thấy thiết bị với mã QR: " + qrCode));
        return toEquipmentDto(equipment);
    }

    @Transactional
    public EquipmentDto createEquipment(SaveEquipmentRequest request) {
        String code = request.getEquipmentCode().trim().toUpperCase();
        if (equipmentRepository.existsByEquipmentCode(code)) {
            throw new IllegalArgumentException("Mã thiết bị đã tồn tại: " + code);
        }

        String qr = (request.getQrCode() != null && !request.getQrCode().isBlank())
                ? request.getQrCode().trim()
                : "QR_" + code;
        if (equipmentRepository.existsByQrCode(qr)) {
            throw new IllegalArgumentException("Mã QR đã tồn tại: " + qr);
        }

        Category category = categoryRepository.findById(request.getCategoryId())
                .orElseThrow(() -> new IllegalArgumentException("Không tìm thấy danh mục ID: " + request.getCategoryId()));

        Room room = null;
        if (request.getRoomId() != null) {
            room = roomRepository.findById(request.getRoomId())
                    .orElseThrow(() -> new IllegalArgumentException("Không tìm thấy phòng/kho ID: " + request.getRoomId()));
        }

        Equipment equipment = Equipment.builder()
                .equipmentCode(code)
                .qrCode(qr)
                .name(request.getName().trim())
                .category(category)
                .room(room)
                .brand(request.getBrand())
                .modelNumber(request.getModelNumber())
                .serialNumber(request.getSerialNumber())
                .specifications(request.getSpecifications())
                .imageUrl(request.getImageUrl())
                .status(request.getStatus() != null ? request.getStatus() : EquipmentStatus.AVAILABLE)
                .conditionNote(request.getConditionNote())
                .purchaseDate(request.getPurchaseDate())
                .build();

        return toEquipmentDto(equipmentRepository.save(equipment));
    }

    @Transactional
    public EquipmentDto updateEquipment(Long id, SaveEquipmentRequest request) {
        Equipment equipment = equipmentRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Không tìm thấy thiết bị ID: " + id));

        String newCode = request.getEquipmentCode().trim().toUpperCase();
        if (!equipment.getEquipmentCode().equals(newCode) && equipmentRepository.existsByEquipmentCode(newCode)) {
            throw new IllegalArgumentException("Mã thiết bị đã tồn tại: " + newCode);
        }

        Category category = categoryRepository.findById(request.getCategoryId())
                .orElseThrow(() -> new IllegalArgumentException("Không tìm thấy danh mục ID: " + request.getCategoryId()));

        Room room = null;
        if (request.getRoomId() != null) {
            room = roomRepository.findById(request.getRoomId())
                    .orElseThrow(() -> new IllegalArgumentException("Không tìm thấy phòng/kho ID: " + request.getRoomId()));
        }

        equipment.setEquipmentCode(newCode);
        if (request.getQrCode() != null && !request.getQrCode().isBlank()) {
            String newQr = request.getQrCode().trim();
            if (!equipment.getQrCode().equals(newQr) && equipmentRepository.existsByQrCode(newQr)) {
                throw new IllegalArgumentException("Mã QR đã tồn tại: " + newQr);
            }
            equipment.setQrCode(newQr);
        }
        equipment.setName(request.getName().trim());
        equipment.setCategory(category);
        equipment.setRoom(room);
        equipment.setBrand(request.getBrand());
        equipment.setModelNumber(request.getModelNumber());
        equipment.setSerialNumber(request.getSerialNumber());
        equipment.setSpecifications(request.getSpecifications());
        equipment.setImageUrl(request.getImageUrl());
        if (request.getStatus() != null) {
            equipment.setStatus(request.getStatus());
        }
        if (request.getConditionNote() != null) {
            equipment.setConditionNote(request.getConditionNote());
        }
        equipment.setPurchaseDate(request.getPurchaseDate());

        return toEquipmentDto(equipmentRepository.save(equipment));
    }

    @Transactional
    public EquipmentDto updateEquipmentStatus(Long id, EquipmentStatus status, String conditionNote) {
        Equipment equipment = equipmentRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Không tìm thấy thiết bị ID: " + id));
        equipment.setStatus(status);
        if (conditionNote != null && !conditionNote.isBlank()) {
            equipment.setConditionNote(conditionNote.trim());
        }
        return toEquipmentDto(equipmentRepository.save(equipment));
    }

    public CategoryDto toCategoryDto(Category category) {
        return CategoryDto.builder()
                .id(category.getId())
                .code(category.getCode())
                .name(category.getName())
                .description(category.getDescription())
                .build();
    }

    public RoomDto toRoomDto(Room room) {
        return RoomDto.builder()
                .id(room.getId())
                .roomCode(room.getRoomCode())
                .building(room.getBuilding())
                .roomType(room.getRoomType())
                .capacity(room.getCapacity())
                .status(room.getStatus())
                .build();
    }

    public EquipmentDto toEquipmentDto(Equipment equipment) {
        Category category = equipment.getCategory();
        Room room = equipment.getRoom();
        return EquipmentDto.builder()
                .id(equipment.getId())
                .equipmentCode(equipment.getEquipmentCode())
                .qrCode(equipment.getQrCode())
                .name(equipment.getName())
                .categoryId(category != null ? category.getId() : null)
                .categoryCode(category != null ? category.getCode() : null)
                .categoryName(category != null ? category.getName() : null)
                .roomId(room != null ? room.getId() : null)
                .roomCode(room != null ? room.getRoomCode() : null)
                .building(room != null ? room.getBuilding() : null)
                .brand(equipment.getBrand())
                .modelNumber(equipment.getModelNumber())
                .serialNumber(equipment.getSerialNumber())
                .specifications(equipment.getSpecifications())
                .imageUrl(equipment.getImageUrl())
                .status(equipment.getStatus())
                .conditionNote(equipment.getConditionNote())
                .purchaseDate(equipment.getPurchaseDate())
                .build();
    }
}
