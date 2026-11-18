package com.project.studentmanagement.repository;

import com.project.studentmanagement.entity.Grade;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface GradeRepository extends JpaRepository<Grade, Long> {

    List<Grade> findByExamId(Long examId);

    List<Grade> findByStudentId(Long studentId);

    Optional<Grade> findByExamIdAndStudentId(Long examId, Long studentId);

    long countByMarksObtainedGreaterThanEqual(Double marks);
}
