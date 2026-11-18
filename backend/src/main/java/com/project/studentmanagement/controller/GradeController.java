package com.project.studentmanagement.controller;

import com.project.studentmanagement.dto.BatchGradeRequest;
import com.project.studentmanagement.dto.GradeEntryRequest;
import com.project.studentmanagement.dto.StudentReportCardResponse;
import com.project.studentmanagement.entity.Grade;
import com.project.studentmanagement.service.GradeService;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/grades")
public class GradeController {

    private final GradeService gradeService;

    public GradeController(GradeService gradeService) {
        this.gradeService = gradeService;
    }

    @GetMapping("/exam/{examId}")
    public List<Grade> getGradesByExam(@PathVariable Long examId) {
        return gradeService.getGradesByExam(examId);
    }

    @PostMapping("/exam/{examId}")
    public ResponseEntity<Grade> recordGrade(@PathVariable Long examId,
                                             @Valid @RequestBody GradeEntryRequest request) {
        return ResponseEntity.ok(gradeService.recordGrade(examId, request));
    }

    @PostMapping("/batch")
    public ResponseEntity<List<Grade>> batchRecordGrades(@Valid @RequestBody BatchGradeRequest request) {
        return ResponseEntity.ok(gradeService.batchRecordGrades(request));
    }

    @GetMapping("/student/{studentId}")
    public List<Grade> getGradesByStudent(@PathVariable Long studentId) {
        return gradeService.getGradesByStudent(studentId);
    }

    @GetMapping("/student/{studentId}/report-card")
    public StudentReportCardResponse getStudentReportCard(@PathVariable Long studentId) {
        return gradeService.getStudentReportCard(studentId);
    }

    @GetMapping("/stats")
    public Map<String, Object> getGradeStats() {
        return gradeService.getOverallGradeStats();
    }
}
