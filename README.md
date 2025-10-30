# Data Processing Backend Application# Data Processing Backend Application

A high-performance Spring Boot application designed to handle large-scale student data processing with optimized memory management for generating, processing, and managing files with 1,000,000+ records.A high-performance Spring Boot application designed to handle large-scale student data processing with optimized memory management for generating, processing, and managing files with 1,000,000+ records.

## Overview## Overview

This backend application provides REST APIs for:A high-performance Spring Boot application designed to handle large-scale student data processing with optimized memory management for generating, processing, and managing files with 1,000,000+ records.

-   **Generating large Excel files** (1M+ records) using streaming### Local development README

-   **Processing Excel to CSV** with memory-efficient SAX parsing

-   **Importing CSV to PostgreSQL** with batch operationsThis backend application provides REST APIs for:

-   **Paginated reporting** with search and filtering

-   **Multi-format exports** (Excel, CSV, PDF)- **Generating large Excel files** (1M+ records) using streaming

## Key Performance Optimizations- **Processing Excel to CSV** with memory-efficient SAX parsing## OverviewRequirements

### 1. Streaming Excel Generation- **Importing CSV to PostgreSQL** with batch operations

Uses `SXSSFWorkbook` to generate massive Excel files without loading everything into memory.- **Paginated reporting** with search and filtering

-   **Keeps only 100 rows in memory** at a time- **Multi-format exports** (Excel, CSV, PDF)

-   Older rows automatically flushed to disk

-   Can generate **1M records in ~33 seconds** using ~200MB RAMThis backend application provides REST APIs for:- Java 17

### 2. Streaming Excel Processing (SAX Parser)## Key Performance Optimizations

Custom `StreamingExcelReader` utility using event-based SAX parsing.- **Generating large Excel files** (1M+ records) using streaming- Maven (the project includes the Maven wrapper `./mvnw`)

**Why this matters:**### 1. Streaming Excel Generation

-   **Before**: Loading 30MB Excel uses 1.5-2GB RAM and causes OutOfMemoryErrorUses `SXSSFWorkbook` to generate massive Excel files without loading everything into memory.

-   **After**: SAX streaming uses 200-300MB RAM and processes 1M records in ~33 seconds

-   **Processing Excel → CSV** with memory-efficient SAX parsing- Docker (for Postgres when running integration tests or `docker-compose`)

**Performance gains:**

-   **Keeps only 100 rows in memory** at a time

-   80% reduction in memory usage

-   50% faster processing- Older rows automatically flushed to disk- **Importing CSV → PostgreSQL** with batch operations

-   No file size limits

-   Can generate **1M records in 30-60 seconds** using ~200MB RAM

### 3. Java Heap Optimization

-   **Paginated reporting** with search and filteringQuick start (dev)

Docker container configured with:

### 2. Streaming Excel Processing (SAX Parser)

```dockerfile

ENV JAVA_OPTS="-Xms512m -Xmx2g"Custom `StreamingExcelReader` utility using event-based SAX parsing.

```

-   **Multi-format exports** (Excel, CSV, PDF)

-   Initial heap: 512 MB

-   Maximum heap: 2 GB- **Why this matters:**1. Start Docker (macOS): open Docker Desktop and ensure it is running.

### 4. POI Byte Array Limit- **Before**: Loading 30MB Excel uses 1.5-2GB RAM and causes OutOfMemoryError

Increased Apache POI's internal byte array limit:- **After**: SAX streaming uses 200-300MB RAM and processes 1M records in 60-90s## 🚀 Key Performance Optimizations2. Start Postgres via docker-compose (if you prefer):

````java**Performance gains:**

IOUtils.setByteArrayMaxOverride(500 * 1024 * 1024); // 500 MB

```- 80% reduction in memory usage



## Technology Stack- 50% faster processing### 1. Streaming Excel Generation ```bash



- **Java**: 17- No file size limits

- **Spring Boot**: 3.4.5

- **Database**: PostgreSQL 15Uses `SXSSFWorkbook` to generate massive Excel files without loading everything into memory. docker compose up -d

- **Excel**: Apache POI 5.2.3 (with streaming)

- **CSV**: OpenCSV 5.7.1### 3. Java Heap Optimization

- **PDF**: Apache PDFBox 2.0.27

