<template>
  <div class="min-h-screen bg-slate-50">
    <header class="border-b border-slate-200 bg-white">
      <div class="mx-auto flex max-w-6xl items-center justify-between px-6 py-4">
        <RouterLink to="/" class="text-base font-semibold text-slate-950">
          Student Attendance Management System
        </RouterLink>
        <div class="flex items-center gap-3">
          <span class="rounded-full bg-emerald-50 px-3 py-1 text-xs font-medium text-emerald-700">
            Phase 4
          </span>
          <button
            v-if="auth.isAuthenticated"
            type="button"
            class="rounded-md border border-slate-300 px-3 py-1.5 text-sm font-medium text-slate-700 transition hover:bg-slate-100"
            @click="logout"
          >
            Logout
          </button>
          <RouterLink
            v-else
            to="/login"
            class="rounded-md bg-blue-700 px-3 py-1.5 text-sm font-medium text-white transition hover:bg-blue-800"
          >
            Login
          </RouterLink>
        </div>
      </div>
    </header>

    <main class="mx-auto max-w-6xl px-6 py-10">
      <slot />
    </main>
  </div>
</template>

<script setup>
import { useRouter } from 'vue-router'
import { useAuthStore } from '../stores/authStore'

const router = useRouter()
const auth = useAuthStore()

function logout() {
  auth.logout()
  router.push('/login')
}
</script>
