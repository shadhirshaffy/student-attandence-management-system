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
      <AppAlert v-if="attendanceStore.error" type="error" :message="attendanceStore.error" />
      <AppAlert v-if="attendanceStore.success" type="success" :message="attendanceStore.success" />

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
          <td class="px-4 py-3">
            <div v-if="item.status === 'ABSENT'" class="space-y-2">
              <StatusBadge
                v-if="item.justificationStatus"
                :value="item.justificationStatus"
                :label="justificationLabel(item.justificationStatus)"
              />
              <button
                v-else
                type="button"
                class="rounded-md border border-slate-300 px-3 py-1.5 text-xs font-semibold text-slate-700 hover:bg-slate-100"
                @click="startJustification(item)"
              >
                Submit reason
              </button>
              <p v-if="item.justificationReason" class="max-w-xs text-xs text-slate-500">{{ item.justificationReason }}</p>
              <p v-if="item.justificationReviewNote" class="max-w-xs text-xs text-slate-400">{{ item.justificationReviewNote }}</p>
            </div>
            <span v-else class="text-xs text-slate-400">Not needed</span>
          </td>
        </tr>
      </DataTable>

      <div v-if="selectedItem" class="rounded-lg border border-slate-200 bg-white p-4 shadow-sm">
        <div class="flex flex-col gap-1 md:flex-row md:items-start md:justify-between">
          <div>
            <h2 class="font-semibold text-slate-950">Submit absence reason</h2>
            <p class="text-sm text-slate-500">{{ selectedItem.moduleCode }} · {{ selectedItem.sessionDate }}</p>
          </div>
          <button type="button" class="text-sm font-semibold text-slate-500 hover:text-slate-800" @click="cancelJustification">
            Cancel
          </button>
        </div>
        <label class="mt-4 block text-sm font-medium text-slate-700" for="absence-reason">Reason</label>
        <textarea
          id="absence-reason"
          v-model.trim="reason"
          maxlength="1000"
          rows="3"
          class="mt-2 w-full rounded-md border border-slate-300 px-3 py-2 text-sm text-slate-900 shadow-sm outline-none transition placeholder:text-slate-400 focus:border-blue-600 focus:ring-2 focus:ring-blue-100"
          placeholder="Explain why you were absent from this completed session."
        />
        <div class="mt-4 flex justify-end">
          <button
            type="button"
            class="rounded-md bg-blue-700 px-4 py-2 text-sm font-semibold text-white hover:bg-blue-800 disabled:cursor-not-allowed disabled:bg-slate-300"
            :disabled="attendanceStore.saving || !reason"
            @click="submitJustification"
          >
            {{ attendanceStore.saving ? 'Submitting...' : 'Submit justification' }}
          </button>
        </div>
      </div>
    </section>
  </AppLayout>
</template>

<script setup>
import { computed, onMounted, ref } from 'vue'
import AppAlert from '../components/AppAlert.vue'
import DataTable from '../components/DataTable.vue'
import StatusBadge from '../components/StatusBadge.vue'
import AppLayout from '../layouts/AppLayout.vue'
import { useAttendanceStore } from '../stores/attendanceStore'
import { useStatisticsStore } from '../stores/statisticsStore'

const store = useStatisticsStore()
const attendanceStore = useAttendanceStore()
const history = computed(() => store.studentSummary?.history || [])
const selectedItem = ref(null)
const reason = ref('')

const columns = [
  { key: 'module', label: 'Module' },
  { key: 'date', label: 'Date' },
  { key: 'time', label: 'Time' },
  { key: 'room', label: 'Room' },
  { key: 'status', label: 'Status' },
  { key: 'method', label: 'Method' },
  { key: 'marked', label: 'Marked At' },
  { key: 'justification', label: 'Justification' },
]

onMounted(() => {
  attendanceStore.clearMessages()
  return store.fetchStudentSummary()
})

function startJustification(item) {
  attendanceStore.clearMessages()
  selectedItem.value = item
  reason.value = ''
}

function cancelJustification() {
  selectedItem.value = null
  reason.value = ''
}

async function submitJustification() {
  await attendanceStore.submitAbsenceJustification(selectedItem.value.sessionId, reason.value)
  cancelJustification()
  await store.fetchStudentSummary()
}

function justificationLabel(status) {
  return {
    PENDING: 'Pending review',
    APPROVED: 'Approved',
    REJECTED: 'Rejected',
  }[status] || status
}
</script>
