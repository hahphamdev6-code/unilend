# Kế Hoạch Hoàn Thiện Ứng Dụng UniLend (Implementation Plan)

Tài liệu này tổng hợp hiện trạng thực tế của mã nguồn **UniLend** (Database, Backend Spring Boot, Mobile Android) và lộ trình chi tiết từng giai đoạn để hoàn thiện toàn bộ ứng dụng từ bản mẫu giao diện (prototype) thành hệ thống hoạt động hoàn chỉnh (end-to-end).

---

## 1. Đánh Giá Hiện Trạng Dự Án

### 1.1. Cơ sở dữ liệu (`database/unilend_schema.sql`) — **Hoàn thành 95%**
- Đã thiết kế đầy đủ **8 bảng cốt lõi**:
  1. `faculties` (Khoa/Viện)
  2. `users` (Người dùng & 4 vai trò: `ADMIN`, `TECHNICIAN`, `LECTURER`, `STUDENT`)
  3. `categories` (Danh mục thiết bị)
  4. `rooms` (Phòng học / Kho lưu trữ)
  5. `equipments` (Thiết bị & mã QR)
  6. `borrow_requests` (Phiếu mượn trả)
  7. `borrow_request_items` (Chi tiết thiết bị trong phiếu mượn)
  8. `maintenance_logs` (Báo cáo sự cố / bảo trì)
- Đã có dữ liệu mẫu (Seed Data) cho tài khoản các cấp, khoa, phòng, danh mục và thiết bị.
- **Cần bổ sung nhỏ**: Bảng `saved_equipments` (hoặc lưu cục bộ + đồng bộ) cho tính năng Bookmark/Saved Gear trên ứng dụng di động, và thêm dữ liệu mẫu phong phú khớp với giao diện Mobile (Camera Sony A7 IV, DJI Air 3, MacBook Pro M3 Max, RØDE Mic, Bose S1 Pro...).

### 1.2. Backend (`backend/`) — **Hoàn thành ~35%**
- **Đã có**:
  - Cấu hình Spring Boot 3.3.5 (Java 21), Spring Security + JWT (`JwtTokenProvider`, `JwtAuthenticationFilter`, `SecurityConfig`), xử lý lỗi tập trung (`GlobalExceptionHandler`).
  - API Xác thực (`AuthController`, `AuthService`): `/api/v1/auth/login`, `/api/v1/auth/register`, `/api/v1/auth/me`.
  - API Quản trị người dùng (`AdminUserController`): `/api/v1/admin/users`, đổi Role, khóa/mở khóa tài khoản.
  - Một số Entity & Repository cơ bản: `User`, `Faculty`, `Equipment`, `BorrowRequest`.
- **Còn thiếu**:
  - Các Entity & Repository còn lại trong schema: `Category`, `Room`, `BorrowRequestItem`, `MaintenanceLog`.
  - Các API nghiệp vụ cốt lõi: Danh mục & Thiết bị (`EquipmentController`), Mượn trả & Gia hạn (`BorrowRequestController`), Báo cáo sự cố (`MaintenanceController`), Thống kê & Duyệt phiếu cho Kỹ thuật viên/Admin.

### 1.3. Mobile App (`mobile_android/`) — **Hoàn thành ~45% (Đã có UI, chưa nối API)**
- **Đã có**:
  - Thiết kế giao diện hoàn chỉnh, hiện đại cho các màn hình: `WelcomeActivity`, `LoginActivity`, `RegisterActivity`, `MainActivity` (Khám phá, Tìm kiếm, Bộ lọc BottomSheet), `EquipmentDetailActivity` (Chi tiết thiết bị, chọn ngày mượn, tạo yêu cầu), `ProfileActivity` (Thẻ sinh viên điện tử, QR Kiosk Pass, Thiết bị đang mượn, Thiết bị đã lưu, Báo cáo sự cố, Chọn trường học).
