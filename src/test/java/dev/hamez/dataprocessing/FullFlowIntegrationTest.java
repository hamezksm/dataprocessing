package dev.hamez.dataprocessing;

import java.io.File;
import java.io.FileInputStream;
import java.nio.file.Files;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.mock.web.MockMultipartFile;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

import dev.hamez.dataprocessing.entity.Student;
import dev.hamez.dataprocessing.service.DataProcessingService;
import dev.hamez.dataprocessing.service.StudentService;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@Testcontainers
public class FullFlowIntegrationTest {

    @Container
    public static PostgreSQLContainer<?> postgres = new PostgreSQLContainer<>("postgres:15")
            .withDatabaseName("datadb")
            .withUsername("devuser")
            .withPassword("devpass");

    @Autowired
    DataProcessingService dataProcessingService;

    @Autowired
    StudentService studentService;

    @Test
    public void fullFlow_generate_process_upload_and_verify() throws Exception {
        // generate Excel with 5 records
        String excelPath = dataProcessingService.generateExcel(5);
        assertThat(Files.exists(new File(excelPath).toPath())).isTrue();

        // create MultipartFile from generated Excel
        try (FileInputStream fis = new FileInputStream(excelPath)) {
            MockMultipartFile excelMultipart = new MockMultipartFile("file", new File(excelPath).getName(), "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet", fis);

            // process Excel -> CSV
            String csvPath = dataProcessingService.processExcel(excelMultipart);
            assertThat(Files.exists(new File(csvPath).toPath())).isTrue();

            // upload CSV and parse into Student objects
            try (FileInputStream csvFis = new FileInputStream(csvPath)) {
                MockMultipartFile csvMultipart = new MockMultipartFile("file", new File(csvPath).getName(), "text/csv", csvFis);
                List<Student> students = dataProcessingService.readCsvAndApplyOffset(csvMultipart, 5);
                // persist
                List<Student> saved = studentService.saveAll(students);
                assertThat(saved).isNotEmpty();
                assertThat(saved.size()).isEqualTo(students.size());
            }
        }
    }
}
