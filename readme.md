# Kế hoạch: Tái cấu trúc Hệ thống Xác thực & Phân chia Dự án cho 4 Nhà phát triển

## TÓM TẮT
Thay thế đăng nhập/đăng ký bằng **xác định người dùng tự động dựa trên UUID**. Mỗi người dùng nhận được ID duy nhất khi bật ứng dụng lần đầu (lưu trữ cục bộ), sau đó đăng ký với backend. Chia dự án thành **4 module độc lập** với mỗi nhà phát triển sở hữu cả frontend (Flutter) và backend (C#/.NET) cho tính năng của họ.

---

## **Phân chia Nhiệm vụ & Trách nhiệm**

| **Module** | **Người** | **Tính năng Frontend** | **Tính năng Backend** | **Tích hợp chính** |
|---|---|---|---|---|
| **Auth + Hồ sơ Người dùng** | Người 1 | SplashScreen, UUID generation, Edit/View Profile | RegisterUserDevice, User entity, JWT setup | Local storage ↔ Backend registration |
| **Design Studio** | Người 2 | Upload ảnh, UI xử lý, Slider Before/After | Image analysis, AI call, Status polling | DesignResult entity, ExternalAI service |
| **Lịch sử Dự án** | Người 3 | Danh sách dự án, Chi tiết view, Delete action | Project queries, Pagination, Filtering | DesignResult queries, soft delete |
| **Infrastructure** | Người 4 | Error dialogs, Retry logic, Network setup | Webhooks, Health check, Middleware | Program.cs, appsettings, CORS, Error handling |

---

## **Phân chia Giai đoạn (Quy trình Công việc)**

### **Giai đoạn 1: Thiết lập Infrastructure** *(Tất cả song song)*
- Người 4: Cấu hình `Program.cs`, JWT authentication, CORS, DbContext
- Người 1: Flutter local storage cho UUID, API client header injection
- Người 1+4: Định nghĩa appsettings.json (JWT secret, DB connection, API keys)

### **Giai đoạn 2: Database & Entities** *(Tất cả song song)*
- Người 1: `User` entity (Id, Name, AvatarUrl, CreatedAt)
- Người 2: `DesignResult` entity (Id, UserId FK, ImageUrls, Status)
- Người 3: Query design results (sử dụng entity của Người 2)
- Người 4: Đảm bảo EF Core migrations chạy

### **Giai đoạn 3: Dịch vụ Cốt lõi** *(Tuần tự: 1 → 2 → 3)*
- Người 1: `AuthManager` (RegisterOrGetUserAsync, UpdateProfileAsync)
- Người 2: `DesignManager` (CreateDesignAsync, StatusAsync, ProcessDesignAsync)
  - *Phụ thuộc vào*: User entity của Người 1 + DbContext setup của Người 4
- Người 3: Thêm history queries vào DesignManager (GetUserProjectsAsync, DeleteAsync)

### **Giai đoạn 4: API Endpoints** *(Tất cả song parallel)*
- **Người 1**: AuthController
  - `POST /api/auth/register-device` (userId, name) → Save user
  - `GET /api/auth/profile/{userId}` → Lấy hồ sơ
  - `PUT /api/auth/profile/{userId}` → Cập nhật hồ sơ
  
- **Người 2**: DesignController  
  - `POST /api/design/analyze` (image + userId) → Khởi tạo design job
  - `GET /api/design/status/{designId}` → Poll trạng thái
  
- **Người 3**: DesignController (thêm vào controller của Người 2)
  - `GET /api/design/projects?userId=...` → Danh sách dự án người dùng
  - `DELETE /api/design/{designId}` → Xóa dự án
  
- **Người 4**: WebhookController
  - `GET /api/health` → Health check
  - Error middleware + standardized response DTOs

### **Giai đoạn 5: Frontend Screens** *(Tất cả song parallel)*
- **Người 1**: SplashScreen (check local ID → register nếu mới), ProfileScreen
- **Người 2**: UploadPhotoScreen, ProcessingScreen, ResultScreen + before/after slider widget
- **Người 3**: HistoryListScreen, ProjectDetailScreen, ProjectCardWidget
- **Người 4**: Error handling, retry logic, loading overlays

### **Giai đoạn 6: Integration Testing** *(Xác minh tuần tự)*
- Người 1 → 2 → 3 → 4: Quy trình end-to-end (new user signup → upload → history)

---

## **Các File Quan trọng cần Tạo/Sửa đổi**

### **Backend (C#)**
| File | Hành động | Thay đổi chính |
|---|---|---|
| `Program.cs` | MODIFY | Thêm JWT + DbContext + CORS + error middleware |
| `appsettings.json` | MODIFY | Jwt, Database, ExternalAI, Cors config |
| `Extensions/ServiceCollectionExtensions.cs` | MODIFY | AddJwtBearer, AddCors, AddDbContext |
| `Controllers/AuthController.cs` | CREATE | RegisterDevice, GetProfile, UpdateProfile |
| `Controllers/DesignController.cs` | CREATE | AnalyzeImage, GetStatus, ListProjects, DeleteProject |
| `Controllers/WebhookController.cs` | CREATE | Health check, webhook handlers tương lai |
| `Models/Entities/User.cs` | CREATE | Id (UUID), Name, AvatarUrl, timestamps |
| `Models/Entities/DesignResult.cs` | CREATE | Id, UserId (FK), ImageUrls, Status, timestamps |
| `Models/DTOs/*` | CREATE | RegisterDeviceRequest, DesignRequest, ProjectListResponse, etc. |
| `Services/AuthManager.cs` | CREATE | RegisterOrGetUserAsync, UpdateProfileAsync |
| `Services/DesignManager.cs` | CREATE | Design CRUD, status queries, user project queries |
| `Services/ExternalAI.cs` | MODIFY | Implement AI generation (Gemini/Pollinations) |

### **Frontend (Flutter)**
| File | Hành động | Thay đổi chính |
|---|---|---|
| `lib/main.dart` | MODIFY | Xóa login route, thêm SplashScreen entry |
| `lib/features/auth/screens/splash_screen.dart` | CREATE | UUID check → register nếu mới → home |
| `lib/features/auth/screens/user_profile_screen.dart` | CREATE | Edit name, avatar |
| `lib/features/auth/screens/login_screen.dart` | DELETE | Không cần thiết nữa |
| `lib/features/auth/screens/register_screen.dart` | DELETE | Không cần thiết nữa |
| `lib/features/auth/controllers/auth_controller.dart` | MODIFY | UUID generation, local storage, registration logic |
| `lib/features/design_studio/screens/*` | CREATE | UploadPhotoScreen, ProcessingScreen, ResultScreen |
| `lib/features/design_studio/controllers/design_controller.dart` | CREATE | Upload, poll, save |
| `lib/features/project_history/screens/*` | CREATE | HistoryScreen, ProjectDetailScreen |
| `lib/core/network/api_client.dart` | CREATE | Dio setup + user-id header injection |
| `lib/core/constants/api_endpoints.dart` | CREATE | Base URL + tất cả endpoint paths |
| `lib/routes/app_routes.dart` | MODIFY | Cập nhật route config (xóa login/register) |
| `pubspec.yaml` | MODIFY | Thêm: uuid, shared_preferences, image_picker, intl |

---

## **Danh sách Kiểm tra Xác minh**

### **Người 1 ✓**
- [ ] UUID được tạo khi bật ứng dụng lần đầu → lưu vào SharedPreferences
- [ ] `POST /api/auth/register-device` tạo user trong DB
- [ ] `GET /api/auth/profile/{userId}` trả về dữ liệu người dùng
- [ ] `PUT /api/auth/profile/{userId}` cập nhật hồ sơ
- [ ] User ID được gửi trong header `user-id` của tất cả các yêu cầu sau
- [ ] SplashScreen redirect hoạt động (user mới → register → main app)

### **Người 2 ✓**
- [ ] Photo picker mở (camera + gallery)
- [ ] `POST /api/design/analyze` trả về designId
- [ ] `GET /api/design/status/{designId}` poll chính xác (khoảng 2s)
- [ ] Slider Before/After hiển thị cả hai ảnh
- [ ] DesignResult liên kết với UserId chính xác

### **Người 3 ✓**
- [ ] `GET /api/design/projects` trả về dự án phân trang của người dùng
- [ ] History screen hiển thị grid dự án với thumbnails
- [ ] Click dự án → detail screen hiển thị ảnh đầy đủ
- [ ] Nút delete xóa dự án khỏi danh sách

### **Người 4 ✓**
- [ ] Backend biên dịch, `GET /api/health` trả về 200
- [ ] JWT middleware xác thực yêu cầu
- [ ] CORS cho phép yêu cầu từ frontend
- [ ] Response lỗi tuân theo định dạng tiêu chuẩn
- [ ] Frontend hiển thị thông báo lỗi thân thiện

---

## **Quyết định Chính**

✅ **UUID-based auth** (đơn giản hơn đăng nhập mật khẩu, phù hợp với di động)  
✅ **Local persistence** (SharedPreferences + DB backup)  
✅ **User-id header** thay vì JWT tokens (có thể nâng cấp sau)  
✅ **Short-polling** cho trạng thái design (nâng cấp lên Hangfire/webhooks nếu mở rộng)  
✅ **Module ownership**: Mỗi người sở hữu cả frontend + backend (giảm thiểu chặn chéo)  
✅ **Cloud storage** được giả định cho ảnh (Azure Blob / AWS S3)

---

## **Xem xét Thêm**

1. **Tốc độ Xử lý Design**: Nếu >30s, triển khai Hangfire thay vì polling
2. **Nhà cung cấp Lưu trữ Ảnh**: Chọn Azure Blob, AWS S3, hoặc tùy chọn lưu trữ cục bộ
3. **Push Notifications** (Tương lai): Người 4 có thể thêm Firebase Cloud Messaging khi design hoàn thành

---

## **Cấu trúc Dự án Flutter Cập nhật**
