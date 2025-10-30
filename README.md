# Data Processing Backend Application

A high-performance Spring Boot application designed to handle large-scale student data processing with optimized memory management for generating, processing, and managing files with 1,000,000+ records.

## Overview

This backend application provides REST APIs for:

- **Generating large Excel files** (1M+ records) using streaming
- **Processing Excel to CSV** with memory-efficient SAX parsing
- **Importing CSV to PostgreSQL** with batch operations
- **Paginated reporting** with search and filtering
- **Multi-format exports** (Excel, CSV, PDF)

## Key Performance Optimizations

### 1. Streaming Excel Generation

Uses `SXSSFWorkbook` to generate massive Excel files without loading everything into memory.

- **Keeps only 100 rows in memory** at a time
- Older rows automatically flushed to disk
- Can generate **1M records in ~33 seconds** using ~200MB RAM

### 2. Streaming Excel Processing (SAX Parser)

Custom `StreamingExcelReader` utility using event-based SAX parsing.

**Why this matters:**

- **Before**: Loading 30MB Excel uses 1.5-2GB RAM and causes OutOfMemoryError
- **After**: SAX streaming uses 200-300MB RAM and processes 1M records in ~33 seconds

**Performance gains:**

- 80% reduction in memory usage
- 50% faster processing
- No file size limits

### 3. Java Heap Optimization

Docker container configured with:

```dockerfile
ENV JAVA_OPTS="-Xms512m -Xmx2g"
```

- Initial heap: 512 MB
- Maximum heap: 2 GB

### 4. POI Byte Array Limit

Increased Apache POI's internal byte array limit:

```java
IOUtils.setByteArrayMaxOverride(500 * 1024 * 1024); // 500 MB
```

## Technology Stack

- **Java**: 17
- **Spring Boot**: 3.4.5
- **Database**: PostgreSQL 15
- **Excel**: Apache POI 5.2.3 (with streaming)
- **CSV**: OpenCSV 5.7.1
- **PDF**: Apache PDFBox 2.0.27
- **Build**: Maven 3.9.4
- **Container**: Docker & Docker Compose

## Quick Start

### Prerequisites

- Docker & Docker Compose installed
- (Optional) Java 17+ and Maven for local development

### Start with Docker (Recommended)

1. **Build and start all services**

```bash
docker compose up -d --build
```

2. **Verify services are running**

```bash
docker ps
docker compose logs -f app
```

3. **Test the API**

```bash
curl http://localhost:8080/api/data/files/excel
```

The backend will be available at **http://localhost:8080**

### Local Development (without Docker)

1. **Start PostgreSQL**

```bash
docker run -d --name postgres-dev \\
  -e POSTGRES_DB=datadb \\
  -e POSTGRES_USER=devuser \\
  -e POSTGRES_PASSWORD=devpass \\
  -p 5432:5432 \\
  postgres:15-alpine
```

2. **Build and run**

```bash
./mvnw clean package -DskipTests
java -Xmx2g -jar target/dataprocessing-0.0.1-SNAPSHOT.jar
```

## API Endpoints

### Data Processing

#### Generate Excel

```http
POST /api/data/generate?numberOfRecords={n}
```

**Example:**

```bash
curl -X POST "http://localhost:8080/api/data/generate?numberOfRecords=1000000"
```

#### List Files

```http
GET /api/data/files/excel    # List Excel files
GET /api/data/files/csv      # List CSV files
```

#### Process Excel to CSV

```http
POST /api/data/process?filename={name}
```

**Transformation**: Adds 10 to each score

```bash
curl -X POST "http://localhost:8080/api/data/process?filename=students_20251030123456.xlsx"
```

#### Upload CSV to Database

```http
POST /api/data/upload?filename={name}
```

**Transformation**: Adds 5 to each score (total +15 from original)

```bash
curl -X POST "http://localhost:8080/api/data/upload?filename=students_20251030123456_processed_20251030124500.csv"
```

### Student Reports

#### Get Paginated Report

```http
GET /api/students/report?page={p}&size={s}&studentId={id}&className={class}
```

**Examples:**

```bash
# Get first page
curl "http://localhost:8080/api/students/report?page=0&size=10"

# Search by student ID
curl "http://localhost:8080/api/students/report?studentId=12345"

# Filter by class
curl "http://localhost:8080/api/students/report?className=Class1&page=0&size=25"
```

#### Export Data

```http
GET /api/students/export/excel?className={class}    # Download Excel
GET /api/students/export/csv?className={class}      # Download CSV
GET /api/students/export/pdf?className={class}      # Download PDF
```

## File Storage

Files are stored at: `/var/log/applications/API/dataprocessing/`

**Docker volume mapping:**

```yaml
volumes:
  - ./data:/var/log/applications/API/dataprocessing
```

**Access files:**

```bash
# Inside container
docker exec dataprocessing-app ls -lh /var/log/applications/API/dataprocessing/

# On host machine
ls -lh ./data/
```

## Architecture

