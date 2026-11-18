package com.project.studentmanagement.service;

import com.project.studentmanagement.dto.PageResponse;
import com.project.studentmanagement.dto.StudentRequest;
import com.project.studentmanagement.entity.Course;
import com.project.studentmanagement.entity.Student;
import com.project.studentmanagement.exception.DuplicateResourceException;
import com.project.studentmanagement.exception.ResourceNotFoundException;
import com.project.studentmanagement.repository.CourseRepository;
import com.project.studentmanagement.repository.StudentRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class StudentServiceImpl implements StudentService {

    private final StudentRepository studentRepository;
    private final CourseRepository courseRepository;

    public StudentServiceImpl(StudentRepository studentRepository,
                              CourseRepository courseRepository) {
        this.studentRepository = studentRepository;
        this.courseRepository = courseRepository;
    }

    @Override
    @Transactional
    public Student addStudent(StudentRequest request) {
        String code = request.getStudentCode().trim();
        String email = request.getEmail().trim();

        if (studentRepository.existsByStudentCode(code)) {
            throw new DuplicateResourceException("Student code already exists: " + code);
        }
        if (studentRepository.existsByEmailIgnoreCase(email)) {
            throw new DuplicateResourceException("Email already registered: " + email);
        }

        Student student = new Student();
        apply(student, request);
        return studentRepository.save(student);
    }

    @Override
    @Transactional(readOnly = true)
    public List<Student> getAllStudents() {
        return studentRepository.findAll();
    }

    @Override
    @Transactional(readOnly = true)
    public PageResponse<Student> getStudentsPaginated(String search, Long courseId, int page, int size, String sortBy, String sortDir) {
        if (page < 0) page = 0;
        if (size <= 0) size = 10;
        if (size > 100) size = 100;
        if (sortBy == null || sortBy.isBlank()) sortBy = "id";

        Sort.Direction direction = "desc".equalsIgnoreCase(sortDir) ? Sort.Direction.DESC : Sort.Direction.ASC;
        Pageable pageable = PageRequest.of(page, size, Sort.by(direction, sortBy));

        String query = (search == null || search.isBlank()) ? null : search.trim();
        Page<Student> resultPage = studentRepository.findByFilter(query, courseId, pageable);

        return new PageResponse<>(
                resultPage.getContent(),
                resultPage.getNumber(),
                resultPage.getSize(),
                resultPage.getTotalElements(),
                resultPage.getTotalPages(),
                resultPage.isFirst(),
                resultPage.isLast()
        );
    }

    @Override
    @Transactional(readOnly = true)
    public Student getStudentById(Long id) {
        return studentRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Student not found with id " + id));
    }

    @Override
    @Transactional
    public Student updateStudent(Long id, StudentRequest request) {
        Student student = getStudentById(id);
        String code = request.getStudentCode().trim();
        String email = request.getEmail().trim();

        if (studentRepository.existsByStudentCodeAndIdNot(code, id)) {
            throw new DuplicateResourceException("Student code already exists: " + code);
        }
        if (studentRepository.existsByEmailIgnoreCaseAndIdNot(email, id)) {
            throw new DuplicateResourceException("Email already registered: " + email);
        }

        apply(student, request);
        return studentRepository.save(student);
    }

    @Override
    @Transactional
    public void deleteStudent(Long id) {
        Student student = getStudentById(id);
        studentRepository.delete(student);
    }

    @Override
    @Transactional(readOnly = true)
    public List<Student> searchStudents(String query) {
        if (query == null || query.isBlank()) {
            return studentRepository.findAll();
        }
        return studentRepository.search(query.trim());
    }

    private void apply(Student student, StudentRequest r) {
        student.setStudentCode(r.getStudentCode().trim());
        student.setFirstName(r.getFirstName().trim());
        student.setLastName(r.getLastName().trim());
        student.setEmail(r.getEmail().trim());
        student.setPhone(blankToNull(r.getPhone()));
        student.setDob(r.getDob());
        student.setGender(blankToNull(r.getGender()));
        student.setAddress(blankToNull(r.getAddress()));
        student.setAdmissionDate(r.getAdmissionDate());

        if (r.getCourseId() != null) {
            Course course = courseRepository.findById(r.getCourseId())
                    .orElseThrow(() -> new ResourceNotFoundException(
                            "Course not found with id " + r.getCourseId()));
            student.setCourse(course);
        } else {
            student.setCourse(null);
        }
    }

    private String blankToNull(String s) {
        return (s == null || s.isBlank()) ? null : s.trim();
    }
}
