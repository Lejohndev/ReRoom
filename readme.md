```text
app/src/main/java/com/example/revroom/
|
+-- MainActivity.kt
|   Chuc nang: Entry point cua app, bat edge-to-edge, ap RevroomTheme va goi AppNavigation().
|
+-- core/
|   |
|   +-- network/
|   |   |
|   |   +-- ApiClient.kt
|   |   |   Chuc nang: Khoi tao Retrofit/OkHttp singleton, doc BuildConfig.API_BASE_URL, expose AuthApi va DesignApi.
|   |   |
|   |   +-- Network.kt
|   |       Chuc nang: Placeholder rong, giu lai de mo rong cau hinh network dung chung sau nay.
|   |
|   +-- theme/
|   |   |
|   |   +-- Color.kt
|   |   |   Chuc nang: Khai bao cac mau theme goc cua app.
|   |   |
|   |   +-- Theme.kt
|   |   |   Chuc nang: Dinh nghia RevroomTheme va ap Material theme cho Compose UI.
|   |   |
|   |   +-- Type.kt
|   |       Chuc nang: Khai bao typography dung chung cho theme.
|   |
|   +-- ui/
|   |   |
|   |   +-- RevRoomScaffold.kt
|   |   |   Chuc nang: Chua UI shell dung chung toan app: StudioScaffold, bottom navigation, TopTitleBar, StepProgress, GradientButton, BackOutlineButton, StudioTab va mau/gradient RevRoom.
|   |   |
|   |   +-- exampleUI.kt
|   |       Chuc nang: Placeholder rong, giu lai de them composable dung chung sau nay.
|   |
|   +-- utils/
|       |
|       +-- ImageCompressor.kt
|       |   Chuc nang: Doc anh tu Uri, resize/compress anh truoc khi upload len backend.
|       |
|       +-- Example.kt
|           Chuc nang: Placeholder rong, giu lai de them helper dung chung sau nay.
|
+-- data/
|   |
|   +-- local/
|   |   |
|   |   +-- LocalUserIdProvider.kt
|   |   |   Chuc nang: Tao va luu userId local bang SharedPreferences, dung lam header user-id khi goi backend.
|   |   |
|   |   +-- example.kt
|   |       Chuc nang: Placeholder rong, giu lai de them local data source sau nay.
|   |
|   +-- remote/
|   |   |
|   |   +-- AuthApi.kt
|   |   |   Chuc nang: Dinh nghia Retrofit endpoint dang ky thiet bi/user local voi backend.
|   |   |
|   |   +-- DesignApi.kt
|   |       Chuc nang: Dinh nghia Retrofit endpoint upload anh tao thiet ke va lay status theo designId.
|   |
|   +-- repository/
|       |
|       +-- DesignRepository.kt
|           Chuc nang: Trung gian giua ViewModel va API; dang ky user neu can, nen anh, tao multipart request, goi analyze/status va map loi/response ve model FE.
|
+-- features/
|   |
|   +-- chat/
|   |   |
|   |   +-- ui/
|   |       |
|   |       +-- ChatScreen.kt
|   |           Chuc nang: Man placeholder cho tab Chat, dung StudioScaffold chung va hien thi empty state cho tinh nang chat sau nay.
|   |
|   +-- design_studio/
|   |   |
|   |   +-- components/
|   |   |   |
|   |   |   +-- BeforeAfterSlider.kt
|   |   |   |   Chuc nang: Component so sanh anh before/after bang slider keo ngang.
|   |   |   |
|   |   |   +-- FanSpinnerAnimation.kt
|   |   |       Chuc nang: Animation loading dang canh quat bang Canvas cho buoc processing.
|   |   |
|   |   +-- model/
|   |   |   |
|   |   |   +-- DesignModels.kt
|   |   |   |   Chuc nang: Chua DesignRequest, DesignResult, DesignMode va DesignJobStatus.
|   |   |   |
|   |   |   +-- DesignUiState.kt
|   |   |       Chuc nang: State tong cua flow tao thiet ke: mode, feature, anh, room type, style, phase, designId, image URLs va error message.
|   |   |
|   |   +-- ui/
|   |   |   |
|   |   |   +-- DesignHomeScreen.kt
|   |   |   |   Chuc nang: Man home Interior/Exterior, hien thi chip loai thiet ke va card feature de bat dau flow tao thiet ke.
|   |   |   |
|   |   |   +-- UploadPhotoScreen.kt
|   |   |   |   Chuc nang: Buoc 1, chon anh bang Android Photo Picker, hien thi preview va chuyen sang buoc tiep theo.
|   |   |   |
|   |   |   +-- RoomTypeScreen.kt
|   |   |   |   Chuc nang: Buoc 2 cho Interior, chon loai phong bang grid thumbnail tron.
|   |   |   |
|   |   |   +-- StyleScreen.kt
|   |   |   |   Chuc nang: Buoc 3, chon style Interior/Exterior va kich hoat tao thiet ke.
|   |   |   |
|   |   |   +-- ProcessingScreen.kt
|   |   |   |   Chuc nang: Buoc 4, hien thi trang thai Uploading/Processing/Completed/Failed, spinner, before/after slider, retry va create another.
|   |   |   |
|   |   |   +-- ResultScreen.kt
|   |   |       Chuc nang: Man ket qua cu dang tach rieng, hien chua duoc route chinh dung vi ProcessingScreen da gop processing va result.
|   |   |
|   |   +-- viewmodel/
|   |       |
|   |       +-- DesignViewModel.kt
|   |           Chuc nang: Quan ly state Phase 4, danh sach feature/room/style, xu ly chon input, tao design, retry/reset va polling backend.
|   |
|   +-- history/
|       |
|       +-- ui/
|           |
|           +-- HistoryScreen.kt
|               Chuc nang: Man My Projects/History cho tab Gallery, hien empty state No projects yet va cho module lich su that sau nay.
|
+-- navigation/
    |
    +-- AppNavigation.kt
        Chuc nang: Tao DesignViewModel, collect uiState, khai bao route va dieu phoi flow Interior, Exterior, Upload, Room Type, Style, Processing, Chat, History.
```
