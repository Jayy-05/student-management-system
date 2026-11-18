renderNavbar("exams");

const $ = (id) => document.getElementById(id);

let exams = [];
let courses = [];
let selectedExam = null;
let currentStudents = [];
let gradeMap = {}; // studentId -> { marksObtained, remarks }

// ---------- Modal Helpers ----------

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
  document.querySelectorAll("[data-error]").forEach(el => el.textContent = "");
}

// ---------- Loading & Rendering Exams ----------

async function loadCourses() {
  try {
    courses = await CourseApi.list();
    $("examCourseId").innerHTML = `<option value="">— All Courses —</option>` +
      courses.map(c => `<option value="${esc(c.id)}">${esc(c.courseName)}</option>`).join("");
  } catch (err) {
    showToast(err.message, "error");
  }
}

async function loadExams(selectExamId = null) {
  try {
    exams = await ExamApi.list();

    // Render table
    $("examsBody").innerHTML = exams.length
      ? exams.map(e => `
          <tr>
            <td><strong>${esc(e.title)}</strong></td>
            <td><span class="badge badge-primary">${esc(e.course ? e.course.courseName : "All Courses")}</span></td>
            <td>${esc(e.examDate)}</td>
            <td>${e.maxMarks}</td>
            <td>${e.passingMarks}</td>
            <td>
              <div class="actions">
                <button class="btn btn-sm btn-primary" data-action="grade" data-id="${esc(e.id)}">Record Grades</button>
                <button class="btn btn-sm btn-danger" data-action="delete" data-id="${esc(e.id)}" data-title="${esc(e.title)}">Delete</button>
              </div>
            </td>
          </tr>
        `).join("")
      : `<tr><td colspan="6" class="empty">No examinations scheduled yet. Click "+ Create new exam" to add one.</td></tr>`;

    // Populate dropdown
    const selectEl = $("selectExamForGrading");
    selectEl.innerHTML = `<option value="">— Select an examination —</option>` +
      exams.map(e => `<option value="${esc(e.id)}" ${selectExamId === e.id ? 'selected' : ''}>${esc(e.title)} (${esc(e.course ? e.course.courseName : "General")})</option>`).join("");

    if (selectExamId) {
      selectEl.value = selectExamId;
      await onExamSelected(selectExamId);
    } else if (exams.length > 0 && !selectEl.value) {
      selectEl.value = exams[0].id;
      await onExamSelected(exams[0].id);
    }
  } catch (err) {
    $("examsBody").innerHTML = `<tr><td colspan="6" class="empty">Could not load examinations.</td></tr>`;
    showToast(err.message, "error");
  }
}

// ---------- Grade Entry Logic ----------

function getGradeLetter(pct) {
  if (pct >= 90) return "A+";
  if (pct >= 80) return "A";
  if (pct >= 70) return "B";
  if (pct >= 60) return "C";
  if (pct >= 50) return "D";
  return "F";
}

async function onExamSelected(examId) {
  if (!examId) {
    selectedExam = null;
    $("selectedExamMeta").textContent = "Select an exam to record marks";
    $("gradesBody").innerHTML = `<tr><td colspan="7" class="empty">Please select an exam above.</td></tr>`;
    $("saveGradesBtn").disabled = true;
    $("bottomSaveGradesBtn").disabled = true;
    return;
  }

  selectedExam = exams.find(e => e.id === Number(examId));
  if (!selectedExam) return;

  $("selectedExamMeta").textContent =
    `Course: ${selectedExam.course ? selectedExam.course.courseName : 'All Courses'} · Date: ${selectedExam.examDate} · Max Marks: ${selectedExam.maxMarks} · Passing: ${selectedExam.passingMarks}`;

  $("gradesBody").innerHTML = `<tr><td colspan="7" class="empty">Loading enrolled students…</td></tr>`;

  try {
    // 1. Fetch eligible students
    const allStudents = await StudentApi.list();
    if (selectedExam.course && selectedExam.course.id) {
      currentStudents = allStudents.filter(s => s.course && s.course.id === selectedExam.course.id);
    } else {
      currentStudents = allStudents;
    }

    // 2. Fetch existing grades for this exam
    const existingGrades = await GradeApi.byExam(selectedExam.id);
    gradeMap = {};
    existingGrades.forEach(g => {
      if (g.student && g.student.id) {
        gradeMap[g.student.id] = {
          marksObtained: g.marksObtained,
          remarks: g.remarks || ""
        };
      }
    });

    renderGradesTable();
    $("saveGradesBtn").disabled = currentStudents.length === 0;
    $("bottomSaveGradesBtn").disabled = currentStudents.length === 0;
  } catch (err) {
    $("gradesBody").innerHTML = `<tr><td colspan="7" class="empty">Could not load students or grades.</td></tr>`;
    showToast(err.message, "error");
  }
}

