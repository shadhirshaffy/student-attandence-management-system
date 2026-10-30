<template>
  <AppLayout>
    <section class="space-y-6">
      <header class="flex flex-col gap-4 md:flex-row md:items-end md:justify-between">
        <div>
          <p class="page-eyebrow">Lecturer</p>
          <h1 class="mt-2 text-3xl font-bold tracking-tight text-slate-950">Sessions</h1>
          <p class="mt-2 text-sm text-slate-500">Create and manage attendance sessions for your assigned modules.</p>
        </div>
        <RouterLink to="/lecturer/sessions/create" class="ui-button ui-button-primary">
          Create session
        </RouterLink>
      </header>

      <div v-if="store.error" class="rounded-md bg-red-50 px-4 py-3 text-sm text-red-700">{{ store.error }}</div>

      <section class="ui-panel overflow-hidden">
        <div class="grid gap-3 border-b border-slate-200 p-5 sm:grid-cols-[1fr_190px]">
          <label class="text-xs font-semibold text-slate-600">Search sessions<input v-model.trim="search" type="search" class="ui-input mt-2" placeholder="Module, date, or room" /></label>
          <label class="text-xs font-semibold text-slate-600">Session status<select v-model="status" class="ui-input mt-2"><option value="all">All statuses</option><option v-for="value in ['SCHEDULED', 'ACTIVE', 'LOCKED', 'COMPLETED']" :key="value" :value="value">{{ value.charAt(0) + value.slice(1).toLowerCase() }}</option></select></label>
          <p class="text-xs text-slate-500" role="status">{{ sortedSessions.length }} of {{ store.sessions.length }} sessions &middot; Newest first</p>
        </div>
        <div v-if="store.loading" class="p-8 text-center text-sm text-slate-500">Loading sessions...</div>
        <div v-else-if="store.sessions.length === 0" class="p-8 text-center">
          <h2 class="font-semibold text-slate-950">No sessions yet</h2>
          <p class="mt-1 text-sm text-slate-500">Create a session for one of your assigned modules.</p>
        </div>
        <div v-else-if="sortedSessions.length === 0" class="p-10 text-center"><h2 class="font-semibold text-slate-900">No matching sessions</h2><button class="ui-button ui-button-secondary mt-4" @click="search = ''; status = 'all'">Reset filters</button></div>
        <div v-else class="overflow-x-auto" role="region" aria-label="Attendance sessions" tabindex="0">
          <table class="min-w-full divide-y divide-slate-200 text-sm">
            <thead class="bg-slate-50 text-left text-xs font-semibold uppercase tracking-wide text-slate-500">
              <tr>
                <th class="px-5 py-4">Module</th>
                <th class="px-5 py-4">Date</th>
                <th class="px-5 py-4">Time</th>
                <th class="px-5 py-4">Room</th>
                <th class="px-5 py-4">Status</th>
                <th class="px-5 py-4 text-right">Actions</th>
              </tr>
            </thead>
            <tbody class="divide-y divide-slate-100">
              <tr v-for="session in sortedSessions" :key="session.id" class="hover:bg-blue-50/40">
                <td class="px-5 py-4">
                  <div class="font-medium text-slate-950">{{ session.moduleCode }}</div>
                  <div class="text-slate-500">{{ session.moduleName }}</div>
                </td>
                <td class="px-5 py-4 text-slate-600">{{ session.sessionDate }}</td>
                <td class="px-5 py-4 text-slate-600">{{ session.startTime }} - {{ session.endTime }}</td>
                <td class="px-5 py-4 text-slate-600">{{ session.room || 'Not set' }}</td>
                <td class="px-5 py-4">
                  <span :class="statusClass(session.status)" class="rounded-full px-2.5 py-1 text-xs font-semibold">
                    {{ session.status }}
                  </span>
                </td>
                <td class="px-5 py-4 text-right">
                  <RouterLink :to="`/lecturer/sessions/${session.id}`" class="rounded-md border border-slate-300 px-3 py-1.5 font-medium text-slate-700 hover:bg-slate-100">
                    View
                  </RouterLink>
                </td>
              </tr>
            </tbody>
          </table>
        </div>
      </section>
    </section>
  </AppLayout>
</template>

<script setup>
import { computed, onMounted, ref } from 'vue'
import AppLayout from '../layouts/AppLayout.vue'
import { useSessionsStore } from '../stores/sessionsStore'

const store = useSessionsStore()

const search = ref('')
const status = ref('all')
const sortedSessions = computed(() =>
  store.sessions.filter((session) => (status.value === 'all' || session.status === status.value) &&
    [session.moduleCode, session.moduleName, session.sessionDate, session.room].some((value) => String(value || '').toLowerCase().includes(search.value.toLowerCase()))).sort((a, b) => `${b.sessionDate} ${b.startTime}`.localeCompare(`${a.sessionDate} ${a.startTime}`)),
)

onMounted(() => {
  return Promise.allSettled([store.fetchMySessions()])
})

function statusClass(status) {
  return {
    SCHEDULED: 'bg-slate-100 text-slate-700',
    ACTIVE: 'bg-emerald-50 text-emerald-700',
    LOCKED: 'bg-amber-50 text-amber-700',
    COMPLETED: 'bg-blue-50 text-blue-700',
  }[status] || 'bg-slate-100 text-slate-700'
}
</script>
