<template>
  <AppLayout>
    <section class="space-y-6">
      <RouterLink :to="`/lecturer/sessions/${route.params.id}`" class="text-sm font-semibold text-blue-700 hover:text-blue-800">
        Back to session
      </RouterLink>

      <header>
        <p class="page-eyebrow">Manual attendance</p>
        <h1 class="mt-2 text-3xl font-bold tracking-tight text-slate-950">{{ title }}</h1>
        <p class="mt-2 text-sm text-slate-500">{{ subtitle }}</p>
      </header>

      <AppAlert v-if="store.error" type="error" :message="store.error" />
      <AppAlert v-if="store.success" type="success" :message="store.success" />

      <section class="ui-panel overflow-hidden">
        <div v-if="store.loading" class="p-8 text-center text-sm text-slate-500">Loading attendance...</div>
        <div v-else-if="!attendance" class="p-8 text-center text-sm text-slate-500">Attendance data is not available.</div>
        <template v-else>
          <div class="flex flex-col gap-3 border-b border-slate-200 p-4 md:flex-row md:items-center md:justify-between">
            <div>
              <span :class="attendance.editable ? 'bg-emerald-50 text-emerald-700' : 'bg-slate-100 text-slate-600'" class="rounded-full px-2.5 py-1 text-xs font-semibold">
                {{ attendance.editable ? 'Editable' : 'Read only' }}
              </span>
              <span class="ml-2 rounded-full bg-blue-50 px-2.5 py-1 text-xs font-semibold text-blue-700">
                {{ attendance.sessionStatus }}
              </span>
              <div class="mt-3 flex flex-wrap gap-2 text-xs font-semibold text-slate-600">
                <span class="rounded-full bg-emerald-50 px-2.5 py-1 text-emerald-700">{{ presentCount }} present</span>
                <span class="rounded-full bg-red-50 px-2.5 py-1 text-red-700">{{ absentCount }} absent</span>
                <span class="rounded-full bg-slate-100 px-2.5 py-1 text-slate-700">{{ unmarkedCount }} unmarked</span>
              </div>
            </div>
            <div v-if="attendance.editable" class="flex flex-wrap gap-2">
              <button class="ui-button ui-button-secondary" :disabled="store.saving" @click="markAll('PRESENT')">
                Mark all present
              </button>
              <button
                class="rounded-md bg-blue-700 px-3 py-1.5 text-sm font-semibold text-white hover:bg-blue-800 disabled:cursor-not-allowed disabled:bg-slate-300"
                :disabled="store.saving || !canSave"
                @click="save"
              >
                {{ store.saving ? 'Saving...' : 'Save attendance' }}
              </button>
            </div>
          </div>

          <div v-if="attendance.editable" class="border-b border-slate-200 p-4">
            <label class="block text-sm font-medium text-slate-700" for="manual-reason">Reason for manual changes</label>
            <textarea
              id="manual-reason"
              v-model.trim="changeReason"
              maxlength="500"
              rows="2"
              class="mt-2 w-full rounded-md border border-slate-300 px-3 py-2 text-sm text-slate-900 shadow-sm outline-none transition placeholder:text-slate-400 focus:border-blue-600 focus:ring-2 focus:ring-blue-100"
              placeholder="Example: Student had QR scanner issue."
              required
            />
            <div class="mt-2 flex flex-wrap justify-between gap-2 text-xs text-slate-500"><p>Only changed selections are saved with this reason.</p><span>{{ changeReason.length }}/500</span></div>
            <p class="mt-3 text-sm font-semibold text-blue-700" role="status">{{ changedStudents.length }} pending changes</p>
          </div>

          <div v-if="attendance.students.length === 0" class="p-8 text-center text-sm text-slate-500">
            No students are enrolled in this module.
          </div>
          <div v-else class="overflow-x-auto" role="region" aria-label="Manual attendance list" tabindex="0">
            <table class="w-full min-w-[760px] divide-y divide-slate-200 text-sm">
              <thead class="bg-slate-50 text-left text-xs font-semibold uppercase tracking-wide text-slate-500">
                <tr>
                  <th class="px-4 py-3">Student Number</th>
                  <th class="px-4 py-3">Student Name</th>
                  <th class="px-4 py-3">Status</th>
                  <th class="px-4 py-3">Last Marked</th>
                  <th class="px-4 py-3">Audit</th>
                </tr>
              </thead>
              <tbody class="divide-y divide-slate-100">
                <tr v-for="student in attendance.students" :key="student.studentId" class="hover:bg-blue-50/40">
                  <td class="px-4 py-3 font-medium text-slate-950">{{ student.studentNumber }}</td>
                  <td class="px-4 py-3">
                    <div class="font-medium text-slate-950">{{ student.studentName }}</div>
                    <div class="text-slate-500">{{ student.email }}</div>
                  </td>
                  <td class="px-4 py-3">
                    <div v-if="attendance.editable" class="inline-flex whitespace-nowrap rounded-md border border-slate-300 bg-white p-1">
                      <button
                        type="button"
                        :disabled="store.saving"
                        :aria-label="`Mark ${student.studentName} ${student.studentNumber} present`"
                        :aria-pressed="selectedStatuses[student.studentId] === 'PRESENT'"
                        :class="selectedStatuses[student.studentId] === 'PRESENT' ? 'bg-emerald-600 text-white' : 'text-slate-600 hover:bg-slate-100'"
                        class="rounded px-3 py-1 text-xs font-semibold"
                        @click="selectedStatuses[student.studentId] = 'PRESENT'"
                      >
                        Present
                      </button>
                      <button
                        type="button"
                        :disabled="store.saving"
                        :aria-label="`Mark ${student.studentName} ${student.studentNumber} absent`"
                        :aria-pressed="selectedStatuses[student.studentId] === 'ABSENT'"
                        :class="selectedStatuses[student.studentId] === 'ABSENT' ? 'bg-red-600 text-white' : 'text-slate-600 hover:bg-slate-100'"
                        class="rounded px-3 py-1 text-xs font-semibold"
                        @click="selectedStatuses[student.studentId] = 'ABSENT'"
                      >
                        Absent
                      </button>
                    </div>
                    <span v-else :class="statusClass(student.status)" class="rounded-full px-2.5 py-1 text-xs font-semibold">
                      {{ student.status || 'UNMARKED' }}
                    </span>
                  </td>
                  <td class="px-4 py-3 text-slate-500">{{ formatDateTime(student.markedAt) }}</td>
                  <td class="px-4 py-3 text-slate-500">
                    <div>{{ student.markedByName || 'No marker yet' }}</div>
                    <div v-if="student.manualChangeReason" class="mt-1 max-w-xs text-xs text-slate-400">
                      {{ student.manualChangeReason }}
                    </div>
                  </td>
                </tr>
              </tbody>
            </table>
          </div>
        </template>
      </section>
    </section>
  </AppLayout>
