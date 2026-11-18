package com.project.studentmanagement.entity;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.PositiveOrZero;
import jakarta.validation.constraints.Size;

@Entity
@Table(
    name = "grades",
    uniqueConstraints = {
        @UniqueConstraint(name = "uk_exam_student", columnNames = {"exam_id", "student_id"})
    }
)
public class Grade {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotNull(message = "Exam is required")
    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "exam_id", nullable = false)
    private Exam exam;

    @NotNull(message = "Student is required")
    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "student_id", nullable = false)
    private Student student;

    @NotNull(message = "Marks obtained is required")
    @PositiveOrZero(message = "Marks obtained must be zero or positive")
    @Column(name = "marks_obtained", nullable = false)
    private Double marksObtained;

    @Size(max = 5)
    @Column(name = "grade_letter", length = 5)
    private String gradeLetter;

    @Size(max = 255)
    @Column(length = 255)
    private String remarks;

    public Grade() {}

    public Grade(Exam exam, Student student, Double marksObtained, String gradeLetter, String remarks) {
        this.exam = exam;
        this.student = student;
        this.marksObtained = marksObtained;
        this.gradeLetter = gradeLetter;
        this.remarks = remarks;
    }

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public Exam getExam() { return exam; }
    public void setExam(Exam exam) { this.exam = exam; }

    public Student getStudent() { return student; }
    public void setStudent(Student student) { this.student = student; }

    public Double getMarksObtained() { return marksObtained; }
    public void setMarksObtained(Double marksObtained) { this.marksObtained = marksObtained; }

    public String getGradeLetter() { return gradeLetter; }
    public void setGradeLetter(String gradeLetter) { this.gradeLetter = gradeLetter; }

    public String getRemarks() { return remarks; }
    public void setRemarks(String remarks) { this.remarks = remarks; }
}
