package dev.hamez.dataprocessing.service;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.Writer;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;

import org.apache.commons.io.FilenameUtils;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.ss.usermodel.WorkbookFactory;
import org.apache.poi.util.IOUtils;
import org.apache.poi.xssf.streaming.SXSSFWorkbook;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import com.opencsv.CSVWriter;

import dev.hamez.dataprocessing.entity.Student;
import static dev.hamez.dataprocessing.util.RandomDataGenerator.randomAlphaString;
import static dev.hamez.dataprocessing.util.RandomDataGenerator.randomDateBetween;
import static dev.hamez.dataprocessing.util.RandomDataGenerator.randomInt;
import dev.hamez.dataprocessing.util.StreamingExcelReader;
import jakarta.annotation.PostConstruct;

@Service
public class DataProcessingService {

    private final Logger logger = LoggerFactory.getLogger(DataProcessingService.class);

    @Value("${app.file.storage:/var/log/applications/API/dataprocessing}")
    private String storagePath;

    private final DateTimeFormatter tsFormatter = DateTimeFormatter.ofPattern("yyyyMMddHHmmss");

    @PostConstruct
    public void init() throws IOException {
        // Increase POI byte array max override to handle large Excel files (e.g., 1M records)
        // Default is 100MB, we set to 500MB to handle large files
        IOUtils.setByteArrayMaxOverride(500 * 1024 * 1024); // 500 MB

        Path p = Paths.get(storagePath);
        if (!Files.exists(p)) {
            Files.createDirectories(p);
        }
        logger.info("Using storage path: {}", storagePath);
        logger.info("POI byte array max override set to 500MB for large file handling");
    }

    public String generateExcel(int numberOfRecords) throws IOException {
        String timestamp = java.time.LocalDateTime.now().format(tsFormatter);
        String filename = String.format("students_%s.xlsx", timestamp);
        Path out = Paths.get(determineStoragePath(), filename);

        // Use SXSSFWorkbook for streaming - keeps only 100 rows in memory
        try (SXSSFWorkbook wb = new SXSSFWorkbook(100)) {
            var sheet = wb.createSheet("students");
            int r = 0;
            var header = sheet.createRow(r++);
            header.createCell(0).setCellValue("studentId");
            header.createCell(1).setCellValue("firstName");
            header.createCell(2).setCellValue("lastName");
            header.createCell(3).setCellValue("dob");
            header.createCell(4).setCellValue("className");
            header.createCell(5).setCellValue("score");

            LocalDate start = LocalDate.of(2000, 1, 1);
            LocalDate end = LocalDate.of(2010, 12, 31);
            String[] classes = {"Class1", "Class2", "Class3", "Class4", "Class5"};

            logger.info("Starting to generate {} records...", numberOfRecords);
            long startTime = System.currentTimeMillis();

            for (int i = 1; i <= numberOfRecords; i++) {
                var row = sheet.createRow(r++);
                int c = 0;
                row.createCell(c++).setCellValue(i);
                row.createCell(c++).setCellValue(randomAlphaString(3, 8));
                row.createCell(c++).setCellValue(randomAlphaString(3, 8));
                row.createCell(c++).setCellValue(randomDateBetween(start, end).toString());
                row.createCell(c++).setCellValue(classes[randomInt(0, classes.length - 1)]);
                row.createCell(c++).setCellValue(randomInt(55, 75));

                // Log progress every 100k records
                if (i % 100000 == 0) {
                    logger.info("Generated {} records...", i);
                }
            }

            long generationTime = System.currentTimeMillis() - startTime;
            logger.info("Record generation completed in {} ms", generationTime);

            try (OutputStream fos = Files.newOutputStream(out)) {
                wb.write(fos);
            }

            // Clean up temporary files created by SXSSFWorkbook
            wb.dispose();

            long totalTime = System.currentTimeMillis() - startTime;
            logger.info("Generated Excel with {} records in {} ms: {}", numberOfRecords, totalTime, out.toAbsolutePath());
        }
        return out.toString();
    }

