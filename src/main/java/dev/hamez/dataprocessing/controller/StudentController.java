package dev.hamez.dataprocessing.controller;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.nio.charset.StandardCharsets;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.core.io.InputStreamResource;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import dev.hamez.dataprocessing.entity.Student;
import dev.hamez.dataprocessing.service.StudentService;

@RestController
@RequestMapping("/api/students")
public class StudentController {

    private final StudentService studentService;
    private final Logger logger = LoggerFactory.getLogger(StudentController.class);

    public StudentController(StudentService studentService) {
        this.studentService = studentService;
    }

    @GetMapping("/report")
    public ResponseEntity<?> report(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(required = false) Long studentId,
            @RequestParam(required = false) String className
    ) {
        Pageable pageable = PageRequest.of(page, size);
        Page<Student> result = studentService.search(studentId, className, pageable);
        return ResponseEntity.ok(result);
    }

    @GetMapping("/export/excel")
    public ResponseEntity<?> exportExcel(@RequestParam(required = false) String className) {
        try {
            List<Student> students = (className != null && !className.isEmpty())
                    ? studentService.findByClassName(className)
                    : studentService.findAll();
            ByteArrayOutputStream out = new ByteArrayOutputStream();
            studentService.exportToExcel(students, out);
            ByteArrayInputStream in = new ByteArrayInputStream(out.toByteArray());
            InputStreamResource resource = new InputStreamResource(in);
            return ResponseEntity.ok()
                    .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=students.xlsx")
                    .contentType(MediaType.parseMediaType("application/vnd.openxmlformats-officedocument.spreadsheetml.sheet"))
                    .body(resource);
        } catch (IOException e) {
            logger.error("Error exporting excel", e);
            return ResponseEntity.status(500).body(new ErrorResponse("Export failed", e.getMessage()));
        }
    }

    @GetMapping("/export/csv")
    public ResponseEntity<?> exportCsv(@RequestParam(required = false) String className) {
        try {
            List<Student> students = (className != null && !className.isEmpty())
                    ? studentService.findByClassName(className)
                    : studentService.findAll();
            ByteArrayOutputStream out = new ByteArrayOutputStream();
            try (var writer = new OutputStreamWriter(out, StandardCharsets.UTF_8)) {
                studentService.exportToCsv(students, writer);
            }
            InputStreamResource resource = new InputStreamResource(new ByteArrayInputStream(out.toByteArray()));
            return ResponseEntity.ok()
                    .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=students.csv")
                    .contentType(MediaType.TEXT_PLAIN)
                    .body(resource);
        } catch (IOException e) {
            logger.error("Error exporting csv", e);
            return ResponseEntity.status(500).body(new ErrorResponse("Export failed", e.getMessage()));
        }
    }

    @GetMapping("/export/pdf")
    public ResponseEntity<?> exportPdf(@RequestParam(required = false) String className) {
        try {
            List<Student> students = (className != null && !className.isEmpty())
                    ? studentService.findByClassName(className)
                    : studentService.findAll();
            ByteArrayOutputStream out = new ByteArrayOutputStream();
            studentService.exportToPdf(students, out);
            InputStreamResource resource = new InputStreamResource(new ByteArrayInputStream(out.toByteArray()));
            return ResponseEntity.ok()
                    .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=students.pdf")
                    .contentType(MediaType.APPLICATION_PDF)
                    .body(resource);
        } catch (IOException e) {
            logger.error("Error exporting pdf", e);
            return ResponseEntity.status(500).body(new ErrorResponse("Export failed", e.getMessage()));
        }
    }

    static class ErrorResponse {

        public String error;
        public String detail;

        public ErrorResponse(String error, String detail) {
            this.error = error;
            this.detail = detail;
        }
    }
}