- **Còn thiếu**:
  - Chưa khai báo thư viện kết nối mạng (`Retrofit2`, `OkHttp3`, `Gson`) trong `gradle/libs.versions.toml` và `app/build.gradle.kts`.
  - Chưa có tầng `data/local/SessionManager.java` (lưu trữ JWT Token, thông tin User đăng nhập) và `data/remote/ApiClient.java` (như đã mô tả trong `README.md` nhưng chưa có file thực tế trong `mobile_android`).
  - `LoginActivity` và `RegisterActivity` vẫn đang dùng `// TODO: call backend auth API` và chuyển thẳng màn hình.
  - Dữ liệu thiết bị trên `MainActivity`, `EquipmentDetailActivity`, `ProfileActivity` đang được gắn cứng (hardcoded) thay vì tải động từ Backend qua API.
  - Chưa có giao diện/chức năng dành cho vai trò **Kỹ thuật viên (`TECHNICIAN`)** và **Quản trị viên (`ADMIN`)** (duyệt phiếu mượn, bàn giao/nhận trả thiết bị, quản lý người dùng).

---

## 2. Lộ Trình Hoàn Thiện Chi Tiết (5 Giai Đoạn)

### Giai đoạn 1: Hoàn thiện tầng Entity, Repository & DTO tại Backend
**Mục tiêu**: Đồng bộ 100% cấu trúc bảng từ `database/unilend_schema.sql` sang JPA Entities của Spring Boot.

1. **Bổ sung các Entity & Enum còn thiếu** (`backend/src/main/java/vn/edu/unilend/entity/`):
   - `Category.java` (bảng `categories`)
   - `Room.java` & `enums/RoomType.java`, `enums/RoomStatus.java` (bảng `rooms`)
   - `BorrowRequestItem.java` & `enums/ReturnStatus.java` (bảng `borrow_request_items`)
   - `MaintenanceLog.java` & `enums/Severity.java`, `enums/MaintenanceStatus.java` (bảng `maintenance_logs`)
2. **Cập nhật liên kết khóa ngoại (JPA Relationships)**:
   - Liên kết `Equipment` với `Category` và `Room` để dễ dàng trả về tên danh mục, tên phòng/tòa nhà trong DTO.
   - Liên kết `BorrowRequest` với danh sách `BorrowRequestItem` (`@OneToMany(mappedBy = "borrowRequest", cascade = CascadeType.ALL)`).
3. **Tạo đầy đủ các Spring Data JPA Repositories** (`backend/src/main/java/vn/edu/unilend/repository/`):
   - `CategoryRepository.java`
   - `RoomRepository.java`
   - `BorrowRequestRepository.java` (tìm theo `borrowerId`, `status`, `requestCode`, kiểm tra trùng lịch mượn)
   - `BorrowRequestItemRepository.java`
   - `MaintenanceLogRepository.java`

---

### Giai đoạn 2: Xây dựng RESTful APIs Nghiệp vụ tại Backend
**Mục tiêu**: Cung cấp đầy đủ API cho cả người mượn (`STUDENT`, `LECTURER`) và người quản lý (`TECHNICIAN`, `ADMIN`).

1. **Module Thiết bị & Danh mục (`EquipmentController`, `EquipmentService`)**:
   - `GET /api/v1/categories`: Lấy danh sách danh mục thiết bị.
   - `GET /api/v1/rooms`: Lấy danh sách phòng học / kho lưu trữ.
   - `GET /api/v1/equipments`: Lấy danh sách thiết bị (hỗ trợ lọc theo `keyword`, `categoryId`, `roomId`, `status`).
   - `GET /api/v1/equipments/{id}` & `GET /api/v1/equipments/qr/{qrCode}`: Xem chi tiết thiết bị theo ID hoặc mã QR.
   - `POST /api/v1/equipments`, `PUT /api/v1/equipments/{id}`, `PATCH /api/v1/equipments/{id}/status` (`ROLE_ADMIN`, `ROLE_TECHNICIAN`): Thêm mới, cập nhật thông tin và trạng thái thiết bị.

