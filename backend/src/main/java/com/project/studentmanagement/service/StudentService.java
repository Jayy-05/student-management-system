package com.project.studentmanagement.service;

import com.project.studentmanagement.dto.PageResponse;
import com.project.studentmanagement.dto.StudentRequest;
import com.project.studentmanagement.entity.Student;

import java.util.List;

public interface StudentService {

    Student addStudent(StudentRequest request);

    List<Student> getAllStudents();

    PageResponse<Student> getStudentsPaginated(String search, Long courseId, int page, int size, String sortBy, String sortDir);

    Student getStudentById(Long id);

    Student updateStudent(Long id, StudentRequest request);

    void deleteStudent(Long id);

    List<Student> searchStudents(String query);
}
