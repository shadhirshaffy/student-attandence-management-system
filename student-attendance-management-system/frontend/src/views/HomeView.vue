<template>
  <AppLayout>
    <section class="grid gap-8 lg:grid-cols-[1.15fr_0.85fr] lg:items-center">
      <div>
        <p class="text-sm font-semibold uppercase tracking-wide text-blue-700">
          Foundation ready
        </p>
        <h1 class="mt-3 max-w-3xl text-4xl font-bold tracking-tight text-slate-950 sm:text-5xl">
          Student Attendance Management System
        </h1>
        <p class="mt-5 max-w-2xl text-lg leading-8 text-slate-600">
          The Vue 3 frontend is running with Vite, Tailwind CSS, Vue Router, Pinia,
          and Axios. Feature screens will be added incrementally in later phases.
        </p>
      </div>

      <div class="rounded-lg border border-slate-200 bg-white p-6 shadow-sm">
        <h2 class="text-lg font-semibold text-slate-950">Frontend status</h2>
        <dl class="mt-5 space-y-4 text-sm">
          <div class="flex items-center justify-between gap-4">
            <dt class="text-slate-500">Framework</dt>
            <dd class="font-medium text-slate-900">Vue 3</dd>
          </div>
          <div class="flex items-center justify-between gap-4">
            <dt class="text-slate-500">Router</dt>
            <dd class="font-medium text-slate-900">Configured</dd>
          </div>
          <div class="flex items-center justify-between gap-4">
            <dt class="text-slate-500">State</dt>
            <dd class="font-medium text-slate-900">Pinia ready</dd>
          </div>
          <div class="flex items-center justify-between gap-4">
            <dt class="text-slate-500">API base URL</dt>
            <dd class="break-all text-right font-medium text-slate-900">
              {{ apiBaseUrl }}
            </dd>
          </div>
        </dl>
      </div>
    </section>
  </AppLayout>
</template>

<script setup>
import AppLayout from '../layouts/AppLayout.vue'
import { apiBaseUrl } from '../services/api'
</script>
