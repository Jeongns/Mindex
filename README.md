# Mindex

마인크래프트 도감 플러그인입니다.  
아이템 등록 방식, GUI 배치, 보상, 메시지, 사운드, 저장소를 설정 파일로 커스텀할 수 있습니다.



## 핵심 기능

- 카테고리별 도감 구성
- GUI 레이아웃 커스텀
- 엔트리 보상, 카테고리 완료 보상
- 파일 / PostgreSQL / MySQL 저장소 지원

## 명령어

- `/mindex`: 플러그인 안내 출력
- `/mindex view`: 내 도감 GUI 열기
- `/mindex reload`: 설정 다시 로드
- `/mindex reset <username>`: 대상 유저 도감 데이터 초기화

자세한 사용법과 예시는 [위키](https://github.com/Jeongns/Mindex/wiki) 문서를 확인해 주세요.

## 예제
[시연 영상](https://www.youtube.com/watch?v=uo_gC4ErGWQ)

예제 파일에 있는 설정과 리소스팩을 사용하면 간단한 GUI 커스텀 도감을 사용해 보실 수 있습니다.\
리소스팩은 PAPER에 custom_model_data를 "mindex_<METERIAL\>"을 설정할 경우 해당 아이템의 흑백 아이콘을 얻습니다.\
(모든 아이템 지원 X)

- [예제 설정 폴더](https://github.com/Jeongns/Mindex/tree/main/example/Mindex)
- [리소스팩 다운로드](https://raw.githubusercontent.com/Jeongns/Mindex/main/example/resourcepacks/mindex_ex.zip)


포함 파일:
- `example/Mindex`: Mindex 설정 예제
- `example/resourcepacks/mindex_ex.zip`: 예제 리소스팩

## 이슈

수정사항이나 버그를 발견했다면 GitHub Issue로 남겨주세요.
