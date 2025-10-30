package dev.hamez.dataprocessing.controller;

import java.io.IOException;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import dev.hamez.dataprocessing.entity.Student;
import dev.hamez.dataprocessing.service.DataProcessingService;
import dev.hamez.dataprocessing.service.StudentService;

@RestController
@RequestMapping("/api/data")
public class DataController {

    private final DataProcessingService dataService;
    private final StudentService studentService;
    private final Logger logger = LoggerFactory.getLogger(DataController.class);

    public DataController(DataProcessingService dataService, StudentService studentService) {
        this.dataService = dataService;
        this.studentService = studentService;
    }

    @PostMapping("/generate")
    public ResponseEntity<?> generate(@RequestParam int numberOfRecords) {
        try {
            String path = dataService.generateExcel(numberOfRecords);
            return ResponseEntity.ok().body(new ApiResponse(true, "Excel generated", path));
        } catch (IOException e) {
            logger.error("Error generating excel", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(new ApiResponse(false, e.getMessage(), null));
        }
    }

    // List Excel files in the storage directory
    @GetMapping("/files/excel")
    public ResponseEntity<?> listExcelFiles() {
        try {
            List<String> files = dataService.listExcelFiles();
            return ResponseEntity.ok().body(new ApiResponse(true, "Excel files listed", files));
        } catch (IOException e) {
            logger.error("Error listing excel files", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(new ApiResponse(false, e.getMessage(), null));
        }
    }

    // List CSV files in the storage directory
    @GetMapping("/files/csv")
    public ResponseEntity<?> listCsvFiles() {
        try {
            List<String> files = dataService.listCsvFiles();
            return ResponseEntity.ok().body(new ApiResponse(true, "CSV files listed", files));
        } catch (IOException e) {
            logger.error("Error listing csv files", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(new ApiResponse(false, e.getMessage(), null));
        }
    }

    // Process Excel file from the storage directory by filename
    @PostMapping("/process")
    public ResponseEntity<?> process(@RequestParam("filename") String filename) {
        try {
            String csvPath = dataService.processExcelFromStorage(filename);
            return ResponseEntity.ok().body(new ApiResponse(true, "Processed to CSV", csvPath));
        } catch (Exception e) {
            logger.error("Error processing excel", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(new ApiResponse(false, e.getMessage(), null));
        }
    }

    // Upload CSV file from storage directory to database by filename
    @PostMapping("/upload")
    public ResponseEntity<?> uploadCsv(@RequestParam("filename") String filename) {
        try {
            List<Student> students = dataService.readCsvFromStorageAndApplyOffset(filename, 5);
            var saved = studentService.saveAll(students);
            return ResponseEntity.ok().body(new ApiResponse(true, "Uploaded to DB", saved.size()));
        } catch (Exception e) {
            logger.error("Error uploading csv", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(new ApiResponse(false, e.getMessage(), null));
        }
    }

    // Simple API response wrapper
    public static class ApiResponse {

        public boolean success;
        public String message;
        public Object data;

        public ApiResponse(boolean success, String message, Object data) {
            this.success = success;
            this.message = message;
            this.data = data;
        }
    }
}
