package com.project.studentmanagement.service;

import com.project.studentmanagement.entity.Course;
import com.project.studentmanagement.exception.DuplicateResourceException;
import com.project.studentmanagement.exception.ResourceNotFoundException;
import com.project.studentmanagement.repository.CourseRepository;
import com.project.studentmanagement.repository.StudentRepository;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class CourseService {

    private final CourseRepository courseRepository;
    private final StudentRepository studentRepository;

    public CourseService(CourseRepository courseRepository, StudentRepository studentRepository) {
        this.courseRepository = courseRepository;
        this.studentRepository = studentRepository;
    }

    @Transactional
    public Course addCourse(Course course) {
        course.setId(null);
        course.setCourseName(course.getCourseName().trim());
        if (courseRepository.existsByCourseNameIgnoreCase(course.getCourseName())) {
            throw new DuplicateResourceException("Course already exists: " + course.getCourseName());
        }
        return courseRepository.save(course);
    }

    @Transactional(readOnly = true)
    public List<Course> getAllCourses() {
        return courseRepository.findAll();
    }

    @Transactional
    public void deleteCourse(Long id) {
        Course course = courseRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Course not found with id " + id));

        if (studentRepository.existsByCourseId(id)) {
            throw new DataIntegrityViolationException(
                    "Cannot delete course '" + course.getCourseName() + "' because students are currently enrolled in it.");
        }
        courseRepository.delete(course);
    }
}
