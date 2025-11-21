<div align="center">
<img width="2048" height="1237" alt="image" src="https://github.com/user-attachments/assets/9f8381eb-0ce2-46ae-a998-da21ec0529ab" />
<h1>딸깍</h1>
</div>

## 개요
영상 하이라이트를 분석하여 쇼츠를 생성해주는 쇼츠 자동화 서비스

## 시스템 아키텍처
<img width="863" height="484" alt="image" src="https://github.com/user-attachments/assets/393f9747-ae90-4945-a160-c6b3dae9da4b" />

## 담당한 기능
* [대용량 영상 파일 업로드 최적화](#1-대용량-영상-파일-업로드-최적화)
* [AI 기반 숏폼 추출 서비스 구현](#2-AI-기반-숏폼-추출-서비스-구현)

## 1. 대용량 영상 파일 업로드 최적화
### 개요
* 영상 분석 및 편집을 위해 클라이언트측에서 영상 파일을 업로드 해주어야 함

### 문제 및 의사결정 과정
* 서버를 통해 대용량 영상 파일(예: 321.6 MB)을 업로드할 때, 업로드 처리 시점에 컨테이너 CPU 사용률이 급격히 상승(2 vCPU 한도에서 최대 100%까지 소진)
* 네트워크, 메모리, 버퍼 등 리소스 부담이 커져 확장성과 안정성에 한계가 있었음
* 특히 동시 업로드나 대용량 파일 처리 시 서버가 바이트 스트림을 직접 중계하면서 CPU 스파이크와 지연이 발생해, 서비스 품질 저하와 인프라 비용 증가가 우려되었음
* S3 Presigned URL을 도입하여 해결

#### 1. S3 Presigned URL
1. 기존의 방식

![image](https://github.com/user-attachments/assets/292e59e2-d388-4c64-a3ca-d56202954a90)
 
* **multipart/form-data**를 통한 데이터 전송 시 클라이언트 &rarr; 서버, 서버 &rarr; s3로 데이터를 전송하는 이중 작업이 필요
* 또한 서버측에서 Spring은 file-size-threshold를 넘어가는 파일은 임시 파일로 저장하여 처리하기 때문에 서버에서 대용량 파일을 직접 처리하면, 서버 자원(메모리, CPU 등)의 사용률이 증가함

2. Presigned Url

![image](https://github.com/user-attachments/assets/a2b57311-2b15-411a-a2e2-e238c35cd9a0)

* **Presigned Url**은 s3의 소유자가 미리 업로드, 다운로드 등에 대해 서명을 해준 뒤 사용자에게 해당 Url을 제공해주는 방식
* 해당 방식을 사용하면 클라이언트는 서버를 거치지 않고 파일을 s3에 바로 업로드할 수 있어 서버의 자원을 절약할 수 있고, 트래픽이나 스토리지 사용에 대한 추가 비용 또한 절약할 수 있음

* 프로메테우스 쿼리 : 100 * rate(process_cpu_time_ns_total[5m]) / 1e9 / 2 로 CPU 사용률을 정량적으로 비교

### 성과
* 동일 조건(321.6 MB 단일 파일, 컨테이너 2 VCPU)에서 업로드 처리 구간의 JVM CPU 사용률이 2.0(100%) &rarr; 0.4(20%)로 개선
* 서버가 파일 데이터 중계를 하지 않으므로, 업로드 API의 응답 시간도 3448ms &rarr; 8ms로 개선
* 서버 확장성, 안정성이 향상되고, 인프라 자원 효율 및 비용 최적화 효과를 얻음

## 2. AI 기반 숏폼 추출 서비스 구현
### 개요

<details>
<summary>처리 과정</summary>

<!-- summary 아래 한칸 공백 두어야함 -->
### 1.	사용자 요청
* 클라이언트에서 Google 계정으로 로그인한 사용자가 YouTube URL, 제목, 카테고리 등의 정보를 입력
* 해당 정보는 AI 서버의 /extract-highlights 엔드포인트로 전송
### 2.	비디오 하이라이트 추출 요청
* AI 서버는 해당 YouTube 영상을 다운로드하고 사이즈를 조정
* Whisper AI를 이용하여 비디오의 스크립트를 추출하고, GPT 모델을 통해 하이라이트 구간을 탐색
* 이 과정은 비동기적으로 처리되며, AI 서버는 이 요청에 대해 task_id를 응답으로 반환합니다. task_id는 FastAPI의 로컬 스토리지에서 비동기 작업 상태와 결과 데이터를 관리하는 키로 사용됩니다.
3.	작업 상태 조회 (Polling)
    클라이언트는 task_id를 이용해 FastAPI의 /task-status/{task_id} 엔드포인트에 polling을 시도하며 작업 상태를 확인합니다.
    작업 상태가 “완료”로 변경되면 클라이언트는 FastAPI의 /select-highlight/{task_id} 엔드포인트를 호출하여, 생성된 5개의 하이라이트 후보 영상의 S3 URL과 관련 메타데이터(DTO)를 받아옵니다.
4.	하이라이트 선택 및 업로드
    사용자는 5개의 하이라이트 중 하나를 선택하고, 프론트엔드에서 FastAPI의 /select-highlight 엔드포인트로 task_id와 선택한 영상의 인덱스(index)를 전송합니다.
    FastAPI는 선택한 하이라이트를 Spring Framework 웹 애플리케이션의 /api/videos/create 엔드포인트에 전달하여, 최종적으로 해당 하이라이트를 Video 객체로 웹 서비스에 업로드합니다.
5.	원본 비디오 다운로드 
    업로드된 비디오가 Video 객체로 생성된 후, 사용자는 원본 비디오를 다운로드할 수 있습니다.
    클라이언트는 Spring 웹 애플리케이션의 /api/videos/{videoId}/extract 엔드포인트에 요청하여, 원본 비디오의 AWS S3에 저장된 presigned URL을 받아 로컬로 다운로드할 수 있습니다.
```    
      ┌────────────┐
      │   Client   │
      └─────┬──────┘
      │
      (User Input: │ YouTube URL, ...)
      │
      ▼
      ┌─────────────────────────────┐
      │ Spring Framework Web Server │
      └─────────────┬───────────────┘
      │
      POST /extract-highlights  (video info)
      │
      ▼
      ┌───────────────────┐
      │   FastAPI Server  │
      └───────┬───────────┘
      │
      ┌──────────────▼───────────────┐
      │ Background Task (Async)      │
      │ - Video download             │
      │ - Resize, Whisper model      │
      │ - Highlight extraction (GPT) │
      └──────────────┬───────────────┘
      │
      Respond with │ task_id
      │
      ▼
      ┌────────────────────┐
      │   Client (Polling) │
      └─────────▲──────────┘
      │
      GET /task-status/{task_id}
      │
      If Status = "Complete"
      │
      ▼
      GET /select-highlight/{task_id}
      │
      │
      (5 highlight │ S3 URLs)
      │
      ▼
      ┌────────────────────────────┐
      │ Client selects one video   │
      │ POST /select-highlight     │
      └──────────┬─────────────────┘
      │
      │
      ▼
      ┌───────────────────────────────┐
      │ Spring Framework Web Server   │
      │ POST /api/videos/create       │
      └───────────────────────────────┘
      │
      │
      ▼
      ┌────────────────────┐
      │   AWS S3 Storage   │
      └────────────────────┘
      │
      │
      GET /api/videos/{videoId}/extract
      │
      ▼
      Client receives presigned URL
      (Download)
```      
</details>

* Whisper AI로 영상에서 음성을 추출하고 스크립트 자동 생성
* OpenAI API를 이용해 스크립트 기반으로 핵심 하이라이트 구간 추출
* FFmpeg을 활용해 영상 리사이징(9:16) 및 자막 자동 삽입
* 클라이언트 측에서 폴링 방식으로 진행 정도를 확인할 수 있도록 진행 상황 자동 갱신

### 문제 및 의사결정 과정
#### 1. 폴링 방식 vs WebSocket vs SSE
* 클라우드 GPU 측에서 지속 연결을 차단하여 WebSocket, SSE 사용 불가
* 구현 단순성과 영상 생성 시간이 평균 3분 정도임을 고려하여 5초 주기의 폴링 방식 채택

#### 2. 클라우드 GPU 도입
* Whisper AI와 FFmpeg 영상 편집 작업에서 CPU를 사용하여 처리 시간이 오래 걸림
* 클라우드 GPU 도입으로 Whisper AI와 FFmpeg 영상 편집 작업 처리 속도 향상

### 성과
* 클라우드 GPU 환경 도입으로 Whisper 연산과 FFmpeg 처리속도가 CPU 대비 약 6.7배 향상 (20분 영상 기준, 약 20분 &rarr; 약 3분)
