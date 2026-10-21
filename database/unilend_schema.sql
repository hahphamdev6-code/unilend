-- =================================================================================
-- DATABASE SCHEMA: UNILEND - HỆ THỐNG QUẢN LÝ MƯỢN TRẢ THIẾT BỊ TRƯỜNG ĐẠI HỌC
-- Hệ quản trị CSDL: MySQL 8.0+
-- =================================================================================

CREATE DATABASE IF NOT EXISTS unilend_db
    CHARACTER SET utf8mb4
    COLLATE utf8mb4_unicode_ci;

USE unilend_db;

-- 1. Bảng Khoa / Đơn vị (Faculties / Departments)
CREATE TABLE IF NOT EXISTS faculties (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    code VARCHAR(20) NOT NULL UNIQUE COMMENT 'Mã khoa/viện (VD: CNTT, DTVT)',
    name VARCHAR(150) NOT NULL COMMENT 'Tên khoa/viện',
    description TEXT,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP
) ENGINE=InnoDB;

-- 2. Bảng Người dùng & Phân quyền (Users)
-- Roles: ADMIN (Quản trị viên), TECHNICIAN (Kỹ thuật viên quản lý kho), LECTURER (Giảng viên), STUDENT (Sinh viên)
CREATE TABLE IF NOT EXISTS users (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    user_code VARCHAR(30) NOT NULL UNIQUE COMMENT 'Mã SV / Mã GV / Mã NV',
    full_name VARCHAR(100) NOT NULL,
    email VARCHAR(120) NOT NULL UNIQUE,
    password_hash VARCHAR(255) NOT NULL,
    phone VARCHAR(20),
    role ENUM('ADMIN', 'TECHNICIAN', 'LECTURER', 'STUDENT') NOT NULL DEFAULT 'STUDENT',
    faculty_id BIGINT,
    class_name VARCHAR(50) COMMENT 'Lớp sinh hoạt (dành cho SV)',
    status ENUM('ACTIVE', 'LOCKED') NOT NULL DEFAULT 'ACTIVE',
    avatar_url VARCHAR(500),
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    CONSTRAINT fk_users_faculty FOREIGN KEY (faculty_id) REFERENCES faculties(id) ON DELETE SET NULL
) ENGINE=InnoDB;

-- 3. Bảng Danh mục thiết bị (Equipment Categories)
CREATE TABLE IF NOT EXISTS categories (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    code VARCHAR(30) NOT NULL UNIQUE COMMENT 'Mã danh mục (VD: PROJ, MIC, LAP, IOT)',
    name VARCHAR(100) NOT NULL COMMENT 'Tên danh mục (VD: Máy chiếu, Micro, Laptop, Bộ Kit IoT)',
    description TEXT,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
) ENGINE=InnoDB;

-- 4. Bảng Phòng học / Kho lưu trữ (Rooms / Storage Locations)
CREATE TABLE IF NOT EXISTS rooms (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    room_code VARCHAR(30) NOT NULL UNIQUE COMMENT 'Mã phòng (VD: A1-201, KHO-TB01)',
    building VARCHAR(50) NOT NULL COMMENT 'Tòa nhà / Khu giảng đường',
    room_type ENUM('CLASSROOM', 'LAB', 'STORAGE', 'HALL') NOT NULL DEFAULT 'CLASSROOM',
    capacity INT DEFAULT 0,
    status ENUM('AVAILABLE', 'MAINTENANCE') NOT NULL DEFAULT 'AVAILABLE'
) ENGINE=InnoDB;

-- 5. Bảng Thiết bị (Equipments)
CREATE TABLE IF NOT EXISTS equipments (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    equipment_code VARCHAR(50) NOT NULL UNIQUE COMMENT 'Mã tài sản thiết bị (VD: TB-PROJ-001)',
    qr_code VARCHAR(100) NOT NULL UNIQUE COMMENT 'Chuỗi định danh mã QR dán trên thiết bị',
    name VARCHAR(150) NOT NULL,
    category_id BIGINT NOT NULL,
    room_id BIGINT COMMENT 'Phòng hoặc kho hiện tại đang lưu giữ thiết bị',
    brand VARCHAR(100),
    model_number VARCHAR(100),
    serial_number VARCHAR(100),
    specifications TEXT COMMENT 'Thông số kỹ thuật',
    image_url VARCHAR(500),
    status ENUM('AVAILABLE', 'RESERVED', 'BORROWED', 'MAINTENANCE', 'BROKEN', 'LOST') NOT NULL DEFAULT 'AVAILABLE',
    condition_note VARCHAR(255) DEFAULT 'Hoạt động tốt',
    purchase_date DATE,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    CONSTRAINT fk_equipment_category FOREIGN KEY (category_id) REFERENCES categories(id) ON DELETE RESTRICT,
    CONSTRAINT fk_equipment_room FOREIGN KEY (room_id) REFERENCES rooms(id) ON DELETE SET NULL
) ENGINE=InnoDB;

