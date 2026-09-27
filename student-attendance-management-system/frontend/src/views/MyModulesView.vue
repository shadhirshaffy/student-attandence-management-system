<template>
  <AppLayout>
    <section class="space-y-6">
      <div>
        <p class="text-sm font-semibold uppercase tracking-wide text-blue-700">{{ auth.role }}</p>
        <h1 class="mt-1 text-3xl font-bold text-slate-950">My modules</h1>
        <p class="mt-2 text-sm text-slate-500">{{ subtitle }}</p>
      </div>

      <div v-if="store.error" class="rounded-md bg-red-50 px-4 py-3 text-sm text-red-700">{{ store.error }}</div>

      <section class="rounded-lg border border-slate-200 bg-white shadow-sm">
        <div v-if="store.loading" class="p-8 text-center text-sm text-slate-500">Loading modules...</div>
        <div v-else-if="store.myModules.length === 0" class="p-8 text-center">
          <h2 class="font-semibold text-slate-950">No modules found</h2>
          <p class="mt-1 text-sm text-slate-500">Assigned or enrolled modules will appear here.</p>
        </div>
        <div v-else class="grid gap-4 p-4 md:grid-cols-2">
          <article v-for="module in store.myModules" :key="module.id" class="rounded-lg border border-slate-200 p-4">
            <div class="flex items-start justify-between gap-3">
              <div>
                <h2 class="font-semibold text-slate-950">{{ module.moduleCode }} - {{ module.moduleName }}</h2>
                <p class="mt-2 text-sm text-slate-500">{{ module.description || 'No description' }}</p>
              </div>
              <span :class="module.active ? 'bg-emerald-50 text-emerald-700' : 'bg-slate-100 text-slate-600'" class="rounded-full px-2.5 py-1 text-xs font-semibold">
                {{ module.active ? 'Active' : 'Inactive' }}
              </span>
            </div>
            <p class="mt-4 text-sm text-slate-600">Lecturer: {{ module.lecturerName || 'Not assigned' }}</p>
          </article>
        </div>
      </section>
    </section>
  </AppLayout>
</template>

<script setup>
import { computed, onMounted } from 'vue'
import AppLayout from '../layouts/AppLayout.vue'
import { useAuthStore } from '../stores/authStore'
import { useModulesStore } from '../stores/modulesStore'

const auth = useAuthStore()
const store = useModulesStore()

const subtitle = computed(() =>
  auth.role === 'LECTURER'
    ? 'Modules assigned to your lecturer profile.'
    : 'Modules you are currently enrolled in.',
)

onMounted(() => {
  if (auth.role === 'LECTURER') {
    store.fetchLecturerModules()
  } else {
    store.fetchMyStudentModules()
  }
})
</script>
