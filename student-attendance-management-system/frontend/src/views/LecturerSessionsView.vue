<template>
  <AppLayout>
    <section class="space-y-6">
      <header class="flex flex-col gap-4 md:flex-row md:items-end md:justify-between">
        <div>
          <p class="text-sm font-semibold uppercase tracking-wide text-blue-700">Lecturer</p>
          <h1 class="mt-1 text-3xl font-bold text-slate-950">Sessions</h1>
          <p class="mt-2 text-sm text-slate-500">Create and manage attendance sessions for your assigned modules.</p>
        </div>
        <RouterLink to="/lecturer/sessions/create" class="rounded-md bg-blue-700 px-4 py-2 text-sm font-semibold text-white hover:bg-blue-800">
          Create session
        </RouterLink>
      </header>

      <div v-if="store.error" class="rounded-md bg-red-50 px-4 py-3 text-sm text-red-700">{{ store.error }}</div>

      <section class="rounded-lg border border-slate-200 bg-white shadow-sm">
        <div v-if="store.loading" class="p-8 text-center text-sm text-slate-500">Loading sessions...</div>
        <div v-else-if="store.sessions.length === 0" class="p-8 text-center">
          <h2 class="font-semibold text-slate-950">No sessions yet</h2>
          <p class="mt-1 text-sm text-slate-500">Create a session for one of your assigned modules.</p>
        </div>
        <div v-else class="overflow-x-auto">
          <table class="min-w-full divide-y divide-slate-200 text-sm">
            <thead class="bg-slate-50 text-left text-xs font-semibold uppercase tracking-wide text-slate-500">
              <tr>
                <th class="px-4 py-3">Module</th>
                <th class="px-4 py-3">Date</th>
                <th class="px-4 py-3">Time</th>
                <th class="px-4 py-3">Room</th>
                <th class="px-4 py-3">Status</th>
                <th class="px-4 py-3 text-right">Actions</th>
              </tr>
            </thead>
            <tbody class="divide-y divide-slate-100">
              <tr v-for="session in sortedSessions" :key="session.id">
                <td class="px-4 py-3">
                  <div class="font-medium text-slate-950">{{ session.moduleCode }}</div>
                  <div class="text-slate-500">{{ session.moduleName }}</div>
                </td>
                <td class="px-4 py-3 text-slate-600">{{ session.sessionDate }}</td>
                <td class="px-4 py-3 text-slate-600">{{ session.startTime }} - {{ session.endTime }}</td>
                <td class="px-4 py-3 text-slate-600">{{ session.room || 'Not set' }}</td>
                <td class="px-4 py-3">
                  <span :class="statusClass(session.status)" class="rounded-full px-2.5 py-1 text-xs font-semibold">
                    {{ session.status }}
                  </span>
                </td>
                <td class="px-4 py-3 text-right">
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
import { computed, onMounted } from 'vue'
import AppLayout from '../layouts/AppLayout.vue'
import { useSessionsStore } from '../stores/sessionsStore'

const store = useSessionsStore()

const sortedSessions = computed(() =>
  [...store.sessions].sort((a, b) => `${b.sessionDate} ${b.startTime}`.localeCompare(`${a.sessionDate} ${a.startTime}`)),
)

onMounted(() => {
  store.fetchMySessions()
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
