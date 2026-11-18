package com.project.studentmanagement.repository;

import com.project.studentmanagement.entity.Student;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface StudentRepository extends JpaRepository<Student, Long> {

    boolean existsByStudentCode(String studentCode);

    boolean existsByEmailIgnoreCase(String email);

    boolean existsByStudentCodeAndIdNot(String studentCode, Long id);

    boolean existsByEmailIgnoreCaseAndIdNot(String email, Long id);

    boolean existsByCourseId(Long courseId);

    long countByCourseId(Long courseId);

    List<Student> findByCourseId(Long courseId);

    /** Search by student code, first name, last name or "first last". */
    @Query("""
            SELECT s FROM Student s
            WHERE LOWER(s.studentCode) LIKE LOWER(CONCAT('%', :q, '%'))
               OR LOWER(s.firstName)   LIKE LOWER(CONCAT('%', :q, '%'))
               OR LOWER(s.lastName)    LIKE LOWER(CONCAT('%', :q, '%'))
               OR LOWER(CONCAT(s.firstName, ' ', s.lastName)) LIKE LOWER(CONCAT('%', :q, '%'))
            ORDER BY s.firstName, s.lastName
            """)
    List<Student> search(@Param("q") String q);

    @Query("""
            SELECT s FROM Student s
            WHERE (:courseId IS NULL OR s.course.id = :courseId)
              AND (:q IS NULL OR :q = ''
                   OR LOWER(s.studentCode) LIKE LOWER(CONCAT('%', :q, '%'))
                   OR LOWER(s.firstName)   LIKE LOWER(CONCAT('%', :q, '%'))
                   OR LOWER(s.lastName)    LIKE LOWER(CONCAT('%', :q, '%'))
                   OR LOWER(CONCAT(s.firstName, ' ', s.lastName)) LIKE LOWER(CONCAT('%', :q, '%')))
            """)
    Page<Student> findByFilter(@Param("q") String q, @Param("courseId") Long courseId, Pageable pageable);
}
