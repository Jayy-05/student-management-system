package com.project.studentmanagement.dto;

public class AttendanceStatsResponse {

    private Long studentId;
    private String studentCode;
    private String studentName;
    private long totalDays;
    private long presentDays;
    private long absentDays;
    private long lateDays;
    private long excusedDays;
    private double attendancePercentage;

    public AttendanceStatsResponse() {}

    public AttendanceStatsResponse(Long studentId, String studentCode, String studentName,
                                   long totalDays, long presentDays, long absentDays,
                                   long lateDays, long excusedDays, double attendancePercentage) {
        this.studentId = studentId;
        this.studentCode = studentCode;
        this.studentName = studentName;
        this.totalDays = totalDays;
        this.presentDays = presentDays;
        this.absentDays = absentDays;
        this.lateDays = lateDays;
        this.excusedDays = excusedDays;
        this.attendancePercentage = attendancePercentage;
    }

    public Long getStudentId() { return studentId; }
    public void setStudentId(Long studentId) { this.studentId = studentId; }

    public String getStudentCode() { return studentCode; }
    public void setStudentCode(String studentCode) { this.studentCode = studentCode; }

    public String getStudentName() { return studentName; }
    public void setStudentName(String studentName) { this.studentName = studentName; }

    public long getTotalDays() { return totalDays; }
    public void setTotalDays(long totalDays) { this.totalDays = totalDays; }

    public long getPresentDays() { return presentDays; }
    public void setPresentDays(long presentDays) { this.presentDays = presentDays; }

    public long getAbsentDays() { return absentDays; }
    public void setAbsentDays(long absentDays) { this.absentDays = absentDays; }

    public long getLateDays() { return lateDays; }
    public void setLateDays(long lateDays) { this.lateDays = lateDays; }

    public long getExcusedDays() { return excusedDays; }
    public void setExcusedDays(long excusedDays) { this.excusedDays = excusedDays; }

    public double getAttendancePercentage() { return attendancePercentage; }
    public void setAttendancePercentage(double attendancePercentage) { this.attendancePercentage = attendancePercentage; }
}
