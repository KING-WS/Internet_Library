# Internet Library

웹툰과 소설을 관리하고 열람할 수 있는 데스크톱 애플리케이션입니다.

## 📖 프로젝트 소개

Internet Library는 네이버와 카카오의 웹툰을 탐색하고 북마크할 수 있으며, 소설 정보를 관리할 수 있는 Java Swing 기반의 데스크톱 애플리케이션입니다. 한국 웹툰 API와 연동하여 최신 웹툰 정보를 제공합니다.

## ✨ 주요 기능

### 웹툰 관리
- 네이버 및 카카오 웹툰 탐색
- 요일별 웹툰 필터링 (월~일)
- 키워드 기반 웹툰 검색
- 웹툰 북마크 기능
- 페이지네이션을 통한 콘텐츠 탐색
- 썸네일 이미지 미리보기
- 클릭하여 웹툰 URL 열기

### 소설 관리
- 소설 추가 (제목, 작가, URL)
- 소설 정보 수정
- 소설 삭제
- 저장된 소설 목록 조회
- 더블클릭으로 소설 URL 열기

### 북마크 시스템
- 즐겨찾는 웹툰/소설 저장
- 북마크 제거
- SQL Server를 통한 영구 저장

## 🛠 기술 스택

- **언어**: Java
- **GUI 프레임워크**: Java Swing
- **데이터베이스**: Microsoft SQL Server
- **외부 API**: Korea Webtoon API
- **JSON 파싱**: org.json

## 📋 사전 요구사항

- Java Development Kit (JDK) 8 이상
- Microsoft SQL Server
- JDBC Driver for SQL Server

## ⚙️ 설치 및 설정

### 1. 데이터베이스 설정

SQL Server에서 다음 설정으로 데이터베이스를 생성하세요:

```
서버: localhost:1433
데이터베이스 이름: InternetLibrary
사용자명: sa
비밀번호: 1q2w3e4r@
```

필요한 테이블을 생성하세요 (북마크 저장용).

### 2. 프로젝트 클론

```bash
git clone https://github.com/KING-WS/Internet_Library.git
cd Internet_Library
```

### 3. 프로젝트 빌드 및 실행

Java IDE (IntelliJ IDEA, Eclipse 등)에서 프로젝트를 열고:

1. `InternetLibrary/DB/MainMenuViewer.java` 파일을 엽니다
2. `main` 메서드를 실행합니다

또는 명령줄에서:

```bash
cd InternetLibrary
javac DB/*.java
java DB.MainMenuViewer
```

## 🚀 사용 방법

### 메인 화면
- **웹툰 보기**: 웹툰 탐색 화면으로 이동
- **소설 관리**: 소설 관리 패널 표시

### 웹툰 탐색
1. **네이버/카카오 탭**: 요일별로 웹툰을 탐색합니다
2. **북마크 탭**: 저장한 웹툰을 확인합니다
3. **검색 탭**: 키워드로 웹툰을 검색합니다
4. 웹툰 카드를 클릭하여 원본 페이지로 이동합니다
5. 북마크 버튼으로 즐겨찾기를 추가/제거합니다

### 소설 관리
1. **추가**: 새 소설 정보를 입력합니다 (제목, 작가, URL)
2. **수정**: 선택한 소설의 정보를 편집합니다
3. **삭제**: 선택한 소설을 제거합니다
4. 소설 항목을 더블클릭하여 URL을 엽니다

## 📁 프로젝트 구조

```
InternetLibrary/
├── DB/
│   ├── MainMenuViewer.java      # 메인 진입점 및 UI
│   ├── WebtoonAPI.java           # API 통신 및 데이터 액세스 계층
│   ├── WebtoonTabbedViewer.java  # 웹툰 탐색 인터페이스
│   └── NovelInputViewer.java     # 소설 입력 다이얼로그
└── images/
    └── banner.png                # 홈 화면 배너 이미지
```

## 👥 개발자

- 구민우
- 김우성
- 김형경

## 📝 라이선스

이 프로젝트의 라이선스 정보는 별도로 명시되지 않았습니다.

## 🔗 관련 링크

- Korea Webtoon API: https://korea-webtoon-api-cc7dda2f0d77.herokuapp.com/webtoons

## 📸 스크린샷

*스크린샷 추가 예정*

---

**참고**: 데이터베이스 비밀번호와 같은 민감한 정보는 프로덕션 환경에서는 환경 변수나 설정 파일로 분리하여 관리하는 것을 권장합니다.