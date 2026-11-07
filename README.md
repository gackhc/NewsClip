실행 설정

Git에서 제외된 `local.properties`에 `NYT_API_KEY=발급받은키`를 추가합니다.
CI에서는 환경 변수 또는 Gradle 속성 `NYT_API_KEY`로 전달할 수 있습니다.
실제 기사 검색에는 유효한 키가 필요합니다. API를 대체하는 CI 테스트에는
`-PNYT_API_KEY=test-key`를 전달할 수 있습니다. 키가 없어도 빌드는 가능합니다.
이 설정은 저장소에 키가 들어가는 것을 방지하며, APK 내 키를 숨기지는 않습니다.
이미 공유된 키의 폐기/재발급은 NYT 개발자 계정에서 진행해야 합니다.

// 사용 라이브러리 및 버전
Kotlin : 1.7.20
Lifecycle : 2.4.0
Hilt : 2.44.2
Compose UI / Material : 1.3.1 (Compiler 1.3.2, Kotlin 1.7.20 호환)
Activity Compose : 1.6.1
RoomDB : 2.4.3
Retrofit2 : 2.9.0
Coil : 2.2.2
Paging3 : 3.1.0 / Paging Compose : 1.0.0-alpha17

----------------------------------

프로젝트 구조

MVVM 패턴으로 구성되어 Compose UI와 LiveData를 사용함.
의존성 주입은 Hilt를 통해 진행.

* Main
main/MainActivity : 
    시작 Activity. Hilt ViewModel을 Compose 화면에 전달함
main/NewsclipScreen :
    검색, 기사/클립 목록, 스와이프 저장·삭제, 새로고침 및 오류 재시도 UI
    탭·입력·스크롤 상태를 저장하고 Clip에서 뒤로 가면 Home으로 이동함
main/MainViewModel :
    검색 문자열과 클립 작업을 관리하며 UI 위젯에 의존하지 않음

* Webview
webview/WebViewActivity :
    Compose AndroidView로 WebView를 호스팅하고 페이지 상태를 복원함

* Data
data/db/*
    RoomDB와 관련된 클래스 위치. 클립한 기사의 저장 및 관리 역할
data/network/*
    Retrofit2와 관련된 클래스 위치. 기사를 검색하여 전달하는 역할

레이아웃 XML, Fragment, RecyclerView Adapter, Data Binding은 제거함.
시스템 테마·아이콘·백업 설정용 XML 리소스는 유지함.

검증: JDK 11에서 `gradlew :app:testDebugUnitTest :app:assembleDebug` 실행.
Compose UI 테스트는 Robolectric/Hilt로 검색, 탭 복원, 스와이프 및 DB 반영을 검증함.

