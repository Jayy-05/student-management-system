package com.project.studentmanagement.controller;

import com.project.studentmanagement.dto.StudentRequest;
import com.project.studentmanagement.entity.Student;
import com.project.studentmanagement.service.StudentService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/students")
public class StudentController {

    private final StudentService studentService;

    public StudentController(StudentService studentService) {
        this.studentService = studentService;
    }

    @PostMapping
    public ResponseEntity<Student> addStudent(@Valid @RequestBody StudentRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(studentService.addStudent(request));
    }

    @GetMapping
    public ResponseEntity<?> getStudents(
            @RequestParam(name = "search", required = false) String search,
            @RequestParam(name = "courseId", required = false) Long courseId,
            @RequestParam(name = "page", required = false) Integer page,
            @RequestParam(name = "size", required = false, defaultValue = "10") Integer size,
            @RequestParam(name = "sortBy", required = false, defaultValue = "id") String sortBy,
            @RequestParam(name = "sortDir", required = false, defaultValue = "desc") String sortDir) {
        if (page != null) {
            return ResponseEntity.ok(studentService.getStudentsPaginated(search, courseId, page, size, sortBy, sortDir));
        }
        if (search != null && !search.isBlank()) {
            return ResponseEntity.ok(studentService.searchStudents(search));
        }
        return ResponseEntity.ok(studentService.getAllStudents());
    }

    // Declared before /{id} for clarity; Spring prefers the literal path anyway.
    @GetMapping("/search")
    public List<Student> search(@RequestParam(name = "name", defaultValue = "") String name) {
        return studentService.searchStudents(name);
    }

    @GetMapping("/{id}")
    public Student getStudent(@PathVariable Long id) {
        return studentService.getStudentById(id);
    }

    @PutMapping("/{id}")
    public Student updateStudent(@PathVariable Long id,
                                 @Valid @RequestBody StudentRequest request) {
        return studentService.updateStudent(id, request);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteStudent(@PathVariable Long id) {
        studentService.deleteStudent(id);
        return ResponseEntity.noContent().build();
    }
}
