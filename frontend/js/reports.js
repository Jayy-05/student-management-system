renderNavbar("reports");

const $ = (id) => document.getElementById(id);

let students = [];

async function loadDashboardMetrics() {
  try {
    const data = await ReportApi.dashboard();

    $("kpiStudents").textContent = data.totalStudents;
    $("kpiCourses").textContent = data.totalCourses;

    const att = data.attendanceStats || {};
    if (att.totalMarkedToday > 0) {
      $("kpiAttendance").textContent = att.todayAttendanceRate + "%";
      $("kpiAttendanceHint").textContent = `${att.presentToday} Present · ${att.absentToday} Absent · ${att.lateToday} Late`;
    } else {
      $("kpiAttendance").textContent = "—";
      $("kpiAttendanceHint").textContent = "No attendance marked yet today";
    }

    const grades = data.gradeStats || {};
    if (grades.totalGradesRecorded > 0) {
      $("kpiPassRate").textContent = grades.overallPassRate + "%";
      $("kpiPassRateHint").textContent = `${grades.passedCount} Passed · ${grades.failedCount} Failed out of ${grades.totalGradesRecorded}`;
    } else {
      $("kpiPassRate").textContent = "—";
      $("kpiPassRateHint").textContent = "No exams recorded yet";
    }

    // 1. Course Enrollment Distribution
    const dist = data.courseDistribution || [];
    const maxCourse = Math.max(...dist.map(c => c.studentCount), 1);
    $("courseDistribution").innerHTML = dist.length
      ? dist.map(c => {
          const pct = Math.round((c.studentCount / maxCourse) * 100);
          return `
            <div class="progress-item">
              <div class="progress-header">
                <span><strong>${esc(c.courseName)}</strong></span>
                <span style="color:var(--muted)">${c.studentCount} enrolled (${data.totalStudents > 0 ? Math.round((c.studentCount / data.totalStudents) * 100) : 0}%)</span>
              </div>
              <div class="progress-track">
                <div class="progress-fill" style="width: ${pct}%"></div>
              </div>
            </div>`;
        }).join("")
      : `<div class="empty">No courses created yet.</div>`;

    // 2. Grade Distribution
    const gradeDist = grades.gradeDistribution || {};
    const totalGrades = grades.totalGradesRecorded || 0;
    const gradeLetters = ["A+", "A", "B", "C", "D", "F"];

    $("gradeDistribution").innerHTML = totalGrades > 0
      ? gradeLetters.map(letter => {
          const count = gradeDist[letter] || 0;
          const pct = Math.round((count / totalGrades) * 100);
          const colorClass = (letter === "A+" || letter === "A") ? "success" : (letter === "F" ? "danger" : "warning");
          return `
            <div class="progress-item">
              <div class="progress-header">
                <span><span class="grade-badge grade-${letter.replace('+', '-plus')}">${letter}</span></span>
                <span style="color:var(--muted)">${count} grades (${pct}%)</span>
              </div>
              <div class="progress-track">
                <div class="progress-fill ${colorClass}" style="width: ${pct}%"></div>
              </div>
            </div>`;
        }).join("")
      : `<div class="empty">No exam grades recorded yet to display distribution.</div>`;

  } catch (err) {
    showToast(err.message, "error");
  }
}

async function loadStudentSelector() {
  try {
    students = await StudentApi.list();
    $("studentSelect").innerHTML = `<option value="">— Select a student —</option>` +
      students.map(s => `<option value="${esc(s.id)}">${esc(s.studentCode)} - ${esc(s.firstName)} ${esc(s.lastName)} (${esc(s.course ? s.course.courseName : 'No course')})</option>`).join("");
  } catch (err) {
    showToast(err.message, "error");
  }
}

async function generateReportCard() {
  const studentId = $("studentSelect").value;
  if (!studentId) {
    showToast("Please select a student first.", "error");
    return;
  }

  $("transcriptContainer").innerHTML = `<div class="empty">Generating academic transcript…</div>`;

  try {
    const report = await GradeApi.reportCard(studentId);
    const items = report.items || [];

    $("transcriptContainer").innerHTML = `
      <div class="report-card">
        <div style="display:flex; justify-content:flex-end; margin-bottom:10px;">
          <button class="btn btn-primary btn-sm" onclick="window.print()">🖨️ Print Transcript / Save PDF</button>
        </div>
        <div class="report-card-header">
          <h2>Academic Performance Transcript</h2>
          <p>Student Management System · Official Grade Report</p>
        </div>
        <div style="display:grid; grid-template-columns:1fr 1fr; gap:10px; margin-bottom:18px; font-size:0.92rem;">
          <div><strong>Student Name:</strong> ${esc(report.studentName)}</div>
          <div><strong>Student ID:</strong> ${esc(report.studentCode)}</div>
          <div><strong>Course Program:</strong> ${esc(report.courseName)}</div>
          <div><strong>Email Address:</strong> ${esc(report.email)}</div>
        </div>

        <table style="width:100%; margin-bottom:16px;">
          <thead>
            <tr>
              <th>Assessment / Exam</th>
              <th>Date</th>
              <th>Marks Obtained</th>
              <th>Percentage</th>
              <th>Grade</th>
              <th>Result</th>
              <th>Remarks</th>
            </tr>
          </thead>
          <tbody>
            ${items.length ? items.map(item => `
              <tr>
                <td><strong>${esc(item.examTitle)}</strong></td>
                <td>${esc(item.examDate)}</td>
                <td>${item.marksObtained} / ${item.maxMarks}</td>
                <td>${item.percentage}%</td>
                <td><span class="grade-badge grade-${esc(item.gradeLetter).replace('+', '-plus')}">${esc(item.gradeLetter)}</span></td>
                <td><span class="badge ${item.status === 'PASS' ? 'badge-success' : 'badge-danger'}">${esc(item.status)}</span></td>
                <td>${esc(item.remarks || "—")}</td>
              </tr>
            `).join("") : `<tr><td colspan="7" class="empty">No examination records found for this student.</td></tr>`}
          </tbody>
        </table>

        <div class="report-summary-box">
          <div>
            <div class="kpi-label">Total Marks</div>
            <div class="kpi-val">${report.totalMarksObtained} / ${report.totalMaxMarks}</div>
          </div>
          <div>
            <div class="kpi-label">Cumulative %</div>
            <div class="kpi-val">${report.overallPercentage}%</div>
          </div>
          <div>
            <div class="kpi-label">Cumulative Grade</div>
            <div class="kpi-val">${esc(report.overallGrade)}</div>
          </div>
          <div>
            <div class="kpi-label">Final Outcome</div>
            <div class="kpi-val" style="color:${report.finalResult === 'PASS' ? 'var(--success)' : 'var(--danger)'}">
              ${esc(report.finalResult)}
            </div>
          </div>
        </div>
      </div>
    `;
  } catch (err) {
    $("transcriptContainer").innerHTML = `<div class="empty">${esc(err.message)}</div>`;
  }
}

$("generateReportBtn").addEventListener("click", generateReportCard);

(async function init() {
  await loadDashboardMetrics();
  await loadStudentSelector();
})();
