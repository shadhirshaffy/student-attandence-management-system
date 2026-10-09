<template>
  <AppLayout>
    <section class="space-y-6">
      <RouterLink :to="`/lecturer/sessions/${route.params.id}`" class="text-sm font-semibold text-blue-700 hover:text-blue-800">
        Back to session
      </RouterLink>

      <header>
        <p class="text-sm font-semibold uppercase tracking-wide text-blue-700">Manual attendance</p>
        <h1 class="mt-1 text-3xl font-bold text-slate-950">{{ title }}</h1>
        <p class="mt-2 text-sm text-slate-500">{{ subtitle }}</p>
      </header>

      <div v-if="store.error" class="rounded-md bg-red-50 px-4 py-3 text-sm text-red-700">{{ store.error }}</div>
      <div v-if="store.success" class="rounded-md bg-emerald-50 px-4 py-3 text-sm text-emerald-700">{{ store.success }}</div>

      <section class="rounded-lg border border-slate-200 bg-white shadow-sm">
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
            </div>
            <div v-if="attendance.editable" class="flex flex-wrap gap-2">
              <button class="rounded-md border border-slate-300 px-3 py-1.5 text-sm font-semibold text-slate-700 hover:bg-slate-100" @click="markAll('PRESENT')">
                Mark all present
              </button>
              <button class="rounded-md bg-blue-700 px-3 py-1.5 text-sm font-semibold text-white hover:bg-blue-800" :disabled="store.saving" @click="save">
                {{ store.saving ? 'Saving...' : 'Save attendance' }}
              </button>
            </div>
          </div>

          <div v-if="attendance.students.length === 0" class="p-8 text-center text-sm text-slate-500">
            No students are enrolled in this module.
          </div>
          <div v-else class="overflow-x-auto">
            <table class="min-w-full divide-y divide-slate-200 text-sm">
              <thead class="bg-slate-50 text-left text-xs font-semibold uppercase tracking-wide text-slate-500">
                <tr>
                  <th class="px-4 py-3">Student Number</th>
                  <th class="px-4 py-3">Student Name</th>
                  <th class="px-4 py-3">Status</th>
                  <th class="px-4 py-3">Last Marked</th>
                </tr>
              </thead>
              <tbody class="divide-y divide-slate-100">
                <tr v-for="student in attendance.students" :key="student.studentId">
                  <td class="px-4 py-3 font-medium text-slate-950">{{ student.studentNumber }}</td>
                  <td class="px-4 py-3">
                    <div class="font-medium text-slate-950">{{ student.studentName }}</div>
                    <div class="text-slate-500">{{ student.email }}</div>
                  </td>
                  <td class="px-4 py-3">
                    <div v-if="attendance.editable" class="inline-flex rounded-md border border-slate-300 bg-white p-1">
                      <button
                        type="button"
                        :class="selectedStatuses[student.studentId] === 'PRESENT' ? 'bg-emerald-600 text-white' : 'text-slate-600 hover:bg-slate-100'"
                        class="rounded px-3 py-1 text-xs font-semibold"
                        @click="selectedStatuses[student.studentId] = 'PRESENT'"
                      >
                        Present
                      </button>
                      <button
                        type="button"
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
                  <td class="px-4 py-3 text-slate-500">{{ student.markedAt || 'Not marked' }}</td>
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
import { computed, onMounted, reactive, watch } from 'vue'
import { useRoute } from 'vue-router'
import AppLayout from '../layouts/AppLayout.vue'
import { useAttendanceStore } from '../stores/attendanceStore'

const route = useRoute()
const store = useAttendanceStore()
const selectedStatuses = reactive({})

const attendance = computed(() => store.manualAttendance)
const title = computed(() => attendance.value ? `${attendance.value.moduleCode} - ${attendance.value.moduleName}` : 'Attendance')
const subtitle = computed(() =>
  attendance.value?.editable
    ? 'Mark students present or absent, then save the attendance list.'
    : 'This session is locked, completed, or not active yet, so attendance cannot be edited.',
)

onMounted(() => {
  store.clearMessages()
  return store.fetchManualAttendance(route.params.id)
})

watch(attendance, (value) => {
  if (!value) return
  for (const key of Object.keys(selectedStatuses)) {
    delete selectedStatuses[key]
  }
  for (const student of value.students) {
    selectedStatuses[student.studentId] = student.status || 'ABSENT'
  }
})

function markAll(status) {
  for (const student of attendance.value.students) {
    selectedStatuses[student.studentId] = status
  }
}

async function save() {
  const records = attendance.value.students.map((student) => ({
    studentId: student.studentId,
    status: selectedStatuses[student.studentId],
  }))
  await store.saveManualAttendance(route.params.id, records)
}

function statusClass(status) {
  return {
    PRESENT: 'bg-emerald-50 text-emerald-700',
    ABSENT: 'bg-red-50 text-red-700',
  }[status] || 'bg-slate-100 text-slate-600'
}
</script>
