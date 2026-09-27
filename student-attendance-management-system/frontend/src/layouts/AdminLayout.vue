<template>
  <AppLayout>
    <div class="grid gap-6 lg:grid-cols-[220px_1fr]">
      <aside class="h-fit rounded-lg border border-slate-200 bg-white p-3 shadow-sm">
        <nav class="space-y-1">
          <RouterLink
            v-for="item in navItems"
            :key="item.to"
            :to="item.to"
            class="block rounded-md px-3 py-2 text-sm font-medium text-slate-600 transition hover:bg-slate-100 hover:text-slate-950"
            active-class="bg-blue-50 text-blue-700"
          >
            {{ item.label }}
          </RouterLink>
        </nav>
      </aside>

      <section class="min-w-0">
        <slot />
      </section>
    </div>
  </AppLayout>
</template>

<script setup>
import AppLayout from './AppLayout.vue'

const navItems = [
  { to: '/admin', label: 'Dashboard' },
  { to: '/admin/users', label: 'Users' },
  { to: '/admin/students', label: 'Students' },
  { to: '/admin/lecturers', label: 'Lecturers' },
]
</script>
