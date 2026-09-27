<template>
  <AdminLayout>
    <div class="space-y-6">
      <header class="flex flex-col gap-4 md:flex-row md:items-end md:justify-between">
        <div>
          <p class="text-sm font-semibold uppercase tracking-wide text-blue-700">Modules</p>
          <h1 class="mt-1 text-3xl font-bold text-slate-950">Module management</h1>
          <p class="mt-2 text-sm text-slate-500">Create modules, assign lecturers, and manage enrollments.</p>
        </div>
        <button class="rounded-md bg-blue-700 px-4 py-2 text-sm font-semibold text-white hover:bg-blue-800" @click="openCreate">
          Create module
        </button>
      </header>

      <div v-if="store.success" class="rounded-md bg-emerald-50 px-4 py-3 text-sm text-emerald-700">{{ store.success }}</div>
      <div v-if="store.error" class="rounded-md bg-red-50 px-4 py-3 text-sm text-red-700">{{ store.error }}</div>

      <section class="rounded-lg border border-slate-200 bg-white shadow-sm">
        <div v-if="store.loading" class="p-8 text-center text-sm text-slate-500">Loading modules...</div>
        <div v-else-if="store.modules.length === 0" class="p-8 text-center">
          <h2 class="font-semibold text-slate-950">No modules yet</h2>
          <p class="mt-1 text-sm text-slate-500">Create the first module to start enrollment.</p>
        </div>
        <div v-else class="overflow-x-auto">
          <table class="min-w-full divide-y divide-slate-200 text-sm">
            <thead class="bg-slate-50 text-left text-xs font-semibold uppercase tracking-wide text-slate-500">
              <tr>
                <th class="px-4 py-3">Module</th>
                <th class="px-4 py-3">Lecturer</th>
                <th class="px-4 py-3">Status</th>
                <th class="px-4 py-3 text-right">Actions</th>
              </tr>
            </thead>
            <tbody class="divide-y divide-slate-100">
              <tr v-for="module in store.modules" :key="module.id">
                <td class="px-4 py-3">
                  <div class="font-medium text-slate-950">{{ module.moduleCode }} - {{ module.moduleName }}</div>
                  <div class="max-w-xl text-slate-500">{{ module.description || 'No description' }}</div>
                </td>
                <td class="px-4 py-3 text-slate-600">{{ module.lecturerName || 'Unassigned' }}</td>
                <td class="px-4 py-3">
                  <span :class="module.active ? 'bg-emerald-50 text-emerald-700' : 'bg-slate-100 text-slate-600'" class="rounded-full px-2.5 py-1 text-xs font-semibold">
                    {{ module.active ? 'Active' : 'Inactive' }}
                  </span>
                </td>
                <td class="px-4 py-3">
                  <div class="flex flex-wrap justify-end gap-2">
                    <button class="rounded-md border border-slate-300 px-3 py-1.5 font-medium text-slate-700 hover:bg-slate-100" @click="openEdit(module)">Edit</button>
                    <button class="rounded-md border border-slate-300 px-3 py-1.5 font-medium text-slate-700 hover:bg-slate-100" @click="toggleModule(module)">
                      {{ module.active ? 'Deactivate' : 'Activate' }}
                    </button>
                    <button class="rounded-md border border-slate-300 px-3 py-1.5 font-medium text-slate-700 hover:bg-slate-100" @click="openEnroll(module)">Enrollments</button>
                  </div>
                </td>
              </tr>
            </tbody>
          </table>
        </div>
      </section>
    </div>

    <div v-if="showForm" class="fixed inset-0 z-50 flex items-center justify-center bg-slate-950/40 px-4 py-6">
      <form class="w-full max-w-2xl rounded-lg bg-white p-6 shadow-xl" @submit.prevent="saveModule">
        <h2 class="text-lg font-semibold text-slate-950">{{ editingModule ? 'Edit module' : 'Create module' }}</h2>
        <div class="mt-5 grid gap-4 md:grid-cols-2">
          <label class="text-sm font-medium text-slate-700">Code
            <input v-model.trim="form.moduleCode" required class="mt-2 w-full rounded-md border border-slate-300 px-3 py-2 text-sm" />
          </label>
          <label class="text-sm font-medium text-slate-700">Name
            <input v-model.trim="form.moduleName" required class="mt-2 w-full rounded-md border border-slate-300 px-3 py-2 text-sm" />
          </label>
        </div>
        <label class="mt-4 block text-sm font-medium text-slate-700">Description
          <textarea v-model.trim="form.description" rows="3" class="mt-2 w-full rounded-md border border-slate-300 px-3 py-2 text-sm"></textarea>
        </label>
        <div class="mt-4 grid gap-4 md:grid-cols-2">
          <label class="text-sm font-medium text-slate-700">Lecturer
            <select v-model.number="form.lecturerId" required class="mt-2 w-full rounded-md border border-slate-300 bg-white px-3 py-2 text-sm">
              <option disabled value="">Select lecturer</option>
              <option v-for="lecturer in lecturers" :key="lecturer.profileId" :value="lecturer.profileId">{{ lecturer.name }} ({{ lecturer.employeeNumber }})</option>
            </select>
          </label>
          <label class="flex items-center gap-2 pt-7 text-sm font-medium text-slate-700">
            <input v-model="form.active" type="checkbox" class="h-4 w-4" />
            Active module
          </label>
        </div>
        <div class="mt-6 flex justify-end gap-3">
          <button type="button" class="rounded-md border border-slate-300 px-4 py-2 text-sm font-semibold" @click="showForm = false">Cancel</button>
          <button type="submit" class="rounded-md bg-blue-700 px-4 py-2 text-sm font-semibold text-white" :disabled="store.saving">{{ store.saving ? 'Saving...' : 'Save' }}</button>
        </div>
      </form>
    </div>

    <div v-if="enrollmentModule" class="fixed inset-0 z-50 flex items-center justify-center bg-slate-950/40 px-4 py-6">
      <section class="w-full max-w-3xl rounded-lg bg-white p-6 shadow-xl">
        <div class="flex items-start justify-between gap-4">
          <div>
            <h2 class="text-lg font-semibold text-slate-950">Enrollments: {{ enrollmentModule.moduleCode }}</h2>
            <p class="mt-1 text-sm text-slate-500">{{ enrollmentModule.moduleName }}</p>
          </div>
          <button class="rounded-md px-2 py-1 text-sm text-slate-500 hover:bg-slate-100" @click="enrollmentModule = null">Close</button>
        </div>

        <form class="mt-5 flex flex-col gap-3 sm:flex-row" @submit.prevent="enrollSelectedStudent">
          <select v-model.number="selectedStudentId" required class="flex-1 rounded-md border border-slate-300 bg-white px-3 py-2 text-sm">
            <option disabled value="">Select student</option>
            <option v-for="student in availableStudents" :key="student.profileId" :value="student.profileId">{{ student.name }} ({{ student.studentNumber }})</option>
          </select>
          <button class="rounded-md bg-blue-700 px-4 py-2 text-sm font-semibold text-white" :disabled="store.saving">Enroll</button>
        </form>

        <div class="mt-5 divide-y divide-slate-100 rounded-md border border-slate-200">
          <div v-if="enrolledStudents.length === 0" class="p-4 text-sm text-slate-500">No students enrolled.</div>
          <div v-for="student in enrolledStudents" :key="student.enrollmentId" class="flex items-center justify-between gap-3 p-4 text-sm">
            <div>
              <div class="font-medium text-slate-950">{{ student.name }}</div>
              <div class="text-slate-500">{{ student.studentNumber }} · {{ student.email }}</div>
            </div>
            <button class="rounded-md border border-slate-300 px-3 py-1.5 font-medium text-slate-700 hover:bg-slate-100" @click="removeStudent(student)">
              Remove
            </button>
          </div>
        </div>
      </section>
    </div>
  </AdminLayout>
