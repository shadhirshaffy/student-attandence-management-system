<template>
  <AdminLayout>
    <div class="space-y-6">
      <header class="flex flex-col gap-4 md:flex-row md:items-end md:justify-between">
        <div>
          <p class="page-eyebrow">Academic management</p>
          <h1 class="mt-2 text-3xl font-bold tracking-tight text-slate-950">Module management</h1>
          <p class="mt-2 text-sm text-slate-500">Create modules, assign lecturers, and manage enrollments.</p>
        </div>
        <button class="ui-button ui-button-primary" @click="openCreate">
          Create module
        </button>
      </header>

      <AppAlert v-if="store.success" type="success" :message="store.success" />
      <AppAlert v-if="store.error" type="error" :message="store.error" />

      <div class="grid gap-3 sm:grid-cols-3">
        <article v-for="item in moduleCounts" :key="item.label" class="ui-panel flex items-center justify-between px-5 py-4"><span class="text-sm font-medium text-slate-500">{{ item.label }}</span><span class="text-2xl font-bold text-slate-900">{{ store.loading ? '...' : item.value }}</span></article>
      </div>
      <section class="ui-panel overflow-hidden">
        <div class="border-b border-slate-200 p-4 sm:p-5">
          <div class="grid gap-3 md:grid-cols-[minmax(0,1fr)_160px_180px]">
            <label class="text-xs font-semibold text-slate-600">Search modules<input v-model.trim="search" type="search" placeholder="Search code, name, or lecturer..." class="ui-input mt-2" /></label>
            <label class="text-xs font-semibold text-slate-600">Status<select v-model="statusFilter" class="ui-input mt-2"><option value="all">All statuses</option><option value="active">Active</option><option value="inactive">Inactive</option></select></label>
            <label class="text-xs font-semibold text-slate-600">Sort by<select v-model="sortBy" class="ui-input mt-2"><option value="code">Module code A-Z</option><option value="name">Module name A-Z</option><option value="newest">Newest first</option></select></label>
          </div>
          <div class="mt-4 flex flex-wrap items-center justify-between gap-2"><p class="text-xs text-slate-500" role="status">Showing {{ filteredModules.length }} of {{ store.modules.length }} modules</p><button v-if="search || statusFilter !== 'all' || sortBy !== 'code'" class="text-xs font-semibold text-blue-700 hover:underline" @click="resetFilters">Reset filters</button></div>
        </div>
        <div v-if="store.loading" class="p-8 text-center text-sm text-slate-500">Loading modules...</div>
        <div v-else-if="store.modules.length === 0" class="p-8 text-center">
          <h2 class="font-semibold text-slate-950">No modules yet</h2>
          <p class="mt-1 text-sm text-slate-500">Create the first module to start enrollment.</p>
        </div>
        <div v-else-if="filteredModules.length === 0" class="px-6 py-14 text-center"><h2 class="font-semibold text-slate-950">No matching modules</h2><p class="mt-2 text-sm text-slate-500">Try another search or reset your filters.</p><button class="ui-button ui-button-secondary mt-5" @click="resetFilters">Reset filters</button></div>
        <div v-else class="overflow-x-auto" role="region" aria-label="Module catalogue" tabindex="0">
          <table class="min-w-full divide-y divide-slate-200 text-sm">
            <thead class="bg-slate-50 text-left text-xs font-semibold uppercase tracking-wide text-slate-500">
              <tr>
                <th class="px-5 py-5 align-top">Module</th>
                <th class="px-5 py-5 align-top">Lecturer</th>
                <th class="px-5 py-5 align-top">Status</th>
                <th class="px-4 py-3 text-right">Actions</th>
              </tr>
            </thead>
            <tbody class="divide-y divide-slate-100">
              <tr v-for="module in filteredModules" :key="module.id" class="transition-colors hover:bg-blue-50/40">
                <td class="px-5 py-5 align-top">
                  <div class="font-medium text-slate-950">{{ module.moduleCode }} - {{ module.moduleName }}</div>
                  <div class="mt-1 max-w-sm text-xs leading-5 text-slate-500">{{ module.description || 'No description' }}</div>
                </td>
                <td class="px-4 py-3 text-slate-600">{{ module.lecturerName || 'Unassigned' }}</td>
                <td class="px-5 py-5 align-top">
                  <span :class="module.active ? 'bg-emerald-50 text-emerald-700' : 'bg-slate-100 text-slate-600'" class="rounded-full px-2.5 py-1 text-xs font-semibold">
                    {{ module.active ? 'Active' : 'Inactive' }}
                  </span>
                </td>
                <td class="px-5 py-5 align-top">
                  <div class="flex min-w-48 flex-wrap justify-end gap-2">
                    <button class="rounded-md border border-slate-300 px-3 py-1.5 font-medium text-slate-700 hover:bg-slate-100" @click="openEdit(module)">Edit</button>
                    <button class="rounded-md border border-slate-300 px-3 py-1.5 font-medium text-slate-700 hover:bg-slate-100" @click="toggleModule(module)">
                      {{ module.active ? 'Deactivate' : 'Activate' }}
                    </button>
                    <button class="rounded-md border border-slate-300 px-3 py-1.5 font-medium text-slate-700 hover:bg-slate-100" @click="openEnroll(module)">Enrollments</button>
                    <button class="rounded-md border border-red-300 px-3 py-1.5 font-medium text-red-700 hover:bg-red-50" :disabled="store.saving" @click="store.clearMessages(); pendingDelete = module">Delete</button>
                  </div>
                </td>
              </tr>
            </tbody>
          </table>
        </div>
      </section>
    </div>

    <FormModal
      v-if="showForm"
      :title="editingModule ? 'Edit module' : 'Create module'"
      subtitle="Maintain the module catalogue and lecturer assignment."
      @close="showForm = false"
    >
      <form @submit.prevent="saveModule">
        <AppAlert v-if="store.error" type="error" :message="store.error" />
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
    </FormModal>

    <FormModal
      v-if="enrollmentModule"
      :title="`Enrollments: ${enrollmentModule.moduleCode}`"
      :subtitle="enrollmentModule.moduleName"
      @close="enrollmentModule = null"
    >
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
    </FormModal>

    <ConfirmModal
      v-if="pendingDelete"
      title="Delete module"
      :message="`Permanently delete ${pendingDelete.moduleCode} - ${pendingDelete.moduleName}? This cannot be undone. Modules with enrollments or sessions cannot be deleted.`"
      :confirm-label="store.saving ? 'Deleting...' : 'Delete module'"
      :busy="store.saving"
      danger
      @cancel="pendingDelete = null"
      @confirm="confirmDeleteModule"
    />

    <ConfirmModal
      v-if="pendingModule"
      :title="`${pendingModule.active ? 'Deactivate' : 'Activate'} module`"
      :message="`Are you sure you want to ${pendingModule.active ? 'deactivate' : 'activate'} ${pendingModule.moduleCode}?`"
      :confirm-label="pendingModule.active ? 'Deactivate' : 'Activate'"
      :danger="pendingModule.active"
      @cancel="pendingModule = null"
      @confirm="confirmToggleModule"
    />

    <ConfirmModal
      v-if="pendingEnrollment"
      title="Remove enrollment"
      :message="`Remove ${pendingEnrollment.name} from ${enrollmentModule.moduleCode}?`"
      confirm-label="Remove"
      danger
      @cancel="pendingEnrollment = null"
      @confirm="confirmRemoveStudent"
    />
  </AdminLayout>
