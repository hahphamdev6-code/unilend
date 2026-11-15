# UniLend - Hệ thống Quản lý Mượn Trả Thiết bị Trường Đại học

Dự án được xây dựng theo kiến trúc **Client–Server**:
- **Database**: MySQL 8.0+ (`unilend_db`)
- **Backend (Server)**: Java Spring Boot 3, Spring Data JPA, Spring Security + JWT Authentication
- **Mobile App (Client)**: Android Native (Java + XML), Retrofit2, OkHttp, SharedPreferences

---

## 1. Cấu trúc thư mục dự án

```text
unilend/
├── database/
│   └── unilend_schema.sql       # Script khởi tạo CSDL MySQL & dữ liệu mẫu
├── backend/                     # Dự án Java Spring Boot RESTful API
│   ├── pom.xml
│   └── src/main/
│       ├── java/vn/edu/unilend/
│       │   ├── controller/      # AuthController, AdminUserController
│       │   ├── dto/             # Request/Response DTOs
│       │   ├── entity/          # JPA Entities & Enums (Role, Status...)
│       │   ├── exception/       # GlobalExceptionHandler
│       │   ├── repository/      # Spring Data JPA Repositories
│       │   ├── security/        # JWT Provider, Filter, SecurityConfig
│       │   └── service/         # Business Logic (AuthService)
│       └── resources/
│           └── application.properties
└── mobile-android/              # Dự án Android Native (Java)
    ├── build.gradle.kts
    ├── settings.gradle.kts
    └── app/src/main/
        ├── AndroidManifest.xml
        ├── java/vn/edu/unilend/mobile/
        │   ├── data/local/      # SessionManager (Lưu JWT & Role)
        │   ├── data/model/      # Models đồng bộ với Backend
        │   ├── data/remote/     # Retrofit ApiClient & AuthApiService
        │   └── ui/              # LoginActivity, MainActivity (Phân quyền UI)
        └── res/layout/          # Giao diện XML
```

---

## 2. Hướng dẫn chạy dự án

### Bước 1: Khởi tạo Cơ sở dữ liệu MySQL
1. Mở MySQL Workbench / DBeaver / phpMyAdmin hoặc dòng lệnh MySQL.
2. Chạy toàn bộ file `database/unilend_schema.sql`.
3. Kiểm tra tài khoản mẫu đã được tạo sẵn (mật khẩu chung: `123456`):
   - **Quản trị viên (`ADMIN`)**: `ADMIN001` / `admin@unilend.edu.vn`
   - **Kỹ thuật viên (`TECHNICIAN`)**: `TECH001` / `tech@unilend.edu.vn`
   - **Giảng viên (`LECTURER`)**: `GV001` / `nam.lh@unilend.edu.vn`
   - **Sinh viên (`STUDENT`)**: `SV2026001` / `tuan.pm@sv.unilend.edu.vn`

### Bước 2: Chạy Backend (Spring Boot)
1. Mở thư mục `backend` bằng **IntelliJ IDEA** hoặc **Eclipse / VS Code**.
2. Kiểm tra thông tin `spring.datasource.username` và `spring.datasource.password` trong `backend/src/main/resources/application.properties`.
3. Chạy class `UnilendBackendApplication.java` (Server mặc định chạy tại cổng `8080`).

### Bước 3: Chạy Mobile App (Android Studio)
1. Mở thư mục `mobile-android` bằng **Android Studio**.
2. Đợi Gradle đồng bộ (Sync Project with Gradle Files).
3. Chạy ứng dụng trên máy ảo Android Emulator (mặc định kết nối tới `http://10.0.2.2:8080/`).
   - Nếu chạy trên điện thoại thật: Sửa `BASE_URL` trong `ApiClient.java` thành địa chỉ IP LAN của máy tính chạy Spring Boot.

---

## 3. Các Endpoint API Đăng nhập & Phân quyền đã hoàn thiện

| Phương thức | Endpoint | Quyền truy cập | Mô tả |
| :--- | :--- | :--- | :--- |
| `POST` | `/api/v1/auth/login` | Công khai | Đăng nhập bằng Mã SV/GV/NV hoặc Email, trả về JWT Token & Role |
| `POST` | `/api/v1/auth/register` | Công khai | Đăng ký tài khoản Sinh viên / Giảng viên |
| `GET` | `/api/v1/auth/me` | Đã đăng nhập | Lấy thông tin chi tiết & vai trò từ JWT Token |
| `GET` | `/api/v1/admin/users` | `ROLE_ADMIN` | Danh sách toàn bộ người dùng |
| `PATCH` | `/api/v1/admin/users/{id}/role` | `ROLE_ADMIN` | Thay đổi phân quyền (`STUDENT`, `LECTURER`, `TECHNICIAN`, `ADMIN`) |
| `PATCH` | `/api/v1/admin/users/{id}/status` | `ROLE_ADMIN` | Khóa hoặc mở khóa tài khoản (`ACTIVE`, `LOCKED`) |
| `PUT` | `/api/v1/auth/me` | Đã đăng nhập | Cập nhật thông tin cá nhân người dùng |
| `GET` | `/api/v1/faculties` | Công khai | Danh sách khoa / đơn vị |
| `GET` | `/api/v1/categories` | Đã đăng nhập | Danh sách danh mục thiết bị |
| `GET` | `/api/v1/rooms` | Đã đăng nhập | Danh sách phòng học / kho thiết bị |
| `GET` | `/api/v1/equipments` | Đã đăng nhập | Tìm kiếm & lọc danh sách thiết bị |
| `GET` | `/api/v1/equipments/{id}` | Đã đăng nhập | Chi tiết thiết bị |
| `GET` | `/api/v1/equipments/qr/{qrCode}` | Đã đăng nhập | Tra cứu thiết bị qua mã QR |
| `POST` / `PUT` / `PATCH` | `/api/v1/equipments/**` | `ADMIN`, `TECHNICIAN` | Thêm, sửa, đổi trạng thái thiết bị |
| `POST` | `/api/v1/borrows` | Đã đăng nhập | Tạo phiếu yêu cầu mượn thiết bị |
| `GET` | `/api/v1/borrows/my` | Đã đăng nhập | Danh sách phiếu mượn của bản thân |
| `POST` | `/api/v1/borrows/{id}/cancel` | Đã đăng nhập | Hủy phiếu mượn chờ duyệt |
| `POST` | `/api/v1/borrows/{id}/extend` | Đã đăng nhập | Gia hạn thời gian trả thiết bị (+24h) |
| `GET` / `PATCH` | `/api/v1/borrows/**` | `ADMIN`, `TECHNICIAN` | Duyệt, từ chối, bàn giao & nhận trả thiết bị |
| `POST` | `/api/v1/maintenance/report` | Đã đăng nhập | Báo cáo sự cố / hư hỏng thiết bị |
| `GET` / `PATCH` | `/api/v1/maintenance/**` | `ADMIN`, `TECHNICIAN` | Quản lý & xử lý bảo trì thiết bị |
