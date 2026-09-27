<template>
  <AppLayout>
    <section class="space-y-4">
      <p class="text-sm font-semibold uppercase tracking-wide text-blue-700">Lecturer</p>
      <h1 class="text-3xl font-bold text-slate-950">Lecturer dashboard</h1>
      <p class="max-w-2xl text-slate-600">
        View the modules assigned to your lecturer profile. Sessions, QR attendance, and reports will be added in later phases.
      </p>
      <div class="flex flex-wrap gap-3">
        <RouterLink
          to="/lecturer/modules"
          class="inline-flex rounded-md border border-slate-300 px-4 py-2 text-sm font-semibold text-slate-700 hover:bg-slate-100"
        >
          View my modules
        </RouterLink>
        <RouterLink
          to="/lecturer/sessions"
          class="inline-flex rounded-md bg-blue-700 px-4 py-2 text-sm font-semibold text-white hover:bg-blue-800"
        >
          Manage sessions
        </RouterLink>
      </div>
    </section>
  </AppLayout>
</template>

<script setup>
import AppLayout from '../layouts/AppLayout.vue'
</script>
