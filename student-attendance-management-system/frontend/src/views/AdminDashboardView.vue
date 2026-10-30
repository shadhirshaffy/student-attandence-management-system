<template>
  <AdminLayout>
    <section class="space-y-7">
      <header class="flex flex-col justify-between gap-4 sm:flex-row sm:items-center">
        <div><p class="page-eyebrow">Workspace overview</p><h1 class="mt-2 text-3xl font-bold tracking-tight text-slate-950">Admin dashboard</h1><p class="mt-2 text-sm text-slate-500">Your students, teaching team, and attendance at a glance.</p></div>
        <RouterLink to="/admin/reports" class="ui-button ui-button-secondary">Attendance reports <span aria-hidden="true">&nearr;</span></RouterLink>
      </header>
      <section class="overview-banner">
        <div class="max-w-xl">
          <p class="text-xs font-semibold uppercase tracking-[0.18em] text-blue-200">Student attendance management</p>
          <h2 class="mt-3 text-2xl font-semibold tracking-tight text-white">Keep your academic day organised.</h2>
          <p class="mt-3 text-sm leading-6 text-blue-100">Manage your module catalogue, connect lecturers with students, and keep attendance records in one place.</p>
          <RouterLink to="/admin/modules" class="mt-5 inline-flex gap-3 rounded-lg bg-white px-4 py-2.5 text-sm font-semibold text-blue-900 hover:bg-blue-50">Manage modules <span aria-hidden="true">&rarr;</span></RouterLink>
        </div>
        <div class="hidden h-32 w-32 shrink-0 items-center justify-center rounded-3xl border border-white/20 bg-white/10 sm:flex" aria-hidden="true"><svg viewBox="0 0 64 64" class="h-20 w-20 fill-none stroke-white" stroke-width="2"><rect x="12" y="14" width="40" height="40" rx="7"/><path d="M22 9v12m20-12v12M12 28h40m-30 9 7 7 14-14"/></svg></div>
      </section>
      <AppAlert v-if="store.error" type="error" :message="store.error" />
      <div class="grid gap-4 sm:grid-cols-3"><StatCard v-for="card in cards" :key="card.label" :label="card.label" :value="store.loading ? '...' : card.value" /></div>
      <div class="grid items-start gap-6 xl:grid-cols-[1.5fr_1fr]">
        <section class="ui-panel p-5 sm:p-6">
          <h2 class="text-lg font-semibold text-slate-950">Attendance overview</h2><p class="mb-5 mt-1 text-sm text-slate-500">Calculated from completed sessions.</p>
          <AppAlert v-if="statistics.error" type="error" :message="statistics.error" />
          <p v-if="statistics.loading" role="status" class="py-8 text-sm text-slate-500">Loading attendance overview...</p>
          <div v-else-if="attendance" class="grid gap-4 sm:grid-cols-2">
            <StatCard label="Completed Sessions" :value="attendance.completedSessions" />
            <StatCard label="Attendance Records" :value="attendance.attendanceRecords" />
            <StatCard label="Average Attendance" :value="`${attendance.averageAttendancePercentage}%`" />
            <StatCard label="Low Attendance Cases" :value="attendance.lowAttendanceStudentModules" :hint="`Below ${attendance.lowAttendanceThreshold}% attendance`" />
          </div>
          <p v-else-if="!statistics.error" class="py-8 text-sm text-slate-500">Attendance data will appear here when available.</p>
        </section>
        <section class="ui-panel p-5 sm:p-6"><h2 class="text-lg font-semibold text-slate-950">Quick access</h2><p class="mt-1 text-sm text-slate-500">Your everyday administration tasks.</p>
          <div class="mt-5 divide-y divide-slate-100">
            <RouterLink v-for="link in quickLinks" :key="link.to" :to="link.to" class="group flex items-center justify-between gap-4 rounded-lg py-4 hover:bg-slate-50"><div><p class="text-sm font-semibold text-slate-800 group-hover:text-blue-700">{{ link.label }}</p><p class="mt-1 text-xs leading-5 text-slate-500">{{ link.description }}</p></div><span aria-hidden="true">&rarr;</span></RouterLink>
          </div>
        </section>
      </div>
    </section>
  </AdminLayout>
</template>

<script setup>
import { computed, onMounted } from 'vue'
import AppAlert from '../components/AppAlert.vue'
import StatCard from '../components/StatCard.vue'
import AdminLayout from '../layouts/AdminLayout.vue'
import { useAdminUsersStore } from '../stores/adminUsersStore'
import { useStatisticsStore } from '../stores/statisticsStore'

const store = useAdminUsersStore()
const statistics = useStatisticsStore()
const attendance = computed(() => statistics.adminSummary)

const quickLinks = [
  { to: '/admin/users', label: 'Manage people', description: 'Maintain student and lecturer accounts.' },
  { to: '/admin/modules', label: 'Module catalogue', description: 'Organise modules and assign lecturers.' },
  { to: '/admin/enrollments', label: 'Student enrollments', description: 'Connect students to their modules.' },
]
const cards = computed(() => [
  { label: 'Total users', value: store.users.length },
  { label: 'Students', value: store.students.length },
  { label: 'Lecturers', value: store.lecturers.length },
])

onMounted(() => {
  return Promise.allSettled([store.fetchUsers(), statistics.fetchAdminSummary()])
})
</script>
