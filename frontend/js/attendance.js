renderNavbar("attendance");

const $ = (id) => document.getElementById(id);

let students = [];
let attendanceMap = {}; // studentId -> { status, remarks }

// Initialize date input to today
const todayStr = new Date().toISOString().split("T")[0];
$("attendanceDate").value = todayStr;

async function loadCourses() {
  try {
    const courses = await CourseApi.list();
    $("courseSelect").innerHTML = `<option value="">All courses</option>` +
      courses.map(c => `<option value="${esc(c.id)}">${esc(c.courseName)}</option>`).join("");
  } catch (err) {
    showToast(err.message, "error");
  }
}

async function loadAttendanceRegister() {
  const date = $("attendanceDate").value || todayStr;
  const courseId = $("courseSelect").value;

  $("attendanceBody").innerHTML = `<tr><td colspan="5" class="empty">Loading register for ${esc(date)}…</td></tr>`;

  try {
    // 1. Fetch all students (or filtered by course)
    const allStudents = await StudentApi.list();
    students = courseId ? allStudents.filter(s => s.course && s.course.id === Number(courseId)) : allStudents;

    // 2. Fetch existing attendance records for date and course
    const records = await AttendanceApi.list(date, courseId || undefined);

    // Build map of existing attendance
    attendanceMap = {};
    records.forEach(r => {
      if (r.student && r.student.id) {
        attendanceMap[r.student.id] = {
          status: r.status,
          remarks: r.remarks || ""
        };
      }
    });

    // Default any unmarked students to PRESENT
    students.forEach(s => {
      if (!attendanceMap[s.id]) {
        attendanceMap[s.id] = { status: "PRESENT", remarks: "" };
      }
    });

    renderRegister();
    updateKpis();
  } catch (err) {
    $("attendanceBody").innerHTML = `<tr><td colspan="5" class="empty">Could not load register.</td></tr>`;
    showToast(err.message, "error");
  }
}

function renderRegister() {
  if (!students.length) {
    $("attendanceBody").innerHTML = `<tr><td colspan="5" class="empty">No students found for this course.</td></tr>`;
    return;
  }

  $("attendanceBody").innerHTML = students.map(s => {
    const current = attendanceMap[s.id] || { status: "PRESENT", remarks: "" };
    return `
      <tr data-student-id="${esc(s.id)}">
        <td><strong>${esc(s.studentCode)}</strong></td>
        <td>${esc(s.firstName)} ${esc(s.lastName)}</td>
        <td><span class="badge badge-primary">${esc(s.course ? s.course.courseName : "None")}</span></td>
        <td>
          <div class="status-pill-group" data-student="${esc(s.id)}">
            <button type="button" class="pill-btn ${current.status === 'PRESENT' ? 'active present' : ''}" data-status="PRESENT">Present</button>
            <button type="button" class="pill-btn ${current.status === 'ABSENT' ? 'active absent' : ''}" data-status="ABSENT">Absent</button>
            <button type="button" class="pill-btn ${current.status === 'LATE' ? 'active late' : ''}" data-status="LATE">Late</button>
            <button type="button" class="pill-btn ${current.status === 'EXCUSED' ? 'active excused' : ''}" data-status="EXCUSED">Excused</button>
          </div>
        </td>
        <td>
          <input type="text" class="remarks-input" data-student="${esc(s.id)}"
                 value="${esc(current.remarks)}" placeholder="Optional remark" style="max-width:280px; padding:6px 10px; font-size:0.85rem;">
        </td>
      </tr>
    `;
  }).join("");
}

function updateKpis() {
  const total = students.length;
  let present = 0;
  let absent = 0;
  let late = 0;
  let excused = 0;

  Object.values(attendanceMap).forEach(v => {
    if (v.status === "PRESENT") present++;
    else if (v.status === "ABSENT") absent++;
    else if (v.status === "LATE") late++;
    else if (v.status === "EXCUSED") excused++;
  });

  const rate = (total === 0) ? 0 : Math.round(((present + late) / total) * 100);

  $("kpiTotal").textContent = total;
  $("kpiPresent").textContent = present;
  $("kpiAbsent").textContent = absent;
  $("kpiLate").textContent = late + excused;
  $("kpiRate").textContent = rate + "%";
}

// Delegate pill clicks
$("attendanceBody").addEventListener("click", (e) => {
  const btn = e.target.closest(".pill-btn");
  if (!btn) return;
  const group = btn.closest(".status-pill-group");
  const studentId = Number(group.dataset.student);
  const status = btn.dataset.status;

  // Toggle active class in UI
  group.querySelectorAll(".pill-btn").forEach(b => {
    b.className = "pill-btn";
  });
  btn.className = `pill-btn active ${status.toLowerCase()}`;

  // Update map
  if (!attendanceMap[studentId]) attendanceMap[studentId] = { remarks: "" };
  attendanceMap[studentId].status = status;

  updateKpis();
});

// Delegate remarks changes
$("attendanceBody").addEventListener("input", (e) => {
  if (e.target.classList.contains("remarks-input")) {
    const studentId = Number(e.target.dataset.student);
    if (!attendanceMap[studentId]) attendanceMap[studentId] = { status: "PRESENT" };
    attendanceMap[studentId].remarks = e.target.value.trim();
  }
});

// Quick action buttons
$("markAllPresentBtn").addEventListener("click", () => {
  students.forEach(s => {
    if (!attendanceMap[s.id]) attendanceMap[s.id] = { remarks: "" };
    attendanceMap[s.id].status = "PRESENT";
  });
  renderRegister();
  updateKpis();
});

$("markAllAbsentBtn").addEventListener("click", () => {
  students.forEach(s => {
    if (!attendanceMap[s.id]) attendanceMap[s.id] = { remarks: "" };
    attendanceMap[s.id].status = "ABSENT";
  });
  renderRegister();
  updateKpis();
});

// Save handler
async function saveAttendance() {
  if (!students.length) {
    showToast("No students to save attendance for.", "error");
    return;
  }

  const date = $("attendanceDate").value || todayStr;
  const records = students.map(s => {
    const data = attendanceMap[s.id] || { status: "PRESENT", remarks: "" };
    return {
      studentId: s.id,
      status: data.status,
      remarks: data.remarks || null
    };
  });

  const saveBtns = [$("saveAttendanceBtn"), $("bottomSaveBtn")];
  saveBtns.forEach(b => {
    b.disabled = true;
    b.textContent = "Saving…";
  });

  try {
    await AttendanceApi.batch({
      attendanceDate: date,
      records: records
    });
    showToast(`Attendance saved successfully for ${date}!`);
    await loadAttendanceRegister();
  } catch (err) {
    showToast(err.message, "error");
  } finally {
    saveBtns.forEach(b => {
      b.disabled = false;
      b.textContent = "💾 Save Attendance";
    });
  }
}

$("saveAttendanceBtn").addEventListener("click", saveAttendance);
$("bottomSaveBtn").addEventListener("click", saveAttendance);

$("attendanceDate").addEventListener("change", loadAttendanceRegister);
$("courseSelect").addEventListener("change", loadAttendanceRegister);

// Init
(async function init() {
  await loadCourses();
  await loadAttendanceRegister();
})();
