import { createRouter, createWebHistory } from 'vue-router'
import { useAuthStore } from '../stores/authStore'

import AdminDashboardView from '../views/AdminDashboardView.vue'
import AdminModulesView from '../views/AdminModulesView.vue'
import AdminUsersView from '../views/AdminUsersView.vue'
import CreateSessionView from '../views/CreateSessionView.vue'
import HomeView from '../views/HomeView.vue'
import LecturerDashboardView from '../views/LecturerDashboardView.vue'
import LecturerSessionsView from '../views/LecturerSessionsView.vue'
import LoginView from '../views/LoginView.vue'
import MyModulesView from '../views/MyModulesView.vue'
import SessionDetailsView from '../views/SessionDetailsView.vue'
import StudentDashboardView from '../views/StudentDashboardView.vue'

const routes = [
  {
    path: '/',
    name: 'home',
    component: HomeView,
  },
  {
    path: '/login',
    name: 'login',
    component: LoginView,
    meta: { guestOnly: true },
  },
  {
    path: '/admin',
    name: 'admin-dashboard',
    component: AdminDashboardView,
    meta: { requiresAuth: true, roles: ['ADMIN'] },
  },
  {
    path: '/admin/users',
    name: 'admin-users',
    component: AdminUsersView,
    meta: {
      requiresAuth: true,
      roles: ['ADMIN'],
      title: 'Users',
      subtitle: 'Create, edit, activate, and deactivate all system users.',
    },
  },
  {
    path: '/admin/modules',
    name: 'admin-modules',
    component: AdminModulesView,
    meta: { requiresAuth: true, roles: ['ADMIN'] },
  },
  {
    path: '/admin/students',
    name: 'admin-students',
    component: AdminUsersView,
    meta: {
      requiresAuth: true,
      roles: ['ADMIN'],
      roleFilter: 'STUDENT',
      title: 'Students',
      subtitle: 'Manage student accounts and student numbers.',
    },
  },
  {
    path: '/admin/lecturers',
    name: 'admin-lecturers',
    component: AdminUsersView,
    meta: {
      requiresAuth: true,
      roles: ['ADMIN'],
      roleFilter: 'LECTURER',
      title: 'Lecturers',
      subtitle: 'Manage lecturer accounts and employee numbers.',
    },
  },
  {
    path: '/lecturer',
    name: 'lecturer-dashboard',
    component: LecturerDashboardView,
    meta: { requiresAuth: true, roles: ['LECTURER'] },
  },
  {
    path: '/lecturer/modules',
    name: 'lecturer-modules',
    component: MyModulesView,
    meta: { requiresAuth: true, roles: ['LECTURER'] },
  },
  {
    path: '/lecturer/sessions',
    name: 'lecturer-sessions',
    component: LecturerSessionsView,
    meta: { requiresAuth: true, roles: ['LECTURER'] },
  },
  {
    path: '/lecturer/sessions/create',
    name: 'lecturer-create-session',
    component: CreateSessionView,
    meta: { requiresAuth: true, roles: ['LECTURER'] },
  },
  {
    path: '/lecturer/sessions/:id',
    name: 'lecturer-session-details',
    component: SessionDetailsView,
    meta: { requiresAuth: true, roles: ['LECTURER'] },
  },
  {
    path: '/student',
    name: 'student-dashboard',
    component: StudentDashboardView,
    meta: { requiresAuth: true, roles: ['STUDENT'] },
  },
  {
    path: '/student/modules',
    name: 'student-modules',
    component: MyModulesView,
    meta: { requiresAuth: true, roles: ['STUDENT'] },
  },
]

const router = createRouter({
  history: createWebHistory(import.meta.env.BASE_URL),
  routes,
})

const roleRoutes = {
  ADMIN: '/admin',
  LECTURER: '/lecturer',
  STUDENT: '/student',
}

router.beforeEach(async (to) => {
  const auth = useAuthStore()

  if (auth.token && !auth.user) {
    await auth.restoreSession()
  }

  if (to.meta.guestOnly && auth.isAuthenticated) {
    return roleRoutes[auth.role] || '/'
  }

  if (to.meta.requiresAuth && !auth.isAuthenticated) {
    return '/login'
  }

  if (to.meta.roles && !to.meta.roles.includes(auth.role)) {
    return roleRoutes[auth.role] || '/login'
  }

  return true
})

export default router