    public String processExcel(MultipartFile file) throws Exception {
        // Read Excel, add 10 to score, write CSV
        String baseName = FilenameUtils.getBaseName(file.getOriginalFilename());
        String timestamp = java.time.LocalDateTime.now().format(tsFormatter);
        String outName = String.format("%s/" + baseName + "_processed_%s.csv", determineStoragePath(), timestamp);
        Path outPath = Paths.get(outName);

        try (InputStream is = file.getInputStream(); Workbook wb = WorkbookFactory.create(is); Writer writer = Files.newBufferedWriter(outPath); CSVWriter csv = new CSVWriter(writer)) {
            var sheet = wb.getSheetAt(0);
            boolean headerWritten = false;
            for (var row : sheet) {
                List<String> cols = new ArrayList<>();
                for (Cell cell : row) {
                    switch (cell.getCellType()) {
                        case STRING ->
                            cols.add(cell.getStringCellValue());
                        case NUMERIC ->
                            cols.add(String.valueOf((long) cell.getNumericCellValue()));
                        case BOOLEAN ->
                            cols.add(String.valueOf(cell.getBooleanCellValue()));
                        default ->
                            cols.add("");
                    }
                }
                if (!headerWritten) {
                    csv.writeNext(cols.toArray(String[]::new));
                    headerWritten = true;
                } else {
                    // assume score is last column
                    if (!cols.isEmpty()) {
                        try {
                            int lastIdx = cols.size() - 1;
                            int score = Integer.parseInt(cols.get(lastIdx));
                            score += 10;
                            cols.set(lastIdx, String.valueOf(score));
                        } catch (NumberFormatException ex) {
                            // ignore, leave value
                        }
                    }
                    csv.writeNext(cols.toArray(String[]::new));
                }
            }
        }
        logger.info("Processed Excel to CSV: {}", outPath.toAbsolutePath());
        return outPath.toString();
    }

    public List<Student> readCsvAndApplyOffset(MultipartFile file, int addToScore) throws Exception {
        List<Student> result = new ArrayList<>();
        try (var is = file.getInputStream(); var reader = new InputStreamReader(is); var csv = new com.opencsv.CSVReader(reader)) {
            String[] row;
            boolean first = true;
            while ((row = csv.readNext()) != null) {
                if (first) {
                    first = false;
                    continue;
                }
                if (row.length < 6) {
                    continue;
                }
                Student s = new Student();
                // Do not set studentId - let database auto-generate it
                // try { s.setStudentId(row[0] == null || row[0].isEmpty() ? null : Long.valueOf(row[0])); } catch (NumberFormatException ignored) {}
                s.setFirstName(row[1]);
                s.setLastName(row[2]);
                try {
                    s.setDob(row[3] == null || row[3].isEmpty() ? null : LocalDate.parse(row[3]));
                } catch (Exception ignored) {
                }
                s.setClassName(row[4]);
                try {
                    s.setScore(row[5] == null || row[5].isEmpty() ? null : Integer.parseInt(row[5]) + addToScore);
                } catch (NumberFormatException ignored) {
                    s.setScore(null);
                }
                result.add(s);
            }
        }
        return result;
    }

    private String determineStoragePath() {
        // If on Windows, use C:\var\log... else use configured path
        String os = System.getProperty("os.name").toLowerCase();
        if (os.contains("win")) {
            String win = "C:\\var\\log\\applications\\API\\dataprocessing";
            try {
                Files.createDirectories(Paths.get(win));
            } catch (IOException e) {
                logger.warn("Could not create Windows path {}, falling back to {}", win, storagePath);
                return storagePath;
            }
            return win;
        }
        return storagePath;
    }

    /**
     * List all Excel files in the storage directory
     */
    public List<String> listExcelFiles() throws IOException {
        Path dir = Paths.get(determineStoragePath());
        if (!Files.exists(dir)) {
            Files.createDirectories(dir);
            return List.of();
        }
        return Files.list(dir)
                .filter(p -> p.toString().toLowerCase().endsWith(".xlsx") || p.toString().toLowerCase().endsWith(".xls"))
                .map(p -> p.getFileName().toString())
                .sorted()
                .toList();
    }