</template>

<script setup>
import { computed, onMounted, reactive, ref } from 'vue'
import AdminLayout from '../layouts/AdminLayout.vue'
import { useAdminUsersStore } from '../stores/adminUsersStore'
import { useModulesStore } from '../stores/modulesStore'

const store = useModulesStore()
const usersStore = useAdminUsersStore()

const showForm = ref(false)
const editingModule = ref(null)
const enrollmentModule = ref(null)
const selectedStudentId = ref('')
const form = reactive(defaultForm())

const lecturers = computed(() => usersStore.users.filter((user) => user.role === 'LECTURER' && user.active))
const students = computed(() => usersStore.users.filter((user) => user.role === 'STUDENT' && user.active))
const enrolledStudents = computed(() => enrollmentModule.value ? store.moduleStudents[enrollmentModule.value.id] || [] : [])
const availableStudents = computed(() => {
  const enrolledIds = new Set(enrolledStudents.value.map((student) => student.studentId))
  return students.value.filter((student) => !enrolledIds.has(student.profileId))
})

onMounted(async () => {
  await Promise.all([store.fetchModules(), usersStore.fetchUsers()])
})

function defaultForm(module = null) {
  return {
    moduleCode: module?.moduleCode || '',
    moduleName: module?.moduleName || '',
    description: module?.description || '',
    lecturerId: module?.lecturerId || '',
    active: module?.active ?? true,
  }
}

function openCreate() {
  editingModule.value = null
  Object.assign(form, defaultForm())
  showForm.value = true
  store.clearMessages()
}

function openEdit(module) {
  editingModule.value = module
  Object.assign(form, defaultForm(module))
  showForm.value = true
  store.clearMessages()
}

async function saveModule() {
  if (editingModule.value) {
    await store.updateModule(editingModule.value.id, form)
  } else {
    await store.createModule(form)
  }
  showForm.value = false
}

async function toggleModule(module) {
  const action = module.active ? 'deactivate' : 'activate'
  if (!window.confirm(`Are you sure you want to ${action} ${module.moduleCode}?`)) return
  await store.setModuleActive(module.id, !module.active)
}

async function openEnroll(module) {
  enrollmentModule.value = module
  selectedStudentId.value = ''
  await store.fetchModuleStudents(module.id)
}

async function enrollSelectedStudent() {
  await store.enrollStudent({ studentId: selectedStudentId.value, moduleId: enrollmentModule.value.id })
  selectedStudentId.value = ''
}

async function removeStudent(student) {
  if (!window.confirm(`Remove ${student.name} from ${enrollmentModule.value.moduleCode}?`)) return
  await store.removeEnrollment(student.enrollmentId, enrollmentModule.value.id)
}
</script>
