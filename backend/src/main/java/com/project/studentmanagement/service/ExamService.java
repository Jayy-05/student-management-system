package com.project.studentmanagement.service;

import com.project.studentmanagement.dto.ExamRequest;
import com.project.studentmanagement.entity.Exam;

import java.util.List;

public interface ExamService {

    Exam createExam(ExamRequest request);

    List<Exam> getAllExams();

    Exam getExamById(Long id);

    Exam updateExam(Long id, ExamRequest request);

    void deleteExam(Long id);

    List<Exam> getExamsByCourse(Long courseId);
}
