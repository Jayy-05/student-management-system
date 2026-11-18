package com.project.studentmanagement.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.Size;

import java.time.LocalDate;

public class ExamRequest {

    @NotBlank(message = "Exam title is required")
    @Size(max = 100)
    private String title;

    private Long courseId;

    @NotNull(message = "Exam date is required")
    private LocalDate examDate;

    @NotNull(message = "Maximum marks is required")
    @Positive(message = "Maximum marks must be greater than zero")
    private Double maxMarks = 100.0;

    @NotNull(message = "Passing marks is required")
    @Positive(message = "Passing marks must be greater than zero")
    private Double passingMarks = 40.0;

    @Size(max = 255)
    private String description;

    public String getTitle() { return title; }
    public void setTitle(String title) { this.title = title; }

    public Long getCourseId() { return courseId; }
    public void setCourseId(Long courseId) { this.courseId = courseId; }

    public LocalDate getExamDate() { return examDate; }
    public void setExamDate(LocalDate examDate) { this.examDate = examDate; }

    public Double getMaxMarks() { return maxMarks; }
    public void setMaxMarks(Double maxMarks) { this.maxMarks = maxMarks; }

    public Double getPassingMarks() { return passingMarks; }
    public void setPassingMarks(Double passingMarks) { this.passingMarks = passingMarks; }

    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }
}
