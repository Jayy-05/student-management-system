renderNavbar("students");

let students = [];
let courses = [];
let editingId = null;
let currentPage = 0;
let totalPages = 1;
let totalElements = 0;
let pageSize = 10;

const $ = (id) => document.getElementById(id);
const form = $("studentForm");
const FIELDS = ["studentCode", "firstName", "lastName", "email", "phone",
                "dob", "gender", "address", "courseId", "admissionDate"];

// ---------- Loaders ----------

async function loadCourses() {
  try {
    courses = await CourseApi.list();
    const options = `<option value="">All courses</option>` +
      courses.map(c => `<option value="${esc(c.id)}">${esc(c.courseName)}</option>`).join("");
    $("courseFilter").innerHTML = options;

    const formOptions = `<option value="">— None —</option>` +
      courses.map(c => `<option value="${esc(c.id)}">${esc(c.courseName)}</option>`).join("");
    $("courseId").innerHTML = formOptions;
  } catch (err) {
    showToast(err.message, "error");
  }
}

async function loadStudents(page = 0) {
  currentPage = page;
  const search = $("searchInput").value.trim();
  const courseId = $("courseFilter").value;
  pageSize = Number($("pageSizeSelect").value) || 10;

  try {
    const res = await StudentApi.listPaginated({
      page: currentPage,
      size: pageSize,
      search: search || undefined,
      courseId: courseId || undefined,
      sortBy: "id",
      sortDir: "desc"
    });

    if (res && res.content) {
      students = res.content;
      totalPages = res.totalPages;
      totalElements = res.totalElements;
    } else if (Array.isArray(res)) {
      students = res;
      totalPages = 1;
      totalElements = res.length;
    } else {
      students = [];
      totalPages = 1;
      totalElements = 0;
    }

    renderTable();
    renderPagination();
  } catch (err) {
    $("studentsBody").innerHTML = `<tr><td colspan="6" class="empty">Could not load students.</td></tr>`;
    showToast(err.message, "error");
  }
}

function renderTable() {
  if (!students.length) {
    $("studentsBody").innerHTML = `<tr><td colspan="6" class="empty">No students found matching your criteria.</td></tr>`;
    return;
  }
  $("studentsBody").innerHTML = students.map(s => `
    <tr>
      <td><strong>${esc(s.studentCode)}</strong></td>
      <td>${esc(s.firstName)} ${esc(s.lastName)}</td>
      <td>${esc(s.email)}</td>
      <td>${esc(s.phone || "—")}</td>
      <td><span class="badge badge-primary">${esc(s.course ? s.course.courseName : "None")}</span></td>
      <td>
        <div class="actions">
          <button class="btn btn-sm" data-action="view" data-id="${esc(s.id)}" title="View Details">View</button>
          <button class="btn btn-sm" data-action="report" data-id="${esc(s.id)}" title="Report Card">Report Card</button>
          <button class="btn btn-sm" data-action="attendance" data-id="${esc(s.id)}" title="Attendance Log">Attendance</button>
          <button class="btn btn-sm" data-action="edit" data-id="${esc(s.id)}" title="Edit">Edit</button>
          <button class="btn btn-sm btn-danger" data-action="delete" data-id="${esc(s.id)}" title="Delete">Delete</button>
        </div>
      </td>
    </tr>`).join("");
}

function renderPagination() {
  const wrap = $("paginationWrap");
  if (totalElements === 0) {
    wrap.style.display = "none";
    return;
  }
  wrap.style.display = "flex";

  const start = currentPage * pageSize + 1;
  const end = Math.min((currentPage + 1) * pageSize, totalElements);
  $("paginationInfo").textContent = `Showing ${start} to ${end} of ${totalElements} students`;

  const controls = $("paginationControls");
  let html = `<button class="page-btn" ${currentPage === 0 ? "disabled" : ""} onclick="loadStudents(${currentPage - 1})">Prev</button>`;

  for (let i = 0; i < totalPages; i++) {
    if (i === 0 || i === totalPages - 1 || (i >= currentPage - 1 && i <= currentPage + 1)) {
      html += `<button class="page-btn ${i === currentPage ? "active" : ""}" onclick="loadStudents(${i})">${i + 1}</button>`;
    } else if (i === currentPage - 2 || i === currentPage + 2) {
      html += `<span style="padding:0 4px; color:var(--muted)">…</span>`;
    }
  }

  html += `<button class="page-btn" ${currentPage >= totalPages - 1 ? "disabled" : ""} onclick="loadStudents(${currentPage + 1})">Next</button>`;
  controls.innerHTML = html;
}

// ---------- Modals ----------

