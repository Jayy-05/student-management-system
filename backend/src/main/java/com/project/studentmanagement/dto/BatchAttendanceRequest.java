package com.project.studentmanagement.dto;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;

import java.time.LocalDate;
import java.util.List;

public class BatchAttendanceRequest {

    @NotNull(message = "Attendance date is required")
    private LocalDate attendanceDate;

    @NotEmpty(message = "At least one attendance record must be provided")
    @Valid
    private List<AttendanceRecordRequest> records;

    public LocalDate getAttendanceDate() { return attendanceDate; }
    public void setAttendanceDate(LocalDate attendanceDate) { this.attendanceDate = attendanceDate; }

    public List<AttendanceRecordRequest> getRecords() { return records; }
    public void setRecords(List<AttendanceRecordRequest> records) { this.records = records; }
}