- **Build**: Maven 3.9.4Docker container configured with:

- **Container**: Docker & Docker Compose

- **Keeps only 100 rows in memory** at a time

## Quick Start

    ```java

### Prerequisites    JAVA_OPTS="-Xms512m -Xmx2g"- Older rows automatically flushed to disk



- Docker & Docker Compose installed    ```

- (Optional) Java 17+ and Maven for local development

- Initial heap: 512 MB- Can generate **1M records in 30-60 seconds** using ~200MB RAM3. Build the project:

### Start with Docker (Recommended)

- Maximum heap: 2 GB

1. **Build and start all services**

### 4. POI Byte Array Limit

```bash

docker compose up -d --buildIncreased Apache POI's internal byte array limit:### 2. Streaming Excel Processing (SAX Parser) ```bash

````

````java

2. **Verify services are running**

IOUtils.setByteArrayMaxOverride(500 * 1024 * 1024); // 500 MBCustom `StreamingExcelReader` utility using event-based SAX parsing.    ./mvnw -DskipTests package

```bash

docker ps```

docker compose logs -f app

```- **Before**: Loading 30MB Excel → Uses 1.5-2GB RAM → OutOfMemoryError ❌



3. **Test the API**- **After**: SAX streaming → Uses 200-300MB RAM → Processes 1M records in 60-90s ✅



```bash## Technology Stack

curl http://localhost:8080/api/data/files/excel

```**Why this matters:**



The backend will be available at **http://localhost:8080**- **Java**: 17



### Local Development (without Docker)- **Spring Boot**: 3.4.5



1. **Start PostgreSQL**- **Database**: PostgreSQL 15



```bash- **Excel**: Apache POI 5.2.3 (with streaming)

docker run -d --name postgres-dev \

  -e POSTGRES_DB=datadb \- **CSV**: OpenCSV 5.7.1

  -e POSTGRES_USER=devuser \

  -e POSTGRES_PASSWORD=devpass \- **PDF**: Apache PDFBox 2.0.27 ```bash

  -p 5432:5432 \

  postgres:15-alpine- **Build**: Maven 3.9.4

````

-   **Container**: Docker & Docker Compose

2. **Build and run\*\***Performance gains:\*\* ./mvnw spring-boot:run

`bash## Quick Start- 80% reduction in memory usage `

./mvnw clean package -DskipTests

java -Xmx2g -jar target/dataprocessing-0.0.1-SNAPSHOT.jar### Prerequisites- 50% faster processing

````

- Docker & Docker Compose installed

## API Endpoints

- (Optional) Java 17+ and Maven for local development- No file size limitsRun with Docker (recommended for local parity)

### Data Processing

### Start with Docker (Recommended)

#### Generate Excel

1. **Build and start all services**

```http

POST /api/data/generate?numberOfRecords={n}### 3. Java Heap Optimization1. Build and start both Postgres and the backend using Docker Compose

````

    ```bash

**Example:**

    docker compose up -d --buildDocker container configured with:

````bash

curl -X POST "http://localhost:8080/api/data/generate?numberOfRecords=1000000"    ```

````

    `dockerfile    `bash

#### List Files

2. **Verify services are running**

`````http

GET /api/data/files/excel    # List Excel files````bashENV JAVA_OPTS="-Xms512m -Xmx2g"    docker compose up --build -d

GET /api/data/files/csv      # List CSV files

```docker ps



#### Process Excel to CSVdocker compose logs -f app```    ```



```http````

POST /api/data/process?filename={name}

```- Initial heap: 512 MB



**Transformation**: Adds 10 to each score3. **Test the API**



```bash```bash- Maximum heap: 2 GB2. Check logs to confirm the backend started and connected to Postgres:

curl -X POST "http://localhost:8080/api/data/process?filename=students_20251030123456.xlsx"

```curl http://localhost:8080/api/data/files/excel



#### Upload CSV to Database```



```httpThe backend will be available at **<http://localhost:8080**###> 4. POI Byte Array Limit ```bash

POST /api/data/upload?filename={name}

```### Local Development (without Docker)Increased Apache POI's internal byte array limit: docker compose logs -f app



**Transformation**: Adds 5 to each score (total +15 from original)1. **Start PostgreSQL**`java`



```bash````bash

