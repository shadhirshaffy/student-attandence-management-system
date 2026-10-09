<template>
  <AppLayout>
    <section class="space-y-6">
      <header>
        <p class="text-sm font-semibold uppercase tracking-wide text-blue-700">Student</p>
        <h1 class="mt-1 text-3xl font-bold text-slate-950">Attendance history</h1>
        <p class="mt-2 max-w-2xl text-sm text-slate-500">
          Review completed sessions from your enrolled modules and the attendance status recorded for each session.
        </p>
      </header>

      <AppAlert v-if="store.error" type="error" :message="store.error" />

      <DataTable
        :columns="columns"
        :loading="store.loading"
        loading-label="Loading attendance history..."
        :empty="history.length === 0"
        empty-title="No attendance history yet"
        empty-message="Completed sessions from your enrolled modules will appear here."
      >
        <tr v-for="item in history" :key="`${item.sessionId}-${item.moduleId}`">
          <td class="px-4 py-3">
            <div class="font-medium text-slate-950">{{ item.moduleCode }}</div>
            <div class="text-slate-500">{{ item.moduleName }}</div>
          </td>
          <td class="px-4 py-3 text-slate-600">{{ item.sessionDate }}</td>
          <td class="px-4 py-3 text-slate-600">{{ item.startTime }} - {{ item.endTime }}</td>
          <td class="px-4 py-3 text-slate-600">{{ item.room || 'Not set' }}</td>
          <td class="px-4 py-3"><StatusBadge :value="item.status" /></td>
          <td class="px-4 py-3 text-slate-600">{{ item.markingMethod || 'Not marked' }}</td>
          <td class="px-4 py-3 text-slate-500">{{ item.markedAt || 'Not marked' }}</td>
        </tr>
      </DataTable>
    </section>
  </AppLayout>
</template>

<script setup>
import { computed, onMounted } from 'vue'
import AppAlert from '../components/AppAlert.vue'
import DataTable from '../components/DataTable.vue'
import StatusBadge from '../components/StatusBadge.vue'
import AppLayout from '../layouts/AppLayout.vue'
import { useStatisticsStore } from '../stores/statisticsStore'

const store = useStatisticsStore()
const history = computed(() => store.studentSummary?.history || [])

const columns = [
  { key: 'module', label: 'Module' },
  { key: 'date', label: 'Date' },
  { key: 'time', label: 'Time' },
  { key: 'room', label: 'Room' },
  { key: 'status', label: 'Status' },
  { key: 'method', label: 'Method' },
  { key: 'marked', label: 'Marked At' },
]

onMounted(() => {
  return store.fetchStudentSummary()
})
</script>
