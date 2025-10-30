package dev.hamez.dataprocessing.service.impl;

import java.io.IOException;
import java.io.OutputStream;
import java.io.Writer;
import java.time.format.DateTimeFormatter;
import java.util.List;

import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.pdmodel.PDPage;
import org.apache.pdfbox.pdmodel.PDPageContentStream;
import org.apache.pdfbox.pdmodel.common.PDRectangle;
import org.apache.pdfbox.pdmodel.font.PDType1Font;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.xssf.usermodel.XSSFSheet;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.opencsv.CSVWriter;

import dev.hamez.dataprocessing.entity.Student;
import dev.hamez.dataprocessing.repository.StudentRepository;
import dev.hamez.dataprocessing.service.StudentService;

@Service
public class StudentServiceImpl implements StudentService {

    private final StudentRepository repository;

    public StudentServiceImpl(StudentRepository repository) {
        this.repository = repository;
    }

    @Override
    public Page<Student> search(Long studentId, String className, Pageable pageable) {
        return repository.search(studentId, className, pageable);
    }

    @Override
    public Student save(Student s) {
        return repository.save(s);
    }

    @Override
    public List<Student> saveAll(List<Student> students) {
        return repository.saveAll(students);
    }

    @Override
    public List<Student> findAll() {
        return repository.findAll();
    }

    @Override
    public List<Student> findByClassName(String className) {
        return repository.findByClassName(className);
    }

    @Override
    @Transactional(readOnly = true)
    public void exportToExcel(List<Student> students, OutputStream out) throws IOException {
        try (XSSFWorkbook wb = new XSSFWorkbook()) {
            XSSFSheet sheet = wb.createSheet("students");
            int rownum = 0;
            Row header = sheet.createRow(rownum++);
            header.createCell(0).setCellValue("studentId");
            header.createCell(1).setCellValue("firstName");
            header.createCell(2).setCellValue("lastName");
            header.createCell(3).setCellValue("dob");
            header.createCell(4).setCellValue("className");
            header.createCell(5).setCellValue("score");

            DateTimeFormatter df = DateTimeFormatter.ISO_DATE;
            for (Student s : students) {
                Row r = sheet.createRow(rownum++);
                int c = 0;
                Cell cell = r.createCell(c++);
                if (s.getStudentId() != null) {
                    cell.setCellValue(s.getStudentId());
                }
                r.createCell(c++).setCellValue(s.getFirstName() != null ? s.getFirstName() : "");
                r.createCell(c++).setCellValue(s.getLastName() != null ? s.getLastName() : "");
                r.createCell(c++).setCellValue(s.getDob() != null ? s.getDob().format(df) : "");
                r.createCell(c++).setCellValue(s.getClassName() != null ? s.getClassName() : "");
                r.createCell(c++).setCellValue(s.getScore() != null ? s.getScore().doubleValue() : 0.0);
            }
            wb.write(out);
        }
    }

    @Override
    public void exportToCsv(List<Student> students, Writer writer) throws IOException {
        try (CSVWriter csv = new CSVWriter(writer)) {
            String[] header = {"studentId", "firstName", "lastName", "dob", "className", "score"};
            csv.writeNext(header);
            for (Student s : students) {
                csv.writeNext(new String[]{
                    s.getStudentId() == null ? "" : String.valueOf(s.getStudentId()),
                    s.getFirstName() == null ? "" : s.getFirstName(),
                    s.getLastName() == null ? "" : s.getLastName(),
                    s.getDob() == null ? "" : s.getDob().toString(),
                    s.getClassName() == null ? "" : s.getClassName(),
                    s.getScore() == null ? "" : String.valueOf(s.getScore())
                });
            }
        }
    }

