package com.project.studentmanagement;

import com.project.studentmanagement.dto.PageResponse;
import com.project.studentmanagement.dto.StudentRequest;
import com.project.studentmanagement.entity.Course;
import com.project.studentmanagement.entity.Student;
import com.project.studentmanagement.exception.DuplicateResourceException;
import com.project.studentmanagement.exception.ResourceNotFoundException;
import com.project.studentmanagement.repository.CourseRepository;
import com.project.studentmanagement.repository.StudentRepository;
import com.project.studentmanagement.service.StudentServiceImpl;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.*;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class StudentServiceImplTest {

    @Mock
    private StudentRepository studentRepository;

    @Mock
    private CourseRepository courseRepository;

    @InjectMocks
    private StudentServiceImpl studentService;

    private Student student;
    private Course course;
    private StudentRequest request;

    @BeforeEach
    void setUp() {
        course = new Course();
        course.setId(1L);
        course.setCourseName("Computer Science");

        student = new Student();
        student.setId(1L);
        student.setStudentCode("STU001");
        student.setFirstName("John");
        student.setLastName("Doe");
        student.setEmail("john.doe@example.com");
        student.setCourse(course);

        request = new StudentRequest();
        request.setStudentCode("STU001");
        request.setFirstName("John");
        request.setLastName("Doe");
        request.setEmail("john.doe@example.com");
        request.setCourseId(1L);
        request.setDob(LocalDate.of(2002, 1, 1));
    }

    @Test
    void addStudent_Success() {
        when(studentRepository.existsByStudentCode("STU001")).thenReturn(false);
        when(studentRepository.existsByEmailIgnoreCase("john.doe@example.com")).thenReturn(false);
        when(courseRepository.findById(1L)).thenReturn(Optional.of(course));
        when(studentRepository.save(any(Student.class))).thenAnswer(i -> {
            Student s = i.getArgument(0);
            s.setId(1L);
            return s;
        });

        Student saved = studentService.addStudent(request);
        assertNotNull(saved);
        assertEquals("STU001", saved.getStudentCode());
        assertEquals("John", saved.getFirstName());
        verify(studentRepository, times(1)).save(any(Student.class));
    }

    @Test
    void addStudent_DuplicateCode_ThrowsException() {
        when(studentRepository.existsByStudentCode("STU001")).thenReturn(true);
        assertThrows(DuplicateResourceException.class, () -> studentService.addStudent(request));
        verify(studentRepository, never()).save(any());
    }

    @Test
    void getStudentById_NotFound_ThrowsException() {
        when(studentRepository.findById(99L)).thenReturn(Optional.empty());
        assertThrows(ResourceNotFoundException.class, () -> studentService.getStudentById(99L));
    }

    @Test
    void getStudentsPaginated_ReturnsPageResponse() {
        Page<Student> page = new PageImpl<>(List.of(student), PageRequest.of(0, 10), 1);
        when(studentRepository.findByFilter(isNull(), isNull(), any(Pageable.class))).thenReturn(page);

        PageResponse<Student> response = studentService.getStudentsPaginated(null, null, 0, 10, "id", "asc");
        assertNotNull(response);
        assertEquals(1, response.getContent().size());
        assertEquals(1L, response.getTotalElements());
        assertEquals(0, response.getPageNumber());
    }
}
