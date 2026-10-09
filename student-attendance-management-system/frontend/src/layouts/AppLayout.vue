<template>
  <div class="min-h-screen bg-slate-50">
    <Navbar
      :authenticated="auth.isAuthenticated"
      :role="auth.role"
      :show-menu-button="auth.isAuthenticated && navItems.length > 0"
      @logout="logout"
      @toggle-menu="mobileOpen = true"
    />

    <div v-if="auth.isAuthenticated && navItems.length > 0" class="lg:grid lg:grid-cols-[260px_1fr]">
      <div class="hidden min-h-[calc(100vh-4rem)] lg:block">
        <Sidebar :items="navItems" :role-label="auth.role" title="Attendance workspace" />
      </div>

      <div v-if="mobileOpen" class="fixed inset-0 z-40 lg:hidden">
        <button
          type="button"
          class="absolute inset-0 bg-slate-950/40"
          aria-label="Close navigation"
          @click="mobileOpen = false"
        ></button>
        <div class="relative h-full w-72 max-w-[85vw] shadow-xl">
          <Sidebar :items="navItems" :role-label="auth.role" title="Attendance workspace" @navigate="mobileOpen = false" />
        </div>
      </div>

      <main class="min-w-0 px-4 py-6 sm:px-6 lg:px-8">
        <div class="mx-auto max-w-7xl">
          <slot />
        </div>
      </main>
    </div>

    <main v-else class="mx-auto max-w-6xl px-4 py-10 sm:px-6">
      <slot />
    </main>
  </div>
</template>

<script setup>
import { computed, ref } from 'vue'
import { useRouter } from 'vue-router'
import Navbar from '../components/Navbar.vue'
import Sidebar from '../components/Sidebar.vue'
import { useAuthStore } from '../stores/authStore'

const router = useRouter()
const auth = useAuthStore()
const mobileOpen = ref(false)

const navItemsByRole = {
  ADMIN: [
    { to: '/admin', label: 'Dashboard' },
    { to: '/admin/users', label: 'Users' },
    { to: '/admin/students', label: 'Students' },
    { to: '/admin/lecturers', label: 'Lecturers' },
    { to: '/admin/modules', label: 'Modules' },
    { to: '/admin/enrollments', label: 'Enrollments' },
    { to: '/admin/sessions', label: 'Sessions' },
    { to: '/admin/attendance', label: 'Attendance' },
  ],
  LECTURER: [
    { to: '/lecturer', label: 'Dashboard' },
    { to: '/lecturer/modules', label: 'My Modules' },
    { to: '/lecturer/sessions', label: 'Sessions' },
    { to: '/lecturer/attendance', label: 'Attendance' },
    { to: '/lecturer/reports', label: 'Reports' },
  ],
  STUDENT: [
    { to: '/student', label: 'Dashboard' },
    { to: '/student/modules', label: 'My Modules' },
    { to: '/student/scan', label: 'Scan QR' },
    { to: '/student/attendance-history', label: 'Attendance History' },
  ],
}

const navItems = computed(() => navItemsByRole[auth.role] || [])

function logout() {
  auth.logout()
  router.push('/login')
}
</script>
