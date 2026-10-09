<template>
  <AppLayout>
    <section class="space-y-6">
      <RouterLink to="/lecturer/sessions" class="text-sm font-semibold text-blue-700 hover:text-blue-800">Back to sessions</RouterLink>

      <AppAlert v-if="store.error" type="error" :message="store.error" />
      <AppAlert v-if="store.success" type="success" :message="store.success" />

      <section v-if="store.loading" class="rounded-lg border border-slate-200 bg-white shadow-sm">
        <LoadingSpinner label="Loading session..." />
      </section>

      <section v-else-if="session" class="rounded-lg border border-slate-200 bg-white p-6 shadow-sm">
        <div class="flex flex-col gap-4 md:flex-row md:items-start md:justify-between">
          <div>
            <p class="text-sm font-semibold uppercase tracking-wide text-blue-700">{{ session.moduleCode }}</p>
            <h1 class="mt-1 text-3xl font-bold text-slate-950">{{ session.moduleName }}</h1>
            <p class="mt-2 text-sm text-slate-500">{{ session.sessionDate }} · {{ session.startTime }} - {{ session.endTime }}</p>
          </div>
          <StatusBadge :value="session.status" class="w-fit" />
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
          <RouterLink
            :to="`/lecturer/sessions/${session.id}/attendance`"
            class="rounded-md border border-slate-300 px-4 py-2 text-sm font-semibold text-slate-700 hover:bg-slate-100"
          >
            Manual attendance
          </RouterLink>
          <RouterLink
            v-if="session.status === 'ACTIVE' && !session.locked"
            :to="`/lecturer/sessions/${session.id}/qr`"
            class="rounded-md border border-slate-300 px-4 py-2 text-sm font-semibold text-slate-700 hover:bg-slate-100"
          >
            Show QR
          </RouterLink>
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
import AppAlert from '../components/AppAlert.vue'
import LoadingSpinner from '../components/LoadingSpinner.vue'
import StatusBadge from '../components/StatusBadge.vue'
import AppLayout from '../layouts/AppLayout.vue'
import { useSessionsStore } from '../stores/sessionsStore'

const route = useRoute()
const store = useSessionsStore()

const session = computed(() => store.currentSession)

onMounted(() => {
  store.clearMessages()
  return store.fetchSession(route.params.id)
})

async function transition(action) {
  await store.transitionSession(route.params.id, action)
}

</script>