    @Override
    public void exportToPdf(List<Student> students, OutputStream out) throws IOException {
        // Improved PDF export: render a simple table with pagination using PDFBox.
        try (PDDocument doc = new PDDocument()) {
            final PDType1Font headerFont = PDType1Font.HELVETICA_BOLD;
            final PDType1Font rowFont = PDType1Font.HELVETICA;
            final float fontSize = 10f;
            final float leading = 1.2f * fontSize;
            final float margin = 50f;

            float pageHeight = PDRectangle.LETTER.getHeight();
            float usableHeight = pageHeight - 2 * margin;
            float headerHeight = leading * 1.5f;
            int rowsPerPage = Math.max(1, (int) ((usableHeight - headerHeight) / leading));

            float[] colWidths = new float[]{60f, 90f, 90f, 80f, 80f, 50f};

            // iterate in pages
            for (int idx = 0; idx < students.size(); idx += rowsPerPage) {
                int to = Math.min(students.size(), idx + rowsPerPage);
                PDPage page = new PDPage(PDRectangle.LETTER);
                doc.addPage(page);
                try (PDPageContentStream cs = new PDPageContentStream(doc, page)) {
                    float startX = margin;
                    float y = page.getMediaBox().getHeight() - margin;

                    // header
                    cs.setFont(headerFont, fontSize);
                    cs.beginText();
                    cs.newLineAtOffset(startX, y);
                    String[] headers = new String[]{"ID", "First", "Last", "DOB", "Class", "Score"};
                    for (int i = 0; i < headers.length; i++) {
                        String h = fitText(headerFont, fontSize, headers[i], colWidths[i]);
                        cs.showText(h);
                        cs.newLineAtOffset(colWidths[i], 0);
                    }
                    cs.endText();
                    y -= headerHeight;

                    cs.setFont(rowFont, fontSize);
                    for (int r = idx; r < to; r++) {
                        Student s = students.get(r);
                        cs.beginText();
                        cs.newLineAtOffset(startX, y);
                        String[] cols = new String[]{
                            s.getStudentId() == null ? "" : String.valueOf(s.getStudentId()),
                            s.getFirstName() == null ? "" : s.getFirstName(),
                            s.getLastName() == null ? "" : s.getLastName(),
                            s.getDob() == null ? "" : s.getDob().toString(),
                            s.getClassName() == null ? "" : s.getClassName(),
                            s.getScore() == null ? "" : String.valueOf(s.getScore())
                        };
                        for (int i = 0; i < cols.length; i++) {
                            String cellText = fitText(rowFont, fontSize, cols[i], colWidths[i]);
                            cs.showText(cellText);
                            cs.newLineAtOffset(colWidths[i], 0);
                        }
                        cs.endText();
                        y -= leading;
                    }
                }
            }
            doc.save(out);
        }
    }

    /**
     * Fit text into the available width (points) using the provided font and
     * size. If the text is too long, truncate and append ellipsis.
     */
    private static String fitText(org.apache.pdfbox.pdmodel.font.PDFont font, float fontSize, String text, float maxWidth) {
        if (text == null) {
            return "";
        }
        try {
            float w = font.getStringWidth(text) / 1000f * fontSize;
            if (w <= maxWidth) {
                return text;
            }
            String ell = "...";
            int len = text.length();
            // binary search for best length
            int low = 0, high = len - 1, best = 0;
            while (low <= high) {
                int mid = (low + high) >>> 1;
                String cand = text.substring(0, mid) + ell;
                float cw = font.getStringWidth(cand) / 1000f * fontSize;
                if (cw <= maxWidth) {
                    best = mid;
                    low = mid + 1;
                } else {
                    high = mid - 1;
                }
            }
            if (best <= 0) {
                return ell;
            }
            return text.substring(0, best) + ell;
        } catch (java.io.IOException e) {
            // fallback: simple character-based truncation
            int maxChars = Math.max(1, (int) (maxWidth / (fontSize * 0.5f)));
            if (text.length() <= maxChars) {
                return text;
            }
            if (maxChars <= 3) {
                return "...";
            }
            return text.substring(0, maxChars - 3) + "...";
        }
    }
}
