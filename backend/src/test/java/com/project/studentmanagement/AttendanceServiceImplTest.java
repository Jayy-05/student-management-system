package com.project.studentmanagement;

import com.project.studentmanagement.dto.AttendanceRecordRequest;
import com.project.studentmanagement.dto.AttendanceStatsResponse;
import com.project.studentmanagement.entity.Attendance;
import com.project.studentmanagement.entity.AttendanceStatus;
import com.project.studentmanagement.entity.Student;
import com.project.studentmanagement.repository.AttendanceRepository;
import com.project.studentmanagement.repository.StudentRepository;
import com.project.studentmanagement.service.AttendanceServiceImpl;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDate;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class AttendanceServiceImplTest {

    @Mock
    private AttendanceRepository attendanceRepository;

    @Mock
    private StudentRepository studentRepository;

    @InjectMocks
    private AttendanceServiceImpl attendanceService;

    private Student student;

    @BeforeEach
    void setUp() {
        student = new Student();
        student.setId(1L);
        student.setStudentCode("STU001");
        student.setFirstName("Alice");
        student.setLastName("Smith");
    }

    @Test
    void markAttendance_NewRecord_Success() {
        LocalDate today = LocalDate.now();
        AttendanceRecordRequest req = new AttendanceRecordRequest();
        req.setStudentId(1L);
        req.setStatus(AttendanceStatus.PRESENT);
        req.setRemarks("On time");

        when(studentRepository.findById(1L)).thenReturn(Optional.of(student));
        when(attendanceRepository.findByStudentIdAndAttendanceDate(1L, today)).thenReturn(Optional.empty());
        when(attendanceRepository.save(any(Attendance.class))).thenAnswer(i -> i.getArgument(0));

        Attendance result = attendanceService.markAttendance(today, req);
        assertNotNull(result);
        assertEquals(AttendanceStatus.PRESENT, result.getStatus());
        assertEquals("On time", result.getRemarks());
        verify(attendanceRepository, times(1)).save(any(Attendance.class));
    }

    @Test
    void getStudentAttendanceStats_CalculatesPercentageCorrectly() {
        when(studentRepository.findById(1L)).thenReturn(Optional.of(student));
        when(attendanceRepository.countByStudentId(1L)).thenReturn(10L);
        when(attendanceRepository.countByStudentIdAndStatus(1L, AttendanceStatus.PRESENT)).thenReturn(8L);
        when(attendanceRepository.countByStudentIdAndStatus(1L, AttendanceStatus.ABSENT)).thenReturn(1L);
        when(attendanceRepository.countByStudentIdAndStatus(1L, AttendanceStatus.LATE)).thenReturn(1L);
        when(attendanceRepository.countByStudentIdAndStatus(1L, AttendanceStatus.EXCUSED)).thenReturn(0L);

        AttendanceStatsResponse stats = attendanceService.getStudentAttendanceStats(1L);
        assertNotNull(stats);
        assertEquals(10L, stats.getTotalDays());
        assertEquals(8L, stats.getPresentDays());
        // (8 + 1) / 10 = 90.0%
        assertEquals(90.0, stats.getAttendancePercentage());
    }
}
