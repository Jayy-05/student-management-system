package com.project.studentmanagement.service;

import com.project.studentmanagement.dto.AttendanceRecordRequest;
import com.project.studentmanagement.dto.AttendanceStatsResponse;
import com.project.studentmanagement.dto.BatchAttendanceRequest;
import com.project.studentmanagement.entity.Attendance;

import java.time.LocalDate;
import java.util.List;
import java.util.Map;

public interface AttendanceService {

    List<Attendance> getAttendanceByDateAndCourse(LocalDate date, Long courseId);

    Attendance markAttendance(LocalDate date, AttendanceRecordRequest request);

    List<Attendance> batchMarkAttendance(BatchAttendanceRequest request);

    List<Attendance> getStudentAttendance(Long studentId);

    AttendanceStatsResponse getStudentAttendanceStats(Long studentId);

    Map<String, Object> getOverallAttendanceStats();
}
