# Jelly Order 🍧

### <img width="50" height="50" alt="팀 로고" src="https://github.com/user-attachments/assets/2c140acc-54f4-4b42-b0ba-a3bde1cf5743" /> 혜성충돌
> **먼지와 암석, 얼음이 모여 혜성이 되었고, 우리는 세상을 향해 전속력으로 충돌합니다.**

<table>
  <tr>
    <td align="center">
      <img src="https://avatars.githubusercontent.com/u/98812214?s=120" width="120" height="120" alt="김형진"/><br />
      <sub><b><a href="https://github.com/JeaPple" target="_blank" rel="noreferrer">김형진</a></b></sub>
    </td>
    <td align="center">
      <img src="https://avatars.githubusercontent.com/u/194198612?s=120" width="120" height="120" alt="김현지"/><br />
      <sub><b><a href="https://github.com/ifunhy" target="_blank" rel="noreferrer">김현지</a></b></sub>
    </td>
    <td align="center">
      <img src="https://avatars.githubusercontent.com/u/149755774?s=120" width="120" height="120" alt="김진호"/><br />
      <sub><b><a href="https://github.com/jinnn12" target="_blank" rel="noreferrer">김진호</a></b></sub>
    </td>
    <td align="center">
      <img src="https://avatars.githubusercontent.com/u/171583314?s=120" width="120" height="120" alt="박혜성"/><br />
      <sub><b><a href="https://github.com/solidify-d" target="_blank" rel="noreferrer">박혜성</a></b></sub>
    </td>
  </tr>
</table>

---
## 0. 프로젝트 소개
![img.png](img.png)

  <p align="center">
      Jelly Order는 소상공인의 비용 부담과 디지털 격차 문제를 해결하기 위해,
  </p>
  
  <p align="center">
      누구나 쉽게 도입·운영할 수 있는 오픈소스 경량 테이블 오더·POS 시스템입니다.
  </p>


---
## 1. 프로젝트 기획서

<details>
<summary>프로젝트 개요</summary>

Jelly Order는 소상공인을 위한 경량형 오픈소스 테이블 오더·경량 POS 시스템이다. 상용 솔루션의 초기 설치비·월 구독료·PG 수수료 등 비용 장벽과, 고령층·IT 비숙련자에게 어려운 복잡한 UI를 핵심 문제로 규정하고, 직관적·대형 폰트 기반 UI/UX와 수수료 락인 없는 아키텍처(VAN/현금/기본 카드 단말 연동 중심)를 제공한다. 이를 통해 점주는 더 단순한 POS 운영 경험, 고객은 쉽고 빠른 주문 경험을 얻으며, 누구나 코드와 문서를 내려받아 무상으로 자체 설치·개조할 수 있도록 오픈소스로 배포한다. 비용 부담과 디지털 격차가 실제 현장에서 도입 저해 요인으로 보고되고 있음을 전제로 설계하였다.

</details>

---

<details>
<summary>프로젝트 배경</summary>

<p align="center">
  <img width="800" alt="image" src="https://github.com/user-attachments/assets/832ac568-cf29-438f-b520-9b87d1e6ef6e" />
</p>

