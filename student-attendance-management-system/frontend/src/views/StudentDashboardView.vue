<template>
  <AppLayout>
    <section class="space-y-4">
      <p class="text-sm font-semibold uppercase tracking-wide text-blue-700">Student</p>
      <h1 class="text-3xl font-bold text-slate-950">Student dashboard</h1>
      <p class="max-w-2xl text-slate-600">
        View your enrolled modules. Attendance history, percentages, and warnings will be added in later phases.
      </p>
      <RouterLink
        to="/student/modules"
        class="inline-flex rounded-md bg-blue-700 px-4 py-2 text-sm font-semibold text-white hover:bg-blue-800"
      >
        View my modules
      </RouterLink>
    </section>
  </AppLayout>
</template>

<script setup>
import AppLayout from '../layouts/AppLayout.vue'
</script>
