package com.project.studentmanagement.dto;

import java.util.List;

public class StudentReportCardResponse {

    public static class GradeItem {
        private Long examId;
        private String examTitle;
        private String courseName;
        private String examDate;
        private Double marksObtained;
        private Double maxMarks;
        private Double passingMarks;
        private Double percentage;
        private String gradeLetter;
        private String status; // PASS, FAIL
        private String remarks;

        public GradeItem() {}

        public GradeItem(Long examId, String examTitle, String courseName, String examDate,
                         Double marksObtained, Double maxMarks, Double passingMarks,
                         Double percentage, String gradeLetter, String status, String remarks) {
            this.examId = examId;
            this.examTitle = examTitle;
            this.courseName = courseName;
            this.examDate = examDate;
            this.marksObtained = marksObtained;
            this.maxMarks = maxMarks;
            this.passingMarks = passingMarks;
            this.percentage = percentage;
            this.gradeLetter = gradeLetter;
            this.status = status;
            this.remarks = remarks;
        }

        public Long getExamId() { return examId; }
        public String getExamTitle() { return examTitle; }
        public String getCourseName() { return courseName; }
        public String getExamDate() { return examDate; }
        public Double getMarksObtained() { return marksObtained; }
        public Double getMaxMarks() { return maxMarks; }
        public Double getPassingMarks() { return passingMarks; }
        public Double getPercentage() { return percentage; }
        public String getGradeLetter() { return gradeLetter; }
        public String getStatus() { return status; }
        public String getRemarks() { return remarks; }
    }

    private Long studentId;
    private String studentCode;
    private String studentName;
    private String email;
    private String courseName;
    private List<GradeItem> items;
    private Double totalMarksObtained;
    private Double totalMaxMarks;
    private Double overallPercentage;
    private String overallGrade;
    private int passedCount;
    private int failedCount;
    private String finalResult; // PASS, FAIL

    public StudentReportCardResponse() {}

    public Long getStudentId() { return studentId; }
    public void setStudentId(Long studentId) { this.studentId = studentId; }

    public String getStudentCode() { return studentCode; }
    public void setStudentCode(String studentCode) { this.studentCode = studentCode; }

    public String getStudentName() { return studentName; }
    public void setStudentName(String studentName) { this.studentName = studentName; }

    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }

    public String getCourseName() { return courseName; }
    public void setCourseName(String courseName) { this.courseName = courseName; }

    public List<GradeItem> getItems() { return items; }
    public void setItems(List<GradeItem> items) { this.items = items; }

    public Double getTotalMarksObtained() { return totalMarksObtained; }
    public void setTotalMarksObtained(Double totalMarksObtained) { this.totalMarksObtained = totalMarksObtained; }

    public Double getTotalMaxMarks() { return totalMaxMarks; }
    public void setTotalMaxMarks(Double totalMaxMarks) { this.totalMaxMarks = totalMaxMarks; }

    public Double getOverallPercentage() { return overallPercentage; }
    public void setOverallPercentage(Double overallPercentage) { this.overallPercentage = overallPercentage; }

    public String getOverallGrade() { return overallGrade; }
    public void setOverallGrade(String overallGrade) { this.overallGrade = overallGrade; }

    public int getPassedCount() { return passedCount; }
    public void setPassedCount(int passedCount) { this.passedCount = passedCount; }

    public int getFailedCount() { return failedCount; }
    public void setFailedCount(int failedCount) { this.failedCount = failedCount; }

    public String getFinalResult() { return finalResult; }
    public void setFinalResult(String finalResult) { this.finalResult = finalResult; }
}