</template>

<script setup>
import { computed, onMounted, reactive, ref, watch } from 'vue'
import { useRoute } from 'vue-router'
import AppAlert from '../components/AppAlert.vue'
import AppLayout from '../layouts/AppLayout.vue'
import { useAttendanceStore } from '../stores/attendanceStore'

const route = useRoute()
const store = useAttendanceStore()
const selectedStatuses = reactive({})
const changeReason = ref('')

const attendance = computed(() => store.manualAttendance)
const title = computed(() => attendance.value ? `${attendance.value.moduleCode} - ${attendance.value.moduleName}` : 'Attendance')
const subtitle = computed(() =>
  attendance.value?.editable
    ? 'Mark students present or absent, then save the attendance list.'
    : 'This session is locked, completed, or not active yet, so attendance cannot be edited.',
)
const presentCount = computed(() => countStatus('PRESENT'))
const absentCount = computed(() => countStatus('ABSENT'))
const unmarkedCount = computed(() => attendance.value?.students.filter((student) => !selectedStatuses[student.studentId]).length || 0)
const changedStudents = computed(() => (attendance.value?.students || []).filter((student) => selectedStatuses[student.studentId] && selectedStatuses[student.studentId] !== student.status))
const canSave = computed(() => Boolean(attendance.value?.editable) && Boolean(changeReason.value.trim()) && changedStudents.value.length > 0)

onMounted(() => {
  store.clearMessages()
  return Promise.allSettled([store.fetchManualAttendance(route.params.id)])
})

watch(attendance, (value) => {
  if (!value) return
  for (const key of Object.keys(selectedStatuses)) {
    delete selectedStatuses[key]
  }
  for (const student of value.students) {
    selectedStatuses[student.studentId] = student.status || ''
  }
})

function markAll(status) {
  for (const student of attendance.value.students) {
    selectedStatuses[student.studentId] = status
  }
}

async function save() {
  if (store.saving || !attendance.value?.editable) return
  if (!canSave.value) {
    store.error = 'Enter a reason before saving manual attendance.'
    return
  }
  const records = changedStudents.value.map((student) => ({
    studentId: student.studentId,
    status: selectedStatuses[student.studentId],
    reason: changeReason.value,
  }))
  try {
    await store.saveManualAttendance(route.params.id, records)
    changeReason.value = ''
  } catch {
    // Retain selections and the reason so the lecturer can retry.
  }
}

function countStatus(status) {
  if (!attendance.value) return 0
  return attendance.value.students.filter((student) => selectedStatuses[student.studentId] === status).length
}

function formatDateTime(value) {
  if (!value) return 'Not marked'
  return new Intl.DateTimeFormat(undefined, {
    dateStyle: 'medium',
    timeStyle: 'short',
  }).format(new Date(value))
}

function statusClass(status) {
  return {
    PRESENT: 'bg-emerald-50 text-emerald-700',
    ABSENT: 'bg-red-50 text-red-700',
  }[status] || 'bg-slate-100 text-slate-600'
}
</script>
