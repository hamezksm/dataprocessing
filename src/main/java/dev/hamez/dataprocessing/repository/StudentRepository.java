package dev.hamez.dataprocessing.repository;

import java.util.List;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import dev.hamez.dataprocessing.entity.Student;

public interface StudentRepository extends JpaRepository<Student, Long> {

    @Query("select s from Student s where (:studentId is null or s.studentId = :studentId) and (:className is null or s.className = :className)")
    Page<Student> search(@Param("studentId") Long studentId, @Param("className") String className, Pageable pageable);

    @Query("select s from Student s where (:className is null or s.className = :className)")
    List<Student> findByClassName(@Param("className") String className);
}