curl -X POST "http://localhost:8080/api/data/upload?filename=students_20251030123456_processed_20251030124500.csv"

```docker run -d --name postgres-dev \IOUtils.setByteArrayMaxOverride(500 * 1024 * 1024); // 500 MB



### Student Reports  -e POSTGRES_DB=datadb \



#### Get Paginated Report  -e POSTGRES_USER=devuser \```The backend will be available at http://localhost:8080 and will use an internal Postgres service named `db` in the compose network.



```http  -e POSTGRES_PASSWORD=devpass \

GET /api/students/report?page={p}&size={s}&studentId={id}&className={class}

```  -p 5432:5432 \



**Examples:**  postgres:15-alpine



```bash```## Technology StackAPI endpoints (examples)

# Get first page

curl "http://localhost:8080/api/students/report?page=0&size=10"



# Search by student ID2. **Build and run**

curl "http://localhost:8080/api/students/report?studentId=12345"

```bash

# Filter by class

curl "http://localhost:8080/api/students/report?className=Class1&page=0&size=25"./mvnw clean package -DskipTests- **Java**: 17- Generate Excel with N records

`````

java -Xmx2g -jar target/dataprocessing-0.0.1-SNAPSHOT.jar

#### Export Data

````- **Spring Boot**: 3.4.5

```http

GET /api/students/export/excel?className={class}    # Download Excel

GET /api/students/export/csv?className={class}      # Download CSV

GET /api/students/export/pdf?className={class}      # Download PDF## API Endpoints- **Database**: PostgreSQL 15    POST /api/data/generate?numberOfRecords=10

````

## File Storage

### Data Processing- **Excel**: Apache POI 5.2.3 (with streaming)

Files are stored at: `/var/log/applications/API/dataprocessing/`

**Docker volume mapping:**

#### Generate Excel- **CSV**: OpenCSV 5.7.1- Process uploaded Excel (returns a CSV path)

`````yaml

volumes:````

  - ./data:/var/log/applications/API/dataprocessing

```POST /api/data/generate?numberOfRecords={n}- **PDF**: Apache PDFBox 2.0.27



**Access files:**````



```bash**Example:**- **Build**: Maven 3.9.4    POST /api/data/process (multipart form field `file`)

# Inside container

docker exec dataprocessing-app ls -lh /var/log/applications/API/dataprocessing/```bash



# On host machinecurl -X POST "http://localhost:8080/api/data/generate?numberOfRecords=1000000"- **Container**: Docker & Docker Compose

ls -lh ./data/

`````

## Architecture- Upload CSV (multipart) and save students

```````#### List Files

src/main/java/dev/hamez/dataprocessing/

├── controller/```## Quick Start

│   ├── DataController.java          # Data generation & processing

│   └── StudentController.java       # Reports & exportsGET /api/data/files/excel    # List Excel files

├── service/

│   ├── DataProcessingService.java   # Core business logic (optimized)GET /api/data/files/csv      # List CSV files    POST /api/data/upload (multipart form field `file`)

│   └── impl/

│       └── StudentServiceImpl.java  # Report implementation```

├── repository/

│   └── StudentRepository.java       # JPA repository with custom queries### Prerequisites

├── entity/

│   └── Student.java                 # Database entity#### Process Excel to CSV

├── util/

│   ├── StreamingExcelReader.java   # SAX-based Excel reader (OPTIMIZED)````- Docker & Docker Compose installed- Paginated student report

│   └── RandomDataGenerator.java    # Random data generator

└── config/POST /api/data/process?filename={name}

    └── WebConfig.java              # CORS configuration

``````- (Optional) Java 17+ and Maven for local development



## Data Flow & Transformations**Transformation**: Adds 10 to each score



### Student Record Structure```bash    GET /api/students/report?page=0&size=20



| Field | Type | Specification |curl -X POST "http://localhost:8080/api/data/process?filename=students_20251030123456.xlsx"

|-------|------|---------------|

| studentId | Numeric | Incremental from 1 |```### Start with Docker (Recommended)

| firstName | String | Random 3-8 characters |

| lastName | String | Random 3-8 characters |

| dob | Date | Random 2000-01-01 to 2010-12-31 |

| className | String | Random [Class1-5] |#### Upload CSV to Database- Export all students as Excel/CSV/PDF

| score | Integer | Random 55-75 |

```````