2. **Module Mượn – Trả Thiết bị (`BorrowRequestController`, `BorrowService`)**:
   - `POST /api/v1/borrows`: Tạo phiếu yêu cầu mượn thiết bị mới (kiểm tra thiết bị `AVAILABLE`, sinh mã phiếu `PM-YYYYMMDD-XXX`, chuyển thiết bị sang `RESERVED` hoặc giữ `AVAILABLE` chờ duyệt).
   - `GET /api/v1/borrows/my`: Lấy danh sách phiếu mượn của người dùng đang đăng nhập (lọc theo `ACTIVE`, `PENDING`, `HISTORY`).
   - `GET /api/v1/borrows/{id}`: Xem chi tiết phiếu mượn kèm mã QR Kiosk Pass.
   - `POST /api/v1/borrows/{id}/cancel`: Người mượn hủy phiếu khi còn ở trạng thái `PENDING`.
   - `POST /api/v1/borrows/{id}/extend`: Yêu cầu gia hạn thêm 24h cho phiếu đang mượn (`BORROWING`).
   - **Dành cho `TECHNICIAN` / `ADMIN`**:
     - `GET /api/v1/borrows`: Danh sách toàn bộ phiếu mượn (lọc theo trạng thái `PENDING`, `APPROVED`, `BORROWING`, `OVERDUE`).
     - `PATCH /api/v1/borrows/{id}/approve`: Duyệt phiếu mượn (cập nhật `approver`, chuyển trạng thái sang `APPROVED`, thiết bị sang `RESERVED`).
     - `PATCH /api/v1/borrows/{id}/reject`: Từ chối phiếu mượn kèm lý do (`rejectReason`).
     - `PATCH /api/v1/borrows/{id}/handover`: Xác nhận bàn giao thiết bị tại quầy/Kiosk (`APPROVED` -> `BORROWING`, ghi nhận `actualPickupTime`, thiết bị sang `BORROWED`).
     - `PATCH /api/v1/borrows/{id}/return`: Xác nhận hoàn trả thiết bị (`BORROWING`/`OVERDUE` -> `RETURNED`, ghi nhận `actualReturnTime`, tình trạng sau khi trả `conditionAfter`, cập nhật trạng thái thiết bị về `AVAILABLE` hoặc `BROKEN`/`MAINTENANCE`).

3. **Module Báo cáo sự cố & Bảo trì (`MaintenanceController`, `MaintenanceService`)**:
   - `POST /api/v1/maintenance/report`: Người dùng gửi báo cáo hư hỏng/sự cố thiết bị.
   - `GET /api/v1/maintenance/my`: Xem các báo cáo sự cố đã gửi.
   - `GET /api/v1/maintenance` & `PATCH /api/v1/maintenance/{id}/resolve` (`TECHNICIAN`, `ADMIN`): Xem và xử lý sự cố kỹ thuật.

4. **Cập nhật Hồ sơ cá nhân (`UserController` / `AuthController`)**:
   - `PUT /api/v1/auth/me`: Cho phép cập nhật số điện thoại, khoa/viện, lớp, ảnh đại diện.

---

### Giai đoạn 3: Xây dựng Tầng Network & Lưu trữ Phiên trên Mobile Android
**Mục tiêu**: Thiết lập hạ tầng kết nối Client–Server chuẩn mực trên ứng dụng Android.

1. **Cập nhật Gradle Dependencies**:
   - Thêm vào `mobile_android/gradle/libs.versions.toml` và `mobile_android/app/build.gradle.kts`:
     - `com.squareup.retrofit2:retrofit:2.11.0`
     - `com.squareup.retrofit2:converter-gson:2.11.0`
     - `com.squareup.okhttp3:logging-interceptor:4.12.0`
   - Cấp quyền `android.permission.INTERNET` và cấu hình `android:usesCleartextTraffic="true"` (để kết nối `http://10.0.2.2:8080/` trên máy ảo) trong `AndroidManifest.xml`.

2. **Tạo tầng Local Storage (`vn.edu.unilend.mobile.data.local`)**:
   - `SessionManager.java`: Quản lý `SharedPreferences` để lưu/đọc/xóa `accessToken`, `userId`, `userCode`, `fullName`, `email`, `role`, `facultyName`, `className` và danh sách ID thiết bị đã Bookmark.

