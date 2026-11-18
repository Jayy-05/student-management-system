package com.project.studentmanagement.entity;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.Size;

import java.time.LocalDate;

@Entity
@Table(name = "exams")
public class Exam {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotBlank(message = "Exam title is required")
    @Size(max = 100)
    @Column(nullable = false, length = 100)
    private String title;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "course_id")
    private Course course;

    @NotNull(message = "Exam date is required")
    @Column(name = "exam_date", nullable = false)
    private LocalDate examDate;

    @NotNull(message = "Maximum marks is required")
    @Positive(message = "Maximum marks must be greater than zero")
    @Column(name = "max_marks", nullable = false)
    private Double maxMarks = 100.0;

    @NotNull(message = "Passing marks is required")
    @Positive(message = "Passing marks must be greater than zero")
    @Column(name = "passing_marks", nullable = false)
    private Double passingMarks = 40.0;

    @Size(max = 255)
    @Column(length = 255)
    private String description;

    public Exam() {}

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public String getTitle() { return title; }
    public void setTitle(String title) { this.title = title; }

    public Course getCourse() { return course; }
    public void setCourse(Course course) { this.course = course; }

    public LocalDate getExamDate() { return examDate; }
    public void setExamDate(LocalDate examDate) { this.examDate = examDate; }

    public Double getMaxMarks() { return maxMarks; }
    public void setMaxMarks(Double maxMarks) { this.maxMarks = maxMarks; }

    public Double getPassingMarks() { return passingMarks; }
    public void setPassingMarks(Double passingMarks) { this.passingMarks = passingMarks; }

    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }
}
