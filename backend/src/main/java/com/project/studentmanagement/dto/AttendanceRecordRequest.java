package com.project.studentmanagement.dto;

import com.project.studentmanagement.entity.AttendanceStatus;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

public class AttendanceRecordRequest {

    @NotNull(message = "Student ID is required")
    private Long studentId;

    @NotNull(message = "Status is required (PRESENT, ABSENT, LATE, EXCUSED)")
    private AttendanceStatus status;

    @Size(max = 255)
    private String remarks;

    public Long getStudentId() { return studentId; }
    public void setStudentId(Long studentId) { this.studentId = studentId; }

    public AttendanceStatus getStatus() { return status; }
    public void setStatus(AttendanceStatus status) { this.status = status; }

    public String getRemarks() { return remarks; }
    public void setRemarks(String remarks) { this.remarks = remarks; }
}