### Score Transformation Pipeline

POST /api/data/upload?filename={name}1. **Build and start all services:**

| Stage | Transformation | Example |

|-------|---------------|---------|````

| Excel Generation | Random 55-75 | 60 |

| CSV Processing | +10 | 70 |**Transformation**: Adds 5 to each score (total +15 from original)```bash GET /api/students/export/excel

| Database Import | +5 | **75** (Original + 15) |

```bash

## Verified Performance Metrics

curl -X POST "http://localhost:8080/api/data/upload?filename=students_20251030123456_processed_20251030124500.csv"docker compose up -d --build    GET /api/students/export/csv

**Tested on 1,000,000 records:**

```

| Operation | Seconds | Minutes | Memory | File Size |

|-----------|---------|---------|--------|-----------|```````GET /api/students/export/pdf

| **Excel Generation** | 33s | 0.55 min | ~200 MB | ~30 MB |

| **Excel to CSV Processing** | 33s | 0.55 min | ~300 MB | ~50 MB |### Student Reports

| **CSV to Database Upload** | 277s | 4.62 min | ~200 MB | ~80 MB |

| **Total End-to-End Pipeline** | **343s** | **5.72 min** | - | - |

### Summary#### Get Paginated Report

-   **Data Generation (Excel)**: ~0.55 minutes```2. **Verify services are running:**Running tests

-   **Data Processing (Excel → CSV)**: ~0.55 minutes

-   **Data Upload (CSV → DB)**: ~4.62 minutesGET /api/students/report?page={p}&size={s}&studentId={id}&className={class}

-   **Complete Pipeline**: **~5.72 minutes total**

````````bash

*Note: Timings measured on macOS with Docker Desktop. Actual performance may vary based on hardware.*

**Examples:**

## Configuration

```bashdocker psUnit and integration tests can be run with Maven. Integration tests use Testcontainers and require Docker to be running.

### Environment Variables

# Get first page

| Variable | Default | Description |

|----------|---------|-------------|curl "http://localhost:8080/api/students/report?page=0&size=10"docker compose logs -f app

| `SPRING_DATASOURCE_URL` | `jdbc:postgresql://db:5432/datadb` | Database URL |

| `SPRING_DATASOURCE_USERNAME` | `devuser` | DB username |

| `SPRING_DATASOURCE_PASSWORD` | `devpass` | DB password |

| `APP_FILE_STORAGE` | `/var/log/applications/API/dataprocessing` | Storage path |# Search by student ID``````bash

| `JAVA_OPTS` | `-Xms512m -Xmx2g` | JVM options |

curl "http://localhost:8080/api/students/report?studentId=12345"

### Application Properties

./mvnw test

Located in `src/main/resources/application.properties`:

# Filter by class

```properties

spring.datasource.url=jdbc:postgresql://localhost:5432/datadbcurl "http://localhost:8080/api/students/report?className=Class1&page=0&size=25"3. **Test the API:**```

spring.jpa.hibernate.ddl-auto=update

app.file.storage=/var/log/applications/API/dataprocessing```````

server.port=8080

```````bash



## Testing#### Export Data



### Run All Tests```curl http://localhost:8080/api/data/files/excelNotes



```bashGET /api/students/export/excel?className={class}    # Download Excel

./mvnw test

```GET /api/students/export/csv?className={class}      # Download CSV```



### Integration TestsGET /api/students/export/pdf?className={class}      # Download PDF



Uses Testcontainers for full workflow testing with automatic PostgreSQL container.```- Files generated by the app are stored under `./data` by default (configurable in `src/main/resources/application.properties`).



### Manual Testing Workflow



```bash## File StorageThe backend will be available at **http://localhost:8080**- If you see warnings about `spring.jpa.open-in-view`, the project disables that property in `application.properties` to encourage explicit transactional boundaries.

# 1. Generate Excel

curl -X POST "http://localhost:8080/api/data/generate?numberOfRecords=10000"



# 2. List Excel filesFiles are stored at: `/var/log/applications/API/dataprocessing/`

curl "http://localhost:8080/api/data/files/excel"

### Local Development (without Docker)

# 3. Process to CSV (use filename from step 2)

curl -X POST "http://localhost:8080/api/data/process?filename=students_20251030123456.xlsx"**Docker volume mapping:**



