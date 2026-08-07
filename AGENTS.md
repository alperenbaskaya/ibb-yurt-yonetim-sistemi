# Project

This repository contains:

- Spring Boot / Java backend at repository root
- React frontend under /frontend

Backend:
- Java
- Spring Boot
- Maven
- Spring Security
- JWT
- JPA/Hibernate
- MySQL

Frontend:
- React
- Vite
- TypeScript
- React Router
- Axios
- TanStack Query
- Tailwind CSS
- React Hook Form
- Zod
- Lucide React
- Recharts
- Sonner

# Backend protection

The backend is already implemented.

Do not modify backend files unless the user explicitly asks for a backend
change.

Do not invent backend endpoints or DTO fields.

Frontend implementation must follow the actual existing backend contracts.

# Authentication

Existing login endpoint:

POST /api/auth/login

JWT Bearer authentication is used.

Current authenticated user must be hydrated from:

GET /api/auth/me

/api/auth/me is the authoritative frontend identity source.

Do not make frontend authorization decisions by trusting decoded JWT claims
alone.

For the initial frontend architecture:
- store the access token in sessionStorage
- send it with Authorization: Bearer <token>
- clear authentication state on invalid/expired authentication
- a refresh-token flow does not currently exist

# Roles

Backend Role values:

ADMIN
REVIEWER
STUDENT

AdminScope values:

GLOBAL
DORMITORY

Frontend user categories:

STUDENT
REVIEWER
DORMITORY ADMIN = role ADMIN + adminScope DORMITORY
GLOBAL ADMIN = role ADMIN + adminScope GLOBAL

Never confuse Role with AdminScope.

# Layout

All authenticated users use one shared AppLayout.

Structure:

AppLayout
├── Topbar
├── Role-aware Sidebar
└── Outlet

Every authenticated user lands on:

/home

after login.

Do not automatically send users directly to their dashboards after login.

# Sidebar

STUDENT:
- Home
- Dashboard
- My Documents
- Notifications
- Profile

REVIEWER:
- Home
- Dashboard
- Pending Documents
- Students
- My Reviews
- Notifications
- Profile

DORMITORY ADMIN:
- Home
- Dashboard
- Admissions
- Students
- Reviewers
- Documents
- Notifications
- Profile

GLOBAL ADMIN:
- Home
- Global Dashboard
- Dormitories
- Users
- Students
- Admissions
- Dormitory Terms
- Document Types
- Document Requirements
- Notifications
- Profile

Some later screens may require additional backend endpoints.
Do not fake those APIs.
If an endpoint is missing, report the backend gap instead.

# Planned routes

Public:

/login

Shared authenticated:

/home
/profile
/notifications

Student:

/student/dashboard
/student/documents

Reviewer:

/reviewer/dashboard
/reviewer/documents
/reviewer/students
/reviewer/reviews

Dormitory admin:

/admin/dashboard
/admin/admissions
/admin/students
/admin/reviewers
/admin/documents

Global admin:

/global/dashboard
/global/dormitories
/global/users
/global/students
/global/admissions
/global/terms
/global/document-types
/global/document-requirements

# Frontend architecture

Use feature-oriented organization.

Preferred structure:

frontend/src/
├── api/
├── assets/
├── components/
│   ├── common/
│   ├── layout/
│   └── ui/
├── features/
│   ├── auth/
│   ├── notifications/
│   ├── student/
│   ├── reviewer/
│   ├── dormitory-admin/
│   └── global-admin/
├── hooks/
├── layouts/
├── pages/
├── router/
├── types/
├── utils/
├── App.tsx
└── main.tsx

Avoid giant files.

Avoid putting all API requests into one file.

Avoid unnecessary abstraction.

Prefer clear, maintainable code appropriate for an internship project.

# Server state

Use TanStack Query for backend/server state.

Do not build ordinary API fetching around repetitive:

useEffect + axios + useState

when TanStack Query is appropriate.

Use query invalidation after successful mutations.

# HTTP

Create a centralized Axios client.

During development, requests should use relative /api URLs.

Configure Vite development proxy:

/api -> http://localhost:8080

Do not hardcode http://localhost:8080 throughout React components.

Handle backend ApiError consistently.

Expected shape:

{
  timestamp: string,
  status: number,
  error: string,
  message: string,
  path: string,
  validationErrors: Record<string, string> | null
}

# Security

Frontend route guards are UX controls only.

Spring Security remains the real authorization layer.

Never expose a screen merely because a sidebar link is hidden.

Role and AdminScope guards must still exist for protected frontend routes.

# UI design

The application is for a municipal/institutional dormitory management system.

Design direction:

- professional
- modern
- minimal
- enterprise / municipal
- light theme
- white cards
- very light gray application background
- controlled institutional blue accents
- green for approved/success
- amber for pending/revision
- red for rejected/error
- gray for inactive
- desktop-first
- responsive

Avoid:
- flashy gradients
- excessive animations
- playful consumer-app styling
- overly rounded everything
- visual clutter

# UI behavior

Every data-driven screen must deliberately handle:

- loading
- error
- empty
- success

Forms must show validation feedback.

Destructive or important actions should require deliberate user interaction.

Use Lucide icons instead of arbitrary Unicode symbols.

# Code quality

- TypeScript, not JavaScript.
- Avoid any unless genuinely necessary.
- Reuse backend enum names exactly.
- Keep API types explicit.
- Do not silently change API response contracts.
- Prefer readable code over clever code.
- Do not add dependencies without a real need.
- Keep console free of avoidable errors/warnings.
- Maintain accessible labels and buttons.

# Verification

After meaningful frontend changes:

npm run build

must succeed.

Also inspect:

git diff
git status

Do not commit or push unless explicitly requested.
