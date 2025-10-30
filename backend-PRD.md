# Backend PRD — Data Processing API

Version: 1.0

## Overview

This service provides endpoints to generate large Excel spreadsheets of synthetic student records, process Excel -> CSV (with score adjustments), import CSV into PostgreSQL, and reporting endpoints for persisted students. It is implemented in Java 17 + Spring Boot 3.4.5.

## Goals

- Support generation of large Excel files (target test: 1,000,000 rows) and store them on-disk in a deterministic location.
- Provide upload endpoints to process Excel -> CSV (adding +10 to scores) and CSV -> DB (adding +5 to scores) with robust streaming and batching.
- Provide paginated reporting with search and filter and export capabilities (Excel, CSV, PDF).
- No authentication required.

## Non-functional

- Performance: Support generating 1M rows within reasonable time (minutes) using streaming APIs (Apache POI SXSSF) and low memory footprint.
- Disk location: write files under OS-appropriate runtime locations:
  - Windows: `C:\\var\\log\\applications\\API\\dataprocessing\\<filename>`
  - Linux/macOS: `/var/log/applications/API/dataprocessing/<filename>` (create directories if missing)
- Memory: do not hold whole Excel/CSV in memory. Stream rows and use batching for DB writes.
- DB: PostgreSQL (configured via `application.properties`). Use JPA with batch inserts or JdbcTemplate batching.

## Data Model

Student entity (DB)

- student_id: long (PK)
- first_name: varchar
- last_name: varchar
- dob: date
- class_name: varchar
- score: integer

## APIs

1. Generate Excel

    - POST /api/generate
    - Request: JSON { "records": 1000000, "filename": "students.xlsx" } — `filename` optional
    - Response: 202 Accepted with job id and path where file will be written once done. Optionally return direct download link when finished.
    - Behavior: generate synthetic student rows with fields described in the spec and write to disk streamingly (SXSSF). Use incremental studentId starting at 1.

2. Upload Excel for processing -> CSV

    - POST /api/process/excel-to-csv
    - Multipart form: file (Excel)
    - Response: 200 OK with CSV file path or 202 if processed asynchronously.
    - Processing: stream-read the Excel file, for each row add +10 to score, write to CSV stream to disk.

3. Upload CSV to import to DB

    - POST /api/import/csv-to-db
    - Multipart form: file (CSV)
    - Response: 200 OK with record count imported
    - Processing: stream CSV, for each row add +5 to score (relative to Excel value) and perform batch insert to DB (e.g., batch size 1000).

4. Reporting

    - GET /api/students

    - Query params: page, size, sort, className (optional), studentId (optional)
    - Returns paged response: total, page, size, data[]

    - GET /api/students/export
    - Query params: format=csv|xlsx|pdf, className(optional), studentId(optional)
    - Streams export file back to client.

## Error handling

- Validate input sizes and return 400 for invalid values.
- For long-running generation, return 202 with job id and provide a GET /api/jobs/{id} to check status.

## Implementation notes

- Excel generation: use Apache POI SXSSF to stream; write directly to target file path.
- Excel reading: use POI event or streaming (XSSF) to avoid memory spikes.
- CSV writing: use OpenCSV or plain BufferedWriter with proper escaping.
- PDF export: use Apache PDFBox to create a simple table view (for large exports, limit to page size or generate multi-page PDFs).
- DB: configure `spring.jpa.properties.hibernate.jdbc.batch_size=1000` and use saveAll in batches or JdbcTemplate batchUpdate.
- Logging: write progress logs; ensure exception stack traces are recorded.

## Acceptance criteria

- Able to generate an Excel file at the configured path with N rows where N = input (1,000,000 tested).
- Able to upload an Excel file and produce CSV with score increased by 10.
- Able to upload CSV and import into PostgreSQL with score increased by 5 compared to Excel base.
- Reporting endpoints return correct filtering, search and export outputs.

## Next steps

- Implement controllers, services and repository classes.
- Add integration tests (Testcontainers Postgres) for import and report flows.
