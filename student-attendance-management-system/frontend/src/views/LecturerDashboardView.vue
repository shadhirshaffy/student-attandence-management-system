<template>
  <AppLayout>
    <section class="space-y-7">
      <div>
        <p class="page-eyebrow">Lecturer workspace</p>
        <h1 class="mt-2 text-3xl font-bold tracking-tight text-slate-950">Lecturer dashboard</h1>
        <p class="mt-2 max-w-2xl text-sm text-slate-500">
          Review module attendance health and manage attendance sessions.
        </p>
      </div>

      <section class="overview-banner">
        <div><p class="text-xs font-semibold uppercase tracking-[0.18em] text-blue-200">Teaching &amp; attendance</p><h2 class="mt-3 text-2xl font-semibold text-white">Ready for your next class?</h2><p class="mt-3 max-w-xl text-sm leading-6 text-blue-100">Create a session, display its QR code, and keep your class attendance up to date.</p></div>
        <RouterLink to="/lecturer/sessions/create" class="shrink-0 rounded-lg bg-white px-5 py-3 text-sm font-semibold text-blue-900 hover:bg-blue-50">Create session <span aria-hidden="true">&rarr;</span></RouterLink>
      </section>
      <section v-if="store.loading" class="ui-panel p-8 text-sm text-slate-500" role="status">Loading attendance statistics...</section>

      <AppAlert v-if="store.error" type="error" :message="store.error" />

      <template v-if="summary">
        <div class="grid gap-4 sm:grid-cols-2 xl:grid-cols-4">
          <StatCard label="Assigned Modules" :value="summary.assignedModules" />
          <StatCard label="Completed Sessions" :value="summary.completedSessions" />
          <StatCard label="Average Attendance" :value="`${summary.averageAttendancePercentage}%`" />
          <StatCard label="Low Attendance Students" :value="summary.lowAttendanceStudents" />
        </div>

        <section class="ui-panel overflow-hidden">
          <div class="border-b border-slate-200 px-4 py-3">
            <h2 class="font-semibold text-slate-950">Module statistics</h2>
          </div>
          <div v-if="summary.modules.length === 0" class="p-8 text-center text-sm text-slate-500">No assigned modules yet.</div>
          <div v-else class="overflow-x-auto" tabindex="0" role="region" aria-label="Module statistics">
            <table class="min-w-full divide-y divide-slate-200 text-sm">
              <thead class="bg-slate-50 text-left text-xs font-semibold uppercase tracking-wide text-slate-500">
                <tr>
                  <th class="px-5 py-4">Module</th>
                  <th class="px-5 py-4">Students</th>
                  <th class="px-5 py-4">Completed</th>
                  <th class="px-5 py-4">Average</th>
                  <th class="px-5 py-4">Warnings</th>
                </tr>
              </thead>
              <tbody class="divide-y divide-slate-100">
                <tr v-for="module in summary.modules" :key="module.moduleId" class="hover:bg-blue-50/40">
                  <td class="px-5 py-4">
                    <div class="font-medium text-slate-950">{{ module.moduleCode }}</div>
                    <div class="text-slate-500">{{ module.moduleName }}</div>
                  </td>
                  <td class="px-5 py-4 text-slate-600">{{ module.enrolledStudents }}</td>
                  <td class="px-5 py-4 text-slate-600">{{ module.completedSessions }}</td>
                  <td class="px-5 py-4 font-semibold text-slate-950">{{ module.averageAttendancePercentage }}%
                    <progress class="attendance-progress mt-2" :value="module.averageAttendancePercentage" max="100" :aria-label="`${module.moduleCode} average attendance`"></progress>
                  </td>
                  <td class="px-5 py-4">
                    <StatusBadge :value="module.lowAttendanceStudents > 0 ? 'LOW' : 'HEALTHY'" :label="`${module.lowAttendanceStudents} low`" />
                  </td>
                </tr>
              </tbody>
            </table>
          </div>
        </section>
      </template>

      <div class="flex flex-wrap gap-3">
        <RouterLink to="/lecturer/absence-requests" class="ui-button ui-button-secondary">Review absence requests</RouterLink>
        <RouterLink
          to="/lecturer/modules"
          class="ui-button ui-button-secondary"
        >
          View my modules
        </RouterLink>
        <RouterLink
          to="/lecturer/sessions"
          class="ui-button ui-button-primary"
        >
          Manage sessions
        </RouterLink>
        <RouterLink
          to="/lecturer/reports"
          class="ui-button ui-button-secondary"
        >
          Attendance reports
        </RouterLink>
      </div>
    </section>
  </AppLayout>
</template>

<script setup>
import { computed, onMounted } from 'vue'
import AppAlert from '../components/AppAlert.vue'
import StatCard from '../components/StatCard.vue'
import StatusBadge from '../components/StatusBadge.vue'
import AppLayout from '../layouts/AppLayout.vue'
import { useStatisticsStore } from '../stores/statisticsStore'

const store = useStatisticsStore()
const summary = computed(() => store.lecturerSummary)

onMounted(() => {
  return Promise.allSettled([store.fetchLecturerSummary()])
})
</script>
