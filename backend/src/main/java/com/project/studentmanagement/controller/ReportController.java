package com.project.studentmanagement.controller;

import com.project.studentmanagement.entity.Attendance;
import com.project.studentmanagement.entity.Course;
import com.project.studentmanagement.entity.Grade;
import com.project.studentmanagement.entity.Student;
import com.project.studentmanagement.repository.AttendanceRepository;
import com.project.studentmanagement.repository.CourseRepository;
import com.project.studentmanagement.repository.ExamRepository;
import com.project.studentmanagement.repository.GradeRepository;
import com.project.studentmanagement.repository.StudentRepository;
import com.project.studentmanagement.service.AttendanceService;
import com.project.studentmanagement.service.GradeService;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.nio.charset.StandardCharsets;
import java.util.*;

@RestController
@RequestMapping("/api/reports")
public class ReportController {

    private final StudentRepository studentRepository;
    private final CourseRepository courseRepository;
    private final ExamRepository examRepository;
    private final AttendanceRepository attendanceRepository;
    private final GradeRepository gradeRepository;
    private final AttendanceService attendanceService;
    private final GradeService gradeService;

    public ReportController(StudentRepository studentRepository,
                            CourseRepository courseRepository,
                            ExamRepository examRepository,
                            AttendanceRepository attendanceRepository,
                            GradeRepository gradeRepository,
                            AttendanceService attendanceService,
                            GradeService gradeService) {
        this.studentRepository = studentRepository;
        this.courseRepository = courseRepository;
        this.examRepository = examRepository;
        this.attendanceRepository = attendanceRepository;
        this.gradeRepository = gradeRepository;
        this.attendanceService = attendanceService;
        this.gradeService = gradeService;
    }

    @GetMapping("/dashboard")
    public Map<String, Object> getDashboardData() {
        long studentCount = studentRepository.count();
        long courseCount = courseRepository.count();
        long examCount = examRepository.count();
        long attendanceCount = attendanceRepository.count();

        Map<String, Object> attStats = attendanceService.getOverallAttendanceStats();
        Map<String, Object> gradeStats = gradeService.getOverallGradeStats();

        List<Course> courses = courseRepository.findAll();
        List<Map<String, Object>> courseDist = new ArrayList<>();
        for (Course c : courses) {
            long count = studentRepository.countByCourseId(c.getId());
            courseDist.add(Map.of(
                    "id", c.getId(),
                    "courseName", c.getCourseName(),
                    "studentCount", count
            ));
        }

        Map<String, Object> data = new LinkedHashMap<>();
        data.put("totalStudents", studentCount);
        data.put("totalCourses", courseCount);
        data.put("totalExams", examCount);
        data.put("totalAttendances", attendanceCount);
        data.put("attendanceStats", attStats);
        data.put("gradeStats", gradeStats);
        data.put("courseDistribution", courseDist);

        return data;
    }

    @GetMapping(value = "/export/students", produces = "text/csv")
    public ResponseEntity<byte[]> exportStudentsCsv() {
        List<Student> students = studentRepository.findAll();
        StringBuilder sb = new StringBuilder();
        sb.append("ID,Student Code,First Name,Last Name,Email,Phone,DOB,Gender,Course,Admission Date,Address\n");

        for (Student s : students) {
            sb.append(csv(s.getId())).append(",")
              .append(csv(s.getStudentCode())).append(",")
              .append(csv(s.getFirstName())).append(",")
              .append(csv(s.getLastName())).append(",")
              .append(csv(s.getEmail())).append(",")
              .append(csv(s.getPhone())).append(",")
              .append(csv(s.getDob())).append(",")
              .append(csv(s.getGender())).append(",")
              .append(csv(s.getCourse() != null ? s.getCourse().getCourseName() : "")).append(",")
              .append(csv(s.getAdmissionDate())).append(",")
              .append(csv(s.getAddress())).append("\n");
        }

        byte[] bytes = sb.toString().getBytes(StandardCharsets.UTF_8);
        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"students.csv\"")
                .contentType(MediaType.parseMediaType("text/csv; charset=UTF-8"))
                .body(bytes);
    }

    @GetMapping(value = "/export/attendance", produces = "text/csv")
    public ResponseEntity<byte[]> exportAttendanceCsv() {
        List<Attendance> attendances = attendanceRepository.findAll();
        StringBuilder sb = new StringBuilder();
        sb.append("ID,Date,Student Code,Student Name,Course,Status,Remarks\n");

        for (Attendance a : attendances) {
            Student s = a.getStudent();
            sb.append(csv(a.getId())).append(",")
              .append(csv(a.getAttendanceDate())).append(",")
              .append(csv(s != null ? s.getStudentCode() : "")).append(",")
              .append(csv(s != null ? s.getFirstName() + " " + s.getLastName() : "")).append(",")
              .append(csv(s != null && s.getCourse() != null ? s.getCourse().getCourseName() : "")).append(",")
              .append(csv(a.getStatus())).append(",")
              .append(csv(a.getRemarks())).append("\n");
        }

        byte[] bytes = sb.toString().getBytes(StandardCharsets.UTF_8);
        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"attendance.csv\"")
                .contentType(MediaType.parseMediaType("text/csv; charset=UTF-8"))
                .body(bytes);
    }

    @GetMapping(value = "/export/grades", produces = "text/csv")
    public ResponseEntity<byte[]> exportGradesCsv() {
        List<Grade> grades = gradeRepository.findAll();
        StringBuilder sb = new StringBuilder();
        sb.append("ID,Exam Title,Course,Exam Date,Student Code,Student Name,Marks Obtained,Max Marks,Passing Marks,Grade Letter,Result,Remarks\n");

        for (Grade g : grades) {
            Student s = g.getStudent();
            var exam = g.getExam();
            boolean isPass = exam != null && g.getMarksObtained() >= exam.getPassingMarks();

            sb.append(csv(g.getId())).append(",")
              .append(csv(exam != null ? exam.getTitle() : "")).append(",")
              .append(csv(exam != null && exam.getCourse() != null ? exam.getCourse().getCourseName() : "")).append(",")
              .append(csv(exam != null ? exam.getExamDate() : "")).append(",")
              .append(csv(s != null ? s.getStudentCode() : "")).append(",")
              .append(csv(s != null ? s.getFirstName() + " " + s.getLastName() : "")).append(",")
              .append(csv(g.getMarksObtained())).append(",")
              .append(csv(exam != null ? exam.getMaxMarks() : "")).append(",")
              .append(csv(exam != null ? exam.getPassingMarks() : "")).append(",")
              .append(csv(g.getGradeLetter())).append(",")
              .append(csv(isPass ? "PASS" : "FAIL")).append(",")
              .append(csv(g.getRemarks())).append("\n");
        }

        byte[] bytes = sb.toString().getBytes(StandardCharsets.UTF_8);
        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"grades.csv\"")
                .contentType(MediaType.parseMediaType("text/csv; charset=UTF-8"))
                .body(bytes);
    }

    private String csv(Object val) {
        if (val == null) return "\"\"";
        String str = val.toString().replace("\"", "\"\"");
        return "\"" + str + "\"";
    }
}
