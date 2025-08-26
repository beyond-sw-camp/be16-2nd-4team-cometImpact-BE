# Jelly Order 🍧

### <img width="50" height="50" alt="팀 로고" src="https://github.com/user-attachments/assets/2c140acc-54f4-4b42-b0ba-a3bde1cf5743" /> 혜성충돌
> **먼지와 암석, 얼음이 모여 혜성이 되었고, 우리는 세상을 향해 전속력으로 충돌합니다.**


<table>
  <tr>
    <td align="center">
      <a href="https://github.com/JeaPple" target="_blank">
        <img src="./images/parkjihun.png" width="120px"><br />
        <sub><b>김형진</b></sub>
      </a>
    </td>
    <td align="center">
      <a href="https://github.com/ifunhy" target="_blank">
        <img src="./images/parkheechaan.png" width="120px"><br />
        <sub><b>김현지</b></sub>
      </a>
    </td>
    <td align="center">
      <a href="https://github.com/jinnn12" target="_blank">
        <img src="./images/yoonjiyoung.png" width="120px"><br />
        <sub><b>김진호</b></sub>
      </a>
    </td>
    <td align="center">
      <a href="https://github.com/solidify-d" target="_blank">
        <img src="./images/ominseong.png" width="120px"><br />
        <sub><b>박헤성</b></sub>
      </a>
    </td>
  </tr>
</table>

---
## 1. 📌 프로젝트 기획서



## 프로젝트 개요
Jelly Order는 소상공인을 위한 경량형 오픈소스 테이블 오더·경량 POS 시스템이다. 상용 솔루션의 초기 설치비·월 구독료·PG 수수료 등 비용 장벽과, 고령층·IT 비숙련자에게 어려운 복잡한 UI를 핵심 문제로 규정하고, 직관적·대형 폰트 기반 UI/UX와 수수료 락인 없는 아키텍처(VAN/현금/기본 카드 단말 연동 중심)를 제공한다. 이를 통해 점주는 더 단순한 POS 운영 경험, 고객은 쉽고 빠른 주문 경험을 얻으며, 누구나 코드와 문서를 내려받아 무상으로 자체 설치·개조할 수 있도록 오픈소스로 배포한다. 비용 부담과 디지털 격차가 실제 현장에서 도입 저해 요인으로 보고되고 있음을 전제로 설계하였다.

## 프로젝트 배경
<p align="center">
  <img width="800" alt="image" src="https://github.com/user-attachments/assets/832ac568-cf29-438f-b520-9b87d1e6ef6e" />
</p>
### 1) 비용 장벽과 수수료 구조의 불투명성

