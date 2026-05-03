# 📋 KẾ HOẠCH PHÂN CHIA CÔNG VIỆC - DỰ ÁN THIẾT KẾ NỘI THẤT

> **Dự án:** Interior AI - Ứng dụng thiết kế nội thất AI từ ảnh chụp  



---

## 📌 TÓM TẮT CHUNG

Dự án sẽ **loại bỏ hoàn toàn hệ thống đăng nhập/đăng ký truyền thống**. Thay vào đó:

✅ **Mỗi người dùng sẽ tự động nhận 1 UUID ** khi bật ứng dụng lần đầu  
✅ **UUID được lưu cục bộ** trên điện thoại + đồng bộ với server  
✅ **Dự án chia thành 4 module độc lập**, mỗi người nắm cả Frontend (Flutter) + Backend (C#)  
✅ **Không có phụ thuộc ngang**, giảm xung đột merge code

---

## 👥 PHÂN CHIA 

### **👤 Duy: Xác thực + Hồ sơ Người dùng (Auth + Profile)**

**Mô tả công việc:**

- Tạo hệ thống UUID tự động
- Quản lý lưu trữ người dùng cục bộ
- Xây dựng màn hình sửa profile
- Tạo API đăng ký thiết bị

**Frontend (Flutter):**

1. `lib/features/auth/screens/splash_screen.dart`
   - Kiểm tra nếu có UUID đã lưu?
   - **Có:** Lấy profile → vào app chính
   - **Không:** Tạo UUID mới → gửi đến backend → lưu vào local storage → vào app chính

2. `lib/features/auth/screens/user_profile_screen.dart`
   - Hiển thị thông tin user (tên, hình đại diện)
   - Form chỉnh sửa: tên, chọn ảnh avatar
   - Nút lưu → gửi API cập nhật

3. `lib/features/auth/controllers/auth_controller.dart`
   - Hàm tạo UUID: `generateUUID()`
   - Hàm lưu cục bộ: `saveUserIdLocally(uuid)`
   - Hàm tải từ cục bộ: `loadUserIdFromLocal()`
   - Hàm đăng ký device: `registerDevice(userId, name)`
   - Hàm cập nhật profile: `updateProfile(userId, name, avatarUrl)`

4. `lib/core/network/api_client.dart` - **QUAN TRỌNG**
   - Thiết lập Dio HTTP client
   - **Tự động thêm header:** `user-id: {uuid}` vào mỗi request
   - Xử lý lỗi 401 Unauthorized

5. Sửa `lib/main.dart` → Thay thế LoginScreen bằng SplashScreen

6. Dependencies cần thêm:
   ```yaml
   uuid: ^4.0.0
   shared_preferences: ^2.0.0
   dio: ^5.0.0
   ```

**Backend (C#):**

1. `Models/Entities/User.cs`

   ```csharp
   public class User {
     public string Id { get; set; }           // UUID từ client
     public string? Name { get; set; }        // Tên người dùng
     public string? AvatarUrl { get; set; }  // Link ảnh avatar
     public DateTime CreatedAt { get; set; } // Ngày tạo
     public DateTime UpdatedAt { get; set; } // Lần sửa cuối
   }
   ```

2. `Models/DTOs/RegisterDeviceRequest.cs`

   ```csharp
   public class RegisterDeviceRequest {
     public string UserId { get; set; }      // UUID từ Flutter
     public string? Name { get; set; }
     public string? AvatarUrl { get; set; }
   }
   ```

3. `Models/DTOs/UserProfileResponse.cs`

   ```csharp
   public class UserProfileResponse {
     public string UserId { get; set; }
     public string? Name { get; set; }
     public string? AvatarUrl { get; set; }
     public DateTime CreatedAt { get; set; }
   }
   ```

4. `Services/AuthManager.cs`

   ```csharp
   public class AuthManager {
     public async Task<User> RegisterOrGetUserAsync(string userId, string? name) {
       // Kiểm tra user đã tồn tại?
       // Nếu không → tạo user mới
       // Trả về user object
     }

     public async Task<User> UpdateUserProfileAsync(string userId, string? name, string? avatarUrl) {
       // Cập nhật name và avatarUrl
       // Trả về user object đã cập nhật
     }

     public async Task<User?> GetUserAsync(string userId) {
       // Lấy thông tin user từ DB
     }
   }
   ```

5. `Controllers/AuthController.cs`

   ```csharp
   [ApiController]
   [Route("api/[controller]")]
   public class AuthController : ControllerBase {
     private readonly AuthManager _authManager;

     // POST /api/auth/register-device
     [HttpPost("register-device")]
     public async Task<IActionResult> RegisterDevice([FromBody] RegisterDeviceRequest request) {
       var user = await _authManager.RegisterOrGetUserAsync(request.UserId, request.Name);
       return Ok(new UserProfileResponse { ... });
     }

     // GET /api/auth/profile/{userId}
     [HttpGet("profile/{userId}")]
     public async Task<IActionResult> GetProfile(string userId) {
       var user = await _authManager.GetUserAsync(userId);
       return Ok(new UserProfileResponse { ... });
     }

     // PUT /api/auth/profile/{userId}
     [HttpPut("profile/{userId}")]
     public async Task<IActionResult> UpdateProfile(string userId, [FromBody] UpdateProfileRequest request) {
       var user = await _authManager.UpdateUserProfileAsync(userId, request.Name, request.AvatarUrl);
       return Ok(new UserProfileResponse { ... });
     }
   }
   ```

6. `Data/AppDbContext.cs`

   ```csharp
   public DbSet<User> Users { get; set; }
   ```

7. Tạo migration và update database
   ```bash
   dotnet ef migrations add AddUserEntity
   dotnet ef database update
   ```

**Quy trình người dùng Duy xây dựng:**

```
Bật ứng dụng lần đầu
    ↓
SplashScreen check SharedPreferences
    ↓
  ✗ Không có UUID
    ↓
  Tạo UUID mới (uuid: ^4.0.0 library)
    ↓
  Lưu vào SharedPreferences
    ↓
  Gọi POST /api/auth/register-device
    ↓
  (Backend: AuthManager tạo user record)
    ↓
  ✓ Có UUID & đã đăng ký
    ↓
  Vào Home Screen
```

---

### **👤 Hào: Design Studio (Phòng thiết kế)**

**Mô tả công việc:**

- Xây dựng tính năng chụp/chọn ảnh
- Gửi ảnh lên AI để xử lý
- Hiển thị trạng thái xử lý (đang chờ)
- Hiển thị kết quả trước/sau bằng slider

**Frontend (Flutter):**

1. `lib/features/design_studio/screens/upload_photo_screen.dart`
   - Nút "Chọn từ Camera" → mở camera
   - Nút "Chọn từ Thư viện" → mở gallery
   - Hiển thị preview ảnh đã chọn
   - Nút "Tạo Thiết kế" → gửi API

2. `lib/features/design_studio/screens/processing_screen.dart`
   - Hiển thị loading spinner
   - Sau từng 2 giây, gọi `GET /api/design/status/{designId}`
   - Kiểm tra status:
     - `"pending"` → tiếp tục chờ + polling
     - `"completed"` → chuyển sang ResultScreen
     - `"failed"` → hiển thị lỗi

3. `lib/features/design_studio/screens/result_screen.dart`
   - Hiển thị ảnh gốc bên trái, ảnh thiết kế bên phải
   - Có slider giữa 2 ảnh (dùng widget bên dưới)
   - Nút "Lưu vào Dự án" → thêm vào history
   - Nút "Quay lại" → upload ảnh khác

4. `lib/features/design_studio/widgets/image_slider_before_after.dart`
   - Custom widget hiển thị 2 ảnh side-by-side
   - Có slider ngang để so sánh
   - Smooth animation

5. `lib/features/design_studio/controllers/design_controller.dart`

   ```dart
   class DesignController extends ChangeNotifier {
     String? designId;
     String? originalImageUrl;
     String? designedImageUrl;
     String status = "idle"; // "idle", "uploading", "processing", "completed", "error"

     Future<void> uploadPhoto(File imageFile) async {
       // Gọi POST /api/design/analyze
       // Nhận designId
       // Đặt status = "processing"
       // Gọi startPolling()
     }

     Future<void> pollStatus() async {
       // Gọi GET /api/design/status/{designId}
       // Nếu status đã "completed" → loadDesignImages()
     }

     void startPolling() {
       // Mỗi 2 giây gọi 1 lần pollStatus()
     }

     Future<void> saveDesign() async {
       // Lưu vào project history
       // Update DB
     }
   }
   ```

6. Models: `design_request.dart`, `design_result.dart`

   ```dart
   class DesignRequest {
     final String imageBase64; // hoặc File
     final String userId;
   }

   class DesignResult {
     final String designId;
     final String originalImageUrl;
     final String? designedImageUrl;
     final String status; // "pending", "completed", "failed"
   }
   ```

7. Dependencies:
   ```yaml
   image_picker: ^1.0.0
   image: ^4.0.0
   ```

**Backend (C#):**

1. `Models/Entities/DesignResult.cs`

   ```csharp
   public class DesignResult {
     public string Id { get; set; }                // UUID của design job
     public string UserId { get; set; }            // FK tới User
     public string OriginalImageUrl { get; set; } // Link ảnh gốc (cloud storage)
     public string? DesignedImageUrl { get; set; }// Link ảnh thiết kế (null nếu chưa xong)
     public string? DesignPrompt { get; set; }    // Prompt cho AI (tùy chọn)
     public string Status { get; set; }           // "pending" | "completed" | "failed"
     public DateTime CreatedAt { get; set; }
     public DateTime UpdatedAt { get; set; }
   }
   ```

2. `Models/DTOs/DesignRequest.cs`

   ```csharp
   public class DesignRequest {
     public string ImageBase64 { get; set; }  // Hoặc upload file stream
     public string? DesignPrompt { get; set; }
   }
   ```

3. `Models/DTOs/DesignResponse.cs`

   ```csharp
   public class DesignResponse {
     public string DesignId { get; set; }
     public string OriginalImageUrl { get; set; }
     public string Status { get; set; } // "pending"
   }
   ```

4. `Models/DTOs/DesignStatusResponse.cs`

   ```csharp
   public class DesignStatusResponse {
     public string DesignId { get; set; }
     public string Status { get; set; }
     public string? DesignedImageUrl { get; set; }
   }
   ```

5. `Services/DesignManager.cs`

   ```csharp
   public class DesignManager {
     public async Task<DesignResult> CreateDesignAsync(string userId, Stream imageStream) {
       // 1. Lưu ảnh lên cloud storage (Azure Blob / AWS S3)
       // 2. Tạo DesignResult record trong DB với status = "pending"
       // 3. Trigger background job để xử lý (queue message)
       // 4. Return DesignResult object
     }

     public async Task<DesignResult?> GetDesignStatusAsync(string designId) {
       // Lấy DesignResult từ DB
       // Return (bao gồm status + designedImageUrl nếu completed)
     }

     public async Task ProcessDesignAsync(string designId) {
       // 1. Lấy DesignResult từ DB
       // 2. Lấy ảnh gốc từ cloud storage
       // 3. Gọi ExternalAI.GenerateDesignAsync(imageUrl)
       // 4. Lưu ảnh thiết kế lên cloud storage
       // 5. Cập nhật DesignResult.DesignedImageUrl + Status = "completed"
     }
   }
   ```

6. `Services/ExternalAI.cs`

   ```csharp
   public class ExternalAI : IExternalAIService {
     public async Task<string> GenerateDesignAsync(string imageUrl, string? prompt) {
       // Gọi API Gemini / Pollinations API
       // Gửi ảnh URL + prompt
       // Nhận kết quả ảnh thiết kế
       // Return URL của ảnh thiết kế
     }
   }
   ```

7. `Controllers/DesignController.cs`

   ```csharp
   [ApiController]
   [Route("api/[controller]")]
   public class DesignController : ControllerBase {
     private readonly DesignManager _designManager;

     // POST /api/design/analyze
     [HttpPost("analyze")]
     public async Task<IActionResult> AnalyzeImage([FromForm] IFormFile image) {
       string userId = HttpContext.Request.Headers["user-id"];
       var result = await _designManager.CreateDesignAsync(userId, image.OpenReadStream());
       return Ok(new DesignResponse {
         DesignId = result.Id,
         OriginalImageUrl = result.OriginalImageUrl,
         Status = result.Status
       });
     }

     // GET /api/design/status/{designId}
     [HttpGet("status/{designId}")]
     public async Task<IActionResult> GetStatus(string designId) {
       var result = await _designManager.GetDesignStatusAsync(designId);
       return Ok(new DesignStatusResponse {
         DesignId = result.Id,
         Status = result.Status,
         DesignedImageUrl = result.DesignedImageUrl
       });
     }
   }
   ```

8. `Data/AppDbContext.cs`

   ```csharp
   public DbSet<DesignResult> DesignResults { get; set; }
   ```

9. Migration:
   ```bash
   dotnet ef migrations add AddDesignResultEntity
   dotnet ef database update
   ```

**Background Job (Hiệp hỗ trợ):**

- Sử dụng Hangfire hoặc Azure Service Bus để xử lý ảnh không đồng bộ
- Hoặc dùng simple Timer Task (cho MVP)

**Quy trình Hào xây dựng:**

```
Upload ảnh
    ↓
POST /api/design/analyze
    ↓
Backend: DesignManager.CreateDesignAsync()
    ↓
  Lưu ảnh gốc → cloud
  Tạo DesignResult (status = "pending")
  Trigger background job
    ↓
Frontend: Chuyển sang ProcessingScreen
    ↓
Polling: GET /api/design/status/{designId}
    ↓
  Status = "pending" → chờ 2s → polling lại
  Status = "completed" → chuyển ResultScreen
    ↓
Backend: ProcessDesignAsync() chạy ở background
    ↓
  Gọi AI (Gemini/Pollinations)
  Lưu ảnh thiết kế → cloud
  Cập nhật DesignResult (status = "completed", DesignedImageUrl)
    ↓
Frontend ResultScreen: Hiển thị before/after slider
```

---

### **👤 Đạt: Project History (Lịch sử Dự án)**

**Mô tả công việc:**

- Xây dựng màn hình danh sách các thiết kế đã tạo
- Hiển thị chi tiết từng dự án
- Cho phép xóa dự án
- Hỗ trợ pagination & filtering

**Frontend (Flutter):**

1. `lib/features/project_history/screens/history_screen.dart`
   - Khi mở: gọi `GET /api/design/projects?page=1`
   - Hiển thị grid hoặc list các dự án (thumbnail + ngày tạo)
   - Pull-to-refresh cập nhật
   - Scroll xuống → load thêm (pagination: page=1, page=2, ...)
   - Tap project → chuyển sang ProjectDetailScreen

2. `lib/features/project_history/screens/project_detail_screen.dart`
   - Hiển thị:
     - Ảnh gốc (toàn màn hình)
     - Ảnh thiết kế (toàn màn hình)
     - Thông tin: ngày tạo, ID dự án
   - Nút "Xem again" → Quay lại result_screen
   - Nút "Xóa" → Gọi `DELETE /api/design/{designId}` → quay về list

3. `lib/features/project_history/widgets/project_card.dart`
   - Custom widget card:
     - Thumbnail ảnh thiết kế
     - Tên dự án (hoặc ID)
     - Ngày tạo (format: "19/4/2026")
   - OnTap → Navigate tới detail

4. `lib/features/project_history/controllers/history_controller.dart`

   ```dart
   class HistoryController extends ChangeNotifier {
     List<ProjectModel> projects = [];
     int currentPage = 1;
     bool isLoadingMore = false;

     Future<void> fetchProjects({int page = 1}) async {
       // Gọi GET /api/design/projects?page={page}&pageSize=10
       // Cập nhật projects list
       // notifyListeners()
     }

     Future<void> deleteProject(String designId) async {
       // Gọi DELETE /api/design/{designId}
       // Xóa khỏi projects list
       // notifyListeners()
     }
   }
   ```

5. Model: `project_model.dart`

   ```dart
   class ProjectModel {
     final String designId;
     final String originalImageUrl;
     final String designedImageUrl;
     final DateTime createdAt;
     final String status;
   }
   ```

6. Dependencies:
   ```yaml
   intl: ^0.19.0  // Date formatting
   ```

**Backend (C#):**

1. Sửa `Controllers/DesignController.cs` (Hào xây dựng, Đạt thêm endpoints)

   ```csharp
   // GET /api/design/projects?page=1&pageSize=10
   [HttpGet("projects")]
   public async Task<IActionResult> GetUserProjects(int page = 1, int pageSize = 10) {
     string userId = HttpContext.Request.Headers["user-id"];
     var (projects, total) = await _designManager.GetUserProjectsAsync(
       userId, page, pageSize
     );
     return Ok(new {
       data = projects,
       page = page,
       pageSize = pageSize,
       total = total,
       totalPages = (total + pageSize - 1) / pageSize
     });
   }

   // DELETE /api/design/{designId}
   [HttpDelete("{designId}")]
   public async Task<IActionResult> DeleteProject(string designId) {
     string userId = HttpContext.Request.Headers["user-id"];
     await _designManager.DeleteProjectAsync(designId, userId);
     return Ok(new { message = "Deleted successfully" });
   }
   ```

2. Sửa `Services/DesignManager.cs` - Thêm methods:

   ```csharp
   public async Task<(List<DesignResult>, int)> GetUserProjectsAsync(
     string userId, int page, int pageSize) {
     // Query DB: SELECT * FROM DesignResults
     // WHERE UserId = userId AND IsDeleted = false
     // ORDER BY CreatedAt DESC
     // SKIP (page-1)*pageSize TAKE pageSize

     var projects = await _context.DesignResults
       .Where(d => d.UserId == userId && !d.IsDeleted)
       .OrderByDescending(d => d.CreatedAt)
       .Skip((page - 1) * pageSize)
       .Take(pageSize)
       .ToListAsync();

     var total = await _context.DesignResults
       .Where(d => d.UserId == userId && !d.IsDeleted)
       .CountAsync();

     return (projects, total);
   }

   public async Task DeleteProjectAsync(string designId, string userId) {
     // Kiểm tra: design có thuộc quyền sở hữu userId này không?
     var design = await _context.DesignResults.FindAsync(designId);
     if (design.UserId != userId) throw new UnauthorizedAccessException();

     // Soft delete: đặt IsDeleted = true
     design.IsDeleted = true;
     design.UpdatedAt = DateTime.UtcNow;
     await _context.SaveChangesAsync();
   }
   ```

3. Sửa `Models/Entities/DesignResult.cs` - Thêm:

   ```csharp
   public bool IsDeleted { get; set; } = false;  // Soft delete flag
   ```

4. Migration:
   ```bash
   dotnet ef migrations add AddIsDeletedToDesignResult
   dotnet ef database update
   ```

**Quy trình Đạt xây dựng:**

```
Vào màn History
    ↓
GET /api/design/projects?page=1&pageSize=10
    ↓
Backend: DesignManager.GetUserProjectsAsync()
    ↓
  Query: Lấy tất cả DesignResults của user
  Filter: IsDeleted = false
  Order: theo CreatedAt DESC
  Paginate: skip & take
    ↓
Frontend: Hiển thị grid projects
    ↓
  Scroll xuống → Tự động load page tiếp theo
Tap project
    ↓
ProjectDetailScreen: Hiển thị before/after
    ↓
Nút Delete
    ↓
DELETE /api/design/{designId}
    ↓
Soft delete: IsDeleted = true
    ↓
Frontend: Xóa khỏi list, back về history
```

---

### **👤 Hiệp: Infrastructure + Web Hooks**

**Mô tả công việc:**

- Cấu hình toàn bộ backend (Program.cs)
- Thiết lập JWT authentication & CORS
- Xây dựng error handling & middleware
- Tạo health check endpoints
- Chuẩn bị background jobs (Hangfire optional)

**Frontend (Flutter):**

1. `lib/core/utils/error_handler.dart`

   ```dart
   class ErrorHandler {
     static void showError(BuildContext context, String message) {
       ScaffoldMessenger.of(context).showSnackBar(
         SnackBar(content: Text(message), backgroundColor: Colors.red)
       );
     }

     static String getErrorMessage(DioException error) {
       // Map error codes → Vietnamese messages
       if (error.response?.statusCode == 401) {
         return "Phiên làm việc hết hạn, vui lòng restart ứng dụng";
       } else if (error.response?.statusCode == 500) {
         return "Lỗi máy chủ, vui lòng thử lại sau";
       }
       return error.message ?? "Lỗi kết nối";
     }
   }
   ```

2. Sửa `lib/core/network/api_client.dart`
   - Thêm Interceptor xử lý retry (exponential backoff)
   - Thêm error mapping

3. `lib/shared/widgets/error_dialog.dart`

   ```dart
   class ErrorDialog extends StatelessWidget {
     final String title;
     final String message;
     final VoidCallback? onRetry;

     // Dialog hiển thị error với nút Retry
   }
   ```

4. Logging (tùy chọn):
   - Firebase Crashlytics hoặc Sentry

**Backend (C# - ĐÂY LÀ CÔNG VIỆC CHÍNH CỦA Hiệp):**

1. **Sửa `Program.cs`** - CẤU HÌNH TOÀN BỘ:

   ```csharp
   var builder = WebApplicationBuilder.CreateBuilder(args);

   // 1️⃣ Cấu hình Database
   builder.Services.AddDbContext<AppDbContext>(options =>
       options.UseSqlServer(
           builder.Configuration.GetConnectionString("DefaultConnection")
       )
   );

   // 2️⃣ Cấu hình JWT Authentication
   var jwtKey = builder.Configuration["Jwt:Key"];
   var jwtIssuer = builder.Configuration["Jwt:Issuer"];
   var jwtAudience = builder.Configuration["Jwt:Audience"];

   builder.Services.AddAuthentication(JwtBearerDefaults.AuthenticationScheme)
       .AddJwtBearer(options => {
           options.TokenValidationParameters = new TokenValidationParameters {
               ValidateIssuerSigningKey = true,
               IssuerSigningKey = new SymmetricSecurityKey(
                   Encoding.ASCII.GetBytes(jwtKey)
               ),
               ValidateIssuer = true,
               ValidIssuer = jwtIssuer,
               ValidateAudience = true,
               ValidAudience = jwtAudience,
               ValidateLifetime = true,
               ClockSkew = TimeSpan.Zero
           };
       });

   // 3️⃣ Cấu hình CORS
   builder.Services.AddCors(options => {
       options.AddPolicy("AllowAll", (builder) => {
           builder
               .AllowAnyOrigin()
               .AllowAnyMethod()
               .AllowAnyHeader();
       });
   });

   // 4️⃣ Đăng ký Services
   builder.Services.AddScoped<AuthManager>();
   builder.Services.AddScoped<DesignManager>();
   builder.Services.AddScoped<IExternalAIService, ExternalAI>();

   // 5️⃣ Thêm Controllers & Swagger
   builder.Services.AddControllers();
   builder.Services.AddSwaggerGen();

   var app = builder.Build();

   // 6️⃣ Middleware Pipeline
   if (app.Environment.IsDevelopment()) {
       app.UseSwagger();
       app.UseSwaggerUI();
   }

   // Thứ tự middleware rất quan trọng!
   app.UseHttpsRedirection();
   app.UseCors("AllowAll");
   app.UseAuthentication();  // ← Phải trước UseAuthorization
   app.UseAuthorization();
   app.UseMiddleware<ErrorHandlingMiddleware>();  // Custom error handler

   app.MapControllers();

   app.Run();
   ```

2. **Sửa `appsettings.json`:**

   ```json
   {
     "ConnectionStrings": {
       "DefaultConnection": "Server=localhost;Database=InteriorAI;Integrated Security=true;"
     },
     "Jwt": {
       "Key": "your-super-secret-key-that-is-at-least-32-characters-long",
       "Issuer": "InteriorAI",
       "Audience": "InteriorAI-App"
     },
     "ExternalAI": {
       "Provider": "Gemini",
       "ApiKey": "your-gemini-api-key-here",
       "BaseUrl": "https://generativelanguage.googleapis.com/v1beta/models"
     },
     "CloudStorage": {
       "Provider": "AzureBlob",
       "ConnectionString": "DefaultEndpointsProtocol=https;..."
     },
     "Logging": {
       "LogLevel": {
         "Default": "Information"
       }
     }
   }
   ```

3. **Tạo `Extensions/ServiceCollectionExtensions.cs`:**

   ```csharp
   public static class ServiceCollectionExtensions {
       public static IServiceCollection AddJwtAuthentication(
           this IServiceCollection services,
           IConfiguration configuration) {
           var key = Encoding.ASCII.GetBytes(configuration["Jwt:Key"]);

           services.AddAuthentication(JwtBearerDefaults.AuthenticationScheme)
               .AddJwtBearer(options => {
                   options.TokenValidationParameters = new TokenValidationParameters {
                       ValidateIssuerSigningKey = true,
                       IssuerSigningKey = new SymmetricSecurityKey(key),
                       ValidateIssuer = true,
                       ValidIssuer = configuration["Jwt:Issuer"],
                       ValidateAudience = true,
                       ValidAudience = configuration["Jwt:Audience"],
                       ValidateLifetime = true
                   };
               });

           return services;
       }

       public static IServiceCollection AddCorsPolicy(
           this IServiceCollection services) {
           services.AddCors(options => {
               options.AddPolicy("AllowAll", builder => {
                   builder
                       .AllowAnyOrigin()
                       .AllowAnyMethod()
                       .AllowAnyHeader();
               });
           });

           return services;
       }
   }
   ```

4. **Tạo `Middleware/ErrorHandlingMiddleware.cs`:**

   ```csharp
   public class ErrorHandlingMiddleware {
       private readonly RequestDelegate _next;
       private readonly ILogger<ErrorHandlingMiddleware> _logger;

       public ErrorHandlingMiddleware(RequestDelegate next, ILogger<ErrorHandlingMiddleware> logger) {
           _next = next;
           _logger = logger;
       }

       public async Task InvokeAsync(HttpContext context) {
           try {
               await _next(context);
           } catch (Exception ex) {
               _logger.LogError(ex, "Unhandled exception occurred");
               context.Response.StatusCode = StatusCodes.Status500InternalServerError;
               context.Response.ContentType = "application/json";

               await context.Response.WriteAsJsonAsync(new {
                   error = "Internal Server Error",
                   message = ex.Message,
                   statusCode = 500
               });
           }
       }
   }
   ```

5. **Tạo `Controllers/WebhookController.cs`:**

   ```csharp
   [ApiController]
   [Route("api/[controller]")]
   public class WebhookController : ControllerBase {

       [HttpGet("health")]
       public IActionResult Health() {
           return Ok(new {
               status = "healthy",
               timestamp = DateTime.UtcNow,
               version = "1.0.0"
           });
       }

       [HttpPost("design-complete")]
       public async Task<IActionResult> DesignComplete([FromBody] DesignCompleteWebhookDto dto) {
           // Handle callback từ AI service khi design xong
           // Sau này sử dụng với Hangfire / Background Jobs
           return Ok(new { message = "Webhook received" });
       }
   }
   ```

6. **Tạo Base Controller:**

   ```csharp
   [ApiController]
   public abstract class BaseApiController : ControllerBase {
       protected string? GetUserId() {
           return HttpContext.Request.Headers["user-id"].ToString();
       }
   }
   ```

7. **Sửa `Data/AppDbContext.cs`:**

   ```csharp
   public class AppDbContext : DbContext {
       public AppDbContext(DbContextOptions<AppDbContext> options) : base(options) {
       }

       public DbSet<User> Users { get; set; }
       public DbSet<DesignResult> DesignResults { get; set; }

       protected override void OnModelCreating(ModelBuilder modelBuilder) {
           base.OnModelCreating(modelBuilder);

           // Cấu hình relationships
           modelBuilder.Entity<DesignResult>()
               .HasOne<User>()
               .WithMany()
               .HasForeignKey(d => d.UserId);
       }
   }
   ```

8. **Database Setup:**

   ```bash
   # Tạo migration đầu tiên
   dotnet ef migrations add InitialCreate

   # Cập nhật database
   dotnet ef database update
   ```

9. **Response DTO Standard:**

   ```csharp
   public class ApiResponse<T> {
       public T? Data { get; set; }
       public string? Message { get; set; }
       public bool Success { get; set; }
       public int StatusCode { get; set; }
   }

   public class ApiErrorResponse {
       public string Error { get; set; }
       public string? Details { get; set; }
       public int StatusCode { get; set; }
   }
   ```

**Quy trình Hiệp xây dựng:**

```
Tuần 1: Infrastructure Setup
    ↓
Program.cs ← Cấu hình Services
    ↓
appsettings.json ← Config Jwt, DB, API keys
    ↓
ServiceCollectionExtensions ← Helper registration
    ↓
DbContext ← Kết nối database
    ↓
Middleware (Authentication, CORS, Error handling)
    ↓
Health check + Webhook endpoints
    ↓
Database migrations
    ↓
Tuần 2: Support các teams khác
    ↓
middleware & config issues
```

---

## 📅 LỘ TRÌNH THỰC HIỆN

### **Giai đoạn 1: Chuẩn bị Infrastructure (Tuần 1)**

**Task song song:**

- ✅ Hiệp: Cấu hình Program.cs, DbContext, Middleware
- ✅ Duy: Thiết lập local storage (SharedPreferences), UUID library
- ✅ Duy: Xác định config (JWT secret, DB connection string)

**Output:**

- Backend compiles successfully
- `dotnet ef database update` chạy được
- `GET /api/health` trả về 200 OK

---

### **Giai đoạn 2: Database Entities (Tuần 1)**

**Task song parallel:**

- ✅ Duy: Tạo User.cs entity + migration
- ✅ Hào: Tạo DesignResult.cs entity + migration
- ✅ Đạt: Xem xét entity của Hào (sẽ query nó)

**Output:**

- User table created
- DesignResult table created
- Migrations run successfully

---

### **Giai đoạn 3: Core Services (Tuần 1-2)**

**Task tuần tự (có phụ thuộc):**

- ✅ Duy: Viết AuthManager → không phụ thuộc ai
- ✅ Hào: Viết DesignManager (phụ thuộc User entity từ Duy)
- ✅ Đạt: Thêm query methods vào DesignManager

---

### **Giai đoạn 4: API Controllers (Tuần 2)**

**Task song parallel:**

- ✅ Duy: Viết AuthController
- ✅ Hào: Viết DesignController (endpoints analyze + status)
- ✅ Đạt: Thêm endpoints cho project list + delete
- ✅ Hiệp: WebhookController + Health check

---

### **Giai đoạn 5: Frontend Screens & Logic (Tuần 2-3)**

**Task song parallel:**

- ✅ Duy: SplashScreen, UserProfileScreen, auth_controller
- ✅ Hào: UploadPhotoScreen, ProcessingScreen, ResultScreen
- ✅ Đạt: HistoryScreen, ProjectDetailScreen
- ✅ Hiệp: Error handling, retry logic, API client setup

---

### **Giai đoạn 6: API Integration Testing (Tuần 3-4)**

**Task tuần tự:**

- ✅ Duy: Test UUID → register-device → load profile (end-to-end)
- ✅ Hào: Test upload photo → polling → result display
- ✅ Đạt: Test list projects → delete project
- ✅ Hiệp: Test error handling, retry logic, health check

---

### **Giai đoạn 7: Performance & UI Polish (Tuần 4-5)**

- ✅ Tất cả: Bug fixing, performance optimization
- ✅ Kiểm tra UX, messaging
- ✅ Load testing (hình ảnh lớn, polling, pagination)

---

## ✅ CHECKLIST QUA TỪNG NGƯỜI

### **Duy - Auth + Profile**

- [ ] UUID được tạo lần đầu và lưu vào SharedPreferences
- [ ] `POST /api/auth/register-device` tạo user trong DB
- [ ] `GET /api/auth/profile/{userId}` trả về user data
- [ ] `PUT /api/auth/profile/{userId}` cập nhật name/avatar
- [ ] Mỗi request có header `user-id` tự động
- [ ] SplashScreen logic: new user → register → home
- [ ] Profile screen: edit name + avatar

### **Hào - Design Studio**

- [ ] Photo picker hoạt động (camera + gallery)
- [ ] `POST /api/design/analyze` trả designId + originalImageUrl
- [ ] `GET /api/design/status/{designId}` return real-time status
- [ ] Frontend polling mỗi 2 giây
- [ ] ResultScreen hiển thị before/after slider chính xác
- [ ] Save design lưu vào database

### **Đạt - Project History**

- [ ] `GET /api/design/projects` với pagination hoạt động
- [ ] HistoryScreen hiển thị grid/list projects
- [ ] Click project → detail screen
- [ ] Delete button xóa khỏi list
- [ ] Filtering/sorting (tùy chọn)

### **Hiệp - Infrastructure**

- [ ] Backend biên dịch không error
- [ ] `GET /api/health` → 200 OK
- [ ] Database migrations chạy thành công
- [ ] CORS không báo lỗi browser
- [ ] Error responses có format thống nhất
- [ ] Error dialogs hiển thị trên frontend

---

## 🔑 CÁC QUYẾT ĐỊNH THIẾT KẾ

| Quyết định           | Giải pháp                      | Lý do                                |
| -------------------- | ------------------------------ | ------------------------------------ |
| **User Auth**        | UUID-based (no password)       | Đơn giản cho mobile, không cần login |
| **User Storage**     | Local (SharedPreferences) + DB | Offline support + audit trail        |
| **API Protocol**     | REST với header `user-id`      | Dễ test, không cần JWT complexity    |
| **Image Processing** | Short-polling (2s interval)    | MVP fast, upgrade to Hangfire later  |
| **Image Hosting**    | Cloud (Azure Blob / AWS S3)    | Scalable, fast delivery              |
| **Database**         | SQL Server + EF Core           | Type-safe, migrations support        |
| **Architecture**     | 4 modules độc lập              | Giảm conflict, parallel work         |

---

## 🚀 CHẠY NGAY

### **Bước 1: Chuẩn bị môi trường**

```bash
# Backend
cd InteriorAI
dotnet restore
dotnet ef migrations add InitialCreate
dotnet ef database update

# Frontend
cd my_app
flutter pub get
flutter pub upgrade
```

### **Bước 2: Khởi động**

```bash
# Backend (terminal 1)
cd InteriorAI
dotnet run

# Frontend (terminal 2)
cd my_app
flutter run
```

### **Bước 3: Test Health**

- GET http://localhost:5000/api/health
- Xem Swagger: http://localhost:5000/swagger


---

**Version:** 1.0  
**Last Updated:** 19/4/2026  
**Status:** Ready for Implementation 🎯