function openModal(id) { $(id).classList.add("open"); }
function closeModals() {
  document.querySelectorAll(".modal-backdrop").forEach(m => m.classList.remove("open"));
}

document.addEventListener("click", (e) => {
  if (e.target.closest("[data-close]") || e.target.classList.contains("modal-backdrop")) {
    closeModals();
  }
});
document.addEventListener("keydown", (e) => { if (e.key === "Escape") closeModals(); });

function clearErrors() {
  document.querySelectorAll("[data-error]").forEach(el => (el.textContent = ""));
}

function showFieldErrors(fieldErrors) {
  Object.entries(fieldErrors || {}).forEach(([field, message]) => {
    const el = document.querySelector(`[data-error="${field}"]`);
    if (el) el.textContent = message;
  });
}

function openAddForm() {
  editingId = null;
  form.reset();
  clearErrors();
  $("formTitle").textContent = "Add student";
  openModal("formModal");
  $("studentCode").focus();
}

function openEditForm(id) {
  const s = students.find(x => x.id === id);
  if (!s) return;
  editingId = id;
  form.reset();
  clearErrors();
  $("formTitle").textContent = "Edit student";
  FIELDS.forEach(f => {
    if (f === "courseId") $("courseId").value = s.course ? s.course.id : "";
    else $(f).value = s[f] ?? "";
  });
  openModal("formModal");
}

function openDetails(id) {
  const s = students.find(x => x.id === id);
  if (!s) return;
  const rows = [
    ["Student ID", s.studentCode],
    ["Name", `${s.firstName} ${s.lastName}`],
    ["Email", s.email],
    ["Mobile", s.phone],
    ["Date of birth", s.dob],
    ["Gender", s.gender],
    ["Course", s.course ? s.course.courseName : "None"],
    ["Admission date", s.admissionDate],
    ["Address", s.address],
  ];
  $("detailsBody").innerHTML = rows
    .map(([k, v]) => `<dt>${esc(k)}</dt><dd>${esc(v || "—")}</dd>`).join("");
  openModal("detailsModal");
}

