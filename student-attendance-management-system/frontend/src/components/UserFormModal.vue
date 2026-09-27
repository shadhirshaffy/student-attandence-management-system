<template>
  <div class="fixed inset-0 z-50 flex items-center justify-center bg-slate-950/40 px-4 py-6">
    <section class="max-h-[90vh] w-full max-w-2xl overflow-y-auto rounded-lg bg-white shadow-xl">
      <header class="flex items-start justify-between border-b border-slate-200 px-6 py-4">
        <div>
          <h2 class="text-lg font-semibold text-slate-950">{{ title }}</h2>
          <p class="mt-1 text-sm text-slate-500">{{ mode === 'create' ? 'Add an account with the correct role profile.' : 'Update account and profile details.' }}</p>
        </div>
        <button type="button" class="rounded-md px-2 py-1 text-slate-500 hover:bg-slate-100" @click="$emit('close')">
          Close
        </button>
      </header>

      <form class="space-y-5 px-6 py-5" @submit.prevent="submit">
        <div class="grid gap-4 md:grid-cols-2">
          <label class="block text-sm font-medium text-slate-700">
            Name
            <input v-model.trim="form.name" required class="mt-2 w-full rounded-md border border-slate-300 px-3 py-2 text-sm outline-none focus:border-blue-600 focus:ring-2 focus:ring-blue-100" />
          </label>

          <label class="block text-sm font-medium text-slate-700">
            Email
            <input v-model.trim="form.email" type="email" required class="mt-2 w-full rounded-md border border-slate-300 px-3 py-2 text-sm outline-none focus:border-blue-600 focus:ring-2 focus:ring-blue-100" />
          </label>
        </div>

        <div class="grid gap-4 md:grid-cols-2">
          <label class="block text-sm font-medium text-slate-700">
            Role
            <select v-model="form.role" class="mt-2 w-full rounded-md border border-slate-300 bg-white px-3 py-2 text-sm outline-none focus:border-blue-600 focus:ring-2 focus:ring-blue-100">
              <option value="ADMIN">Admin</option>
              <option value="LECTURER">Lecturer</option>
              <option value="STUDENT">Student</option>
            </select>
          </label>

          <label class="block text-sm font-medium text-slate-700">
            Password
            <input
              v-model="form.password"
              type="password"
              :required="mode === 'create'"
              class="mt-2 w-full rounded-md border border-slate-300 px-3 py-2 text-sm outline-none focus:border-blue-600 focus:ring-2 focus:ring-blue-100"
              :placeholder="mode === 'edit' ? 'Leave blank to keep current password' : ''"
            />
          </label>
        </div>

        <div v-if="form.role === 'STUDENT'">
          <label class="block text-sm font-medium text-slate-700">
            Student number
            <input v-model.trim="form.studentNumber" required class="mt-2 w-full rounded-md border border-slate-300 px-3 py-2 text-sm outline-none focus:border-blue-600 focus:ring-2 focus:ring-blue-100" />
          </label>
        </div>

        <div v-if="form.role === 'LECTURER'">
          <label class="block text-sm font-medium text-slate-700">
            Employee number
            <input v-model.trim="form.employeeNumber" required class="mt-2 w-full rounded-md border border-slate-300 px-3 py-2 text-sm outline-none focus:border-blue-600 focus:ring-2 focus:ring-blue-100" />
          </label>
        </div>

        <label class="flex items-center gap-2 text-sm font-medium text-slate-700">
          <input v-model="form.active" type="checkbox" class="h-4 w-4 rounded border-slate-300 text-blue-700" />
          Active account
        </label>

        <p class="text-sm text-slate-500">
          Passwords must be at least 8 characters and include uppercase, lowercase, and numeric characters.
        </p>

        <footer class="flex flex-col-reverse gap-3 border-t border-slate-200 pt-5 sm:flex-row sm:justify-end">
          <button type="button" class="rounded-md border border-slate-300 px-4 py-2 text-sm font-semibold text-slate-700 hover:bg-slate-100" @click="$emit('close')">
            Cancel
          </button>
          <button type="submit" :disabled="saving" class="rounded-md bg-blue-700 px-4 py-2 text-sm font-semibold text-white hover:bg-blue-800 disabled:bg-slate-300">
            {{ saving ? 'Saving...' : 'Save user' }}
          </button>
        </footer>
      </form>
    </section>
  </div>
</template>

<script setup>
import { computed, reactive, watch } from 'vue'

const props = defineProps({
  mode: { type: String, required: true },
  user: { type: Object, default: null },
  saving: { type: Boolean, default: false },
})

const emit = defineEmits(['close', 'submit'])

const form = reactive(defaultForm())

const title = computed(() => (props.mode === 'create' ? 'Create user' : 'Edit user'))

watch(
  () => props.user,
  () => {
    Object.assign(form, defaultForm(props.user))
  },
  { immediate: true },
)

function defaultForm(user = null) {
  return {
    name: user?.name || '',
    email: user?.email || '',
    password: '',
    role: user?.role || 'STUDENT',
    studentNumber: user?.studentNumber || '',
    employeeNumber: user?.employeeNumber || '',
    active: user?.active ?? true,
  }
}

function submit() {
  const payload = {
    name: form.name,
    email: form.email,
    role: form.role,
    active: form.active,
  }

  if (form.password) {
    payload.password = form.password
  }
  if (props.mode === 'create') {
    payload.password = form.password
  }
  if (form.role === 'STUDENT') {
    payload.studentNumber = form.studentNumber
  }
  if (form.role === 'LECTURER') {
    payload.employeeNumber = form.employeeNumber
  }

  emit('submit', payload)
}
</script>
