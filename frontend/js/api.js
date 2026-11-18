// API Base URL - update if backend runs elsewhere
const API_BASE = "http://localhost:8080/api";

const TOKEN_KEY = "sms_auth_token";
const USER_KEY = "sms_auth_user";

/**
 * Enhanced fetch wrapper supporting authentication headers,
 * JSON error payload parsing, and standard HTTP error propagation.
 */
async function apiRequest(path, options = {}) {
  const token = localStorage.getItem(TOKEN_KEY);
  const headers = {
    "Content-Type": "application/json",
    ...(token ? { "Authorization": `Bearer ${token}`, "X-Auth-Token": token } : {}),
    ...(options.headers || {})
  };

  let response;
  try {
    response = await fetch(API_BASE + path, {
      ...options,
      headers
    });
  } catch (networkError) {
    throw new Error("Cannot reach the server. Is the backend running on port 8080?");
  }

  if (response.status === 204) return null;

  let body = null;
  try { body = await response.json(); } catch (_) { /* no json */ }

  if (!response.ok) {
    if (response.status === 401 && !path.startsWith("/auth/login")) {
      // Clear token if expired or invalid
      localStorage.removeItem(TOKEN_KEY);
      localStorage.removeItem(USER_KEY);
      const isLogin = window.location.pathname.endsWith("login.html");
      if (!isLogin && options.method && options.method !== "GET") {
        window.location.href = "login.html?expired=1";
      }
    }
    const error = new Error((body && body.message) || `Request failed (${response.status})`);
    error.fieldErrors = (body && body.errors) || {};
    error.status = response.status;
    throw error;
  }
  return body;
}

// ---------- Auth API ----------
const AuthApi = {
  login: async (username, password) => {
    const res = await apiRequest("/auth/login", {
      method: "POST",
      body: JSON.stringify({ username, password })
    });
    if (res && res.token) {
      localStorage.setItem(TOKEN_KEY, res.token);
      localStorage.setItem(USER_KEY, JSON.stringify({
        username: res.username,
        fullName: res.fullName,
        role: res.role
      }));
    }
    return res;
  },
  logout: async () => {
    try {
      await apiRequest("/auth/logout", { method: "POST" });
    } catch (_) {}
    localStorage.removeItem(TOKEN_KEY);
    localStorage.removeItem(USER_KEY);
    window.location.href = "login.html";
  },
  me: () => apiRequest("/auth/me"),
  changePassword: (oldPassword, newPassword) =>
    apiRequest("/auth/change-password", {
      method: "PUT",
      body: JSON.stringify({ oldPassword, newPassword })
    }),
  getToken: () => localStorage.getItem(TOKEN_KEY),
  getUser: () => {
    try {
      const u = localStorage.getItem(USER_KEY);
      return u ? JSON.parse(u) : null;
    } catch (_) { return null; }
  },
  isAuthenticated: () => !!localStorage.getItem(TOKEN_KEY)
};

// ---------- Student API ----------
const StudentApi = {
  list: () => apiRequest("/students"),
  listPaginated: (params = {}) => {
    const qs = new URLSearchParams();
    if (params.page !== undefined) qs.set("page", params.page);
    if (params.size !== undefined) qs.set("size", params.size);
    if (params.search) qs.set("search", params.search);
    if (params.courseId) qs.set("courseId", params.courseId);
    if (params.sortBy) qs.set("sortBy", params.sortBy);
    if (params.sortDir) qs.set("sortDir", params.sortDir);
    return apiRequest(`/students?${qs.toString()}`);
  },
  get: (id) => apiRequest(`/students/${id}`),
  search: (q) => apiRequest(`/students/search?name=${encodeURIComponent(q)}`),
  create: (data) => apiRequest("/students", { method: "POST", body: JSON.stringify(data) }),
  update: (id, data) => apiRequest(`/students/${id}`, { method: "PUT", body: JSON.stringify(data) }),
  remove: (id) => apiRequest(`/students/${id}`, { method: "DELETE" }),
};

// ---------- Course API ----------
const CourseApi = {
  list: () => apiRequest("/courses"),
  create: (data) => apiRequest("/courses", { method: "POST", body: JSON.stringify(data) }),
  remove: (id) => apiRequest(`/courses/${id}`, { method: "DELETE" }),
};

