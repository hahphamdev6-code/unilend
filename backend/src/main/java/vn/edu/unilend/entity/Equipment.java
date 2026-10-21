package vn.edu.unilend.entity;

import jakarta.persistence.*;
import lombok.*;
import vn.edu.unilend.entity.enums.EquipmentStatus;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Entity
@Table(name = "equipments")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Equipment {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "equipment_code", nullable = false, unique = true, length = 50)
    private String equipmentCode;

    @Column(name = "qr_code", nullable = false, unique = true, length = 100)
    private String qrCode;

    @Column(nullable = false, length = 150)
    private String name;

    @Column(name = "category_id", nullable = false)
    private Long categoryId;

    @Column(name = "room_id")
    private Long roomId;

    @Column(length = 100)
    private String brand;

    @Column(name = "model_number", length = 100)
    private String modelNumber;

    @Column(name = "serial_number", length = 100)
    private String serialNumber;

    @Column(columnDefinition = "TEXT")
    private String specifications;

    @Column(name = "image_url", length = 500)
    private String imageUrl;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private EquipmentStatus status;

    @Column(name = "condition_note")
    private String conditionNote;

    @Column(name = "purchase_date")
    private LocalDate purchaseDate;

    @Column(name = "created_at", updatable = false)
    private LocalDateTime createdAt;

    @Column(name = "updated_at")
    private LocalDateTime updatedAt;
}