### 1) 비용 장벽과 수수료 구조의 불투명성
테이블오더 확산은 인건비 절감을 배경으로 빠르게 진행되었으나, **PG 연동에 따른 높은 결제 수수료(대개 0.8~2.5% 이상)**, **월 구독료·통신비·장비비** 등으로 기대 절감액보다 **고정지출이 커지는 사례**가 다수 보도되었다. 일부 사업장은 **의무사용·위약금** 등 계약 리스크도 경험한다. 업계·언론 보도는 **VAN 위주 결제 대비 PG 경유 시 월 수십만 원~연 수백만 원 추가 비용**이 발생할 수 있음을 지적한다. 또한 법·제도상 **PG 수수료에 대한 제한이나 표준화가 미흡**하다는 점도 반복적으로 제기되고 있다. [YTN](https://www.ytn.co.kr/_ln/0102_202410181024214328)[서울경제](https://www.sedaily.com/NewsView/2DFEDQEVRN)[네이트 뉴스](https://news.nate.com/view/20240108n02405)

> 예시: 월매출 2,000만 원 업장에서 PG 3.3% + 서비스 0.6% ≈ 3.9%로 월 78만 원 수준의 수수료가 발생했다는 보도 사례가 확인된다. 정부 지원으로 설치했더라도 운영 수수료 부담은 영세사업자 몫이라는 점도 지적되었다. 네이트 뉴스

정부·유관기관도 최근에는 **결제기기 수수료 안내 및 계약 전 유의사항**을 배포하며 자율 예방을 독려하고 있으나, **도입 이후의 비용 구조 최적화**는 여전히 점주에게 과도하게 전가되는 양상이다. [소상공인24](https://www.sbiz.or.kr/smst/bbs/view.do?bbsSn=6840&key=211130603916)

---

### 2) 고령층·IT 비숙련자 사용성 문제
고령층은 **작은 글씨·복잡한 화면 흐름·결제 오류 대응 난이도**로 인해 디지털 주문 채널 이용을 **포기하거나 직원 의존**이 잦다는 탐방·설문 결과가 다수 보고된다. 서울시 조사 등에서도 **기기 이용률 저조·심리적 부담**이 확인되며, **교육·큰 글씨·음성안내** 등 보조 설계 시 **적응이 빠르게 개선**된 사례가 보고되었다. 이는 UI의 직관성·접근성(가독성, 단계 축소, 용어 단순화)이 도입 성패를 좌우함을 시사한다. [브라보마이라이프](https://bravo.etoday.co.kr/view/atc_view/13547)

---

### 3) 디지털 전환의 필요와 현실 간 간극
학술·정책·언론 자료는 무인주문·스마트오더·키오스크 등 **스마트 기술이 소상공인 운영 효율에 기여**함을 인정하는 동시에, **도입·운영 비용과 투자 부담**, **연령대별 디지털 격차**가 보급의 병목임을 제시한다. Jelly Order는 **오픈소스 배포·저비용 도입·고령친화 UI**로 이 간극을 해소하고자 한다. [KCI](https://www.kci.go.kr/kciportal/ci/sereArticleSearch/ciSereArtiView.kci?sereArticleSearchBean.artiId=ART002995983)

</details>

---

<details>
<summary>목표 및 목적</summary>

### 1) 최상위 목적(미션)
- **비용 장벽 제거:** 오픈소스로 공개하여 **초기비용·구독료 부담 없이** 자체 호스팅·로컬 설치를 가능하게 한다. **PG 락인 회피**와 **VAN/기존 카드단말·현금결제 연동**을 우선시해 수수료 부담을 최소화한다. [소상공인24](https://www.sbiz.or.kr/smst/bbs/view.do?bbsSn=6840&key=211130603916)
- **디지털 격차 완화:** 대형 폰트·높은 대비·명료한 한국어 용어·단계 최소화 설계를 통해 **고령층·IT 비숙련자**도 스스로 주문·결제가 가능하도록 한다. [브라보마이라이프](https://bravo.etoday.co.kr/view/atc_view/13547)

---

### 2) 이해관계자별 목표
- **점주(매장) 관점**
  - 설치·학습·운영이 **쉬운 경량 POS 콘솔** 제공(메뉴·재고·품절·좌석/테이블 관리 최소 클릭 흐름).
- **고객(사용자) 관점**
  - 노령친화 모드(글자 크게/버튼 크게/음성 힌트/오류 복구 유도)로 주문 포기율 감소.
  - **5클릭 이하 주문**(카테고리→메뉴→옵션→수량→결제)으로 인지부하 최소화. [브라보마이라이프](https://bravo.etoday.co.kr/view/atc_view/13547)

---

### 3) 정량 KPI(초기 제안)
- **도입비 절감**: 상용 대비 **초기 CAPEX 80% 이상 절감**(오픈소스·상용 태블릿 활용 전제, 장비비만 부담). 근거: 상용은 **월 구독료+PG 수수료**로 누적 비용이 커지는 구조가 다수 보고. [YTN](https://www.ytn.co.kr/_ln/0102_202410181024214328)[서울경제](https://www.sedaily.com/NewsView/2DFEDQEVRN)
- **수수료 최적화**: **VAN/직접 정산** 우선 구성 시 **PG 추가 수수료 0~최소화**를 목표(매장 계약 환경에 따라 변동). [서울경제](https://www.sedaily.com/NewsView/2DFEDQEVRN)
- **주문 성공률**: **노령친화 UI** 도입 매장 기준 **주문 포기율(결제 전 이탈) 30%↓** 목표. 관련 탐방·조사에서 고령층의 사용 포기 및 심리적 부담이 높은 것으로 확인. [브라보마이라이프](https://bravo.etoday.co.kr/view/atc_view/13547)
- **학습 곡선**: 점주 대상 **초기 온보딩 30분 내 기본 운영 가능**(메뉴 등록·품절 처리·영수증 확인).

---

### 4) 핵심 기능 범위(요약)
- **점주 콘솔**: 메뉴/옵션/가격, 테이블·좌석, 재고/품절(재료 연동), 주문 현황, 정산 리포트(수수료 시뮬레이터 포함).
- **고객 UI**: **대형 폰트·단순 레이아웃**, 고대비 테마, **오류 복구 안내**, 결제 유도 최소 단계. [브라보마이라이프](https://bravo.etoday.co.kr/view/atc_view/13547)
- **결제 연동**: **VAN/기존 카드단말·현금 결제** 기본, 필요 시 **PG 연동 선택적 제공**(수수료 경고/비교 안내). [서울경제](https://www.sedaily.com/NewsView/2DFEDQEVRN)
- **배포·운영**: 오픈소스 공개, **도커·자체 호스팅 템플릿**, 설치 자동화 스크립트, 운영 가이드/보안 체크리스트.

</details>

---

<details>
<summary>문제정의 요약(자료 근거)</summary>

- **수수료·운영비 부담**이 인건비 절감을 상쇄하는 사례 다수, **PG 수수료 상한 부재**·계약 리스크 지적. → 비용 장벽 해소 필요. [YTN](https://www.ytn.co.kr/_ln/0102_202410181024214328) [서울경제](https://www.sedaily.com/NewsView/2DFEDQEVRN) [네이트 뉴스](https://news.nate.com/view/20240108n02405)
- **고령층·비숙련자 사용성 한계**가 도입/이용 저해. **큰 글씨·음성·단계 축소** 지원 시 적응 개선. → 접근성 중심 설계 필요. [브라보마이라이프](https://bravo.etoday.co.kr/view/atc_view/13547)
- 학술·정책 연구는 **스마트 기술의 효용**과 함께 **도입·투자 부담**을 병목으로 지적. → **오픈소스·저비용·자체 설치** 모델의 공공적 가치. [KCI](https://www.kci.go.kr/kciportal/ci/sereArticleSearch/ciSereArtiView.kci?sereArticleSearchBean.artiId=ART002995983)

</details>

<br>

## 2. 기술 스택

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

<br>

## 3. 분석 및 설계

### 요구사항 명세서 [상세보기](https://docs.google.com/spreadsheets/d/1lV6OYcvnEEBzQFvDyBVyoR1MRkYB52oj-mvlcYLXNRM/edit?gid=1037534638#gid=1037534638)
<details>
  <summary><b>요구사항 명세서</b></summary>
<img width="1362" height="608" alt="스크린샷 2025-08-26 오후 3 29 12" src="https://github.com/user-attachments/assets/8a6c232d-0e74-44f2-a18a-fe436ef44985" />
</details>

###  화면 설계서 - [상세보기](https://www.figma.com/design/WuBdoUWSz5n3gdSHdMN2qo/be16-4th-team?node-id=0-1&t=5D9WbMwwOTOxQaVj-1)
<details>
  <summary><b>점주 페이지</b></summary>
<img width="800" alt="image" src="https://github.com/user-attachments/assets/50a33d51-f4e6-4848-b90b-70fbc4c9f666" />
</details>
<details>
  <summary><b>테이블 오더 페이지</b></summary>
<img width="800" alt="image" src="https://github.com/user-attachments/assets/c0bb2ccf-a485-407f-874e-c51e33b41423" />
</details>

### ERD - [상세보기](https://www.erdcloud.com/d/fAJgKBWBde3CPAkgW)
<details>
  <summary><b>ERD</b></summary>
  <img src="https://raw.githubusercontent.com/beyond-sw-camp/be16-2nd-4team-cometImpact-BE/develop/Jelly_order_ERD.png"></img>
</details>

### WBS - [상세보기](https://docs.google.com/spreadsheets/d/1lV6OYcvnEEBzQFvDyBVyoR1MRkYB52oj-mvlcYLXNRM/edit?gid=0#gid=0)
<details>
  <summary><b>WBS</b></summary>
<img width="1420" height="648" alt="스크린샷 2025-08-26 오후 3 01 35" src="https://github.com/user-attachments/assets/bcfe1f43-44c1-418a-a2a6-12862316e928" />
</details>

### API 명세서 - [상세보기](https://docs.google.com/spreadsheets/d/1lV6OYcvnEEBzQFvDyBVyoR1MRkYB52oj-mvlcYLXNRM/edit?gid=394720298#gid=394720298)
<details>
  <summary><b>API 명세서</b></summary>
<img width="1402" height="580" alt="스크린샷 2025-08-26 오후 3 02 55" src="https://github.com/user-attachments/assets/ebaf6eaa-b99c-4d8d-bfa1-b98f8370fd9c" />
</details>
<br>

---

## 4. 사용한 핵심 기술 & 코드
<table>
  <thead>
    <tr>
      <th>사용 기술</th>
      <th>설명</th>
    </tr>
  </thead>
  <tbody>
    <tr>
      <td><b>Redis Pub/Sub 기반 실시간 메시징</b></td>
      <td>
        점주가 식자재 상태를 수정/삭제하거나 고객 태블릿에서 요청사항·주문을 전송하면,
        서비스 계층에서 상태 변화를 즉시 계산하고 <code>Redis Pub/Sub</code> 채널로 이벤트를 발행합니다.  
        이를 통해 다수의 구독자(테이블 단말기·대시보드)에 동시에 전달되며,
        분산 환경에서도 이벤트 유실 없이 안정적으로 실시간 동기화가 이루어집니다.
      </td>
    </tr>
    <tr>
      <td><b>Server-Sent Events(SSE) 기반 상태 동기화</b></td>
      <td>
        백엔드에서 발생한 메뉴/요청 변경 이벤트를 SSE를 통해 단말기로 스트리밍 전송했습니다. 
        Vue 프론트엔드(테블릿 앱·대시보드)는 별도 폴링 없이 항상 최신 상태를 유지할 수 있습니다.
      </td>
    </tr>
    <tr>
      <td><b>WebSocket 기반 실시간 주문 처리</b></td>
      <td>
        <code>STOMP WebSocket</code>을 통해 주문 요청 이벤트를 실시간 전송하고, 
        매장 대시보드는 <code>storeId</code> 기반 topic을, 테이블 단말은 <code>tableId</code> 기반 queue를 구독해 역할을 분리했습니다.  
        Vue 프론트엔드 단말은 해당 채널을 구독하여 주문을 자동 반영하도록 구현해, 
        지연 없이 항상 최신 주문 화면이 갱신됩니다.
      </td>
    </tr>
    <tr>
      <td><b>카카오페이 QR 결제 연동</b></td>
      <td>
        외부 결제 API인 카카오페이를 연동하여 QR 결제를 지원했습니다.  
        <code>결제 준비 → QR 코드 생성/스캔 → 승인 콜백 → 결과 화면 리다이렉트</code>의 플로우를 구성해 안정적인 결제 프로세스를 제공했습니다.
      </td>
    </tr>
  </tbody>
</table>

---

## 5. 단위 테스트 결과서

> 점주 페이지와 테이블 오더 페이지의 핵심 기능을 단위 테스트로 검증했습니다.  
> 각 결과는 접을 수 있는 토글(`details`)로 제공하며, 이미지와 설명을 정렬했습니다.


### 📌 점주 페이지
<details>
  <summary><b>회원가입 및 로그인</b></summary>
  
  - **점주 회원가입**  
    <p align="center">
      <img src="https://github.com/user-attachments/assets/43680d12-01ba-4f90-8d4f-7e6965704932" alt="점주 회원 가입" width="720">
    </p>
  
  - **점주 일반 로그인**  
    <p align="center">
      <img src="https://github.com/user-attachments/assets/b431146e-4368-4559-8c06-44291ab59692" alt="점주 일반로그인" width="720">
    </p>
  
  - **점주 자동 로그인**  
    <p align="center">
      <img src="https://github.com/user-attachments/assets/3a68f0f8-9e06-4513-abcc-a30a9631e2a7" alt="점주 자동로그인" width="720">
    </p>

  - **아이디 찾기**  
    <p align="center">
      <img src="https://github.com/user-attachments/assets/99220b56-a342-4283-b39d-7465954cd054" alt="점주 아이디 찾기" width="720">
    </p>

  - **비밀번호 재설정**  
    <p align="center">
      <img src="https://github.com/user-attachments/assets/f0d92778-05ff-4068-afbc-3727e21502e2" alt="점주 비밀번호 재설정" width="720">
    </p>
</details>

<details>
  <summary><b>테이블 현황</b></summary>

  - **구역 별 주문 테이블 조회**  
    <p align="center">
      <img src="https://github.com/user-attachments/assets/0db521d3-c686-4b13-9538-68711bf03a52" alt="구역 별 주문 테이블 조회" width="720">
    </p>

  - **QR 결제 후 테이블 상태 변환**  
    <p align="center">
      <img src="https://github.com/user-attachments/assets/eee7ec00-0810-4648-bc99-dcb33b79cb1a" alt="QR 결제 후 상태 변환" width="720">
    </p>

  - **카운터 결제**  
    <p align="center">
      <img src="https://github.com/user-attachments/assets/674e6a6a-7e93-455e-b80c-343aef914e7e" alt="카운터 결제" width="720">
    </p>

  - **테이블 비우기**  
    <p align="center">
      <img src="https://github.com/user-attachments/assets/d5e6c6f5-a588-4372-adf4-bbb2d0876af8" alt="카운터 결제" width="720">
    </p>

  - **주문 수정**  
    <p align="center">
      <img src="https://github.com/user-attachments/assets/14d64ee4-fe28-418b-9345-a51e7153f3e0" alt="카운터 결제" width="720">
    </p>
</details>

<details>
  <summary><b>주문 현황</b></summary>

  - **주문 접수 페이지 (접수 탭)**  
    <p align="center">
      <img src="https://github.com/user-attachments/assets/f06d9dee-5260-4e89-9a31-bec36bb6c273" width="720">
    </p>
    
  - **주문 접수 페이지 (완료 탭)**  
    <p align="center">
      <img src="https://github.com/user-attachments/assets/92b9335d-69ac-4f8f-b4a5-461c91bd1202" width="720">
    </p>

  - **주문 접수 페이지 (취소 탭)**  
    <p align="center">
      <img src="https://github.com/user-attachments/assets/0c85ad12-07e4-4f37-9824-1bd5233de9cd" width="720">
    </p>
    
  - **실시간 주문 확인**  
    <p align="center">
      <img src="https://github.com/user-attachments/assets/3f330879-6b42-4359-b4f9-647f251f6872" width="720">
    </p>

  - **조리 완료**  
    <p align="center">
      <img src="https://github.com/user-attachments/assets/5d7d0036-7118-4266-a042-a58685159562" width="720">
    </p>

  - **조리 취소**  
    <p align="center">
      <img src="https://github.com/user-attachments/assets/ac051629-bcb0-4246-b293-cd525c17eb9d" width="720">
    </p>
</details>

<details>
  <summary><b>메뉴 관리</b></summary>

  - **메뉴 페이지**  
    <p align="center">
      <img src="https://github.com/user-attachments/assets/4a7618f2-1817-49aa-8d80-322b85c4300e" width="720">
    </p>

  - **메뉴 생성 (옵션 추가)**  
    <p align="center">
      <img src="https://github.com/user-attachments/assets/4040d775-de6d-4cac-8dbf-2165de76ba2c" width="720">
    </p>

  - **메뉴 생성 (식자재 설정 및 추가)**  
    <p align="center">
      <img src="https://github.com/user-attachments/assets/661784de-255c-4c98-b093-6dea3d34c9f7" width="720">
    </p>

  - **메뉴 수정 (식자재 소진 시 메뉴 품절)**  
    <p align="center">
      <img src="https://github.com/user-attachments/assets/100132f8-5a50-4664-a66c-6ea66690a2da" width="720">
    </p>

  - **메뉴 삭제**  
    <p align="center">
      <img src="https://github.com/user-attachments/assets/56d18fdf-eebc-4cdd-bc8f-1ab9e50b1d1d" width="720">
    </p>

  - **메뉴 검색**  
    <p align="center">
      <img src="https://github.com/user-attachments/assets/c65e3f9e-0b37-4385-81ac-196dec89f4ad" width="720">
    </p>
    
  - **카테고리별 메뉴 조회**  
    <p align="center">
      <img src="https://github.com/user-attachments/assets/04735dfc-0fa5-4d4f-aa4a-63b1a8705cac" width="720">
    </p>
        
  - **카테고리 생성 (중복 불가)**  
    <p align="center">
      <img src="https://github.com/user-attachments/assets/5909c887-f8b1-47e5-a632-da3fc2b27148" width="720">
    </p>
</details>

<details>
  <summary><b>식자재 관리</b></summary>
  
  - **식자재 관리 페이지**  
    <p align="center">
      <img src="https://github.com/user-attachments/assets/8b0c4cb0-c68d-4bb2-993e-6dc62ec5e91b" width="720">
    </p>

  - **식자재 검색**  
    <p align="center">
      <img src="https://github.com/user-attachments/assets/d077f4a8-feed-40b4-a6ce-bfc5eb4e4556" width="720">
    </p>
    
  - **식자재 추가**  
    <p align="center">
      <img src="https://github.com/user-attachments/assets/1418257f-615a-472d-8541-067a7336605d" width="720">
    </p>

  - **식자재 수정**  
    <p align="center">
      <img src="https://github.com/user-attachments/assets/8bd52f97-1c99-43a8-8327-15b7f78ad1cf" width="720">
    </p>

  - **식자재 삭제**  
    <p align="center">
      <img src="https://github.com/user-attachments/assets/16e3bcbf-af83-4015-a762-4d284ee905dc" width="720">
    </p>
  

</details>

---

### 📌 테이블 오더
<details>
  <summary><b>주요 기능</b></summary>

  - **대기 화면**  
    <p align="center">
      <img src="https://github.com/user-attachments/assets/afd3565a-5b1e-44b3-9c58-1b97094320f1" width="720">
    </p>

  - **카테고리 선택**  
    <p align="center">
      <img src="https://github.com/user-attachments/assets/07a70800-979e-41e6-b517-fd40b81b0f63" width="720">
    </p>
    
  - **레이아웃 변경**  
    <p align="center">
      <img src="https://github.com/user-attachments/assets/a7f501bb-f8f8-42cc-b21e-fff3e00abf24" width="720">
    </p>

  - **요청사항 전송**  
    <p align="center">
      <img src="https://github.com/user-attachments/assets/0540c3af-c6b0-45b3-9976-1980a0e20637" width="720">
    </p>

  - **메뉴 상세 페이지 + 메뉴 담기**  
    <p align="center">
      <img src="https://github.com/user-attachments/assets/40bb14c4-01f6-469f-af61-9758ec436699" width="720">
    </p>

  - **옵션 타입별 선택**  
    <p align="center">
      <img src="https://github.com/user-attachments/assets/1f3f30d9-a050-4c56-9066-249a8e628a8e" width="720">
    </p>
    
  - **장바구니 수량 선택**  
    <p align="center">
      <img src="https://github.com/user-attachments/assets/df401c5c-590f-440e-9ccb-57ba28d2489b" width="720">
    </p>
    
  - **품절된 메뉴 조회**  
    <p align="center">
      <img src="https://github.com/user-attachments/assets/592b79e4-dccf-47b3-9fe1-8e90bca6a213" alt=" storeTable 품절된 메뉴 조회" width="720">
    </p>

  - **품절된 메뉴 주문**  
    <p align="center">
      <img src="https://github.com/user-attachments/assets/992cf2b5-7e2b-4895-be24-741a5c7f7fb6" alt=" storeTable 품절된 메뉴 주문" width="720">
    </p>
    
  - **한정 수량인 메뉴 주문**  
    <p align="center">
      <img src="https://github.com/user-attachments/assets/d22f5bb0-0362-463f-9123-dfd217a83598" alt=" storeTable 한정 수량인 메뉴 주문" width="720">
    </p>
        
  - **정상 주문**  
    <p align="center">
      <img src="https://github.com/user-attachments/assets/537c4ed4-81d5-4167-94be-30de2f9ec02c" alt=" storeTable 정상 주문" width="720">
    </p>
    
  - **주문 내역 조회**  
    <p align="center">
      <img src="https://github.com/user-attachments/assets/d6ccd45f-038d-44a6-b252-cb9482ea43e4" alt=" storeTable 주문 내역 조회" width="720">
    </p>

  - **QR 결제**  
    <p align="center">
      <img src="https://github.com/user-attachments/assets/5c60bfee-ff61-4d4b-9a89-5f57f87d24e2" alt=" storeTable QR 결제" width="720">
      <img src="https://github.com/user-attachments/assets/f114d151-7382-4a0e-8bea-089697a3ee11" alt=" storeTable QR 결제2" width="720">
    </p>
</details>