# 4. List CSV files```yaml1. **Start PostgreSQL:**

curl "http://localhost:8080/api/data/files/csv"

volumes:```bash

# 5. Upload to database (use filename from step 4)

curl -X POST "http://localhost:8080/api/data/upload?filename=students_20251030123456_processed_20251030124500.csv"  - ./data:/var/log/applications/API/dataprocessingdocker run -d --name postgres-dev \



# 6. Query data```  -e POSTGRES_DB=datadb \

curl "http://localhost:8080/api/students/report?page=0&size=10"

```  -e POSTGRES_USER=devuser \



## Troubleshooting**Access files:**  -e POSTGRES_PASSWORD=devpass \



### OutOfMemoryError```bash  -p 5432:5432 \



**Symptoms:** `java.lang.OutOfMemoryError: Java heap space`# Inside container  postgres:15-alpine



**Solutions:**docker exec dataprocessing-app ls -lh /var/log/applications/API/dataprocessing/```



1. Verify Docker heap size: `docker exec dataprocessing-app env | grep JAVA_OPTS`

2. Check using streaming methods: Look for "streaming" in logs

3. Ensure POI override is set: Look for "POI byte array max override set to 500MB" in logs# On host machine2. **Build and run:**



### File Not Foundls -lh ./data/```bash



**Solutions:**```./mvnw clean package -DskipTests



```bashjava -Xmx2g -jar target/dataprocessing-0.0.1-SNAPSHOT.jar

# Check file exists

docker exec dataprocessing-app ls /var/log/applications/API/dataprocessing/## Architecture```



# Check storage path in logs

docker compose logs app | grep "Using storage path"

``````## API Endpoints



### Slow Processingsrc/main/java/dev/hamez/dataprocessing/



**Expected times for 1,000,000 records:**├── controller/### Data Processing



- Generation: ~33 seconds (0.55 minutes)│   ├── DataController.java          # Data generation & processing

- Processing: ~33 seconds (0.55 minutes)

- Upload: ~277 seconds (4.62 minutes)│   └── StudentController.java       # Reports & exports#### Generate Excel



If significantly slower, check:├── service/```http



```bash│   ├── DataProcessingService.java   # Core business logic (optimized)POST /api/data/generate?numberOfRecords={n}

# Monitor resources

docker stats dataprocessing-app│   └── impl/```



# Check for errors│       └── StudentServiceImpl.java  # Report implementation**Example:**

docker compose logs -f app

```├── repository/```bash



## Monitoring│   └── StudentRepository.java       # JPA repository with custom queriescurl -X POST "http://localhost:8080/api/data/generate?numberOfRecords=1000000"



### View Logs├── entity/```



```bash│   └── Student.java                 # Database entity

docker compose logs -f app

```├── util/#### List Files



### Check Database│   ├── StreamingExcelReader.java   # SAX-based Excel reader (OPTIMIZED)```http



```bash│   └── RandomDataGenerator.java    # Random data generatorGET /api/data/files/excel    # List Excel files

docker exec -it dataprocessing-db psql -U devuser -d datadb

└── config/GET /api/data/files/csv      # List CSV files

# Count records

SELECT COUNT(*) FROM student;    └── WebConfig.java              # CORS configuration```



# Check score range (should be original + 15)````

SELECT MIN(score), MAX(score), AVG(score) FROM student;

```#### Process Excel to CSV



### Monitor Progress## Data Flow & Transformations```http



During large file processing, logs show:POST /api/data/process?filename={name}



```### Student Record Structure```

INFO ... Starting streaming Excel read...

INFO ... Processed 100000 rows...| Field | Type | Specification |**Transformation**: Adds 10 to each score

INFO ... Processed 200000 rows...

INFO ... Completed streaming Excel read in 60000 ms|-------|------|---------------|```bash

```

| studentId | Numeric | Incremental from 1 |curl -X POST "<http://localhost:8080/api/data/process?filename=students_20251030123456.xlsx>"

## Building

| firstName | String | Random 3-8 characters |```

```bash

# Clean and build| lastName | String | Random 3-8 characters |

./mvnw clean package

| dob | Date | Random 2000-01-01 to 2010-12-31 |#### Upload CSV to Database

# Skip tests

