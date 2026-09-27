<template>
  <main class="flex min-h-screen items-center justify-center bg-slate-100 px-4 py-10">
    <section class="w-full max-w-md rounded-lg bg-white p-8 shadow-lg shadow-slate-200/70">
      <div class="mb-8 text-center">
        <p class="text-sm font-semibold uppercase tracking-wide text-blue-700">Secure access</p>
        <h1 class="mt-2 text-2xl font-bold text-slate-950">Sign in</h1>
        <p class="mt-2 text-sm text-slate-500">Student Attendance Management System</p>
      </div>

      <form class="space-y-5" @submit.prevent="handleLogin">
        <div>
          <label for="role" class="block text-sm font-medium text-slate-700">Role</label>
          <select
            id="role"
            v-model="role"
            class="mt-2 w-full rounded-md border border-slate-300 bg-white px-3 py-2 text-sm text-slate-900 shadow-sm outline-none transition focus:border-blue-600 focus:ring-2 focus:ring-blue-100"
          >
            <option value="STUDENT">Student</option>
            <option value="LECTURER">Lecturer</option>
            <option value="ADMIN">Admin</option>
          </select>
        </div>

        <div>
          <label for="email" class="block text-sm font-medium text-slate-700">Email</label>
          <input
            id="email"
            v-model.trim="email"
            type="email"
            autocomplete="email"
            class="mt-2 w-full rounded-md border border-slate-300 px-3 py-2 text-sm text-slate-900 shadow-sm outline-none transition placeholder:text-slate-400 focus:border-blue-600 focus:ring-2 focus:ring-blue-100"
            placeholder="name@example.com"
            required
          />
        </div>

        <div>
          <label for="password" class="block text-sm font-medium text-slate-700">Password</label>
          <input
            id="password"
            v-model="password"
            type="password"
            autocomplete="current-password"
            class="mt-2 w-full rounded-md border border-slate-300 px-3 py-2 text-sm text-slate-900 shadow-sm outline-none transition placeholder:text-slate-400 focus:border-blue-600 focus:ring-2 focus:ring-blue-100"
            placeholder="Enter your password"
            required
          />
        </div>

        <p v-if="errorMessage" class="rounded-md bg-red-50 px-3 py-2 text-sm text-red-700">
          {{ errorMessage }}
        </p>

        <button
          type="submit"
          :disabled="!canSubmit || auth.loading"
          class="w-full rounded-md bg-blue-700 px-4 py-2.5 text-sm font-semibold text-white shadow-sm transition hover:bg-blue-800 focus:outline-none focus:ring-2 focus:ring-blue-200 disabled:cursor-not-allowed disabled:bg-slate-300"
        >
          {{ auth.loading ? 'Signing in...' : 'Sign in' }}
        </button>
      </form>
    </section>
  </main>
</template>

<script setup>
import { computed, ref } from 'vue'
import { useRouter } from 'vue-router'
import { useAuthStore } from '../stores/authStore'

const router = useRouter()
const auth = useAuthStore()

const email = ref('')
const password = ref('')
const role = ref('STUDENT')
const errorMessage = ref('')

const roleRoutes = {
  ADMIN: '/admin',
  LECTURER: '/lecturer',
  STUDENT: '/student',
}

const canSubmit = computed(() => email.value && password.value && role.value)

async function handleLogin() {
  if (!canSubmit.value) {
    return
  }

  errorMessage.value = ''

  try {
    const user = await auth.login({
      email: email.value,
      password: password.value,
    })

    if (user.role !== role.value) {
      auth.logout()
      errorMessage.value = 'Selected role does not match this account.'
      return
    }

    router.push(roleRoutes[user.role] || '/')
  } catch {
    errorMessage.value = auth.error || 'Invalid email or password.'
  }
}
</script>
