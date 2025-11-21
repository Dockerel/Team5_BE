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
### 문제 및 의사결정 과정
### 성과