테이블오더 확산은 인건비 절감을 배경으로 빠르게 진행되었으나, **PG 연동에 따른 높은 결제 수수료(대개 0.8~2.5% 이상)**, **월 구독료·통신비·장비비** 등으로 기대 절감액보다 **고정지출이 커지는 사례**가 다수 보도되었다. 일부 사업장은 **의무사용·위약금** 등 계약 리스크도 경험한다. 업계·언론 보도는 **VAN 위주 결제 대비 PG 경유 시 월 수십만 원~연 수백만 원 추가 비용**이 발생할 수 있음을 지적한다. 또한 법·제도상 **PG 수수료에 대한 제한이나 표준화가 미흡**하다는 점도 반복적으로 제기되고 있다. [YTN](https://www.ytn.co.kr/_ln/0102_202410181024214328)[서울경제](https://www.sedaily.com/NewsView/2DFEDQEVRN)[네이트 뉴스](https://news.nate.com/view/20240108n02405)

> 예시: 월매출 2,000만 원 업장에서 PG 3.3% + 서비스 0.6% ≈ 3.9%로 월 78만 원 수준의 수수료가 발생했다는 보도 사례가 확인된다. 정부 지원으로 설치했더라도 운영 수수료 부담은 영세사업자 몫이라는 점도 지적되었다. 네이트 뉴스
> 

정부·유관기관도 최근에는 **결제기기 수수료 안내 및 계약 전 유의사항**을 배포하며 자율 예방을 독려하고 있으나, **도입 이후의 비용 구조 최적화**는 여전히 점주에게 과도하게 전가되는 양상이다. [소상공인24](https://www.sbiz.or.kr/smst/bbs/view.do?bbsSn=6840&key=2111306039167)

### 2) 고령층·IT 비숙련자 사용성 문제

고령층은 **작은 글씨·복잡한 화면 흐름·결제 오류 대응 난이도**로 인해 디지털 주문 채널 이용을 **포기하거나 직원 의존**이 잦다는 탐방·설문 결과가 다수 보고된다. 서울시 조사 등에서도 **기기 이용률 저조·심리적 부담**이 확인되며, **교육·큰 글씨·음성안내** 등 보조 설계 시 **적응이 빠르게 개선**된 사례가 보고되었다. 이는 UI의 직관성·접근성(가독성, 단계 축소, 용어 단순화)이 도입 성패를 좌우함을 시사한다. [브라보마이라이프](https://bravo.etoday.co.kr/view/atc_view/13547)

### 3) 디지털 전환의 필요와 현실 간 간극

학술·정책·언론 자료는 무인주문·스마트오더·키오스크 등 **스마트 기술이 소상공인 운영 효율에 기여**함을 인정하는 동시에, **도입·운영 비용과 투자 부담**, **연령대별 디지털 격차**가 보급의 병목임을 제시한다. Jelly Order는 **오픈소스 배포·저비용 도입·고령친화 UI**로 이 간극을 해소하고자 한다. [KCI](https://www.kci.go.kr/kciportal/ci/sereArticleSearch/ciSereArtiView.kci?sereArticleSearchBean.artiId=ART002995983)

## 목표 및 목적
### 1) 최상위 목적(미션)
- **비용 장벽 제거:** 오픈소스로 공개하여 **초기비용·구독료 부담 없이** 자체 호스팅·로컬 설치를 가능하게 한다. **PG 락인 회피**와 **VAN/기존 카드단말·현금결제 연동**을 우선시해 수수료 부담을 최소화한다. [소상공인24](https://www.sbiz.or.kr/smst/bbs/view.do?bbsSn=6840&key=211130603916)
- **디지털 격차 완화: 대형 폰트·높은 대비·명료한 한국어 용어·단계 최소화** 설계를 통해 **고령층·IT 비숙련자**도 스스로 주문·결제가 가능하도록 한다. [브라보마이라이프](https://bravo.etoday.co.kr/view/atc_view/13547)

### 2) 이해관계자별 목표
- **점주(매장) 관점**
  - 설치·학습·운영이 **쉬운 경량 POS 콘솔** 제공(메뉴·재고·품절·좌석/테이블 관리 최소 클릭 흐름).
- **고객(사용자) 관점**
  - 노령친화 모드(글자 크게/버튼 크게/음성 힌트/오류 복구 유도)로 주문 포기율 감소.
  - **5클릭 이하 주문**(카테고리→메뉴→옵션→수량→결제)으로 인지부하 최소화. [브라보마이라이프](https://bravo.etoday.co.kr/view/atc_view/13547)

### 3) 정량 KPI(초기 제안)
- **도입비 절감**: 상용 대비 **초기 CAPEX 80% 이상 절감**(오픈소스·상용 태블릿 활용 전제, 장비비만 부담). 근거: 상용은 **월 구독료+PG 수수료**로 누적 비용이 커지는 구조가 다수 보고. [YTN](https://www.ytn.co.kr/_ln/0102_202410181024214328)[서울경제](https://www.sedaily.com/NewsView/2DFEDQEVRN)
- **수수료 최적화**: **VAN/직접 정산** 우선 구성 시 **PG 추가 수수료 0~최소화**를 목표(매장 계약 환경에 따라 변동). [서울경제](https://www.sedaily.com/NewsView/2DFEDQEVRN)
- **주문 성공률**: **노령친화 UI** 도입 매장 기준 **주문 포기율(결제 전 이탈) 30%↓** 목표. 관련 탐방·조사에서 고령층의 사용 포기 및 심리적 부담이 높은 것으로 확인. [브라보마이라이프](https://bravo.etoday.co.kr/view/atc_view/13547)
- **학습 곡선**: 점주 대상 **초기 온보딩 30분 내 기본 운영 가능**(메뉴 등록·품절 처리·영수증 확인).

### 4) 핵심 기능 범위(요약)
- **점주 콘솔**: 메뉴/옵션/가격, 테이블·좌석, 재고/품절(재료 연동), 주문 현황, 정산 리포트(수수료 시뮬레이터 포함).
- **고객 UI**: **대형 폰트·단순 레이아웃**, 고대비 테마, **오류 복구 안내**, 결제 유도 최소 단계. [브라보마이라이프](https://bravo.etoday.co.kr/view/atc_view/13547)
- **결제 연동**: **VAN/기존 카드단말·현금 결제** 기본, 필요 시 **PG 연동 선택적 제공**(수수료 경고/비교 안내). [서울경제](https://www.sedaily.com/NewsView/2DFEDQEVRN)
- **배포·운영**: 오픈소스 공개, **도커·자체 호스팅 템플릿**, 설치 자동화 스크립트, 운영 가이드/보안 체크리스트.

## 문제정의 요약(자료 근거)
- **수수료·운영비 부담**이 인건비 절감을 상쇄하는 사례 다수, **PG 수수료 상한 부재**·계약 리스크 지적. → 비용 장벽 해소 필요. [YTN](https://www.ytn.co.kr/_ln/0102_202410181024214328) [서울경제](https://www.sedaily.com/NewsView/2DFEDQEVRN) [네이트 뉴스](https://news.nate.com/view/20240108n02405)
- **고령층·비숙련자 사용성 한계**가 도입/이용 저해. **큰 글씨·음성·단계 축소** 지원 시 적응 개선. → 접근성 중심 설계 필요. [브라보마이라이프](https://bravo.etoday.co.kr/view/atc_view/13547)
- 학술·정책 연구는 **스마트 기술의 효용**과 함께 **도입·투자 부담**을 병목으로 지적. → **오픈소스·저비용·자체 설치** 모델의 공공적 가치. [KCI](https://www.kci.go.kr/kciportal/ci/sereArticleSearch/ciSereArtiView.kci?sereArticleSearchBean.artiId=ART002995983)
---

## 2. 🛠 기술 스택

### Backend
![Java 17](https://img.shields.io/badge/Java%2017-007396?style=for-the-badge&logo=openjdk&logoColor=white)
![Spring Boot](https://img.shields.io/badge/Spring%20Boot%203.5-6DB33F?style=for-the-badge&logo=springboot&logoColor=white)
![Spring Security](https://img.shields.io/badge/Spring%20Security-6DB33F?style=for-the-badge&logo=springsecurity&logoColor=white)
![Spring Data JPA](https://img.shields.io/badge/Spring%20Data%20JPA-59666C?style=for-the-badge&logo=hibernate&logoColor=white)
![Hibernate](https://img.shields.io/badge/Hibernate-59666C?style=for-the-badge&logo=hibernate&logoColor=white)
![Redis](https://img.shields.io/badge/Redis-DC382D?style=for-the-badge&logo=redis&logoColor=white)
![WebSocket](https://img.shields.io/badge/WebSocket-000000?style=for-the-badge&logo=websocket&logoColor=white)
![MariaDB](https://img.shields.io/badge/MariaDB-003545?style=for-the-badge&logo=mariadb&logoColor=white)
![Docker](https://img.shields.io/badge/Docker-2496ED?style=for-the-badge&logo=docker&logoColor=white)
![SSE](https://img.shields.io/badge/SSE%20(Server--Sent%20Events)-000000?style=for-the-badge&logo=fastapi&logoColor=white)
![JWT](https://img.shields.io/badge/JWT-black?style=for-the-badge&logo=jsonwebtokens&logoColor=white)


### Frontend
![Vue.js](https://img.shields.io/badge/Vue.js%203-4FC08D?style=for-the-badge&logo=vue.js&logoColor=white)
![Vuetify](https://img.shields.io/badge/Vuetify-1867C0?style=for-the-badge&logo=vuetify&logoColor=white)
![JavaScript](https://img.shields.io/badge/JavaScript-F7DF1E?style=for-the-badge&logo=javascript&logoColor=black)
![HTML5](https://img.shields.io/badge/HTML5-E34F26?style=for-the-badge&logo=html5&logoColor=white)
![CSS3](https://img.shields.io/badge/CSS3-1572B6?style=for-the-badge&logo=css3&logoColor=white)

### ☁️ Infra & Cloud
![AWS S3](https://img.shields.io/badge/AWS%20S3-569A31?style=for-the-badge&logo=amazons3&logoColor=white)
![AWS](https://img.shields.io/badge/AWS-232F3E?style=for-the-badge&logo=amazonaws&logoColor=white)

### External API & Integration
![RestTemplate](https://img.shields.io/badge/RestTemplate-6DB33F?style=for-the-badge&logo=spring&logoColor=white)
![KakaoPay](https://img.shields.io/badge/KakaoPay-FFCD00?style=for-the-badge&logo=kakaotalk&logoColor=black)
![ODcloud](https://img.shields.io/badge/ODcloud_API-005BAC?style=for-the-badge&logo=databricks&logoColor=white)
![Gmail SMTP](https://img.shields.io/badge/Gmail%20SMTP-EA4335?style=for-the-badge&logo=gmail&logoColor=white)

### Test & Docs
![Swagger UI](https://img.shields.io/badge/Swagger-85EA2D?style=for-the-badge&logo=swagger&logoColor=black)
![Postman](https://img.shields.io/badge/Postman-FF6C37?style=for-the-badge&logo=postman&logoColor=white)
![JMeter](https://img.shields.io/badge/Apache%20JMeter-D22128?style=for-the-badge&logo=apachejmeter&logoColor=white)


###  Tools & Collaboration
![Git](https://img.shields.io/badge/Git-F05032?style=for-the-badge&logo=git&logoColor=white)
![GitHub](https://img.shields.io/badge/GitHub-181717?style=for-the-badge&logo=github&logoColor=white)
![Notion](https://img.shields.io/badge/Notion-000000?style=for-the-badge&logo=notion&logoColor=white)
![Discord](https://img.shields.io/badge/Discord-5865F2?style=for-the-badge&logo=discord&logoColor=white)
![Figma](https://img.shields.io/badge/Figma-F24E1E?style=for-the-badge&logo=figma&logoColor=white)

---

## 3. 📐 분석 및 설계

### ✅ [요구사항 명세서](https://docs.google.com/spreadsheets/d/1lV6OYcvnEEBzQFvDyBVyoR1MRkYB52oj-mvlcYLXNRM/edit?gid=1037534638#gid=1037534638)

### 🎨 [화면 설계서](https://www.figma.com/design/WuBdoUWSz5n3gdSHdMN2qo/be16-4th-team?node-id=0-1&t=5D9WbMwwOTOxQaVj-1)
<details>
  <summary><b>화면설계서</b></summary>
  - 화면설계_점주(STORE)  
    <p align="center">
      <img width="800" alt="image" src="https://github.com/user-attachments/assets/50a33d51-f4e6-4848-b90b-70fbc4c9f666" />
    </p>

  - 화면설계_테이블(STORE_TABLE)  
    <p align="center">
      <img width="800" alt="image" src="https://github.com/user-attachments/assets/d50ff505-8019-4977-9770-1dcf7d4a3ed3" />
    </p>
</details>

### 🧾 [ERD](https://www.erdcloud.com/d/fAJgKBWBde3CPAkgW)
<details>
  <summary><b>ERD</b></summary>
  <img src="https://raw.githubusercontent.com/beyond-sw-camp/be16-2nd-4team-cometImpact-BE/develop/Jelly_order_ERD.png"></img>
</details>

### 📆 [WBS (Work Breakdown Structure)](https://docs.google.com/spreadsheets/d/1lV6OYcvnEEBzQFvDyBVyoR1MRkYB52oj-mvlcYLXNRM/edit?gid=0#gid=0)

---



---

## 4. 🧪 단위테스트 결과서
> 각 항목을 클릭하면 테스트 시연 GIF/영상이 펼쳐집니다.

점주 페이지
-

### 회원가입 및 로그인
<details>
  <summary><b>점주 회원가입</b></summary>
    <p align="center">
    <img src="https://github.com/user-attachments/assets/43680d12-01ba-4f90-8d4f-7e6965704932" alt="점주 회원 가입" width="720">
    </p>
</details>
<details>
  <summary><b>점주 일반로그인</b></summary>
    <p align="center">
    <img src="https://github.com/user-attachments/assets/b431146e-4368-4559-8c06-44291ab59692" alt="점주 일반로그인" width="720">
    </p>
</details>
<details>
  <summary><b>점주 자동로그인</b></summary>
    <p align="center">
    <img src="https://github.com/user-attachments/assets/3a68f0f8-9e06-4513-abcc-a30a9631e2a7" alt="점주 자동로그인" width="720">
    </p>
</details>
<details>
  <summary><b>점주 아이디 찾기</b></summary>
    <p align="center">
    <img src="https://github.com/user-attachments/assets/99220b56-a342-4283-b39d-7465954cd054" alt="점주 아이디찾기" width="720">
    </p>
</details>
<details>
  <summary><b>점주 비밀번호 재설정</b></summary>
    <p align="center">
    <img src="https://github.com/user-attachments/assets/f0d92778-05ff-4068-afbc-3727e21502e2" alt="점주 비밀번호 재설정" width="720">
    </p>
</details>


### 테이블 현황


<details>
  <summary><b>구역 별 주문 테이블 정보 조회 (기본 화면)</b></summary>
  <p align="center">

  </p>
</details>

<details>
  <summary><b>주문 테이블 상세 정보</b></summary>
  <p align="center">
![ezgif com-video-to-gif-converter](https://github.com/user-attachments/assets/8c38f22f-75bf-4186-82db-2d20e6949115)
  </p>
</details>

### 주문 현황
<details>
  <summary><b>주문 접수 페이지 (접수 탭)</b></summary>
  <p align="center">
    <img src="" alt="주문 접수 페이지" width="720">
  </p>
</details>

<details>
  <summary><b>주문 접수 페이지 (완료 탭)</b></summary>
  <p align="center">
    <img src="" alt="주문 접수 페이지" width="720">
  </p>
</details>

<details>
  <summary><b>주문 접수 페이지 (취소 탭)</b></summary>
  <p align="center">
    <img src="" alt="주문 접수 페이지" width="720">
  </p>
</details>

<details>
  <summary><b>실시간 주문 확인 (WebSocket 연결/수신)</b></summary>
  <p align="center">
   ![실시간 주문](https://github.com/user-attachments/assets/60762f7f-825d-460a-900d-a0b90cfade3b)
 </p>
</details>

<details>
  <summary><b>조리완료</b></summary>
  <p align="center">
    <img src="" alt="조리완료" width="720">
  </p>
</details>


<details>
  <summary><b>조리 취소</b></summary>
  <p align="center">
    <img src="" alt="취소" width="720">
  </p>
</details>


### 테이블 및 구역 관리
<details>
  <summary><b>테이블 추가</b></summary>
  <p align="center">


  </p>
</details>

<details>
  <summary><b>테이블 수정</b></summary>
  <p align="center">
    <img src="" alt="테이블 수정" width="720">
  </p>
</details>

<details>
  <summary><b>구역 추가</b></summary>
  <p align="center">
    <img src="" alt="구역 추가" width="720">
  </p>
</details>

<details>
  <summary><b>구역 수정</b></summary>
  <p align="center">
![구역수정](https://github.com/user-attachments/assets/44c2ccee-7ab7-4ea2-a659-a8d62898b0e5)
  </p>
</details>











### 메뉴 관리
  
<details>
  <summary><b>메뉴 추가 (기본 값 설정)</b></summary>
    <p align="center">
    <img src="https://github.com/user-attachments/assets/4da9ec68-da96-4310-8dc9-3359ec646ba0" alt="Menu - 메뉴 추가 (기본 값 설정)" width="720">
    </p>
</details>

<details>
  <summary><b>메뉴 추가 (옵션 추가)</b></summary>
    <p align="center">
    <img src="https://github.com/user-attachments/assets/2c3d99f7-1062-4d01-a39f-c62ae301e427" alt="Menu - 메뉴 추가 (옵션 추가)" width="720">
    </p>
</details>

<details>
  <summary><b>메뉴 추가 (신규 카테고리 추가)</b></summary>
    <p align="center">
    <img src="https://github.com/user-attachments/assets/4ce31115-c2da-46c2-a783-14209c5d8a71" alt="Menu - 메뉴 추가 (신규 카테고리 추가)" width="720">
    </p>
</details>

<details>
  <summary><b>메뉴 추가 (동일 카테고리 생성 방지)</b></summary>
    <p align="center">
    <img src="https://github.com/user-attachments/assets/83c4ea18-a9a8-4181-919b-d97c66aac2e6" alt="Menu - 메뉴 추가 (동일 카테고리 생성 방지)" width="720">
    </p>
</details>

<details>
  <summary><b>메뉴 추가 (이미지 미리보기)</b></summary>
    <p align="center">
    <img src="https://github.com/user-attachments/assets/222e1747-fe92-4ff2-91e2-3484e6718091" alt="Menu - 메뉴 추가 (이미지 미리보기)" width="720">
    </p>
</details>

<details>
  <summary><b>메뉴 추가 (식자재 선택 - 선택 X)</b></summary>
    <p align="center">
    <img src="https://github.com/user-attachments/assets/10198fee-c74f-4964-8340-c6f4bbdb7d9c" alt="Menu - 메뉴 추가 (식자재 선택 - 선택 X)" width="720">
    </p>
</details>

<details>
  <summary><b>메뉴 추가 (식자재 선택 - 선택 O)</b></summary>
    <p align="center">
    <img src="https://github.com/user-attachments/assets/629ce26d-8ba4-40ad-8d7b-44ca0ab1eafd" alt="Menu - 메뉴 추가 (식자재 선택 - 선택 O)" width="720">
    </p>
</details>

<details>
  <summary><b>메뉴 추가 (식자재 추가 - 기본화면)</b></summary>
    <p align="center">
    <img src="https://github.com/user-attachments/assets/7b39637c-2dfe-4081-9e6f-c95901e43847" alt="Menu - 메뉴 추가 (식자재 추가 - 기본화면)" width="720">
    </p>
</details>

<details>
  <summary><b>메뉴 추가 (식자재 추가 - 식자재 추가 정상 작동 확인 알림)</b></summary>
    <p align="center">
    <img src="https://github.com/user-attachments/assets/e0080c55-a7c5-44b5-b310-e80df1d41d3e" alt="Menu - 메뉴 추가 (식자재 추가 - 식자재 추가 정상 작동 확인 알림)" width="720">
    </p>
</details>

<details>
  <summary><b>메뉴 삭제 (재확인 모달)</b></summary>
    <p align="center">
    <img src="https://github.com/user-attachments/assets/7a685621-b65d-4f1b-83dc-74e226253e06" alt="Menu - 메뉴 삭제 (재확인 모달)" width="720">
    </p>
</details>

<details>
  <summary><b>메뉴 삭제 (정상 작동 알림)</b></summary>
    <p align="center">
    <img src="https://github.com/user-attachments/assets/3e9ecdee-c512-46ea-91f0-945061f96e71" alt="Menu - 메뉴 삭제 (정상 작동 알림)" width="720">
    </p>
</details>

<details>
  <summary><b>메뉴 검색 (상태 기반 검색)</b></summary>
    <p align="center">
    <img src="https://github.com/user-attachments/assets/be5af027-9563-4946-8068-7e64f43c5cbc" alt="Menu - 메뉴 검색 (상태 기반 검색)" width="720">
    </p>
</details>

<details>
  <summary><b>메뉴 검색 (상태 + 카테고리)</b></summary>
    <p align="center">
    <img src="https://github.com/user-attachments/assets/eecc59c3-2919-4b1d-8569-c25636e5f3e9" alt="Menu - 메뉴 검색 (상태 + 카테고리)" width="720">
    </p>
</details>

<details>
  <summary><b>메뉴 수정 (식자재 수정)</b></summary>
    <p align="center">
    <img src="https://github.com/user-attachments/assets/899f6a61-3423-4c69-9ba6-b5939d73fff0" alt="Menu - 메뉴 수정 (식자재 수정)" width="720">
    </p>
</details>

<details>
  <summary><b>메뉴 수정 (식자재 수정 후 상태값 자동 변경 - 치즈_듬뿍_피자)</b></summary>
    <p align="center">
    <img src="https://github.com/user-attachments/assets/f1f9e8f7-b6aa-479e-b8c7-501b9d93a079" alt="Menu - 메뉴 수정 (식자재 수정 후 상태값 자동 변경)" width="720">
    </p>
</details>

### 식자재 관리
<details>
  <summary><b>식자재 관리 메인 페이지</b></summary>
    <p align="center">
    <img src="https://github.com/user-attachments/assets/dae3073d-0040-4472-93e6-2514becee436" alt="Ingredient - 식자재 관리 메인 페이지" width="720">
    </p>
</details>

<details>
  <summary><b>식자재 수정 모달</b></summary>
    <p align="center">
    <img src="https://github.com/user-attachments/assets/dd819b53-0ad7-4b33-bc4f-a88735f0b00a" alt="Ingredient - 식자재 수정 모달" width="720">
    </p>
</details>

<details>
  <summary><b>식자재 추가 모달</b></summary>
    <p align="center">
    <img src="https://github.com/user-attachments/assets/b6670ca4-9dbd-4845-8e9e-392c5ad77ee5" alt="Ingredient - 식자재 추가 모달" width="720">
    </p>
</details>

<details>
  <summary><b>식자재 삭제 확인 모달</b></summary>
    <p align="center">
    <img src="https://github.com/user-attachments/assets/16e3bcbf-af83-4015-a762-4d284ee905dc" alt="Ingredient - 식자재 삭제 확인 모달" width="720">
    </p>
</details>
</details>




---
테이블 오더 페이지
-
<details>
  <summary><b>카테고리 선택</b></summary>
    <p align="center">
    <img src="https://github.com/user-attachments/assets/21cc53b1-4653-4cb9-9858-a95ddebe1155" alt=" storeTable 카테고리 선택" width="720">
    </p>
</details>

<details>
  <summary><b>레이아웃 변경</b></summary>
    <p align="center">
    <img src="https://github.com/user-attachments/assets/026e292b-18c3-472a-80ef-36152b4bde2f" alt=" storeTable 레이아웃 변경" width="720">
    </p>
</details>

<details>
  <summary><b>메뉴 상세 모달</b></summary>
    <p align="center">
    <img src="https://github.com/user-attachments/assets/76c3b3ed-b80d-4e9b-b8e3-abe38b5001ec" alt=" storeTable 메뉴 상세 모달" width="720">
    </p>
</details>

<details>
  <summary><b>옵션 선택</b></summary>
    <p align="center">
    <img src="https://github.com/user-attachments/assets/3d0562ee-d29e-4ba0-a3cf-4d7c8e4422f8" alt=" storeTable 옵션 선택" width="720">
    </p>
</details>

<details>
  <summary><b>장바구니 수량 선택</b></summary>
    <p align="center">
    <img src="https://github.com/user-attachments/assets/4abeafd8-bfac-42a9-bda7-0e0b1da6618d" alt=" storeTable 장바구니 수량 선택" width="720">
    </p>
</details>

<details>
  <summary><b>품절된 상품 주문</b></summary>
    <p align="center">
    <img src="https://github.com/user-attachments/assets/e62121e9-09de-440f-bb8f-d81273a76c46" alt=" storeTable 품절된 상품 주문" width="720">
    </p>
</details>

<details>
  <summary><b>한정 수량 상품 주문</b></summary>
    <p align="center">
    <img src="https://github.com/user-attachments/assets/b19dd341-1b77-4406-85a4-b760ae345d6d" alt=" storeTable 한정 수량 상품 주문" width="720">
    </p>
</details>

<details>
  <summary><b>동일 메뉴, 다른 옵션</b></summary>
    <p align="center">
    <img src="https://github.com/user-attachments/assets/b9cd78a8-1eb0-4137-aab8-3c95ef1d6c43" alt=" storeTable 동일 메뉴, 다른 옵션" width="720">
    </p>
</details>

<details>
  <summary><b>주문 후 주문 내역 조회</b></summary>
    <p align="center">
    <img src="https://github.com/user-attachments/assets/21048e43-0a4b-452d-957b-6b531aa686fa" alt=" storeTable 주문 후 주문 내역 조회" width="720">
    </p>
</details>

<details>
  <summary><b>QR 결제</b></summary>
    <p align="center">
    <img src="https://github.com/user-attachments/assets/036a22f0-19a1-4447-b9e1-90b585e686ed" alt=" storeTable QR 결제" width="720">
    </p>
</details>