./mvnw clean package -DskipTests| className | String | Random [Class1-5] |```http



# Run locally| score | Integer | Random 55-75 |POST /api/data/upload?filename={name}

./mvnw spring-boot:run

````````

## Production Considerations### Score Transformation Pipeline**Transformation**: Adds 5 to each score (total +15 from original)

⚠️ **Note**: This is a demonstration application without authentication.| Stage | Transformation | Example |```bash

### For Production|-------|---------------|---------|curl -X POST "http://localhost:8080/api/data/upload?filename=students_20251030123456_processed_20251030124500.csv"

-   Add Spring Security with JWT/OAuth2| Excel Generation | Random 55-75 | 60 |```

-   Implement rate limiting

-   Use cloud storage (S3, Azure Blob)| CSV Processing | +10 | 70 |

-   Add message queue for async processing

-   Use managed PostgreSQL| Database Import | +5 | **75** (Original + 15) |### Student Reports

-   Add comprehensive monitoring (Prometheus, Grafana)

-   Implement audit logging

-   Add file upload size limits

## Performance Metrics (1M Records)#### Get Paginated Report

## Summary

````http

This backend efficiently handles **large-scale data processing** with:

| Operation | Time | Memory | File Size |GET /api/students/report?page={p}&size={s}&studentId={id}&className={class}

✅ **Streaming Excel generation** - 1M records in 0.55 minutes

✅ **Memory-optimized SAX parsing** - 80% less RAM usage  |-----------|------|--------|-----------|```

✅ **Batch database operations** - Efficient bulk inserts

✅ **Comprehensive REST APIs** - Full CRUD + reporting  | **Excel Generation** | 30-60s | ~200 MB | ~30 MB |**Examples:**

✅ **Docker containerization** - Easy deployment

✅ **Production-ready architecture** - Scalable and maintainable  | **Excel to CSV** | 60-90s | ~300 MB | ~50 MB |```bash



**Built for performance and scalability!** 🚀| **CSV to Database** | 120-180s | ~200 MB | ~80 MB |# Get first page


curl "http://localhost:8080/api/students/report?page=0&size=10"

## Configuration

# Search by student ID

### Environment Variablescurl "http://localhost:8080/api/students/report?studentId=12345"

| Variable | Default | Description |

|----------|---------|-------------|# Filter by class

| `SPRING_DATASOURCE_URL` | `jdbc:postgresql://db:5432/datadb` | Database URL |curl "http://localhost:8080/api/students/report?className=Class1&page=0&size=25"

| `SPRING_DATASOURCE_USERNAME` | `devuser` | DB username |```

| `SPRING_DATASOURCE_PASSWORD` | `devpass` | DB password |

| `APP_FILE_STORAGE` | `/var/log/applications/API/dataprocessing` | Storage path |#### Export Data

| `JAVA_OPTS` | `-Xms512m -Xmx2g` | JVM options |```http

GET /api/students/export/excel?className={class}    # Download Excel

### Application PropertiesGET /api/students/export/csv?className={class}      # Download CSV

Located in `src/main/resources/application.properties`:GET /api/students/export/pdf?className={class}      # Download PDF

```properties```

spring.datasource.url=jdbc:postgresql://localhost:5432/datadb

spring.jpa.hibernate.ddl-auto=update## File Storage

app.file.storage=/var/log/applications/API/dataprocessing

server.port=8080Files are stored at: `/var/log/applications/API/dataprocessing/`

````

**Docker volume mapping:**

## Testing```yaml

volumes:

### Run All Tests - ./data:/var/log/applications/API/dataprocessing

`bash`

./mvnw test

`````**Access files:**

```bash

### Integration Tests# Inside container

Uses Testcontainers for full workflow testing with automatic PostgreSQL container.docker exec dataprocessing-app ls -lh /var/log/applications/API/dataprocessing/



### Manual Testing Workflow# On host machine

```bashls -lh ./data/

# 1. Generate Excel```

curl -X POST "http://localhost:8080/api/data/generate?numberOfRecords=10000"

## Architecture

# 2. List Excel files

