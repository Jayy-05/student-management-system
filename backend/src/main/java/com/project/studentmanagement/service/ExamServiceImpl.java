package com.project.studentmanagement.service;

import com.project.studentmanagement.dto.ExamRequest;
import com.project.studentmanagement.entity.Course;
import com.project.studentmanagement.entity.Exam;
import com.project.studentmanagement.exception.ResourceNotFoundException;
import com.project.studentmanagement.repository.CourseRepository;
import com.project.studentmanagement.repository.ExamRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class ExamServiceImpl implements ExamService {

    private final ExamRepository examRepository;
    private final CourseRepository courseRepository;

    public ExamServiceImpl(ExamRepository examRepository, CourseRepository courseRepository) {
        this.examRepository = examRepository;
        this.courseRepository = courseRepository;
    }

    @Override
    @Transactional
    public Exam createExam(ExamRequest request) {
        Exam exam = new Exam();
        apply(exam, request);
        return examRepository.save(exam);
    }

    @Override
    @Transactional(readOnly = true)
    public List<Exam> getAllExams() {
        return examRepository.findAllByOrderByExamDateDesc();
    }

    @Override
    @Transactional(readOnly = true)
    public Exam getExamById(Long id) {
        return examRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Exam not found with id " + id));
    }

    @Override
    @Transactional
    public Exam updateExam(Long id, ExamRequest request) {
        Exam exam = getExamById(id);
        apply(exam, request);
        return examRepository.save(exam);
    }

    @Override
    @Transactional
    public void deleteExam(Long id) {
        Exam exam = getExamById(id);
        examRepository.delete(exam);
    }

    @Override
    @Transactional(readOnly = true)
    public List<Exam> getExamsByCourse(Long courseId) {
        return examRepository.findByCourseId(courseId);
    }

    private void apply(Exam exam, ExamRequest request) {
        exam.setTitle(request.getTitle().trim());
        exam.setExamDate(request.getExamDate());
        exam.setMaxMarks(request.getMaxMarks() != null ? request.getMaxMarks() : 100.0);
        exam.setPassingMarks(request.getPassingMarks() != null ? request.getPassingMarks() : 40.0);
        exam.setDescription(blankToNull(request.getDescription()));

        if (request.getCourseId() != null) {
            Course course = courseRepository.findById(request.getCourseId())
                    .orElseThrow(() -> new ResourceNotFoundException("Course not found with id " + request.getCourseId()));
            exam.setCourse(course);
        } else {
            exam.setCourse(null);
        }
    }

    private String blankToNull(String s) {
        return (s == null || s.isBlank()) ? null : s.trim();
    }
}
