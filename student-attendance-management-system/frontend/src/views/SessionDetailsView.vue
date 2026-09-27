<template>
  <AppLayout>
    <section class="space-y-6">
      <RouterLink to="/lecturer/sessions" class="text-sm font-semibold text-blue-700 hover:text-blue-800">Back to sessions</RouterLink>

      <div v-if="store.error" class="rounded-md bg-red-50 px-4 py-3 text-sm text-red-700">{{ store.error }}</div>
      <div v-if="store.success" class="rounded-md bg-emerald-50 px-4 py-3 text-sm text-emerald-700">{{ store.success }}</div>

      <section v-if="store.loading" class="rounded-lg border border-slate-200 bg-white p-8 text-center text-sm text-slate-500 shadow-sm">
        Loading session...
      </section>

      <section v-else-if="session" class="rounded-lg border border-slate-200 bg-white p-6 shadow-sm">
        <div class="flex flex-col gap-4 md:flex-row md:items-start md:justify-between">
          <div>
            <p class="text-sm font-semibold uppercase tracking-wide text-blue-700">{{ session.moduleCode }}</p>
            <h1 class="mt-1 text-3xl font-bold text-slate-950">{{ session.moduleName }}</h1>
            <p class="mt-2 text-sm text-slate-500">{{ session.sessionDate }} · {{ session.startTime }} - {{ session.endTime }}</p>
          </div>
          <span :class="statusClass(session.status)" class="w-fit rounded-full px-3 py-1 text-xs font-semibold">
            {{ session.status }}
          </span>
        </div>

        <dl class="mt-6 grid gap-4 md:grid-cols-3">
          <div class="rounded-md bg-slate-50 p-4">
            <dt class="text-xs font-semibold uppercase tracking-wide text-slate-500">Room</dt>
            <dd class="mt-1 font-medium text-slate-950">{{ session.room || 'Not set' }}</dd>
          </div>
          <div class="rounded-md bg-slate-50 p-4">
            <dt class="text-xs font-semibold uppercase tracking-wide text-slate-500">Locked</dt>
            <dd class="mt-1 font-medium text-slate-950">{{ session.locked ? 'Yes' : 'No' }}</dd>
          </div>
          <div class="rounded-md bg-slate-50 p-4">
            <dt class="text-xs font-semibold uppercase tracking-wide text-slate-500">Token expiry</dt>
            <dd class="mt-1 font-medium text-slate-950">{{ session.tokenExpiry || 'Not active' }}</dd>
          </div>
        </dl>

        <div class="mt-6 flex flex-wrap gap-3">
          <button
            v-if="session.status === 'SCHEDULED'"
            class="rounded-md bg-emerald-700 px-4 py-2 text-sm font-semibold text-white hover:bg-emerald-800"
            :disabled="store.saving"
            @click="transition('activate')"
          >
            Activate
          </button>
          <button
            v-if="session.status === 'ACTIVE'"
            class="rounded-md bg-amber-600 px-4 py-2 text-sm font-semibold text-white hover:bg-amber-700"
            :disabled="store.saving"
            @click="transition('lock')"
          >
            Lock
          </button>
          <button
            v-if="session.status === 'ACTIVE' || session.status === 'LOCKED'"
            class="rounded-md bg-blue-700 px-4 py-2 text-sm font-semibold text-white hover:bg-blue-800"
            :disabled="store.saving"
            @click="transition('complete')"
          >
            Complete
          </button>
        </div>
      </section>
    </section>
  </AppLayout>
</template>

<script setup>
import { computed, onMounted } from 'vue'
import { useRoute } from 'vue-router'
import AppLayout from '../layouts/AppLayout.vue'
import { useSessionsStore } from '../stores/sessionsStore'

const route = useRoute()
const store = useSessionsStore()

const session = computed(() => store.currentSession)

onMounted(() => {
  store.clearMessages()
  store.fetchSession(route.params.id)
})

async function transition(action) {
  await store.transitionSession(route.params.id, action)
}

function statusClass(status) {
  return {
    SCHEDULED: 'bg-slate-100 text-slate-700',
    ACTIVE: 'bg-emerald-50 text-emerald-700',
    LOCKED: 'bg-amber-50 text-amber-700',
    COMPLETED: 'bg-blue-50 text-blue-700',
  }[status] || 'bg-slate-100 text-slate-700'
}
</script>