curl "http://localhost:8080/api/data/files/excel"```

src/main/java/dev/hamez/dataprocessing/

# 3. Process to CSV (use filename from step 2)├── controller/

curl -X POST "http://localhost:8080/api/data/process?filename=students_20251030123456.xlsx"│   ├── DataController.java          # Data generation & processing

│   └── StudentController.java       # Reports & exports

# 4. List CSV files├── service/

curl "http://localhost:8080/api/data/files/csv"│   ├── DataProcessingService.java   # Core business logic (optimized)

│   └── impl/

# 5. Upload to database (use filename from step 4)│       └── StudentServiceImpl.java  # Report implementation

curl -X POST "http://localhost:8080/api/data/upload?filename=students_20251030123456_processed_20251030124500.csv"├── repository/

│   └── StudentRepository.java       # JPA repository with custom queries

# 6. Query data├── entity/

curl "http://localhost:8080/api/students/report?page=0&size=10"│   └── Student.java                 # Database entity

```├── util/

│   ├── StreamingExcelReader.java   # SAX-based Excel reader (OPTIMIZED)

## Troubleshooting│   └── RandomDataGenerator.java    # Random data generator

└── config/

### OutOfMemoryError    └── WebConfig.java              # CORS configuration

**Symptoms:** `java.lang.OutOfMemoryError: Java heap space````



**Solutions:**## Data Flow & Transformations

1. Verify Docker heap size: `docker exec dataprocessing-app env | grep JAVA_OPTS`

2. Check using streaming methods: Look for "streaming" in logs### Student Record Structure

3. Ensure POI override is set: Look for "POI byte array max override set to 500MB" in logs| Field | Type | Specification |

|-------|------|---------------|

### File Not Found| studentId | Numeric | Incremental from 1 |

**Solutions:**| firstName | String | Random 3-8 characters |

```bash| lastName | String | Random 3-8 characters |

# Check file exists| dob | Date | Random 2000-01-01 to 2010-12-31 |

docker exec dataprocessing-app ls /var/log/applications/API/dataprocessing/| className | String | Random [Class1-5] |

| score | Integer | Random 55-75 |

# Check storage path in logs

docker compose logs app | grep "Using storage path"### Score Transformation Pipeline

```| Stage | Transformation | Example |

|-------|---------------|---------|

### Slow Processing| Excel Generation | Random 55-75 | 60 |

**Expected times for 1M records:**| CSV Processing | +10 | 70 |

- Generation: 30-60 seconds| Database Import | +5 | **75** (Original + 15) |

- Processing: 60-90 seconds

- Upload: 120-180 seconds## Performance Metrics (1M Records)



If slower, check:| Operation | Time | Memory | File Size |

```bash|-----------|------|--------|-----------|

# Monitor resources| **Excel Generation** | 30-60s | ~200 MB | ~30 MB |

docker stats dataprocessing-app| **Excel → CSV** | 60-90s | ~300 MB | ~50 MB |

| **CSV → Database** | 120-180s | ~200 MB | ~80 MB |

# Check for errors

docker compose logs -f app## Configuration

`````

### Environment Variables

## Monitoring| Variable | Default | Description |

|----------|---------|-------------|

### View Logs| `SPRING_DATASOURCE_URL` | `jdbc:postgresql://db:5432/datadb` | Database URL |

```bash| `SPRING_DATASOURCE_USERNAME`|`devuser` | DB username |

docker compose logs -f app| `SPRING_DATASOURCE_PASSWORD` | `devpass` | DB password |

```| `APP_FILE_STORAGE`|`/var/log/applications/API/dataprocessing` | Storage path |

| `JAVA_OPTS` | `-Xms512m -Xmx2g` | JVM options |

### Check Database

````bash### Application Properties

docker exec -it dataprocessing-db psql -U devuser -d datadbLocated in `src/main/resources/application.properties`:

```properties

# Count recordsspring.datasource.url=jdbc:postgresql://localhost:5432/datadb

SELECT COUNT(*) FROM student;spring.jpa.hibernate.ddl-auto=update

app.file.storage=/var/log/applications/API/dataprocessing

# Check score range (should be original + 15)server.port=8080

SELECT MIN(score), MAX(score), AVG(score) FROM student;```

````

## Testing

### Monitor Progress

During large file processing, logs show:### Run All Tests

````bash

INFO ... Starting streaming Excel read..../mvnw test

INFO ... Processed 100000 rows...```

INFO ... Processed 200000 rows...

INFO ... Completed streaming Excel read in 60000 ms### Integration Tests

```Uses Testcontainers for full workflow testing with automatic PostgreSQL container.