async function openReportCard(id) {
  openModal("reportCardModal");
  $("reportCardContent").innerHTML = `<div class="empty">Loading student report card…</div>`;
  try {
    const report = await GradeApi.reportCard(id);
    const items = report.items || [];

    $("reportCardContent").innerHTML = `
      <div class="report-card">
        <div class="report-card-header">
          <h2>Academic Performance Transcript</h2>
          <p>Student Management System · Official Grade Report</p>
        </div>
        <div style="display:grid; grid-template-columns:1fr 1fr; gap:10px; margin-bottom:18px; font-size:0.92rem;">
          <div><strong>Student Name:</strong> ${esc(report.studentName)}</div>
          <div><strong>Student ID:</strong> ${esc(report.studentCode)}</div>
          <div><strong>Course Program:</strong> ${esc(report.courseName)}</div>
          <div><strong>Email:</strong> ${esc(report.email)}</div>
        </div>

        <table style="width:100%; margin-bottom:14px;">
          <thead>
            <tr>
              <th>Exam Title</th>
              <th>Date</th>
              <th>Marks</th>
              <th>Percentage</th>
              <th>Grade</th>
              <th>Result</th>
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
              </tr>
            `).join("") : `<tr><td colspan="6" class="empty">No exam grades recorded for this student yet.</td></tr>`}
          </tbody>
        </table>

        <div class="report-summary-box">
          <div>
            <div class="kpi-label">Total Marks</div>
            <div class="kpi-val">${report.totalMarksObtained} / ${report.totalMaxMarks}</div>
          </div>
          <div>
            <div class="kpi-label">Average %</div>
            <div class="kpi-val">${report.overallPercentage}%</div>
          </div>
          <div>
            <div class="kpi-label">Overall Grade</div>
            <div class="kpi-val">${esc(report.overallGrade)}</div>
          </div>
          <div>
            <div class="kpi-label">Final Status</div>
            <div class="kpi-val" style="color:${report.finalResult === 'PASS' ? 'var(--success)' : 'var(--danger)'}">
              ${esc(report.finalResult)}
            </div>
          </div>
        </div>
      </div>
    `;
  } catch (err) {
    $("reportCardContent").innerHTML = `<div class="empty">${esc(err.message)}</div>`;
  }
}

async function openAttendanceHistory(id) {
  openModal("attendanceModal");
  $("attendanceHistoryContent").innerHTML = `<div class="empty">Loading attendance history…</div>`;
  try {
    const [stats, history] = await Promise.all([
      AttendanceApi.studentStats(id),
      AttendanceApi.studentHistory(id)
    ]);

    $("attendanceHistoryContent").innerHTML = `
      <div style="margin-bottom:18px;">
        <h3 style="margin:0 0 10px;">${esc(stats.studentName)} (${esc(stats.studentCode)})</h3>
        <div style="display:flex; gap:12px; flex-wrap:wrap;">
          <span class="badge badge-primary">Total Days: ${stats.totalDays}</span>
          <span class="badge badge-present">Present: ${stats.presentDays}</span>
          <span class="badge badge-absent">Absent: ${stats.absentDays}</span>
          <span class="badge badge-late">Late: ${stats.lateDays}</span>
          <span class="badge badge-excused">Excused: ${stats.excusedDays}</span>
          <span class="badge badge-success" style="font-size:0.85rem;">Attendance Rate: ${stats.attendancePercentage}%</span>
        </div>
      </div>

      <div class="table-wrap">
        <table>
          <thead>
            <tr>
              <th>Date</th>
              <th>Status</th>
              <th>Remarks</th>
            </tr>
          </thead>
          <tbody>
            ${history.length ? history.map(h => `
              <tr>
                <td>${esc(h.attendanceDate)}</td>
                <td><span class="badge badge-${esc(h.status).toLowerCase()}">${esc(h.status)}</span></td>
                <td>${esc(h.remarks || "—")}</td>
              </tr>
            `).join("") : `<tr><td colspan="3" class="empty">No attendance records found for this student.</td></tr>`}
          </tbody>
        </table>
      </div>
    `;
  } catch (err) {
    $("attendanceHistoryContent").innerHTML = `<div class="empty">${esc(err.message)}</div>`;
  }
}

// ---------- Form Submit ----------

function readForm() {
  const data = {};
  FIELDS.forEach(f => {
    const value = $(f).value.trim();
    data[f] = value === "" ? null : value;
  });
  data.courseId = data.courseId ? Number(data.courseId) : null;
  return data;
}

function validateClientSide(data) {
  const errors = {};
  if (!data.studentCode) errors.studentCode = "Student ID is required";
  if (!data.firstName) errors.firstName = "First name is required";
  if (!data.lastName) errors.lastName = "Last name is required";
  if (!data.email) errors.email = "Email is required";
  else if (!/^[^\s@]+@[^\s@]+\.[^\s@]+$/.test(data.email)) errors.email = "Email must be valid";
  if (data.phone && !/^[0-9+\- ]{7,15}$/.test(data.phone)) errors.phone = "Phone must be 7-15 digits";
  if (data.dob && new Date(data.dob) >= new Date()) errors.dob = "Date of birth must be in the past";
  return errors;
}

form.addEventListener("submit", async (e) => {
  e.preventDefault();
  clearErrors();
  const data = readForm();
  const errors = validateClientSide(data);
  if (Object.keys(errors).length) {
    showFieldErrors(errors);
    return;
  }

  $("saveBtn").disabled = true;
  try {
    if (editingId) await StudentApi.update(editingId, data);
    else await StudentApi.create(data);
    closeModals();
    showToast(editingId ? "Student updated successfully" : "Student registered successfully");
    await loadStudents(currentPage);
  } catch (err) {
    showFieldErrors(err.fieldErrors);
    showToast(err.message, "error");
  } finally {
    $("saveBtn").disabled = false;
  }
});

// ---------- Action Click Delegations ----------

$("studentsBody").addEventListener("click", async (e) => {
  const btn = e.target.closest("button[data-action]");
  if (!btn) return;
  const id = Number(btn.dataset.id);

  if (btn.dataset.action === "view") openDetails(id);
  else if (btn.dataset.action === "edit") openEditForm(id);
  else if (btn.dataset.action === "report") openReportCard(id);
  else if (btn.dataset.action === "attendance") openAttendanceHistory(id);
  else if (btn.dataset.action === "delete") {
    const s = students.find(x => x.id === id);
    const name = s ? `${s.firstName} ${s.lastName}` : "this student";
    if (!confirm(`Are you sure you want to delete ${name}? All associated attendance and grades will also be affected.`)) return;
    try {
      await StudentApi.remove(id);
      showToast("Student record deleted");
      await loadStudents(currentPage);
    } catch (err) {
      showToast(err.message, "error");
    }
  }
});

// ---------- Filters & Search ----------

let searchTimer;
$("searchInput").addEventListener("input", () => {
  clearTimeout(searchTimer);
  searchTimer = setTimeout(() => loadStudents(0), 300);
});

$("courseFilter").addEventListener("change", () => loadStudents(0));
$("pageSizeSelect").addEventListener("change", () => loadStudents(0));
$("addBtn").addEventListener("click", openAddForm);

// ---------- Init ----------

(async function init() {
  await loadCourses();
  await loadStudents(0);
  if (new URLSearchParams(location.search).get("new") === "1") openAddForm();
})();
