<template>
  <AdminLayout>
    <section class="space-y-6">
      <div>
        <p class="text-sm font-semibold uppercase tracking-wide text-blue-700">Admin</p>
        <h1 class="mt-1 text-3xl font-bold text-slate-950">Admin dashboard</h1>
        <p class="mt-2 max-w-2xl text-slate-600">
          Manage user accounts, role profiles, modules, lecturers, and enrollments for the Student Attendance Management System.
        </p>
      </div>

      <div class="grid gap-4 md:grid-cols-3">
        <StatCard v-for="card in cards" :key="card.label" :label="card.label" :value="card.value" />
      </div>

      <AppAlert v-if="statistics.error" type="error" :message="statistics.error" />
      <div v-if="attendance" class="grid gap-4 md:grid-cols-4">
        <StatCard label="Completed Sessions" :value="attendance.completedSessions" />
        <StatCard label="Attendance Records" :value="attendance.attendanceRecords" />
        <StatCard label="Average Attendance" :value="`${attendance.averageAttendancePercentage}%`" />
        <StatCard label="Low Attendance Cases" :value="attendance.lowAttendanceStudentModules" :hint="`Threshold: ${attendance.lowAttendanceThreshold}%`" />
      </div>

      <RouterLink
        to="/admin/modules"
        class="inline-flex rounded-md bg-blue-700 px-4 py-2 text-sm font-semibold text-white hover:bg-blue-800"
      >
        Manage modules
      </RouterLink>
      <RouterLink
        to="/admin/reports"
        class="ml-3 inline-flex rounded-md border border-slate-300 px-4 py-2 text-sm font-semibold text-slate-700 hover:bg-slate-100"
      >
        Attendance reports
      </RouterLink>
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

const cards = computed(() => [
  { label: 'Total users', value: store.users.length },
  { label: 'Students', value: store.students.length },
  { label: 'Lecturers', value: store.lecturers.length },
])

onMounted(() => {
  return Promise.all([store.fetchUsers(), statistics.fetchAdminSummary()])
})
</script>
