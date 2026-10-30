<template>
  <component :is="layout">
    <section class="space-y-6">
      <header>
        <p class="page-eyebrow">Reports</p>
        <h1 class="mt-2 text-3xl font-bold tracking-tight text-slate-950">Attendance reports</h1>
        <p class="mt-2 max-w-2xl text-sm text-slate-500">
          Generate real attendance reports by module, student, session, or date range.
        </p>
      </header>

      <AppAlert v-if="store.error" type="error" :message="store.error" />

      <section class="ui-panel p-5 sm:p-6">
        <h2 class="font-semibold text-slate-950">Build your report</h2><p class="mb-5 mt-1 text-sm text-slate-500">Choose a module or date range, then generate or download the results.</p>
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
          <button class="ui-button ui-button-primary" :disabled="store.loading || store.exporting" @click="generate">
            {{ store.loading ? 'Generating...' : 'Generate Report' }}
          </button>
          <button class="ui-button ui-button-secondary" :disabled="store.exporting || store.loading" @click="exportFile('csv')">
            Export CSV
          </button>
          <button class="ui-button ui-button-secondary" :disabled="store.exporting || store.loading" @click="exportFile('pdf')">
            Export PDF
          </button>
          <button class="ui-button ui-button-secondary" :disabled="store.loading || store.exporting" @click="resetFilters">Reset filters</button>
          <span v-if="store.exporting" class="self-center text-sm text-blue-700" role="status">Preparing download...</span>
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
          <div class="flex flex-wrap items-center justify-between gap-2"><h2 class="font-semibold text-slate-950">Report results</h2><p v-if="store.report" class="text-xs text-slate-500" role="status">{{ store.report.rows.length }} student-module records</p></div>
        </template>

        <tr v-for="row in store.report?.rows || []" :key="`${row.studentId}-${row.moduleId}`" class="hover:bg-blue-50/40">
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
    await Promise.allSettled([modules.fetchModules(), adminUsers.fetchUsers()])
  } else {
    await Promise.allSettled([modules.fetchLecturerModules()])
  }
})

function validFilters() {
  if (filters.dateFrom && filters.dateTo && filters.dateFrom > filters.dateTo) {
    store.error = 'The From date must be on or before the To date.'
    return false
  }
  if (filters.sessionId && (!Number.isInteger(Number(filters.sessionId)) || Number(filters.sessionId) < 1)) {
    store.error = 'Session ID must be a positive whole number.'
    return false
  }
  return true
}
function resetFilters() {
  Object.keys(filters).forEach((key) => { filters[key] = '' })
  store.error = ''
  store.report = null
}
async function generate() {
  if (store.loading || store.exporting || !validFilters()) return
  try { await store.generate({ ...filters }) } catch { /* Store displays API errors. */ }
}
async function exportFile(format) {
  if (store.loading || store.exporting || !validFilters()) return
  try { await store.exportReport(format, { ...filters }) } catch { /* Store displays API errors. */ }
}
</script>
