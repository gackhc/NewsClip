// 사용 라이브러리 및 버전
Kotlin : 1.7.20
Lifecycle : 2.4.0
Hilt : 2.44.2
Navigation : 2.5.3
RoomDB : 2.4.3
Retrofit2 : 2.9.0
Coil : 2.2.2
Paging3 : 3.1.0

----------------------------------

프로젝트 구조

MVVM 패턴으로 구성되어 Databinding 과 Livedata를 사용함.
의존성 주입은 Hilt를 통해 진행.

* Main
main/MainActivity : 
    시작 activity. Navigation으로 Fragment 관리함
main/ArticleListFragment : 
    Retrofit2를 통해 검색된 기사를 보여주는 Fragment. Paging3로 페이징 처리 되어있음
main/ClipListFragment :
    클립한 기사를 보여주는 Fragment. 클립한 기사는 RoomDB에 저장됨

* Webview
webview/WebViewActivity :
    Main에서 선택한 기사의 전문을 보여주기 위한 activity

* Data
data/db/*
    RoomDB와 관련된 클래스 위치. 클립한 기사의 저장 및 관리 역할
data/network/*
    Retrofit2와 관련된 클래스 위치. 기사를 검색하여 전달하는 역할

* ItemTouchHelper
itemtouchhelper/*
    리스트에서 스와이프 클립/삭제 기능을 구현하기 위한 보조 클래스

* Binding
binding/BindingAdapter
    데이터 바인딩 어댑터 위치

