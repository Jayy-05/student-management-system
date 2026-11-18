package com.project.studentmanagement.controller;

import com.project.studentmanagement.dto.ExamRequest;
import com.project.studentmanagement.entity.Exam;
import com.project.studentmanagement.service.ExamService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/exams")
public class ExamController {

    private final ExamService examService;

    public ExamController(ExamService examService) {
        this.examService = examService;
    }

    @GetMapping
    public List<Exam> getAllExams(@RequestParam(name = "courseId", required = false) Long courseId) {
        if (courseId != null) {
            return examService.getExamsByCourse(courseId);
        }
        return examService.getAllExams();
    }

    @GetMapping("/{id}")
    public Exam getExam(@PathVariable Long id) {
        return examService.getExamById(id);
    }

    @PostMapping
    public ResponseEntity<Exam> createExam(@Valid @RequestBody ExamRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(examService.createExam(request));
    }

    @PutMapping("/{id}")
    public Exam updateExam(@PathVariable Long id, @Valid @RequestBody ExamRequest request) {
        return examService.updateExam(id, request);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteExam(@PathVariable Long id) {
        examService.deleteExam(id);
        return ResponseEntity.noContent().build();
    }
}