function renderGradesTable() {
  if (!currentStudents.length) {
    $("gradesBody").innerHTML = `<tr><td colspan="7" class="empty">No students enrolled in this course yet.</td></tr>`;
    return;
  }

  $("gradesBody").innerHTML = currentStudents.map(s => {
    const entry = gradeMap[s.id] || { marksObtained: null, remarks: "" };
    const hasMarks = entry.marksObtained !== null && entry.marksObtained !== undefined && entry.marksObtained !== "";
    const marksNum = hasMarks ? Number(entry.marksObtained) : 0;
    const max = selectedExam.maxMarks || 100;
    const passing = selectedExam.passingMarks || 40;
    const pct = hasMarks ? Math.round((marksNum / max) * 1000) / 10 : null;
    const letter = hasMarks ? getGradeLetter(pct) : "—";
    const isPass = hasMarks && marksNum >= passing;

    return `
      <tr data-student-id="${esc(s.id)}">
        <td><strong>${esc(s.studentCode)}</strong></td>
        <td>${esc(s.firstName)} ${esc(s.lastName)}</td>
        <td style="width:140px;">
          <input type="number" step="0.5" min="0" max="${max}" class="marks-input"
                 data-student="${esc(s.id)}" value="${hasMarks ? marksNum : ''}"
                 placeholder="0 - ${max}" style="padding:6px 10px; width:110px;">
        </td>
        <td class="pct-cell" data-student="${esc(s.id)}">${hasMarks ? pct + '%' : '—'}</td>
        <td class="letter-cell" data-student="${esc(s.id)}">
          ${hasMarks ? `<span class="grade-badge grade-${letter.replace('+', '-plus')}">${letter}</span>` : '—'}
        </td>
        <td class="status-cell" data-student="${esc(s.id)}">
          ${hasMarks ? `<span class="badge ${isPass ? 'badge-success' : 'badge-danger'}">${isPass ? 'PASS' : 'FAIL'}</span>` : '<span style="color:var(--muted)">Pending</span>'}
        </td>
        <td>
          <input type="text" class="grade-remarks" data-student="${esc(s.id)}"
                 value="${esc(entry.remarks)}" placeholder="Optional remark" style="max-width:260px; padding:6px 10px; font-size:0.85rem;">
        </td>
      </tr>
    `;
  }).join("");
}

// Live recalculation on typing marks
$("gradesBody").addEventListener("input", (e) => {
  if (e.target.classList.contains("marks-input")) {
    const studentId = Number(e.target.dataset.student);
    const valStr = e.target.value.trim();
    const max = selectedExam.maxMarks || 100;
    const passing = selectedExam.passingMarks || 40;

    const row = e.target.closest("tr");
    const pctCell = row.querySelector(".pct-cell");
    const letterCell = row.querySelector(".letter-cell");
    const statusCell = row.querySelector(".status-cell");

    if (!gradeMap[studentId]) gradeMap[studentId] = { remarks: "" };

    if (valStr === "") {
      gradeMap[studentId].marksObtained = null;
      pctCell.textContent = "—";
      letterCell.textContent = "—";
      statusCell.innerHTML = `<span style="color:var(--muted)">Pending</span>`;
    } else {
      let num = Number(valStr);
      if (num < 0) num = 0;
      if (num > max) {
        num = max;
        e.target.value = max;
      }
      gradeMap[studentId].marksObtained = num;

      const pct = Math.round((num / max) * 1000) / 10;
      const letter = getGradeLetter(pct);
      const isPass = num >= passing;

      pctCell.textContent = pct + "%";
      letterCell.innerHTML = `<span class="grade-badge grade-${letter.replace('+', '-plus')}">${letter}</span>`;
      statusCell.innerHTML = `<span class="badge ${isPass ? 'badge-success' : 'badge-danger'}">${isPass ? 'PASS' : 'FAIL'}</span>`;
    }
  } else if (e.target.classList.contains("grade-remarks")) {
    const studentId = Number(e.target.dataset.student);
    if (!gradeMap[studentId]) gradeMap[studentId] = { marksObtained: null };
    gradeMap[studentId].remarks = e.target.value.trim();
  }
});

