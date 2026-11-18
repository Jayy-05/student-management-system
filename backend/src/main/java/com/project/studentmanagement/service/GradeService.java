package com.project.studentmanagement.service;

import com.project.studentmanagement.dto.BatchGradeRequest;
import com.project.studentmanagement.dto.GradeEntryRequest;
import com.project.studentmanagement.dto.StudentReportCardResponse;
import com.project.studentmanagement.entity.Grade;

import java.util.List;
import java.util.Map;

public interface GradeService {

    List<Grade> getGradesByExam(Long examId);

    Grade recordGrade(Long examId, GradeEntryRequest request);

    List<Grade> batchRecordGrades(BatchGradeRequest request);

    List<Grade> getGradesByStudent(Long studentId);

    StudentReportCardResponse getStudentReportCard(Long studentId);

    Map<String, Object> getOverallGradeStats();
}