3. **Tạo tầng Models / DTOs (`vn.edu.unilend.mobile.data.model`)**:
   - `ApiResponse<T>`, `LoginRequest`, `RegisterRequest`, `AuthResponse`, `UserProfileDto`
   - `EquipmentDto`, `CategoryDto`, `RoomDto`
   - `CreateBorrowRequest`, `BorrowRequestDto`, `BorrowItemDto`
   - `MaintenanceReportRequest`, `MaintenanceLogDto`

4. **Tạo tầng Remote API (`vn.edu.unilend.mobile.data.remote`)**:
   - `AuthInterceptor.java`: Tự động đính kèm header `Authorization: Bearer <token>` vào mọi request.
   - `ApiClient.java`: Khởi tạo `Retrofit` instance trỏ tới `http://10.0.2.2:8080/`.
   - Các interface dịch vụ: `AuthApiService.java`, `EquipmentApiService.java`, `BorrowApiService.java`, `MaintenanceApiService.java`, `AdminApiService.java`.

---

### Giai đoạn 4: Tích hợp API vào các Màn hình Mobile Hiện có
**Mục tiêu**: Thay thế toàn bộ dữ liệu giả lập (mock/hardcoded) bằng dữ liệu thực từ Server, đồng thời giữ nguyên trải nghiệm UI mượt mà (có fallback dữ liệu mẫu khi offline nếu cần).

1. **Luồng Khởi động & Xác thực (`WelcomeActivity`, `LoginActivity`, `RegisterActivity`)**:
   - Kiểm tra `SessionManager.isLoggedIn()` tại `WelcomeActivity`: nếu đã có JWT Token hợp lệ thì chuyển thẳng vào `MainActivity`.
   - `LoginActivity`: Cho phép đăng nhập bằng cả **Email** hoặc **Mã SV/GV/NV (`userCode`)** (cập nhật validation không bắt buộc phải có ký tự `@` nếu người dùng nhập mã `SV2026001` hoặc `ADMIN001`), gọi `POST /api/v1/auth/login`, lưu token + profile vào `SessionManager` và điều hướng vào `MainActivity`.
   - `RegisterActivity`: Bổ sung trường nhập Mã SV/GV (hoặc tự động trích xuất từ email sinh viên), gọi `POST /api/v1/auth/register`, sau khi thành công chuyển về `LoginActivity` và điền sẵn thông tin.

2. **Màn hình Chính (`MainActivity`)**:
   - Hiển thị lời chào tên thật và vai trò của người dùng từ `SessionManager`.
   - Gọi `GET /api/v1/equipments` để cập nhật trạng thái tồn kho, vị trí phòng, danh mục của các thiết bị.
   - Đồng bộ trạng thái Bookmark giữa `MainActivity`, `EquipmentDetailActivity` và `ProfileActivity`.

3. **Màn hình Chi tiết & Đặt mượn (`EquipmentDetailActivity`)**:
   - Khi nhấn **"Confirm & Submit"**: Gọi `POST /api/v1/borrows` với `equipmentId`, `borrowTime`, `expectedReturnTime`, `purpose`, `note`.
   - Nhận về mã phiếu thực tế từ Server (ví dụ `#PM-20261112-001`), hiển thị thông báo thành công và cập nhật danh sách phiếu mượn.

4. **Màn hình Hồ sơ & Quản lý Mượn Trả (`ProfileActivity`)**:
   - Hiển thị thông tin thẻ sinh viên/giảng viên thực tế từ `GET /api/v1/auth/me` (`fullName`, `userCode`, `facultyName`, `className`, `role`).
   - Gọi `GET /api/v1/borrows/my` để hiển thị danh sách **Đang mượn (Active Loan)** và **Chờ duyệt (Pending Request)** thực tế.
   - Tích hợp nút **Gia hạn 24h** gọi `POST /api/v1/borrows/{id}/extend`.
   - Tích hợp mục **Báo cáo sự cố thiết bị** gọi `POST /api/v1/maintenance/report`.
   - Khi nhấn **Đăng xuất**: Xóa sạch token trong `SessionManager` trước khi quay về `LoginActivity`.

---

