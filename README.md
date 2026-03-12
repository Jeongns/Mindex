# Mindex

마인크래프트 도감 플러그인입니다! 사용자가 원하는 방식대로 커스텀에 유연한 플러그인을 만들고자 했습니다.\
도감 아이템부터 보상, GUI까지 사용자가 커스텀 할 수 있습니다!

## 명령어

```text
/mindex
  플러그인 안내 메시지를 출력합니다.

/mindex view
  자신의 도감 GUI를 엽니다.
  플레이어만 사용할 수 있습니다.

/mindex reload
  config.yml, gui.yml, categories/*.yml, 자동 저장 스케줄을 다시 불러옵니다.
  플레이어 데이터는 pending 변경사항 저장 후 캐시를 비웁니다.
  권한: mindex.command.reload

/mindex reset <username>
  대상 유저의 도감 진행도를 초기화합니다.
  권한: mindex.command.reset
```

## `config.yml`

플러그인 전역 설정 파일입니다.

설명:

```text
config.yml
├─ player-state-storage                                    # 플레이어 데이터 저장소 타입
│                                                           값:
│                                                           - FILE: 플러그인 폴더 아래 yml 파일로 저장
│                                                           - IN_MEMORY: 메모리에만 저장, 서버 재시작 시 초기화
│                                                           - POSTGRESQL: PostgreSQL 데이터베이스에 저장
│                                                           - MYSQL: MySQL 데이터베이스에 저장
├─ database                                                # DB 저장소 공통 설정
│  ├─ jdbc-url                                             # JDBC URL 문자열: 선택한 DB 저장소 타입에 맞는 접속 URL
│  │                                                        예시:
│  │                                                        - jdbc:postgresql://localhost:5432/mindex
│  │                                                        - jdbc:mysql://localhost:3306/mindex
│  ├─ username                                             # DB 접속 계정 문자열: PostgreSQL/MySQL 접속 계정
│  └─ password                                             # DB 접속 비밀번호 문자열: 비밀번호가 없으면 빈 문자열 사용 가능
├─ locked-entry-display                                    # 잠금 상태 엔트리 표시 설정
│  ├─ mode                                                 # 잠금 엔트리 표시 방식
│  │                                                        값:
│  │                                                        - FIXED_ITEM: 지정한 아이템으로 잠금 엔트리를 표시
│  │                                                        - ENTRY_ITEM_CUSTOM_MODEL_DATA: 엔트리 원래 아이템을 유지하고 custom-model-data만 적용
│  │                                                        - FIXED_ITEM_ENTRY_MATERIAL_STRING: 지정한 아이템을 사용하고 string-prefix + 엔트리 material 문자열을 custom-model-data.strings에 적용
│  ├─ material                                             # Bukkit Material 이름: FIXED_ITEM 모드에서 사용할 아이템
│  ├─ name                                                 # [선택] 문자열: 잠금 엔트리 이름, 비워두면 엔트리 원래 이름 사용
│  ├─ string-prefix                                        # [선택] 문자열: FIXED_ITEM_ENTRY_MATERIAL_STRING 모드에서 custom-model-data.strings에 붙일 접두사
│  └─ custom-model-data                                    # [선택] CustomModelData 설정 객체
│     ├─ floats                                            # [선택] float 리스트
│     ├─ flags                                             # [선택] boolean 리스트
│     └─ strings                                           # [선택] 문자열 리스트
├─ sounds                                                  # GUI 사운드 설정
│  ├─ menu-select                                          # 일반 선택 사운드 설정 묶음
│  ├─ registration-success                                 # 등록 성공 사운드 설정 묶음
│  └─ registration-fail                                    # 등록 실패/보상 실패 사운드 설정 묶음
│
│  공통 하위 키
│  ├─ enabled                                              # true | false: 해당 사운드 사용 여부
│  ├─ sound                                                # namespaced key 문자열: 마인크래프트 사운드 키, 예시 minecraft:ui.button.click
│  ├─ volume                                               # 실수(float/double): 사운드 볼륨
│  └─ pitch                                                # 실수(float/double): 사운드 피치
├─ messages                                                # 플레이어 안내 메시지
│  ├─ registration                                         # 도감 등록 관련 메시지 묶음
│  │  ├─ success                                           # 문자열: 등록 성공 메시지, 지원 치환값 <entry_name>
│  │  ├─ already-registered                                # 문자열: 이미 등록된 엔트리일 때 표시
│  │  └─ requirement-not-met                               # 문자열: 등록 조건 미충족 시 표시
│  └─ category-reward                                      # 카테고리 완료 보상 관련 메시지 묶음
│     ├─ success                                           # 문자열: 보상 수령 성공 메시지
│     ├─ not-complete                                      # 문자열: 카테고리가 아직 완료되지 않았을 때 표시
│     └─ already-claimed                                   # 문자열: 이미 수령한 보상일 때 표시
└─ auto-save-interval-minutes                              # 0 이상의 정수: 플레이어 데이터 자동 저장 주기(분), 0은 비활성화
```

주의:

- `sound`는 반드시 유효한 마인크래프트 사운드 키여야 합니다.
- `material`은 반드시 유효한 Bukkit `Material` 이름이어야 합니다.
- 메시지에는 MiniMessage 형식을 사용합니다.
- 예: `<green>성공`, `<yellow>경고`, `<red>오류`, `<gray>설명`
- `player-state-storage`와 `database.*` 변경은 `/mindex reload`가 아니라 서버 재시작 후 반영됩니다.

## `gui.yml`

GUI 배치와 버튼 메타데이터를 정의하는 파일입니다.

설명:

```text
gui.yml
├─ defaultSymbols                                          # 공통 심볼 정의
│  └─ "<symbol>"                                           # 한 글자 심볼
│     ├─ role                                              # 심볼 역할
│     │                                                     값:
│     │                                                     - BORDER
│     │                                                     - ENTRY_SLOT
│     │                                                     - PREV_PAGE
│     │                                                     - NEXT_PAGE
│     │                                                     - OPEN_DEFAULT
│     │                                                     - CLAIM_CATEGORY_REWARD
│     ├─ material                                          # Bukkit Material 이름: 버튼 아이템 재질
│     ├─ name                                              # [선택] 문자열: 버튼 표시 이름
│     └─ lore                                              # [선택] 문자열 리스트: 버튼 설명
├─ categorySymbols                                         # 기본 화면 카테고리 버튼 정의
│  └─ "<symbol>"                                           # 한 글자 심볼
│     ├─ categoryId                                        # 카테고리 ID 문자열: 클릭 시 열 카테고리
│     ├─ material                                          # Bukkit Material 이름: 버튼 아이템 재질
│     ├─ name                                              # [선택] 문자열: 버튼 표시 이름
│     └─ lore                                              # [선택] 문자열 리스트: 버튼 설명
├─ entryView                                               # 카테고리 상세 화면
│  ├─ title                                                # 문자열: 카테고리 상세 GUI 제목
│  │                                                        지원 치환값: <category_name> <page> <max_page>
│  ├─ rows                                                 # 1~6 정수: 인벤토리 행 수
│  └─ layout                                               # 문자열 리스트: 각 줄은 9칸이어야 함
└─ defaultView                                             # 카테고리 선택 화면
   ├─ title                                                # 문자열: 카테고리 선택 GUI 제목
   ├─ rows                                                 # 1~6 정수: 인벤토리 행 수
   └─ layout                                               # 문자열 리스트: 각 줄은 9칸이어야 함
```

주의:

- `defaultView`에는 `CLAIM_CATEGORY_REWARD`를 둘 수 없습니다.
- `categorySymbols.categoryId`는 실제 존재하는 카테고리 ID여야 합니다.
- 정의되지 않은 심볼을 layout에서 사용하면 로드 단계에서 실패합니다.

## `categories/*.yml`

카테고리별 도감 데이터 파일입니다.

설명:

```text
categories/<category>.yml
├─ id                                                      # 카테고리 ID 문자열: 내부 식별자
├─ name                                                    # 문자열: 카테고리 표시 이름
├─ reward                                                  # [선택] 문자열 또는 문자열 리스트: 카테고리 완료 보상 명령
│                                                           지원 치환값: <player>
├─ rewardButton                                            # 보상 수령 전 버튼 메타데이터
│  ├─ material                                             # Bukkit Material 이름: 보상 수령 전 버튼 재질
│  ├─ custom-model-data                                    # [선택] CustomModelData 설정 객체
│  │  ├─ floats                                            # [선택] float 리스트
│  │  ├─ flags                                             # [선택] boolean 리스트
│  │  └─ strings                                           # [선택] 문자열 리스트
│  ├─ name                                                 # 문자열: 보상 수령 전 버튼 이름
│  └─ lore                                                 # [선택] 문자열 리스트: 보상 수령 전 버튼 설명
├─ claimedRewardButton                                     # 보상 수령 후 버튼 메타데이터
│  ├─ material                                             # Bukkit Material 이름: 보상 수령 후 버튼 재질
│  ├─ custom-model-data                                    # [선택] CustomModelData 설정 객체
│  │  ├─ floats                                            # [선택] float 리스트
│  │  ├─ flags                                             # [선택] boolean 리스트
│  │  └─ strings                                           # [선택] 문자열 리스트
│  ├─ name                                                 # 문자열: 보상 수령 후 버튼 이름
│  └─ lore                                                 # [선택] 문자열 리스트: 보상 수령 후 버튼 설명
└─ entries                                                 # 도감 엔트리 목록
   └─ - entry
      ├─ id                                                # 엔트리 ID suffix 문자열
      │                                                     최종 ID는 <category>.<id> 형태로 생성
      ├─ amount                                            # 정수: 등록에 필요한 아이템 수량
      ├─ name                                              # 문자열: 엔트리 표시 이름
      ├─ description                                       # 문자열: 엔트리 설명
      ├─ material                                          # Bukkit Material 이름: 등록 판정과 기본 아이콘 기준
      ├─ custom-model-data                                 # [선택] CustomModelData 설정 객체: 등록 판정 시 함께 비교
      │  ├─ floats                                         # [선택] float 리스트
      │  ├─ flags                                          # [선택] boolean 리스트
      │  └─ strings                                        # [선택] 문자열 리스트
      └─ reward                                            # [선택] 문자열 또는 문자열 리스트: 등록 성공 보상 명령
                                                            지원 치환값: <player>
```