// ---------- Attendance API ----------
const AttendanceApi = {
  list: (date, courseId) => {
    const qs = new URLSearchParams();
    if (date) qs.set("date", date);
    if (courseId) qs.set("courseId", courseId);
    return apiRequest(`/attendance?${qs.toString()}`);
  },
  mark: (data, date) => {
    const qs = date ? `?date=${encodeURIComponent(date)}` : "";
    return apiRequest(`/attendance${qs}`, { method: "POST", body: JSON.stringify(data) });
  },
  batch: (data) => apiRequest("/attendance/batch", { method: "POST", body: JSON.stringify(data) }),
  studentHistory: (studentId) => apiRequest(`/attendance/student/${studentId}`),
  studentStats: (studentId) => apiRequest(`/attendance/student/${studentId}/stats`),
  stats: () => apiRequest("/attendance/stats"),
};

// ---------- Exam API ----------
const ExamApi = {
  list: (courseId) => {
    const qs = courseId ? `?courseId=${courseId}` : "";
    return apiRequest(`/exams${qs}`);
  },
  get: (id) => apiRequest(`/exams/${id}`),
  create: (data) => apiRequest("/exams", { method: "POST", body: JSON.stringify(data) }),
  update: (id, data) => apiRequest(`/exams/${id}`, { method: "PUT", body: JSON.stringify(data) }),
  remove: (id) => apiRequest(`/exams/${id}`, { method: "DELETE" }),
};

// ---------- Grade API ----------
const GradeApi = {
  byExam: (examId) => apiRequest(`/grades/exam/${examId}`),
  record: (examId, data) => apiRequest(`/grades/exam/${examId}`, { method: "POST", body: JSON.stringify(data) }),
  batch: (data) => apiRequest("/grades/batch", { method: "POST", body: JSON.stringify(data) }),
  byStudent: (studentId) => apiRequest(`/grades/student/${studentId}`),
  reportCard: (studentId) => apiRequest(`/grades/student/${studentId}/report-card`),
  stats: () => apiRequest("/grades/stats"),
};

// ---------- Report API ----------
const ReportApi = {
  dashboard: () => apiRequest("/reports/dashboard"),
  exportStudentsUrl: () => `${API_BASE}/reports/export/students`,
  exportAttendanceUrl: () => `${API_BASE}/reports/export/attendance`,
  exportGradesUrl: () => `${API_BASE}/reports/export/grades`,
};

// ---------- Shared Helpers ----------

// Escape string to prevent XSS in HTML string interpolation
function esc(value) {
  if (value === null || value === undefined) return "";
  return String(value)
    .replace(/&/g, "&amp;")
    .replace(/</g, "&lt;")
    .replace(/>/g, "&gt;")
    .replace(/"/g, "&quot;")
    .replace(/'/g, "&#39;");
}

let toastTimer;
function showToast(message, type = "success") {
  let el = document.getElementById("toast");
  if (!el) {
    el = document.createElement("div");
    el.id = "toast";
    document.body.appendChild(el);
  }
  el.textContent = message;
  el.className = type;
  el.style.display = "block";
  clearTimeout(toastTimer);
  toastTimer = setTimeout(() => (el.style.display = "none"), 3500);
}

// Render dynamic topbar with user badge & logout
function renderNavbar(activePage = "") {
  const topbar = document.querySelector(".topbar");
  if (!topbar) return;

  const user = AuthApi.getUser();
  const isAuth = AuthApi.isAuthenticated();

  topbar.innerHTML = `
    <div class="topbar-inner">
      <div class="brand-group">
        <a href="index.html" class="brand">🎓 Student Management</a>
      </div>
      <nav class="nav">
        <a href="index.html" class="${activePage === 'dashboard' ? 'active' : ''}">Dashboard</a>
        <a href="students.html" class="${activePage === 'students' ? 'active' : ''}">Students</a>
        <a href="courses.html" class="${activePage === 'courses' ? 'active' : ''}">Courses</a>
        <a href="attendance.html" class="${activePage === 'attendance' ? 'active' : ''}">Attendance</a>
        <a href="exams.html" class="${activePage === 'exams' ? 'active' : ''}">Examinations</a>
        <a href="reports.html" class="${activePage === 'reports' ? 'active' : ''}">Reports</a>
      </nav>
      <div class="nav-user">
        ${isAuth ? `
          <span class="user-badge">👤 ${esc((user && user.fullName) || (user && user.username) || 'Admin')}</span>
          <button class="btn btn-sm" id="logoutBtn" title="Log out">Logout</button>
        ` : `
          <a class="btn btn-sm btn-primary" href="login.html">Login</a>
        `}
      </div>
    </div>
  `;

  const logoutBtn = document.getElementById("logoutBtn");
  if (logoutBtn) {
    logoutBtn.addEventListener("click", () => AuthApi.logout());
  }
}
