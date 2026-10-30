<template>
  <AppLayout>
    <section class="space-y-7">
      <div>
        <p class="page-eyebrow">Student workspace</p>
        <h1 class="mt-2 text-3xl font-bold tracking-tight text-slate-950">Student dashboard</h1>
        <p class="mt-2 max-w-2xl text-sm text-slate-500">
          Track your attendance percentage by module and watch for low-attendance warnings.
        </p>
      </div>

      <section class="overview-banner">
        <div><p class="text-xs font-semibold uppercase tracking-[0.18em] text-blue-200">Your academic journey</p><h2 class="mt-3 text-2xl font-semibold text-white">Every class counts.</h2><p class="mt-3 max-w-xl text-sm leading-6 text-blue-100">Mark your attendance in class and keep track of your progress across modules.</p></div>
        <RouterLink to="/student/scan" class="shrink-0 rounded-lg bg-white px-5 py-3 text-sm font-semibold text-blue-900 hover:bg-blue-50">Scan QR <span aria-hidden="true">&rarr;</span></RouterLink>
      </section>

      <AppAlert v-if="store.error" type="error" :message="store.error" />
      <section v-if="store.loading" class="ui-panel overflow-hidden">
        <LoadingSpinner label="Loading attendance statistics..." />
      </section>

      <template v-if="summary">
        <div class="grid gap-4 sm:grid-cols-3">
          <StatCard label="Overall Attendance" :value="`${summary.overallAttendancePercentage}%`">
            <StatusBadge class="mt-3" :value="summary.overallLowAttendance ? 'LOW' : 'HEALTHY'" :label="summary.overallLowAttendance ? 'Below threshold' : 'Healthy'" />
          </StatCard>
          <StatCard label="Enrolled Modules" :value="summary.enrolledModules" />
          <StatCard label="Low Attendance Modules" :value="summary.lowAttendanceModules" :hint="`Threshold: ${summary.lowAttendanceThreshold}%`" />
        </div>

        <section class="ui-panel overflow-hidden">
          <div class="border-b border-slate-200 px-4 py-3">
            <h2 class="font-semibold text-slate-950">Module attendance</h2>
          </div>
          <div v-if="summary.modules.length === 0" class="p-8 text-center text-sm text-slate-500">No enrolled modules yet.</div>
          <div v-else class="grid gap-4 p-4 sm:p-5 md:grid-cols-2">
            <article v-for="module in summary.modules" :key="module.moduleId" class="rounded-xl border border-slate-200 p-5">
              <div class="flex flex-wrap items-start justify-between gap-3">
                <div class="min-w-0"><p class="text-xs font-bold tracking-wide text-blue-700">{{ module.moduleCode }}</p><h3 class="mt-2 break-words font-semibold text-slate-900">{{ module.moduleName }}</h3></div>
                <StatusBadge :value="module.lowAttendance ? 'LOW' : 'HEALTHY'" :label="module.lowAttendance ? 'Needs attention' : 'On track'" />
              </div>
              <div class="mt-6 flex items-baseline justify-between"><span class="text-sm text-slate-500">Attendance</span><span class="text-2xl font-bold text-slate-950">{{ module.attendancePercentage }}%</span></div>
              <progress class="attendance-progress mt-3" :class="{ 'attendance-progress-low': module.lowAttendance }" :value="module.attendancePercentage" max="100" :aria-label="`${module.moduleCode} attendance`"></progress>
              <div class="mt-4 flex flex-wrap gap-4 text-xs text-slate-500"><span><strong class="text-slate-800">{{ module.presentSessions }}</strong> present</span><span><strong class="text-slate-800">{{ module.absentSessions }}</strong> absent</span></div>
              <p v-if="module.lowAttendance" class="mt-4 rounded-lg bg-amber-50 p-3 text-xs leading-5 text-amber-800">Below the {{ summary.lowAttendanceThreshold }}% threshold. Review your attendance history for details.</p>
            </article>
          </div>
        </section>
      </template>

      <div class="flex flex-wrap gap-3">
        <RouterLink
          to="/student/modules"
          class="ui-button ui-button-primary"
        >
          View my modules
        </RouterLink>
        <RouterLink to="/student/attendance-history" class="ui-button ui-button-secondary">Attendance history</RouterLink>
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
  return Promise.allSettled([store.fetchStudentSummary()])
})
</script>
