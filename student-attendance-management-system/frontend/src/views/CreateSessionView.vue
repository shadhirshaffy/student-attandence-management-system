<template>
  <AppLayout>
    <section class="mx-auto max-w-3xl space-y-6">
      <div>
        <p class="text-sm font-semibold uppercase tracking-wide text-blue-700">Lecturer</p>
        <h1 class="mt-1 text-3xl font-bold text-slate-950">Create session</h1>
        <p class="mt-2 text-sm text-slate-500">Create an attendance session for one of your assigned modules.</p>
      </div>

      <div v-if="sessionsStore.error" class="rounded-md bg-red-50 px-4 py-3 text-sm text-red-700">{{ sessionsStore.error }}</div>

      <form class="rounded-lg border border-slate-200 bg-white p-6 shadow-sm" @submit.prevent="saveSession">
        <div class="grid gap-4 md:grid-cols-2">
          <label class="text-sm font-medium text-slate-700">Module
            <select v-model.number="form.moduleId" required class="mt-2 w-full rounded-md border border-slate-300 bg-white px-3 py-2 text-sm">
              <option disabled value="">Select module</option>
              <option v-for="module in activeModules" :key="module.id" :value="module.id">
                {{ module.moduleCode }} - {{ module.moduleName }}
              </option>
            </select>
          </label>
          <label class="text-sm font-medium text-slate-700">Date
            <input v-model="form.sessionDate" required type="date" class="mt-2 w-full rounded-md border border-slate-300 px-3 py-2 text-sm" />
          </label>
          <label class="text-sm font-medium text-slate-700">Start time
            <input v-model="form.startTime" required type="time" class="mt-2 w-full rounded-md border border-slate-300 px-3 py-2 text-sm" />
          </label>
          <label class="text-sm font-medium text-slate-700">End time
            <input v-model="form.endTime" required type="time" class="mt-2 w-full rounded-md border border-slate-300 px-3 py-2 text-sm" />
          </label>
        </div>
        <label class="mt-4 block text-sm font-medium text-slate-700">Room
          <input v-model.trim="form.room" maxlength="100" class="mt-2 w-full rounded-md border border-slate-300 px-3 py-2 text-sm" />
        </label>

        <div class="mt-6 flex justify-end gap-3">
          <RouterLink to="/lecturer/sessions" class="rounded-md border border-slate-300 px-4 py-2 text-sm font-semibold text-slate-700 hover:bg-slate-100">
            Cancel
          </RouterLink>
          <button type="submit" class="rounded-md bg-blue-700 px-4 py-2 text-sm font-semibold text-white hover:bg-blue-800" :disabled="sessionsStore.saving">
            {{ sessionsStore.saving ? 'Saving...' : 'Create session' }}
          </button>
        </div>
      </form>
    </section>
  </AppLayout>
</template>

<script setup>
import { computed, onMounted, reactive } from 'vue'
import { useRouter } from 'vue-router'
import AppLayout from '../layouts/AppLayout.vue'
import { useModulesStore } from '../stores/modulesStore'
import { useSessionsStore } from '../stores/sessionsStore'

const router = useRouter()
const modulesStore = useModulesStore()
const sessionsStore = useSessionsStore()

const form = reactive({
  moduleId: '',
  sessionDate: '',
  startTime: '',
  endTime: '',
  room: '',
})

const activeModules = computed(() => modulesStore.myModules.filter((module) => module.active))

onMounted(() => {
  modulesStore.fetchLecturerModules()
  sessionsStore.clearMessages()
})

async function saveSession() {
  const session = await sessionsStore.createSession(form)
  router.push(`/lecturer/sessions/${session.id}`)
}
</script>