## Building### Manual Testing Workflow

```bash

```bash# 1. Generate Excel

# Clean and buildcurl -X POST "http://localhost:8080/api/data/generate?numberOfRecords=10000"

./mvnw clean package

# 2. List Excel files

# Skip testscurl "http://localhost:8080/api/data/files/excel"

./mvnw clean package -DskipTests

# 3. Process to CSV (use filename from step 2)

# Run locallycurl -X POST "http://localhost:8080/api/data/process?filename=students_20251030123456.xlsx"

./mvnw spring-boot:run

```# 4. List CSV files

curl "http://localhost:8080/api/data/files/csv"

## Production Considerations

# 5. Upload to database (use filename from step 4)

This is a demonstration application without authentication.curl -X POST "http://localhost:8080/api/data/upload?filename=students_20251030123456_processed_20251030124500.csv"



### For Production# 6. Query data

- Add Spring Security with JWT/OAuth2curl "http://localhost:8080/api/students/report?page=0&size=10"

- Implement rate limiting```

- Use cloud storage (S3, Azure Blob)

- Add message queue for async processing## Troubleshooting

- Use managed PostgreSQL

- Add comprehensive monitoring (Prometheus, Grafana)### OutOfMemoryError

- Implement audit logging**Symptoms:** `java.lang.OutOfMemoryError: Java heap space`

- Add file upload size limits

**Solutions:**

## Summary1. Verify Docker heap size: `docker exec dataprocessing-app env | grep JAVA_OPTS`

2. Check using streaming methods: Look for "streaming" in logs

This backend efficiently handles **large-scale data processing** with:3. Ensure POI override is set: Look for "POI byte array max override set to 500MB" in logs



- **Streaming Excel generation** - 1M+ records in ~60s### File Not Found

- **Memory-optimized SAX parsing** - 80% less RAM usage**Solutions:**

- **Batch database operations** - Efficient bulk inserts```bash

- **Comprehensive REST APIs** - Full CRUD + reporting# Check file exists

- **Docker containerization** - Easy deploymentdocker exec dataprocessing-app ls /var/log/applications/API/dataprocessing/

- **Production-ready architecture** - Scalable and maintainable

# Check storage path in logs

**Built for performance and scalability!**docker compose logs app | grep "Using storage path"

```

### Slow Processing
**Expected times for 1M records:**
- Generation: 30-60 seconds ✅
- Processing: 60-90 seconds ✅
- Upload: 120-180 seconds ✅

If slower, check:
```bash
# Monitor resources
docker stats dataprocessing-app

# Check for errors
docker compose logs -f app
```

## Monitoring

### View Logs
```bash
docker compose logs -f app
```

### Check Database
```bash
docker exec -it dataprocessing-db psql -U devuser -d datadb

# Count records
SELECT COUNT(*) FROM student;

# Check score range (should be original + 15)
SELECT MIN(score), MAX(score), AVG(score) FROM student;
```

### Monitor Progress
During large file processing, logs show:
```
INFO ... Starting streaming Excel read...
INFO ... Processed 100000 rows...
INFO ... Processed 200000 rows...
INFO ... Completed streaming Excel read in 60000 ms
```

## Building

```bash
# Clean and build
./mvnw clean package

# Skip tests
./mvnw clean package -DskipTests

# Run locally
./mvnw spring-boot:run
```

## Production Considerations

⚠️ **Note**: This is a demonstration application without authentication.

### For Production:
- Add Spring Security with JWT/OAuth2
- Implement rate limiting
- Use cloud storage (S3, Azure Blob)
- Add message queue for async processing
- Use managed PostgreSQL
- Add comprehensive monitoring (Prometheus, Grafana)
- Implement audit logging
- Add file upload size limits

## Summary

This backend efficiently handles **large-scale data processing** with:

✅ **Streaming Excel generation** - 1M+ records in ~60s
✅ **Memory-optimized SAX parsing** - 80% less RAM usage
✅ **Batch database operations** - Efficient bulk inserts
✅ **Comprehensive REST APIs** - Full CRUD + reporting
✅ **Docker containerization** - Easy deployment
✅ **Production-ready architecture** - Scalable and maintainable

**Built for performance and scalability!** 🚀
````
