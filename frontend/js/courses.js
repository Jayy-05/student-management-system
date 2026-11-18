renderNavbar("courses");

const $ = (id) => document.getElementById(id);
let courses = [];
let studentCountMap = {};

async function loadCourses() {
  try {
    const [courseList, reportData] = await Promise.all([
      CourseApi.list(),
      ReportApi.dashboard().catch(() => null)
    ]);

    courses = courseList;
    studentCountMap = {};
    if (reportData && reportData.courseDistribution) {
      reportData.courseDistribution.forEach(c => {
        studentCountMap[c.id] = c.studentCount;
      });
    }

    $("coursesBody").innerHTML = courses.length
      ? courses.map(c => {
          const count = studentCountMap[c.id] || 0;
          return `
            <tr>
              <td><strong>${esc(c.courseName)}</strong></td>
              <td>${esc(c.duration || "—")}</td>
              <td><span class="badge badge-primary">${count} student${count === 1 ? '' : 's'}</span></td>
              <td>${esc(c.description || "—")}</td>
              <td>
                <button class="btn btn-sm btn-danger" data-id="${esc(c.id)}" data-name="${esc(c.courseName)}">
                  Delete
                </button>
              </td>
            </tr>`;
        }).join("")
      : `<tr><td colspan="5" class="empty">No courses created yet. Add one above.</td></tr>`;
  } catch (err) {
    $("coursesBody").innerHTML = `<tr><td colspan="5" class="empty">Could not load courses.</td></tr>`;
    showToast(err.message, "error");
  }
}

function clearErrors() {
  document.querySelectorAll("[data-error]").forEach(el => (el.textContent = ""));
}

$("courseForm").addEventListener("submit", async (e) => {
  e.preventDefault();
  clearErrors();

  const courseName = $("courseName").value.trim();
  if (!courseName) {
    document.querySelector('[data-error="courseName"]').textContent = "Course name is required";
    return;
  }

  $("saveCourseBtn").disabled = true;
  try {
    await CourseApi.create({
      courseName,
      duration: $("duration").value.trim() || null,
      description: $("description").value.trim() || null,
    });
    $("courseForm").reset();
    showToast("Course added successfully");
    await loadCourses();
  } catch (err) {
    Object.entries(err.fieldErrors || {}).forEach(([field, msg]) => {
      const el = document.querySelector(`[data-error="${field}"]`);
      if (el) el.textContent = msg;
    });
    showToast(err.message, "error");
  } finally {
    $("saveCourseBtn").disabled = false;
  }
});

$("coursesBody").addEventListener("click", async (e) => {
  const btn = e.target.closest("button[data-id]");
  if (!btn) return;
  const courseId = Number(btn.dataset.id);
  const courseName = btn.dataset.name || "this course";

  if (!confirm(`Are you sure you want to delete course "${courseName}"?`)) return;

  try {
    await CourseApi.remove(courseId);
    showToast("Course deleted");
    await loadCourses();
  } catch (err) {
    showToast(err.message, "error");
  }
});

loadCourses();
