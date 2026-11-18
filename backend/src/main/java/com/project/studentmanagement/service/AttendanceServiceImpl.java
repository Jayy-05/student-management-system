package com.project.studentmanagement.service;

import com.project.studentmanagement.dto.AttendanceRecordRequest;
import com.project.studentmanagement.dto.AttendanceStatsResponse;
import com.project.studentmanagement.dto.BatchAttendanceRequest;
import com.project.studentmanagement.entity.Attendance;
import com.project.studentmanagement.entity.AttendanceStatus;
import com.project.studentmanagement.entity.Student;
import com.project.studentmanagement.exception.ResourceNotFoundException;
import com.project.studentmanagement.repository.AttendanceRepository;
import com.project.studentmanagement.repository.StudentRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.*;

@Service
public class AttendanceServiceImpl implements AttendanceService {

    private final AttendanceRepository attendanceRepository;
    private final StudentRepository studentRepository;

    public AttendanceServiceImpl(AttendanceRepository attendanceRepository,
                                 StudentRepository studentRepository) {
        this.attendanceRepository = attendanceRepository;
        this.studentRepository = studentRepository;
    }

    @Override
    @Transactional(readOnly = true)
    public List<Attendance> getAttendanceByDateAndCourse(LocalDate date, Long courseId) {
        if (date == null) {
            date = LocalDate.now();
        }
        if (courseId != null && courseId > 0) {
            return attendanceRepository.findByAttendanceDateAndCourseId(date, courseId);
        }
        return attendanceRepository.findByAttendanceDate(date);
    }

    @Override
    @Transactional
    public Attendance markAttendance(LocalDate date, AttendanceRecordRequest request) {
        if (date == null) {
            date = LocalDate.now();
        }
        Student student = studentRepository.findById(request.getStudentId())
                .orElseThrow(() -> new ResourceNotFoundException("Student not found with id " + request.getStudentId()));

        Optional<Attendance> existing = attendanceRepository.findByStudentIdAndAttendanceDate(student.getId(), date);
        Attendance attendance;
        if (existing.isPresent()) {
            attendance = existing.get();
            attendance.setStatus(request.getStatus());
            attendance.setRemarks(blankToNull(request.getRemarks()));
        } else {
            attendance = new Attendance(student, date, request.getStatus(), blankToNull(request.getRemarks()));
        }

        return attendanceRepository.save(attendance);
    }

    @Override
    @Transactional
    public List<Attendance> batchMarkAttendance(BatchAttendanceRequest request) {
        LocalDate date = request.getAttendanceDate();
        List<Attendance> results = new ArrayList<>();

        for (AttendanceRecordRequest rec : request.getRecords()) {
            Student student = studentRepository.findById(rec.getStudentId())
                    .orElseThrow(() -> new ResourceNotFoundException("Student not found with id " + rec.getStudentId()));

            Optional<Attendance> existing = attendanceRepository.findByStudentIdAndAttendanceDate(student.getId(), date);
            Attendance attendance;
            if (existing.isPresent()) {
                attendance = existing.get();
                attendance.setStatus(rec.getStatus());
                attendance.setRemarks(blankToNull(rec.getRemarks()));
            } else {
                attendance = new Attendance(student, date, rec.getStatus(), blankToNull(rec.getRemarks()));
            }
            results.add(attendanceRepository.save(attendance));
        }

        return results;
    }

    @Override
    @Transactional(readOnly = true)
    public List<Attendance> getStudentAttendance(Long studentId) {
        if (!studentRepository.existsById(studentId)) {
            throw new ResourceNotFoundException("Student not found with id " + studentId);
        }
        return attendanceRepository.findByStudentIdOrderByAttendanceDateDesc(studentId);
    }

    @Override
    @Transactional(readOnly = true)
    public AttendanceStatsResponse getStudentAttendanceStats(Long studentId) {
        Student student = studentRepository.findById(studentId)
                .orElseThrow(() -> new ResourceNotFoundException("Student not found with id " + studentId));

        long total = attendanceRepository.countByStudentId(studentId);
        long present = attendanceRepository.countByStudentIdAndStatus(studentId, AttendanceStatus.PRESENT);
        long absent = attendanceRepository.countByStudentIdAndStatus(studentId, AttendanceStatus.ABSENT);
        long late = attendanceRepository.countByStudentIdAndStatus(studentId, AttendanceStatus.LATE);
        long excused = attendanceRepository.countByStudentIdAndStatus(studentId, AttendanceStatus.EXCUSED);

        double pct = (total == 0) ? 0.0 : Math.round(((double) (present + late) / total) * 1000.0) / 10.0;

        return new AttendanceStatsResponse(
                student.getId(),
                student.getStudentCode(),
                student.getFirstName() + " " + student.getLastName(),
                total, present, absent, late, excused, pct
        );
    }

    @Override
    @Transactional(readOnly = true)
    public Map<String, Object> getOverallAttendanceStats() {
        LocalDate today = LocalDate.now();
        long todayTotal = attendanceRepository.countByAttendanceDate(today);
        long todayPresent = attendanceRepository.countByAttendanceDateAndStatus(today, AttendanceStatus.PRESENT);
        long todayAbsent = attendanceRepository.countByAttendanceDateAndStatus(today, AttendanceStatus.ABSENT);
        long todayLate = attendanceRepository.countByAttendanceDateAndStatus(today, AttendanceStatus.LATE);
        long todayExcused = attendanceRepository.countByAttendanceDateAndStatus(today, AttendanceStatus.EXCUSED);

        double todayRate = (todayTotal == 0) ? 0.0 : Math.round(((double) (todayPresent + todayLate) / todayTotal) * 1000.0) / 10.0;

        Map<String, Object> map = new LinkedHashMap<>();
        map.put("date", today.toString());
        map.put("totalMarkedToday", todayTotal);
        map.put("presentToday", todayPresent);
        map.put("absentToday", todayAbsent);
        map.put("lateToday", todayLate);
        map.put("excusedToday", todayExcused);
        map.put("todayAttendanceRate", todayRate);
        return map;
    }

    private String blankToNull(String s) {
        return (s == null || s.isBlank()) ? null : s.trim();
    }
}
