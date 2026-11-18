package com.project.studentmanagement.repository;

import com.project.studentmanagement.entity.Attendance;
import com.project.studentmanagement.entity.AttendanceStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

public interface AttendanceRepository extends JpaRepository<Attendance, Long> {

    List<Attendance> findByAttendanceDate(LocalDate attendanceDate);

    @Query("SELECT a FROM Attendance a WHERE a.attendanceDate = :date AND a.student.course.id = :courseId")
    List<Attendance> findByAttendanceDateAndCourseId(@Param("date") LocalDate date, @Param("courseId") Long courseId);

    List<Attendance> findByStudentIdOrderByAttendanceDateDesc(Long studentId);

    Optional<Attendance> findByStudentIdAndAttendanceDate(Long studentId, LocalDate attendanceDate);

    long countByStudentId(Long studentId);

    long countByStudentIdAndStatus(Long studentId, AttendanceStatus status);

    long countByAttendanceDate(LocalDate attendanceDate);

    long countByAttendanceDateAndStatus(LocalDate attendanceDate, AttendanceStatus status);
}
