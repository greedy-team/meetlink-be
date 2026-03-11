# meetlink-be

## Contributing
아래 단계를 통해 로컬 개발 환경을 구성할 수 있습니다.

### 1. Clone Repository
```shell
git clone https://github.com/greedy-team/meetlink-be.git
cd meetlink-be
```

### 2. Prerequisites
계속하려면 아래 의존성이 설치되어 있어야 합니다:
* Docker
* Docker Compose
* Java 21

### 3. Prepare MOTIS data
MOTIS import를 위해 OSM + GTFS 파일이 필요합니다.

#### Required Files
아래 두 파일을 준비합니다:
* OSM: `south-korea-latest.osm.pbf`
* GTFS: `south-korea.gtfs.zip`

준비가 완료되면 파일을 다음 경로 아래에 위치시킵니다:
```text
config/motis/input/
```

최종 디렉토리 구조 예시:
```text
config/
 └── motis/
     └── input/
         ├── south-korea-latest.osm.pbf
         └── south-korea.gtfs.zip
```

### 4. Preload MOTIS data
MOTIS server를 실행하기 위해서는 먼저 MOTIS import를 수행해야 합니다. (최초 1회)
```shell
docker compose -f docker-compose.yml -f docker-compose.local.yml --profile import up motis-import
```
작업이 완료되면 `motis-data` 볼륨(로컬 환경에서는 `./motis-data` 디렉토리)에 데이터가 생성됩니다.
> OSM 또는 GTFS 파일이 변경된 경우 반영을 위해 위 과정을 다시 실행해주어야 합니다.

### 5. Start Local Stack
이제 로컬 Docker Compose 스택을 실행합니다:
```shell
docker compose -f docker-compose.yml -f docker-compose.local.yml up -d
```
실행되는 서비스는 다음과 같습니다:
* PostgreSQL - `localhost:5432`
* MOTIS server - `localhost:3000`
* Grafana - `localhost:3001`
* Prometheus
* node-exporter

### 6. Start Spring Boot Application
이제 IDE에서 Spring Boot Application을 실행합니다.

환경 변수는 `.env.sample` 파일을 참고하여 구성한 후, IDE 내 `Run Configurations`에서 등록하여 적용할 수 있습니다.
> [!NOTE]
> 저희 프로젝트는 pre-commit Hook을 통해 코드 컨벤션을 관리합니다.
> 
> 최초 1회 `./gradlew build`를 실행하면 Git Hook이 자동으로 등록됩니다.