    /**
     * List all CSV files in the storage directory
     */
    public List<String> listCsvFiles() throws IOException {
        Path dir = Paths.get(determineStoragePath());
        if (!Files.exists(dir)) {
            Files.createDirectories(dir);
            return List.of();
        }
        return Files.list(dir)
                .filter(p -> p.toString().toLowerCase().endsWith(".csv"))
                .map(p -> p.getFileName().toString())
                .sorted()
                .toList();
    }

    /**
     * Process Excel file from storage directory by filename using STREAMING for
     * memory efficiency
     */
    public String processExcelFromStorage(String filename) throws Exception {
        Path excelPath = Paths.get(determineStoragePath(), filename);
        if (!Files.exists(excelPath)) {
            throw new IOException("File not found: " + filename);
        }

        String baseName = FilenameUtils.getBaseName(filename);
        String timestamp = java.time.LocalDateTime.now().format(tsFormatter);
        String outName = String.format("%s_processed_%s.csv", baseName, timestamp);
        Path outPath = Paths.get(determineStoragePath(), outName);

        logger.info("Starting streaming Excel processing: {} -> {}", filename, outName);
        long startTime = System.currentTimeMillis();

        try (InputStream is = Files.newInputStream(excelPath); Writer writer = Files.newBufferedWriter(outPath); CSVWriter csv = new CSVWriter(writer)) {

            // Use streaming reader to process Excel row by row
            StreamingExcelReader.readExcelStreaming(is, (rowNum, cells) -> {
                if (rowNum == 1) {
                    // Write header row as-is
                    csv.writeNext(cells.toArray(String[]::new));
                } else {
                    // Process data rows: add 10 to score (last column)
                    if (!cells.isEmpty()) {
                        try {
                            int lastIdx = cells.size() - 1;
                            String scoreStr = cells.get(lastIdx);
                            if (scoreStr != null && !scoreStr.isEmpty()) {
                                int score = (int) Double.parseDouble(scoreStr); // Handle potential decimal values
                                cells.set(lastIdx, String.valueOf(score + 10));
                            }
                        } catch (NumberFormatException ex) {
                            // Leave value as-is if not a number
                            logger.debug("Non-numeric score at row {}: {}", rowNum, cells.get(cells.size() - 1));
                        }
                    }
                    csv.writeNext(cells.toArray(String[]::new));
                }

                // Log progress every 100k rows
                if (rowNum % 100000 == 0) {
                    logger.info("Processed {} rows to CSV...", rowNum);
                }
            });
        }

        long duration = System.currentTimeMillis() - startTime;
        logger.info("Completed streaming Excel processing in {} ms: {}", duration, outPath.toAbsolutePath());
        return outPath.getFileName().toString();
    }

    /**
     * Read CSV from storage directory and apply score offset
     */
    public List<Student> readCsvFromStorageAndApplyOffset(String filename, int addToScore) throws Exception {
        Path csvPath = Paths.get(determineStoragePath(), filename);
        if (!Files.exists(csvPath)) {
            throw new IOException("File not found: " + filename);
        }

        List<Student> result = new ArrayList<>();
        try (var reader = Files.newBufferedReader(csvPath); var csv = new com.opencsv.CSVReader(reader)) {
            String[] row;
            boolean first = true;
            while ((row = csv.readNext()) != null) {
                if (first) {
                    first = false;
                    continue;
                }
                if (row.length < 6) {
                    continue;
                }
                Student s = new Student();
                // Do not set studentId - let database auto-generate it
                s.setFirstName(row[1]);
                s.setLastName(row[2]);
                try {
                    s.setDob(row[3] == null || row[3].isEmpty() ? null : LocalDate.parse(row[3]));
                } catch (Exception ignored) {
                }
                s.setClassName(row[4]);
                try {
                    s.setScore(row[5] == null || row[5].isEmpty() ? null : Integer.parseInt(row[5]) + addToScore);
                } catch (NumberFormatException ignored) {
                    s.setScore(null);
                }
                result.add(s);
            }
        }
        logger.info("Read {} students from CSV: {}", result.size(), csvPath.toAbsolutePath());
        return result;
    }
}
