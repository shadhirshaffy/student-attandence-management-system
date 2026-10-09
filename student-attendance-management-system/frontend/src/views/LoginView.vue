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
          <div class="relative mt-2">
            <input
              id="password"
              v-model="password"
              :type="showPassword ? 'text' : 'password'"
              autocomplete="current-password"
              class="w-full rounded-md border border-slate-300 py-2 pl-3 pr-16 text-sm text-slate-900 shadow-sm outline-none transition placeholder:text-slate-400 focus:border-blue-600 focus:ring-2 focus:ring-blue-100"
              placeholder="Enter your password"
              required
            />
            <button
              type="button"
              class="absolute inset-y-0 right-0 rounded-r-md px-3 text-sm font-medium text-blue-700 hover:text-blue-900 focus:outline-none focus:ring-2 focus:ring-inset focus:ring-blue-600"
              :aria-label="showPassword ? 'Hide password' : 'Show password'"
              :aria-pressed="showPassword"
              @click="showPassword = !showPassword"
            >
              {{ showPassword ? 'Hide' : 'Show' }}
            </button>
          </div>
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

      <section class="mt-6 rounded-md border border-blue-100 bg-blue-50/70 p-4 text-sm text-slate-600" aria-labelledby="sign-in-help">
        <h2 id="sign-in-help" class="font-semibold text-slate-900">How to sign in</h2>
        <ol class="mt-2 list-decimal space-y-1 pl-5">
          <li>Select the role assigned to your account.</li>
          <li>Enter your email address and password.</li>
          <li>Select <span class="font-medium text-slate-800">Show</span> if you need to check your password, then choose <span class="font-medium text-slate-800">Sign in</span>.</li>
        </ol>
        <p class="mt-3 text-xs text-slate-500">For a local development account, enable seed accounts and use the credentials in the project README.</p>
      </section>
    </section>
  </main>
</template>

<script setup>
import { computed, ref } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { useAuthStore } from '../stores/authStore'

const router = useRouter()
const route = useRoute()
const auth = useAuthStore()

const email = ref('')
const password = ref('')
const showPassword = ref(false)
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

    const redirect = route.query.redirect
    const safeRedirect = typeof redirect === 'string' && redirect.startsWith('/') && !redirect.startsWith('//')
    router.push(safeRedirect ? redirect : roleRoutes[user.role] || '/')
  } catch {
    errorMessage.value = auth.error || 'Invalid email or password.'
  }
}
</script>
