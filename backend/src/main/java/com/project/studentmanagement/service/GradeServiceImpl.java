package com.project.studentmanagement.service;

import com.project.studentmanagement.dto.BatchGradeRequest;
import com.project.studentmanagement.dto.GradeEntryRequest;
import com.project.studentmanagement.dto.StudentReportCardResponse;
import com.project.studentmanagement.entity.Exam;
import com.project.studentmanagement.entity.Grade;
import com.project.studentmanagement.entity.Student;
import com.project.studentmanagement.exception.ResourceNotFoundException;
import com.project.studentmanagement.repository.ExamRepository;
import com.project.studentmanagement.repository.GradeRepository;
import com.project.studentmanagement.repository.StudentRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;

@Service
public class GradeServiceImpl implements GradeService {

    private final GradeRepository gradeRepository;
    private final ExamRepository examRepository;
    private final StudentRepository studentRepository;

    public GradeServiceImpl(GradeRepository gradeRepository,
                            ExamRepository examRepository,
                            StudentRepository studentRepository) {
        this.gradeRepository = gradeRepository;
        this.examRepository = examRepository;
        this.studentRepository = studentRepository;
    }

    @Override
    @Transactional(readOnly = true)
    public List<Grade> getGradesByExam(Long examId) {
        if (!examRepository.existsById(examId)) {
            throw new ResourceNotFoundException("Exam not found with id " + examId);
        }
        return gradeRepository.findByExamId(examId);
    }

    @Override
    @Transactional
    public Grade recordGrade(Long examId, GradeEntryRequest request) {
        Exam exam = examRepository.findById(examId)
                .orElseThrow(() -> new ResourceNotFoundException("Exam not found with id " + examId));
        Student student = studentRepository.findById(request.getStudentId())
                .orElseThrow(() -> new ResourceNotFoundException("Student not found with id " + request.getStudentId()));

        if (request.getMarksObtained() > exam.getMaxMarks()) {
            throw new IllegalArgumentException("Marks obtained cannot exceed max marks (" + exam.getMaxMarks() + ")");
        }

        Optional<Grade> existing = gradeRepository.findByExamIdAndStudentId(examId, student.getId());
        Grade grade;
        if (existing.isPresent()) {
            grade = existing.get();
        } else {
            grade = new Grade();
            grade.setExam(exam);
            grade.setStudent(student);
        }

        grade.setMarksObtained(request.getMarksObtained());
        double pct = (exam.getMaxMarks() > 0) ? (request.getMarksObtained() / exam.getMaxMarks()) * 100.0 : 0.0;
        grade.setGradeLetter(calculateGradeLetter(pct));
        grade.setRemarks(blankToNull(request.getRemarks()));

        return gradeRepository.save(grade);
    }

    @Override
    @Transactional
    public List<Grade> batchRecordGrades(BatchGradeRequest request) {
        Exam exam = examRepository.findById(request.getExamId())
                .orElseThrow(() -> new ResourceNotFoundException("Exam not found with id " + request.getExamId()));

        List<Grade> results = new ArrayList<>();
        for (GradeEntryRequest entry : request.getGrades()) {
            Student student = studentRepository.findById(entry.getStudentId())
                    .orElseThrow(() -> new ResourceNotFoundException("Student not found with id " + entry.getStudentId()));

            if (entry.getMarksObtained() > exam.getMaxMarks()) {
                throw new IllegalArgumentException(
                        "Marks obtained (" + entry.getMarksObtained() + ") cannot exceed maximum marks (" + exam.getMaxMarks() + ")");
            }

            Optional<Grade> existing = gradeRepository.findByExamIdAndStudentId(exam.getId(), student.getId());
            Grade grade = existing.orElseGet(() -> {
                Grade g = new Grade();
                g.setExam(exam);
                g.setStudent(student);
                return g;
            });

            grade.setMarksObtained(entry.getMarksObtained());
            double pct = (exam.getMaxMarks() > 0) ? (entry.getMarksObtained() / exam.getMaxMarks()) * 100.0 : 0.0;
            grade.setGradeLetter(calculateGradeLetter(pct));
            grade.setRemarks(blankToNull(entry.getRemarks()));

            results.add(gradeRepository.save(grade));
        }

        return results;
    }

    @Override
    @Transactional(readOnly = true)
    public List<Grade> getGradesByStudent(Long studentId) {
        if (!studentRepository.existsById(studentId)) {
            throw new ResourceNotFoundException("Student not found with id " + studentId);
        }
        return gradeRepository.findByStudentId(studentId);
    }