### Giai đoạn 5: Hoàn thiện Phân quyền Vai trò (Technician & Admin) & Kiểm thử
**Mục tiêu**: Đảm bảo cả 4 vai trò (`STUDENT`, `LECTURER`, `TECHNICIAN`, `ADMIN`) đều có đầy đủ chức năng tương ứng trên ứng dụng.

1. **Giao diện / Bảng điều khiển cho Kỹ thuật viên & Admin (trên Mobile)**:
   - Hiển thị lối vào **"Quản lý Kho & Duyệt Phiếu"** trên `MainActivity` hoặc `ProfileActivity` khi tài khoản đăng nhập có role `TECHNICIAN` hoặc `ADMIN`.
   - Cho phép xem danh sách phiếu `PENDING`, nhấn **Duyệt (Approve)** / **Từ chối (Reject)**, xác nhận **Giao thiết bị (Handover)** và **Nhận trả (Return)** ngay trên ứng dụng.
   - Đối với `ADMIN`: Hỗ trợ xem danh sách người dùng, đổi Role và Khóa/Mở khóa tài khoản (kết nối với `AdminUserController` đã có sẵn ở Backend).

2. **Cập nhật Seed Data (`database/unilend_schema.sql`) & Kiểm thử Tích hợp (End-to-End Testing)**:
   - Bổ sung đầy đủ 7 thiết bị hiển thị trên Mobile (Epson Projector, RØDE Wireless GO II, Raspberry Pi Kit, Sony Alpha a7 IV, DJI Air 3, MacBook Pro 16" M3 Max, Bose S1 Pro+) vào `unilend_schema.sql` để dữ liệu đồng nhất giữa Database – Backend – Mobile.
   - Kiểm thử biên dịch Backend (`mvn clean test compile`) và Mobile (`gradlew assembleDebug`).
   - Kiểm thử luồng nghiệp vụ khép kín:
     1. Sinh viên đăng ký / đăng nhập -> Tìm thiết bị -> Gửi yêu cầu mượn.
     2. Kỹ thuật viên đăng nhập -> Duyệt phiếu -> Xác nhận bàn giao Kiosk.
     3. Sinh viên xem thẻ Kiosk Pass / Gia hạn 24h / Báo cáo sự cố.
     4. Kỹ thuật viên xác nhận nhận trả thiết bị -> Tồn kho tự động cập nhật.

---

## 3. Bảng Tổng Hợp Các File Dự Kiến Tạo Mới / Cập Nhật

| Thành phần | Hạng mục công việc | File tạo mới / cập nhật chính |
| :--- | :--- | :--- |
| **Database** | Đồng bộ dữ liệu mẫu 7 thiết bị & phiếu mượn mẫu | `database/unilend_schema.sql` |
| **Backend** | Entities & Enums còn thiếu | `entity/Category.java`, `entity/Room.java`, `entity/BorrowRequestItem.java`, `entity/MaintenanceLog.java` + Enums |
| **Backend** | Repositories | `CategoryRepository.java`, `RoomRepository.java`, `BorrowRequestRepository.java`, `BorrowRequestItemRepository.java`, `MaintenanceLogRepository.java` |
| **Backend** | DTOs & Services | `EquipmentDto.java`, `BorrowRequestDto.java`, `EquipmentService.java`, `BorrowService.java`, `MaintenanceService.java` |
| **Backend** | REST Controllers | `EquipmentController.java`, `BorrowRequestController.java`, `MaintenanceController.java` |
| **Mobile** | Cấu hình mạng & Quyền truy cập | `gradle/libs.versions.toml`, `app/build.gradle.kts`, `AndroidManifest.xml` |
| **Mobile** | Quản lý phiên & API Client | `data/local/SessionManager.java`, `data/remote/ApiClient.java`, `data/remote/AuthApiService.java`, `data/remote/EquipmentApiService.java`, `data/remote/BorrowApiService.java` |
| **Mobile** | Nối API cho các màn hình | `LoginActivity.java`, `RegisterActivity.java`, `WelcomeActivity.java`, `MainActivity.java`, `EquipmentDetailActivity.java`, `ProfileActivity.java` |
| **Tài liệu** | Cập nhật hướng dẫn & danh sách API | `README.md`, `PLAN.md` |
