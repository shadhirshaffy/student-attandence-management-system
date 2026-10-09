<template>
  <header class="sticky top-0 z-30 border-b border-slate-200 bg-white/95 backdrop-blur">
    <div class="flex h-16 items-center justify-between px-4 sm:px-6 lg:px-8">
      <div class="flex min-w-0 items-center gap-3">
        <button
          v-if="showMenuButton"
          type="button"
          class="rounded-md border border-slate-200 p-2 text-slate-600 hover:bg-slate-100 lg:hidden"
          aria-label="Open navigation"
          @click="$emit('toggle-menu')"
        >
          <span class="block h-0.5 w-5 bg-current"></span>
          <span class="mt-1 block h-0.5 w-5 bg-current"></span>
          <span class="mt-1 block h-0.5 w-5 bg-current"></span>
        </button>
        <RouterLink to="/" class="truncate text-base font-semibold text-slate-950">
          Student Attendance
        </RouterLink>
      </div>
      <div class="flex items-center gap-3">
        <span v-if="role" class="hidden rounded-full bg-blue-50 px-3 py-1 text-xs font-semibold text-blue-700 sm:inline-flex">
          {{ role }}
        </span>
        <button
          v-if="authenticated"
          type="button"
          class="rounded-md border border-slate-300 px-3 py-1.5 text-sm font-medium text-slate-700 transition hover:bg-slate-100 focus:outline-none focus:ring-2 focus:ring-blue-200"
          @click="$emit('logout')"
        >
          Logout
        </button>
        <RouterLink
          v-else
          to="/login"
          class="rounded-md bg-blue-700 px-3 py-1.5 text-sm font-medium text-white transition hover:bg-blue-800 focus:outline-none focus:ring-2 focus:ring-blue-200"
        >
          Login
        </RouterLink>
      </div>
    </div>
  </header>
</template>

<script setup>
defineProps({
  authenticated: {
    type: Boolean,
    default: false,
  },
  role: {
    type: String,
    default: '',
  },
  showMenuButton: {
    type: Boolean,
    default: false,
  },
})

defineEmits(['logout', 'toggle-menu'])
</script>
