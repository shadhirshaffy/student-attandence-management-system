<template>
  <component :is="layout">
    <section class="space-y-6">
      <header>
        <p class="text-sm font-semibold uppercase tracking-wide text-blue-700">Reports</p>
        <h1 class="mt-1 text-3xl font-bold text-slate-950">Attendance reports</h1>
        <p class="mt-2 max-w-2xl text-sm text-slate-500">
          Generate real attendance reports by module, student, session, or date range.
        </p>
      </header>

      <AppAlert v-if="store.error" type="error" :message="store.error" />

      <section class="rounded-lg border border-slate-200 bg-white p-5 shadow-sm">
        <div class="grid gap-4 md:grid-cols-3">
          <label class="text-sm font-medium text-slate-700">Module
            <select v-model="filters.moduleId" class="mt-2 w-full rounded-md border border-slate-300 bg-white px-3 py-2 text-sm">
              <option value="">All modules</option>
              <option v-for="module in availableModules" :key="module.id" :value="module.id">
                {{ module.moduleCode }} - {{ module.moduleName }}
              </option>
            </select>
          </label>

          <label v-if="auth.role === 'ADMIN'" class="text-sm font-medium text-slate-700">Student
            <select v-model="filters.studentId" class="mt-2 w-full rounded-md border border-slate-300 bg-white px-3 py-2 text-sm">
              <option value="">All students</option>
              <option v-for="student in adminUsers.students" :key="student.profileId" :value="student.profileId">
                {{ student.studentNumber }} - {{ student.name }}
              </option>
            </select>
          </label>

          <label class="text-sm font-medium text-slate-700">Session ID
            <input v-model.trim="filters.sessionId" type="number" min="1" class="mt-2 w-full rounded-md border border-slate-300 px-3 py-2 text-sm" />
          </label>

          <label class="text-sm font-medium text-slate-700">From
            <input v-model="filters.dateFrom" type="date" class="mt-2 w-full rounded-md border border-slate-300 px-3 py-2 text-sm" />
          </label>

          <label class="text-sm font-medium text-slate-700">To
            <input v-model="filters.dateTo" type="date" class="mt-2 w-full rounded-md border border-slate-300 px-3 py-2 text-sm" />
          </label>
        </div>

        <div class="mt-5 flex flex-wrap gap-3">
          <button class="rounded-md bg-blue-700 px-4 py-2 text-sm font-semibold text-white hover:bg-blue-800" :disabled="store.loading" @click="generate">
            {{ store.loading ? 'Generating...' : 'Generate Report' }}
          </button>
          <button class="rounded-md border border-slate-300 px-4 py-2 text-sm font-semibold text-slate-700 hover:bg-slate-100" :disabled="store.exporting" @click="exportFile('csv')">
            Export CSV
          </button>
          <button class="rounded-md border border-slate-300 px-4 py-2 text-sm font-semibold text-slate-700 hover:bg-slate-100" :disabled="store.exporting" @click="exportFile('pdf')">
            Export PDF
          </button>
        </div>
      </section>

      <DataTable
        :columns="columns"
        :loading="store.loading"
        loading-label="Loading report..."
        :empty="!store.report || store.report.rows.length === 0"
        :empty-title="store.report ? 'No report data' : 'Generate a report'"
        :empty-message="store.report ? 'No report data matched the selected filters.' : 'Choose filters and generate a report.'"
      >
        <template #header>
          <h2 class="font-semibold text-slate-950">Report results</h2>
        </template>

        <tr v-for="row in store.report?.rows || []" :key="`${row.studentId}-${row.moduleId}`">
          <td class="px-4 py-3">
            <div class="font-medium text-slate-950">{{ row.studentNumber }}</div>
            <div class="text-slate-500">{{ row.studentName }}</div>
          </td>
          <td class="px-4 py-3">
            <div class="font-medium text-slate-950">{{ row.moduleCode }}</div>
            <div class="text-slate-500">{{ row.moduleName }}</div>
          </td>
          <td class="px-4 py-3 text-slate-600">{{ row.sessionsHeld }}</td>
          <td class="px-4 py-3 text-slate-600">{{ row.sessionsPresent }}</td>
          <td class="px-4 py-3 text-slate-600">{{ row.sessionsAbsent }}</td>
          <td class="px-4 py-3 font-semibold text-slate-950">{{ row.attendancePercentage }}%</td>
        </tr>
      </DataTable>
    </section>
  </component>
</template>

<script setup>
import { computed, onMounted, reactive } from 'vue'
import AppAlert from '../components/AppAlert.vue'
import DataTable from '../components/DataTable.vue'
import AdminLayout from '../layouts/AdminLayout.vue'
import AppLayout from '../layouts/AppLayout.vue'
import { useAdminUsersStore } from '../stores/adminUsersStore'
import { useAuthStore } from '../stores/authStore'
import { useModulesStore } from '../stores/modulesStore'
import { useReportsStore } from '../stores/reportsStore'

const auth = useAuthStore()
const modules = useModulesStore()
const adminUsers = useAdminUsersStore()
const store = useReportsStore()

const filters = reactive({
  moduleId: '',
  studentId: '',
  sessionId: '',
  dateFrom: '',
  dateTo: '',
})

const layout = computed(() => (auth.role === 'ADMIN' ? AdminLayout : AppLayout))
const availableModules = computed(() => (auth.role === 'ADMIN' ? modules.modules : modules.myModules))
const columns = [
  { key: 'student', label: 'Student' },
  { key: 'module', label: 'Module' },
  { key: 'held', label: 'Held' },
  { key: 'present', label: 'Present' },
  { key: 'absent', label: 'Absent' },
  { key: 'percentage', label: 'Percentage' },
]

onMounted(async () => {
  if (auth.role === 'ADMIN') {
    await Promise.all([modules.fetchModules(), adminUsers.fetchUsers()])
  } else {
    await modules.fetchLecturerModules()
  }
})

function generate() {
  return store.generate(filters)
}

function exportFile(format) {
  return store.exportReport(format, filters)
}
</script>
