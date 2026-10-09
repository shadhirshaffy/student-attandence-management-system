<template>
  <AppLayout>
    <section class="space-y-6">
      <div>
        <p class="text-sm font-semibold uppercase tracking-wide text-blue-700">Student</p>
        <h1 class="text-3xl font-bold text-slate-950">Student dashboard</h1>
        <p class="max-w-2xl text-slate-600">
          Track your attendance percentage by module and watch for low-attendance warnings.
        </p>
      </div>

      <AppAlert v-if="store.error" type="error" :message="store.error" />
      <section v-if="store.loading" class="rounded-lg border border-slate-200 bg-white shadow-sm">
        <LoadingSpinner label="Loading attendance statistics..." />
      </section>

      <template v-if="summary">
        <div class="grid gap-4 md:grid-cols-3">
          <StatCard label="Overall Attendance" :value="`${summary.overallAttendancePercentage}%`">
            <StatusBadge class="mt-3" :value="summary.overallLowAttendance ? 'LOW' : 'HEALTHY'" :label="summary.overallLowAttendance ? 'Below threshold' : 'Healthy'" />
          </StatCard>
          <StatCard label="Enrolled Modules" :value="summary.enrolledModules" />
          <StatCard label="Low Attendance Modules" :value="summary.lowAttendanceModules" :hint="`Threshold: ${summary.lowAttendanceThreshold}%`" />
        </div>

        <section class="rounded-lg border border-slate-200 bg-white shadow-sm">
          <div class="border-b border-slate-200 px-4 py-3">
            <h2 class="font-semibold text-slate-950">Module attendance</h2>
          </div>
          <div v-if="summary.modules.length === 0" class="p-8 text-center text-sm text-slate-500">No enrolled modules yet.</div>
          <div v-else class="overflow-x-auto">
            <table class="min-w-full divide-y divide-slate-200 text-sm">
              <thead class="bg-slate-50 text-left text-xs font-semibold uppercase tracking-wide text-slate-500">
                <tr>
                  <th class="px-4 py-3">Module</th>
                  <th class="px-4 py-3">Percentage</th>
                  <th class="px-4 py-3">Present</th>
                  <th class="px-4 py-3">Absent</th>
                  <th class="px-4 py-3">Warning</th>
                </tr>
              </thead>
              <tbody class="divide-y divide-slate-100">
                <tr v-for="module in summary.modules" :key="module.moduleId">
                  <td class="px-4 py-3">
                    <div class="font-medium text-slate-950">{{ module.moduleCode }}</div>
                    <div class="text-slate-500">{{ module.moduleName }}</div>
                  </td>
                  <td class="px-4 py-3 font-semibold text-slate-950">{{ module.attendancePercentage }}%</td>
                  <td class="px-4 py-3 text-slate-600">{{ module.presentSessions }}</td>
                  <td class="px-4 py-3 text-slate-600">{{ module.absentSessions }}</td>
                  <td class="px-4 py-3">
                    <StatusBadge :value="module.lowAttendance ? 'LOW' : 'HEALTHY'" :label="module.lowAttendance ? 'Low attendance' : 'OK'" />
                  </td>
                </tr>
              </tbody>
            </table>
          </div>
        </section>
      </template>

      <div class="flex flex-wrap gap-3">
        <RouterLink
          to="/student/modules"
          class="inline-flex rounded-md bg-blue-700 px-4 py-2 text-sm font-semibold text-white hover:bg-blue-800"
        >
          View my modules
        </RouterLink>
        <RouterLink
          to="/student/scan"
          class="inline-flex rounded-md border border-slate-300 px-4 py-2 text-sm font-semibold text-slate-700 hover:bg-slate-100"
        >
          Scan QR
        </RouterLink>
      </div>
    </section>
  </AppLayout>
</template>

<script setup>
import { computed, onMounted } from 'vue'
import AppAlert from '../components/AppAlert.vue'
import LoadingSpinner from '../components/LoadingSpinner.vue'
import StatCard from '../components/StatCard.vue'
import StatusBadge from '../components/StatusBadge.vue'
import AppLayout from '../layouts/AppLayout.vue'
import { useStatisticsStore } from '../stores/statisticsStore'

const store = useStatisticsStore()
const summary = computed(() => store.studentSummary)

onMounted(() => {
  return store.fetchStudentSummary()
})
</script>
