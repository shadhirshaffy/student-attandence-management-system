<template>
  <AppLayout>
    <section class="space-y-6">
      <div>
        <p class="text-sm font-semibold uppercase tracking-wide text-blue-700">Lecturer</p>
        <h1 class="text-3xl font-bold text-slate-950">Lecturer dashboard</h1>
        <p class="max-w-2xl text-slate-600">
          Review module attendance health and manage attendance sessions.
        </p>
      </div>

      <AppAlert v-if="store.error" type="error" :message="store.error" />

      <template v-if="summary">
        <div class="grid gap-4 md:grid-cols-4">
          <StatCard label="Assigned Modules" :value="summary.assignedModules" />
          <StatCard label="Completed Sessions" :value="summary.completedSessions" />
          <StatCard label="Average Attendance" :value="`${summary.averageAttendancePercentage}%`" />
          <StatCard label="Low Attendance Students" :value="summary.lowAttendanceStudents" />
        </div>

        <section class="rounded-lg border border-slate-200 bg-white shadow-sm">
          <div class="border-b border-slate-200 px-4 py-3">
            <h2 class="font-semibold text-slate-950">Module statistics</h2>
          </div>
          <div v-if="summary.modules.length === 0" class="p-8 text-center text-sm text-slate-500">No assigned modules yet.</div>
          <div v-else class="overflow-x-auto">
            <table class="min-w-full divide-y divide-slate-200 text-sm">
              <thead class="bg-slate-50 text-left text-xs font-semibold uppercase tracking-wide text-slate-500">
                <tr>
                  <th class="px-4 py-3">Module</th>
                  <th class="px-4 py-3">Students</th>
                  <th class="px-4 py-3">Completed</th>
                  <th class="px-4 py-3">Average</th>
                  <th class="px-4 py-3">Warnings</th>
                </tr>
              </thead>
              <tbody class="divide-y divide-slate-100">
                <tr v-for="module in summary.modules" :key="module.moduleId">
                  <td class="px-4 py-3">
                    <div class="font-medium text-slate-950">{{ module.moduleCode }}</div>
                    <div class="text-slate-500">{{ module.moduleName }}</div>
                  </td>
                  <td class="px-4 py-3 text-slate-600">{{ module.enrolledStudents }}</td>
                  <td class="px-4 py-3 text-slate-600">{{ module.completedSessions }}</td>
                  <td class="px-4 py-3 font-semibold text-slate-950">{{ module.averageAttendancePercentage }}%</td>
                  <td class="px-4 py-3">
                    <StatusBadge :value="module.lowAttendanceStudents > 0 ? 'LOW' : 'HEALTHY'" :label="`${module.lowAttendanceStudents} low`" />
                  </td>
                </tr>
              </tbody>
            </table>
          </div>
        </section>
      </template>

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
        <RouterLink
          to="/lecturer/reports"
          class="inline-flex rounded-md border border-slate-300 px-4 py-2 text-sm font-semibold text-slate-700 hover:bg-slate-100"
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
  return store.fetchLecturerSummary()
})
</script>