```text
src/main/java/dev/hamez/dataprocessing/
├── controller/
│   ├── DataController.java          # Data generation & processing
│   └── StudentController.java       # Reports & exports
├── service/
│   ├── DataProcessingService.java   # Core business logic (optimized)
│   └── impl/
│       └── StudentServiceImpl.java  # Report implementation
├── repository/
│   └── StudentRepository.java       # JPA repository with custom queries
├── entity/
│   └── Student.java                 # Database entity
├── util/
│   ├── StreamingExcelReader.java   # SAX-based Excel reader (OPTIMIZED)
│   └── RandomDataGenerator.java    # Random data generator
└── config/
    └── WebConfig.java              # CORS configuration
```

## Data Flow & Transformations

### Student Record Structure

| Field | Type | Specification |
|-------|------|---------------|
| studentId | Numeric | Incremental from 1 |
| firstName | String | Random 3-8 characters |
| lastName | String | Random 3-8 characters |
| dob | Date | Random 2000-01-01 to 2010-12-31 |
| className | String | Random [Class1-5] |
| score | Integer | Random 55-75 |

### Score Transformation Pipeline

| Stage | Transformation | Example |
|-------|---------------|---------|
| Excel Generation | Random 55-75 | 60 |
| CSV Processing | +10 | 70 |
| Database Import | +5 | **75** (Original + 15) |

## Verified Performance Metrics

**Tested on 1,000,000 records:**

| Operation | Seconds | Minutes | Memory | File Size |
|-----------|---------|---------|--------|-----------|
| **Excel Generation** | 33s | 0.55 min | ~200 MB | ~30 MB |
| **Excel to CSV Processing** | 33s | 0.55 min | ~300 MB | ~50 MB |
| **CSV to Database Upload** | 277s | 4.62 min | ~200 MB | ~80 MB |
| **Total End-to-End Pipeline** | **343s** | **5.72 min** | - | - |

### Summary

- **Data Generation (Excel)**: ~0.55 minutes
- **Data Processing (Excel to CSV)**: ~0.55 minutes
- **Data Upload (CSV to DB)**: ~4.62 minutes
- **Complete Pipeline**: **~5.72 minutes total**

*Note: Timings measured on macOS with Docker Desktop. Actual performance may vary based on hardware.*

## Configuration

### Environment Variables

| Variable | Default | Description |
|----------|---------|-------------|
| `SPRING_DATASOURCE_URL` | `jdbc:postgresql://db:5432/datadb` | Database URL |
| `SPRING_DATASOURCE_USERNAME` | `devuser` | DB username |
| `SPRING_DATASOURCE_PASSWORD` | `devpass` | DB password |
| `APP_FILE_STORAGE` | `/var/log/applications/API/dataprocessing` | Storage path |
| `JAVA_OPTS` | `-Xms512m -Xmx2g` | JVM options |

### Application Properties

Located in `src/main/resources/application.properties`:

```properties
spring.datasource.url=jdbc:postgresql://localhost:5432/datadb
spring.jpa.hibernate.ddl-auto=update
app.file.storage=/var/log/applications/API/dataprocessing
server.port=8080
```

## Testing

### Run All Tests

```bash
./mvnw test
```

### Integration Tests

Uses Testcontainers for full workflow testing with automatic PostgreSQL container.

### Manual Testing Workflow

```bash
# 1. Generate Excel
curl -X POST "http://localhost:8080/api/data/generate?numberOfRecords=10000"

# 2. List Excel files
curl "http://localhost:8080/api/data/files/excel"

# 3. Process to CSV (use filename from step 2)
curl -X POST "http://localhost:8080/api/data/process?filename=students_20251030123456.xlsx"

# 4. List CSV files
curl "http://localhost:8080/api/data/files/csv"

# 5. Upload to database (use filename from step 4)
curl -X POST "http://localhost:8080/api/data/upload?filename=students_20251030123456_processed_20251030124500.csv"

# 6. Query data
curl "http://localhost:8080/api/students/report?page=0&size=10"
```

## Troubleshooting

### OutOfMemoryError

**Symptoms:** `java.lang.OutOfMemoryError: Java heap space`

**Solutions:**

1. Verify Docker heap size: `docker exec dataprocessing-app env | grep JAVA_OPTS`
2. Check using streaming methods: Look for "streaming" in logs
3. Ensure POI override is set: Look for "POI byte array max override set to 500MB" in logs

### File Not Found

**Solutions:**

```bash
# Check file exists
docker exec dataprocessing-app ls /var/log/applications/API/dataprocessing/

# Check storage path in logs
docker compose logs app | grep "Using storage path"
```

### Slow Processing

**Expected times for 1,000,000 records:**

- Generation: ~33 seconds (0.55 minutes)
- Processing: ~33 seconds (0.55 minutes)
- Upload: ~277 seconds (4.62 minutes)

If significantly slower, check:

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

```text
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

**Note**: This is a demonstration application without authentication.

### For Production

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

- **Streaming Excel generation** - 1M records in 0.55 minutes
- **Memory-optimized SAX parsing** - 80% less RAM usage
- **Batch database operations** - Efficient bulk inserts
- **Comprehensive REST APIs** - Full CRUD + reporting
- **Docker containerization** - Easy deployment
- **Production-ready architecture** - Scalable and maintainable

**Built for performance and scalability!**
