package com.project.studentmanagement.controller;

import com.project.studentmanagement.dto.AttendanceRecordRequest;
import com.project.studentmanagement.dto.AttendanceStatsResponse;
import com.project.studentmanagement.dto.BatchAttendanceRequest;
import com.project.studentmanagement.entity.Attendance;
import com.project.studentmanagement.service.AttendanceService;
import jakarta.validation.Valid;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/attendance")
public class AttendanceController {

    private final AttendanceService attendanceService;

    public AttendanceController(AttendanceService attendanceService) {
        this.attendanceService = attendanceService;
    }

    @GetMapping
    public List<Attendance> getAttendance(
            @RequestParam(name = "date", required = false)
            @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate date,
            @RequestParam(name = "courseId", required = false) Long courseId) {
        return attendanceService.getAttendanceByDateAndCourse(date, courseId);
    }

    @PostMapping
    public ResponseEntity<Attendance> markAttendance(
            @RequestParam(name = "date", required = false)
            @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate date,
            @Valid @RequestBody AttendanceRecordRequest request) {
        return ResponseEntity.ok(attendanceService.markAttendance(date, request));
    }

    @PostMapping("/batch")
    public ResponseEntity<List<Attendance>> batchMarkAttendance(
            @Valid @RequestBody BatchAttendanceRequest request) {
        return ResponseEntity.ok(attendanceService.batchMarkAttendance(request));
    }

    @GetMapping("/student/{studentId}")
    public List<Attendance> getStudentAttendance(@PathVariable Long studentId) {
        return attendanceService.getStudentAttendance(studentId);
    }

    @GetMapping("/student/{studentId}/stats")
    public AttendanceStatsResponse getStudentStats(@PathVariable Long studentId) {
        return attendanceService.getStudentAttendanceStats(studentId);
    }

    @GetMapping("/stats")
    public Map<String, Object> getOverallStats() {
        return attendanceService.getOverallAttendanceStats();
    }
}
