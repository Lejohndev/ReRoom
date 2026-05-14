app/src/main/java/com/interiorai/app/
│
├── core/                       # 1. CỐT LÕI (Dùng chung)
│   ├── network/                # RetrofitClient, Interceptors (Gắn JWT)
│   ├── theme/                  # Color.kt, Type.kt, Theme.kt (Định nghĩa màu sắc, font chữ chuẩn Material 3)
│   ├── ui/                     # Các Composable dùng chung: PrimaryButton, LoadingScreen, ErrorDialog
│   └── utils/                  # Constants, ImageCompressor (Ép dung lượng ảnh)
│
├── data/                       # 2. TẦNG DỮ LIỆU
│   ├── local/                  # Room Database (Entities, Daos) & DataStore (Lưu Token)
│   ├── remote/                 # ApiService (Định nghĩa các endpoint C#)
│   └── repository/             # Chứa logic gọi API hoặc gọi Local DB (VD: DesignRepository)
│
├── features/                   # 3. TÍNH NĂNG CHÍNH (Chia việc tại đây)
│   │
│   ├── auth/                   # Xác thực
│   │   ├── ui/                 # 
│   │   ├── viewmodel/          # 
│   │   └── state/              # AuthState.kt (Sealed class: Loading, Success, Error)
│   │
│   ├── design_studio/          # Cốt lõi: Tạo thiết kế
│   │   ├── model/              # DesignRequest, DesignResult
│   │   ├── ui/                 # UploadScreen.kt, ProcessingScreen.kt, ResultScreen.kt
│   │   ├── components/         # BeforeAfterSlider.kt (Custom Composable cho thanh trượt)
│   │   └── viewmodel/          # DesignViewModel.kt (Luồng gửi ảnh, Coroutines Polling API)
│   │
│   └── history/                # Lịch sử
│       ├── ui/                 # HistoryScreen.kt
│       └── viewmodel/          # HistoryViewModel.kt
│
├── navigation/                 # 4. ĐIỀU HƯỚNG
│   └── AppNavigation.kt        # Cấu hình NavHost, quản lý luồng chuyển màn hình
│
└── MainActivity.kt             # Entry point duy nhất, gọi AppNavigation()