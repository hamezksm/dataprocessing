package dev.hamez.dataprocessing.service;

import java.io.IOException;
import java.util.List;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import dev.hamez.dataprocessing.entity.Student;

public interface StudentService {

    Page<Student> search(Long studentId, String className, Pageable pageable);

    Student save(Student s);

    List<Student> saveAll(List<Student> students);

    List<Student> findAll();

    List<Student> findByClassName(String className);

    void exportToExcel(List<Student> students, java.io.OutputStream out) throws IOException;

    void exportToCsv(List<Student> students, java.io.Writer writer) throws IOException;

    void exportToPdf(List<Student> students, java.io.OutputStream out) throws IOException;
}