-- 6. Bảng Phiếu mượn trả thiết bị (Borrow Requests / Loans)
CREATE TABLE IF NOT EXISTS borrow_requests (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    request_code VARCHAR(40) NOT NULL UNIQUE COMMENT 'Mã phiếu mượn (VD: PM-20261021-001)',
    borrower_id BIGINT NOT NULL COMMENT 'Người mượn (SV / GV)',
    approver_id BIGINT COMMENT 'Kỹ thuật viên / Quản trị viên duyệt phiếu',
    usage_room_id BIGINT COMMENT 'Phòng học sử dụng thiết bị',
    purpose VARCHAR(255) NOT NULL COMMENT 'Mục đích mượn (VD: Phục vụ học phần Lập trình di động)',
    borrow_time DATETIME NOT NULL COMMENT 'Thời gian dự kiến nhận thiết bị',
    expected_return_time DATETIME NOT NULL COMMENT 'Thời gian dự kiến trả thiết bị',
    actual_pickup_time DATETIME COMMENT 'Thời gian thực tế nhận thiết bị',
    actual_return_time DATETIME COMMENT 'Thời gian thực tế hoàn trả',
    status ENUM('PENDING', 'APPROVED', 'REJECTED', 'BORROWING', 'RETURNED', 'OVERDUE', 'CANCELLED') NOT NULL DEFAULT 'PENDING',
    reject_reason VARCHAR(255),
    note TEXT,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    CONSTRAINT fk_borrow_borrower FOREIGN KEY (borrower_id) REFERENCES users(id) ON DELETE RESTRICT,
    CONSTRAINT fk_borrow_approver FOREIGN KEY (approver_id) REFERENCES users(id) ON DELETE SET NULL,
    CONSTRAINT fk_borrow_room FOREIGN KEY (usage_room_id) REFERENCES rooms(id) ON DELETE SET NULL
) ENGINE=InnoDB;

-- 7. Bảng Chi tiết thiết bị trong phiếu mượn (Borrow Request Items)
CREATE TABLE IF NOT EXISTS borrow_request_items (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    borrow_request_id BIGINT NOT NULL,
    equipment_id BIGINT NOT NULL,
    condition_before VARCHAR(255) DEFAULT 'Bình thường' COMMENT 'Tình trạng lúc bàn giao',
    condition_after VARCHAR(255) COMMENT 'Tình trạng lúc nhận trả',
    return_status ENUM('PENDING', 'RETURNED_GOOD', 'RETURNED_DAMAGED', 'LOST') DEFAULT 'PENDING',
    note VARCHAR(255),
    CONSTRAINT fk_item_request FOREIGN KEY (borrow_request_id) REFERENCES borrow_requests(id) ON DELETE CASCADE,
    CONSTRAINT fk_item_equipment FOREIGN KEY (equipment_id) REFERENCES equipments(id) ON DELETE RESTRICT
) ENGINE=InnoDB;

-- 8. Bảng Báo cáo sự cố / Hư hỏng thiết bị (Incident Reports / Maintenance Logs)
CREATE TABLE IF NOT EXISTS maintenance_logs (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    equipment_id BIGINT NOT NULL,
    reported_by BIGINT NOT NULL,
    handled_by BIGINT,
    issue_description TEXT NOT NULL,
    severity ENUM('LOW', 'MEDIUM', 'HIGH', 'CRITICAL') DEFAULT 'MEDIUM',
    status ENUM('OPEN', 'IN_PROGRESS', 'RESOLVED', 'UNREPAIRABLE') DEFAULT 'OPEN',
    repair_cost DECIMAL(12, 2) DEFAULT 0.00,
    resolved_at DATETIME,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT fk_maint_equipment FOREIGN KEY (equipment_id) REFERENCES equipments(id) ON DELETE CASCADE,
    CONSTRAINT fk_maint_reporter FOREIGN KEY (reported_by) REFERENCES users(id) ON DELETE RESTRICT,
    CONSTRAINT fk_maint_handler FOREIGN KEY (handled_by) REFERENCES users(id) ON DELETE SET NULL
) ENGINE=InnoDB;

-- =================================================================================
-- DỮ LIỆU MẪU KHỞI TẠO (SEED DATA)
-- Mật khẩu mặc định cho tất cả tài khoản mẫu: 123456 (BCrypt hash)
-- =================================================================================