    @Override
    @Transactional(readOnly = true)
    public StudentReportCardResponse getStudentReportCard(Long studentId) {
        Student student = studentRepository.findById(studentId)
                .orElseThrow(() -> new ResourceNotFoundException("Student not found with id " + studentId));

        List<Grade> grades = gradeRepository.findByStudentId(studentId);

        StudentReportCardResponse response = new StudentReportCardResponse();
        response.setStudentId(student.getId());
        response.setStudentCode(student.getStudentCode());
        response.setStudentName(student.getFirstName() + " " + student.getLastName());
        response.setEmail(student.getEmail());
        response.setCourseName(student.getCourse() != null ? student.getCourse().getCourseName() : "None");

        List<StudentReportCardResponse.GradeItem> items = new ArrayList<>();
        double totalObtained = 0.0;
        double totalMax = 0.0;
        int passed = 0;
        int failed = 0;

        for (Grade g : grades) {
            Exam exam = g.getExam();
            double pct = (exam.getMaxMarks() > 0) ? (g.getMarksObtained() / exam.getMaxMarks()) * 100.0 : 0.0;
            pct = Math.round(pct * 10.0) / 10.0;
            boolean isPass = g.getMarksObtained() >= exam.getPassingMarks();
            if (isPass) passed++; else failed++;

            totalObtained += g.getMarksObtained();
            totalMax += exam.getMaxMarks();

            items.add(new StudentReportCardResponse.GradeItem(
                    exam.getId(),
                    exam.getTitle(),
                    exam.getCourse() != null ? exam.getCourse().getCourseName() : "All Courses",
                    exam.getExamDate() != null ? exam.getExamDate().toString() : "—",
                    g.getMarksObtained(),
                    exam.getMaxMarks(),
                    exam.getPassingMarks(),
                    pct,
                    g.getGradeLetter(),
                    isPass ? "PASS" : "FAIL",
                    g.getRemarks()
            ));
        }

        response.setItems(items);
        response.setTotalMarksObtained(Math.round(totalObtained * 10.0) / 10.0);
        response.setTotalMaxMarks(Math.round(totalMax * 10.0) / 10.0);
        double overallPct = (totalMax > 0) ? Math.round((totalObtained / totalMax) * 1000.0) / 10.0 : 0.0;
        response.setOverallPercentage(overallPct);
        response.setOverallGrade(calculateGradeLetter(overallPct));
        response.setPassedCount(passed);
        response.setFailedCount(failed);
        response.setFinalResult((failed == 0 && !items.isEmpty()) ? "PASS" : (items.isEmpty() ? "PENDING" : "FAIL"));

        return response;
    }

    @Override
    @Transactional(readOnly = true)
    public Map<String, Object> getOverallGradeStats() {
        List<Grade> allGrades = gradeRepository.findAll();
        long totalGrades = allGrades.size();
        long passedCount = allGrades.stream()
                .filter(g -> g.getMarksObtained() >= g.getExam().getPassingMarks())
                .count();

        double passRate = (totalGrades == 0) ? 0.0 : Math.round(((double) passedCount / totalGrades) * 1000.0) / 10.0;

        Map<String, Long> gradeDist = new LinkedHashMap<>();
        gradeDist.put("A+", 0L);
        gradeDist.put("A", 0L);
        gradeDist.put("B", 0L);
        gradeDist.put("C", 0L);
        gradeDist.put("D", 0L);
        gradeDist.put("F", 0L);

        for (Grade g : allGrades) {
            String letter = g.getGradeLetter();
            if (letter != null && gradeDist.containsKey(letter)) {
                gradeDist.put(letter, gradeDist.get(letter) + 1);
            }
        }

        Map<String, Object> map = new LinkedHashMap<>();
        map.put("totalGradesRecorded", totalGrades);
        map.put("passedCount", passedCount);
        map.put("failedCount", totalGrades - passedCount);
        map.put("overallPassRate", passRate);
        map.put("gradeDistribution", gradeDist);
        return map;
    }

    private String calculateGradeLetter(double pct) {
        if (pct >= 90.0) return "A+";
        if (pct >= 80.0) return "A";
        if (pct >= 70.0) return "B";
        if (pct >= 60.0) return "C";
        if (pct >= 50.0) return "D";
        return "F";
    }

    private String blankToNull(String s) {
        return (s == null || s.isBlank()) ? null : s.trim();
    }
}