</template>

<script setup>
import { computed, onMounted, reactive, ref } from 'vue'
import AppAlert from '../components/AppAlert.vue'
import ConfirmModal from '../components/ConfirmModal.vue'
import FormModal from '../components/FormModal.vue'
import AdminLayout from '../layouts/AdminLayout.vue'
import { useAdminUsersStore } from '../stores/adminUsersStore'
import { useModulesStore } from '../stores/modulesStore'

const store = useModulesStore()
const usersStore = useAdminUsersStore()

const search = ref('')
const statusFilter = ref('all')
const sortBy = ref('code')
const moduleCounts = computed(() => [
  { label: 'Total modules', value: store.modules.length },
  { label: 'Active modules', value: store.modules.filter((m) => m.active).length },
  { label: 'Inactive modules', value: store.modules.filter((m) => !m.active).length },
])
const filteredModules = computed(() => {
  const term = search.value.toLocaleLowerCase()
  return store.modules.filter((module) => {
    const matchesStatus = statusFilter.value === 'all' || (statusFilter.value === 'active' ? module.active : !module.active)
    return matchesStatus && [module.moduleCode, module.moduleName, module.lecturerName].some((value) => String(value || '').toLocaleLowerCase().includes(term))
  }).sort((a, b) => {
    if (sortBy.value === 'newest') return b.id - a.id
    const key = sortBy.value === 'name' ? 'moduleName' : 'moduleCode'
    return String(a[key] || '').localeCompare(String(b[key] || ''), undefined, { numeric: true, sensitivity: 'base' })
  })
})
function resetFilters() {
  search.value = ''
  statusFilter.value = 'all'
  sortBy.value = 'code'
}

const showForm = ref(false)
const editingModule = ref(null)
const enrollmentModule = ref(null)
const pendingModule = ref(null)
const pendingDelete = ref(null)
const pendingEnrollment = ref(null)
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
  await Promise.allSettled([store.fetchModules(), usersStore.fetchUsers()])
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
  if (store.saving) return
  try {
  if (editingModule.value) {
    await store.updateModule(editingModule.value.id, form)
  } else {
    await store.createModule(form)
  }
  showForm.value = false
  } catch {
    // Keep the form open to display the server error.
  }
}

async function confirmDeleteModule() {
  if (!pendingDelete.value || store.saving) return
  try {
    await store.deleteModule(pendingDelete.value.id)
  } catch {
    // The store displays the API error on the page after closing the dialog.
  } finally {
    pendingDelete.value = null
  }
}

async function toggleModule(module) {
  pendingModule.value = module
}

async function confirmToggleModule() {
  if (!pendingModule.value) return
  await store.setModuleActive(pendingModule.value.id, !pendingModule.value.active)
  pendingModule.value = null
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
  pendingEnrollment.value = student
}

async function confirmRemoveStudent() {
  if (!pendingEnrollment.value) return
  await store.removeEnrollment(pendingEnrollment.value.enrollmentId, enrollmentModule.value.id)
  pendingEnrollment.value = null
}
</script>