INSERT INTO faculties (code, name, description) VALUES
('CNTT', 'Khoa Công nghệ Thông tin', 'Đào tạo Kỹ sư CNTT, Khoa học máy tính, An toàn thông tin'),
('DTVT', 'Khoa Điện - Điện tử Viễn thông', 'Đào tạo Kỹ thuật Điện tử, IoT và Hệ thống nhúng'),
('QTKD', 'Khoa Quản trị Kinh doanh', 'Đào tạo Quản trị kinh doanh, Marketing, Thương mại điện tử');

-- Password hash dưới đây tương ứng với chuỗi "123456" (BCrypt cost 10)
INSERT INTO users (user_code, full_name, email, password_hash, phone, role, faculty_id, class_name, status) VALUES
('ADMIN001', 'Nguyễn Quản Trị', 'admin@unilend.edu.vn', '$2a$10$N9qo8uLOickgx2ZMRZoMyeIjZAgcfl7p92ldGxad68LJZdL17lhWy', '0901000001', 'ADMIN', 1, NULL, 'ACTIVE'),
('TECH001', 'Trần Kỹ Thuật', 'tech@unilend.edu.vn', '$2a$10$N9qo8uLOickgx2ZMRZoMyeIjZAgcfl7p92ldGxad68LJZdL17lhWy', '0901000002', 'TECHNICIAN', 1, NULL, 'ACTIVE'),
('GV001', 'TS. Lê Hoàng Nam', 'nam.lh@unilend.edu.vn', '$2a$10$N9qo8uLOickgx2ZMRZoMyeIjZAgcfl7p92ldGxad68LJZdL17lhWy', '0901000003', 'LECTURER', 1, NULL, 'ACTIVE'),
('SV2026001', 'Phạm Minh Tuấn', 'tuan.pm@sv.unilend.edu.vn', '$2a$10$N9qo8uLOickgx2ZMRZoMyeIjZAgcfl7p92ldGxad68LJZdL17lhWy', '0901000004', 'STUDENT', 1, 'CNTT-K65A', 'ACTIVE');

INSERT INTO categories (code, name, description) VALUES
('PROJ', 'Máy chiếu & Trình chiếu', 'Máy chiếu đa năng, bút trình chiếu laser, cáp HDMI/Type-C'),
('AUDIO', 'Thiết bị Âm thanh', 'Micro không dây, loa trợ giảng, bộ thu phát âm thanh hội trường'),
('LAB_KIT', 'Bộ Kit Thực hành & IoT', 'Arduino, Raspberry Pi, Kit cảm biến, đồng hồ đo điện'),
('LAPTOP', 'Máy tính xách tay & Tablet', 'Laptop phục vụ thi cử, thuyết trình và sự kiện');

INSERT INTO rooms (room_code, building, room_type, capacity, status) VALUES
('KHO-A1', 'Tòa nhà A1 - Tầng 1', 'STORAGE', 0, 'AVAILABLE'),
('A1-301', 'Tòa nhà A1 - Tầng 3', 'CLASSROOM', 60, 'AVAILABLE'),
('A1-302', 'Tòa nhà A1 - Tầng 3', 'CLASSROOM', 80, 'AVAILABLE'),
('LAB-CNTT01', 'Tòa nhà B2 - Tầng 2', 'LAB', 40, 'AVAILABLE');

INSERT INTO equipments (equipment_code, qr_code, name, category_id, room_id, brand, model_number, serial_number, specifications, status) VALUES
('TB-PROJ-001', 'QR_UNILEND_PROJ_001', 'Máy chiếu Epson EB-E01 Full HD', 1, 1, 'Epson', 'EB-E01', 'SN-EPS-99201', 'Độ sáng 3300 Ansi Lumens, HDMI, VGA', 'AVAILABLE'),
('TB-PROJ-002', 'QR_UNILEND_PROJ_002', 'Máy chiếu Sony VPL-DX221', 1, 1, 'Sony', 'VPL-DX221', 'SN-SNY-88102', 'Độ sáng 2700 Ansi Lumens, HDMI', 'AVAILABLE'),
('TB-AUD-001', 'QR_UNILEND_AUD_001', 'Bộ Micro không dây Shure BLX288', 2, 1, 'Shure', 'BLX288/PG58', 'SN-SHR-11029', '2 tay micro không dây, thu phát UHF', 'AVAILABLE'),
('TB-IOT-001', 'QR_UNILEND_IOT_001', 'Bộ Kit thực hành Raspberry Pi 5 8GB', 3, 4, 'Raspberry Pi', 'RPi-5-8GB', 'SN-RPI-55410', 'RAM 8GB, thẻ nhớ 64GB, bộ cảm biến nhiệt độ/độ ẩm', 'AVAILABLE');
