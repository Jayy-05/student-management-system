# Student Management System

A full-stack, enterprise-grade **Student Management System** with a Spring Boot REST API backend and a responsive, zero-dependency HTML5/CSS3/JavaScript frontend.

---

## 🚀 Features Implemented

1. **Student Directory & Profile Management**
   - Complete CRUD operations with client-side & server-side validation.
   - Unique constraints on Student ID / Code and Email.
   - Comprehensive student modal view with personal and enrollment details.
   - Server-side pagination (`page`, `size`, `sortBy`, `sortDir`), course filtering, and debounced search.

2. **Course & Program Management**
   - Create, list, and delete degree programs.
   - Tracks total enrolled students per course.
   - Referential integrity protection (prevents accidental course deletion if active students are enrolled).

3. **Daily Attendance Tracking**
   - Date-based register with course filtering.
   - Individual & batch status marking (`PRESENT`, `ABSENT`, `LATE`, `EXCUSED`) with remarks.
   - Quick actions ("Mark all Present", "Mark all Absent").
   - Unique DB constraint on `(student_id, attendance_date)` to prevent duplicates.
   - Student attendance history view with attendance rate percentage calculation.

4. **Examinations & Academic Grading**
   - Create and schedule assessments and examinations with maximum and passing marks.
   - Batch mark recording interface with live calculation of:
     - Percentage score
     - Grade letters (`A+`, `A`, `B`, `C`, `D`, `F`)
     - Pass / Fail status
   - Printable student academic transcript and report cards.

5. **Reports, Analytics & Data Export**
   - Executive dashboard with real-time KPI metrics (Total Students, Active Courses, Today's Attendance %, Overall Exam Pass Rate %).
   - Visual progress bars for Course Enrollment Distribution and Grade Distribution.
   - **One-Click CSV Export**:
     - `GET /api/reports/export/students`
     - `GET /api/reports/export/attendance`
     - `GET /api/reports/export/grades`
   - On-demand official student transcript generation with browser print and PDF export.

6. **Authentication & Security**
   - Token-based administrator authentication (`/api/auth/login`, `/api/auth/logout`, `/api/auth/me`).
   - Salted SHA-256 password hashing.
   - Pre-configured administrator credentials:
     - **Username:** `admin`
     - **Password:** `admin123`
   - Request interceptor protecting mutating operations (`POST`, `PUT`, `DELETE`).
   - XSS sanitization helper (`esc()`) on all frontend DOM renderings.

7. **Automated Unit Testing**
   - Unit test suites in `backend/src/test/java/...` using JUnit 5 and Mockito covering:
     - `StudentServiceImplTest`
     - `AttendanceServiceImplTest`
     - `GradeServiceImplTest`

---

## 🛠️ Quick Start

### 1. Database
Install and start MySQL. The application will automatically create the database if it doesn't already exist:
```sql
CREATE DATABASE student_management;
```

### 2. Backend
Requires JDK 17+ (JDK 25 supported) and Maven.

```bash
cd backend
# Set your MySQL credentials (defaults to user: root, password: YOUR_PASSWORD)
export DB_USERNAME=root
export DB_PASSWORD=your_mysql_password

# Run via Maven
mvn spring-boot:run
```
*(On Windows PowerShell, you can also run `./run-backend.ps1`)*

The REST API starts on `http://localhost:8080`.

### 3. Frontend
Serve the `frontend` folder on port `5500`:

* **Windows PowerShell (Zero install):**
  ```powershell
  cd frontend
  .\start-frontend.ps1
  ```
* **Python:**
  ```bash
  cd frontend
  python3 -m http.server 5500
  ```
* **VS Code:** Right-click `frontend/index.html` > *Open with Live Server*.

Open `http://localhost:5500` in your web browser.

---

## 📡 API Reference

### Authentication (`/api/auth`)
| Method | Endpoint | Description |
|---|---|---|
| `POST` | `/api/auth/login` | Authenticate with username and password, returns token |
| `POST` | `/api/auth/logout` | Invalidate current session token |
| `GET` | `/api/auth/me` | Retrieve profile of currently authenticated user |
| `PUT` | `/api/auth/change-password` | Update user password |

### Students (`/api/students`)
| Method | Endpoint | Description |
|---|---|---|
| `GET` | `/api/students` | List all students or paginated (`?page=0&size=10&search=...&courseId=...`) |
| `GET` | `/api/students/{id}` | Get student details by ID |
| `POST` | `/api/students` | Create new student profile |
| `PUT` | `/api/students/{id}` | Update student profile |
| `DELETE` | `/api/students/{id}` | Delete student profile |
| `GET` | `/api/students/search?name=...` | Case-insensitive search by ID or name |

### Courses (`/api/courses`)
| Method | Endpoint | Description |
|---|---|---|
| `GET` | `/api/courses` | List all academic courses |
| `POST` | `/api/courses` | Create new course program |
| `DELETE` | `/api/courses/{id}` | Delete course (returns 409 if students are enrolled) |

### Attendance (`/api/attendance`)
| Method | Endpoint | Description |
|---|---|---|
| `GET` | `/api/attendance?date=...&courseId=...` | Retrieve attendance records by date & course |
| `POST` | `/api/attendance` | Record or update single student attendance |
| `POST` | `/api/attendance/batch` | Batch record attendance for a date |
| `GET` | `/api/attendance/student/{id}` | Get attendance history for a student |
| `GET` | `/api/attendance/student/{id}/stats` | Get attendance statistics and rate % for a student |
| `GET` | `/api/attendance/stats` | Get system-wide attendance statistics |

### Examinations & Grades (`/api/exams`, `/api/grades`)
| Method | Endpoint | Description |
|---|---|---|
| `GET` | `/api/exams?courseId=...` | List all scheduled exams |
| `POST` | `/api/exams` | Create a new exam / assessment |
| `GET` | `/api/exams/{id}` | Get exam details |
| `PUT` | `/api/exams/{id}` | Update exam |
| `DELETE` | `/api/exams/{id}` | Delete exam and associated marks |
| `GET` | `/api/grades/exam/{examId}` | Get all recorded grades for an exam |
| `POST` | `/api/grades/exam/{examId}` | Record single student marks |
| `POST` | `/api/grades/batch` | Batch submit student grades |
| `GET` | `/api/grades/student/{id}/report-card` | Generate complete student academic transcript |
| `GET` | `/api/grades/stats` | Get grade distribution & pass rate metrics |

### Reports & Export (`/api/reports`)
| Method | Endpoint | Description |
|---|---|---|
| `GET` | `/api/reports/dashboard` | Aggregated metrics, distributions, and stats |
| `GET` | `/api/reports/export/students` | Download Student Directory as CSV |
| `GET` | `/api/reports/export/attendance` | Download Attendance Log as CSV |
| `GET` | `/api/reports/export/grades` | Download Exam Results as CSV |