// Dropdown change
$("selectExamForGrading").addEventListener("change", (e) => {
  onExamSelected(e.target.value);
});

// Save all grades
async function saveGrades() {
  if (!selectedExam) return;

  const validEntries = [];
  currentStudents.forEach(s => {
    const entry = gradeMap[s.id];
    if (entry && entry.marksObtained !== null && entry.marksObtained !== undefined && entry.marksObtained !== "") {
      validEntries.push({
        studentId: s.id,
        marksObtained: Number(entry.marksObtained),
        remarks: entry.remarks || null
      });
    }
  });

  if (!validEntries.length) {
    showToast("Please enter marks for at least one student before saving.", "error");
    return;
  }

  const saveBtns = [$("saveGradesBtn"), $("bottomSaveGradesBtn")];
  saveBtns.forEach(b => {
    b.disabled = true;
    b.textContent = "Saving…";
  });

  try {
    await GradeApi.batch({
      examId: selectedExam.id,
      grades: validEntries
    });
    showToast(`Successfully saved grades for ${validEntries.length} student(s)!`);
    await onExamSelected(selectedExam.id);
  } catch (err) {
    showToast(err.message, "error");
  } finally {
    saveBtns.forEach(b => {
      b.disabled = false;
      b.textContent = "💾 Save All Grades";
    });
  }
}

$("saveGradesBtn").addEventListener("click", saveGrades);
$("bottomSaveGradesBtn").addEventListener("click", saveGrades);

// ---------- Create Exam Modal & Submit ----------

$("createExamBtn").addEventListener("click", () => {
  $("examForm").reset();
  clearErrors();
  $("examDate").value = new Date().toISOString().split("T")[0];
  openModal("examModal");
  $("examTitle").focus();
});

$("examForm").addEventListener("submit", async (e) => {
  e.preventDefault();
  clearErrors();

  const title = $("examTitle").value.trim();
  const courseId = $("examCourseId").value ? Number($("examCourseId").value) : null;
  const examDate = $("examDate").value;
  const maxMarks = Number($("maxMarks").value);
  const passingMarks = Number($("passingMarks").value);
  const description = $("examDescription").value.trim();

  let hasError = false;
  if (!title) {
    document.querySelector('[data-error="title"]').textContent = "Exam title is required";
    hasError = true;
  }
  if (!examDate) {
    document.querySelector('[data-error="examDate"]').textContent = "Exam date is required";
    hasError = true;
  }
  if (!maxMarks || maxMarks <= 0) {
    document.querySelector('[data-error="maxMarks"]').textContent = "Max marks must be greater than 0";
    hasError = true;
  }
  if (!passingMarks || passingMarks <= 0 || passingMarks > maxMarks) {
    document.querySelector('[data-error="passingMarks"]').textContent = "Passing marks must be between 1 and max marks";
    hasError = true;
  }
  if (hasError) return;

  $("saveExamBtn").disabled = true;
  try {
    const created = await ExamApi.create({
      title,
      courseId,
      examDate,
      maxMarks,
      passingMarks,
      description: description || null
    });
    closeModals();
    showToast("Examination created successfully!");
    await loadExams(created.id);
  } catch (err) {
    if (err.fieldErrors) {
      Object.entries(err.fieldErrors).forEach(([field, msg]) => {
        const el = document.querySelector(`[data-error="${field}"]`);
        if (el) el.textContent = msg;
      });
    }
    showToast(err.message, "error");
  } finally {
    $("saveExamBtn").disabled = false;
  }
});

// ---------- Action Delegations on Exams Table ----------

$("examsBody").addEventListener("click", async (e) => {
  const btn = e.target.closest("button[data-action]");
  if (!btn) return;
  const examId = Number(btn.dataset.id);

  if (btn.dataset.action === "grade") {
    $("selectExamForGrading").value = examId;
    await onExamSelected(examId);
    $("gradingSection").scrollIntoView({ behavior: "smooth" });
  } else if (btn.dataset.action === "delete") {
    const title = btn.dataset.title || "this examination";
    if (!confirm(`Are you sure you want to delete exam "${title}"? All student grades for this exam will also be deleted.`)) return;

    try {
      await ExamApi.remove(examId);
      showToast("Examination deleted");
      await loadExams();
    } catch (err) {
      showToast(err.message, "error");
    }
  }
});

// Init
(async function init() {
  await loadCourses();
  await loadExams();
})();
