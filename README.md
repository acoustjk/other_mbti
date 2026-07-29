# 타인이 보는 나의 MBTI (Others' View of My MBTI)

내가 생각하는 나 vs 남이 보는 나의 MBTI Gap 분석 플랫폼 & 안드로이드 앱

## 📁 프로젝트 구조 (Project Architecture)

- **`android/`**: Jetpack Compose 안드로이드 네이티브 앱 (Kotlin, Firebase Firestore 실시간 연동, Scaffold systemBarsPadding, Intent 공유)
- **`web/`**: 모바일 웹 설문지 플랫폼 (HTML5, Vanilla CSS Glassmorphism, JS, Firebase SDK, Canvas 스토리 이미지 생성)

## 🌐 라이브 웹 배포 링크
- **공개 주소**: https://othermbti-app-2026.surge.sh
- **지인 테스트 초대 링크**: https://othermbti-app-2026.surge.sh/test?target=USER_MOCK_101

## ⚙️ 실행 방법

### Android 앱 실행 & 빌드
```bash
cd android
./gradlew assembleDebug
```

### Web 서버 실행
```bash
cd web
python -m http.server 8080
```
