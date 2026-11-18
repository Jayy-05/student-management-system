package com.project.studentmanagement.dto;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;

import java.util.List;

public class BatchGradeRequest {

    @NotNull(message = "Exam ID is required")
    private Long examId;

    @NotEmpty(message = "At least one grade entry is required")
    @Valid
    private List<GradeEntryRequest> grades;

    public Long getExamId() { return examId; }
    public void setExamId(Long examId) { this.examId = examId; }

    public List<GradeEntryRequest> getGrades() { return grades; }
    public void setGrades(List<GradeEntryRequest> grades) { this.grades = grades; }
}
