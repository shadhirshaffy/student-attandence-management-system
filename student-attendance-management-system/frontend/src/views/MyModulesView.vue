<template>
  <AppLayout>
    <section class="space-y-6">
      <div>
        <p class="page-eyebrow">{{ auth.role }}</p>
        <h1 class="mt-1 text-3xl font-bold text-slate-950">My modules</h1>
        <p class="mt-2 text-sm text-slate-500">{{ subtitle }}</p>
      </div>

      <div v-if="store.error" class="rounded-md bg-red-50 px-4 py-3 text-sm text-red-700">{{ store.error }}</div>

      <div v-if="store.success" role="status" class="rounded-lg border border-emerald-200 bg-emerald-50 p-4 text-sm text-emerald-800">{{ store.success }}</div>
      <section class="ui-panel overflow-hidden">
        <h2 class="border-b border-slate-100 px-5 py-4 font-semibold text-slate-900">{{ auth.role === 'STUDENT' ? 'Enrolled modules' : 'Assigned modules' }}</h2>
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
      <section v-if="auth.role === 'STUDENT'" class="ui-panel p-5 sm:p-6">
        <h2 class="text-lg font-semibold text-slate-950">Browse available modules</h2>
        <p class="mt-1 text-sm text-slate-500">Enroll in an active module to join its attendance sessions.</p>
        <label class="mt-5 block text-xs font-semibold text-slate-600">Search available modules<input v-model.trim="search" type="search" class="ui-input mt-2" placeholder="Module code, name, or lecturer" /></label>
        <p v-if="store.catalogLoading" role="status" class="py-8 text-sm text-slate-500">Loading available modules...</p>
        <p v-else-if="availableModules.length === 0" class="py-8 text-sm text-slate-500">{{ search ? 'No modules match your search.' : 'No more active modules are available to enroll in.' }}</p>
        <div v-else class="mt-5 grid gap-4 md:grid-cols-2">
          <article v-for="module in availableModules" :key="module.id" class="flex flex-col rounded-xl border border-slate-200 p-5">
            <p class="text-xs font-bold text-blue-700">{{ module.moduleCode }}</p>
            <h3 class="mt-2 break-words font-semibold text-slate-950">{{ module.moduleName }}</h3>
            <p class="mt-2 text-sm leading-6 text-slate-500">{{ module.description || 'No description available.' }}</p>
            <p class="mb-5 mt-3 text-xs text-slate-500">Lecturer: {{ module.lecturerName || 'Not assigned' }}</p>
            <button class="ui-button ui-button-primary mt-auto self-start" :disabled="store.saving" :aria-label="`Enroll in ${module.moduleCode}`" @click="enroll(module)">{{ enrollingId === module.id ? 'Enrolling...' : 'Enroll in module' }}</button>
          </article>
        </div>
      </section>
    </section>
  </AppLayout>
</template>

<script setup>
import { computed, onMounted, ref } from 'vue'
import AppLayout from '../layouts/AppLayout.vue'
import { useAuthStore } from '../stores/authStore'
import { useModulesStore } from '../stores/modulesStore'

const auth = useAuthStore()
const store = useModulesStore()
const search = ref('')
const enrollingId = ref(null)
const availableModules = computed(() => store.availableModules.filter((module) =>
  [module.moduleCode, module.moduleName, module.lecturerName].some((value) => String(value || '').toLowerCase().includes(search.value.toLowerCase())),
))
async function enroll(module) {
  if (store.saving) return
  enrollingId.value = module.id
  try { await store.enrollMyself(module.id) } catch { /* Store shows the server rejection. */ }
  finally { enrollingId.value = null }
}

const subtitle = computed(() =>
  auth.role === 'LECTURER'
    ? 'Modules assigned to your lecturer profile.'
    : 'View your enrollments and browse active modules to join.',
)

onMounted(() => {
  store.clearMessages()
  if (auth.role === 'LECTURER') {
    return Promise.allSettled([store.fetchLecturerModules()])
  } else {
    return Promise.allSettled([store.fetchMyStudentModules(), store.fetchAvailableModules()])
  }
})
</script>
