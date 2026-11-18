package com.project.studentmanagement.repository;

import com.project.studentmanagement.entity.Course;
import org.springframework.data.jpa.repository.JpaRepository;

public interface CourseRepository extends JpaRepository<Course, Long> {

    boolean existsByCourseNameIgnoreCase(String courseName);
}
