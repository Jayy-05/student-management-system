package com.project.studentmanagement;

import com.project.studentmanagement.dto.GradeEntryRequest;
import com.project.studentmanagement.dto.StudentReportCardResponse;
import com.project.studentmanagement.entity.Course;
import com.project.studentmanagement.entity.Exam;
import com.project.studentmanagement.entity.Grade;
import com.project.studentmanagement.entity.Student;
import com.project.studentmanagement.repository.ExamRepository;
import com.project.studentmanagement.repository.GradeRepository;
import com.project.studentmanagement.repository.StudentRepository;
import com.project.studentmanagement.service.GradeServiceImpl;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class GradeServiceImplTest {

    @Mock
    private GradeRepository gradeRepository;

    @Mock
    private ExamRepository examRepository;

    @Mock
    private StudentRepository studentRepository;

    @InjectMocks
    private GradeServiceImpl gradeService;

    private Exam exam;
    private Student student;

    @BeforeEach
    void setUp() {
        Course course = new Course();
        course.setId(1L);
        course.setCourseName("Mathematics");

        exam = new Exam();
        exam.setId(1L);
        exam.setTitle("Midterm Exam");
        exam.setMaxMarks(100.0);
        exam.setPassingMarks(40.0);
        exam.setCourse(course);
        exam.setExamDate(LocalDate.now());

        student = new Student();
        student.setId(1L);
        student.setStudentCode("STU101");
        student.setFirstName("Bob");
        student.setLastName("Brown");
        student.setCourse(course);
    }

    @Test
    void recordGrade_AssignsGradeLetterCorrectly() {
        GradeEntryRequest req = new GradeEntryRequest();
        req.setStudentId(1L);
        req.setMarksObtained(85.0);
        req.setRemarks("Good effort");

        when(examRepository.findById(1L)).thenReturn(Optional.of(exam));
        when(studentRepository.findById(1L)).thenReturn(Optional.of(student));
        when(gradeRepository.findByExamIdAndStudentId(1L, 1L)).thenReturn(Optional.empty());
        when(gradeRepository.save(any(Grade.class))).thenAnswer(i -> i.getArgument(0));

        Grade saved = gradeService.recordGrade(1L, req);
        assertNotNull(saved);
        assertEquals(85.0, saved.getMarksObtained());
        assertEquals("A", saved.getGradeLetter());
    }

    @Test
    void getStudentReportCard_CalculatesSummaryAccurately() {
        Grade g1 = new Grade(exam, student, 92.0, "A+", "Excellent");
        when(studentRepository.findById(1L)).thenReturn(Optional.of(student));
        when(gradeRepository.findByStudentId(1L)).thenReturn(List.of(g1));

        StudentReportCardResponse report = gradeService.getStudentReportCard(1L);
        assertNotNull(report);
        assertEquals("STU101", report.getStudentCode());
        assertEquals(1, report.getItems().size());
        assertEquals(92.0, report.getTotalMarksObtained());
        assertEquals(100.0, report.getTotalMaxMarks());
        assertEquals(92.0, report.getOverallPercentage());
        assertEquals("A+", report.getOverallGrade());
        assertEquals("PASS", report.getFinalResult());
    }
}
