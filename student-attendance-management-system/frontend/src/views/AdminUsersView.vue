<template>
  <AdminLayout>
    <div class="space-y-6">
      <header class="flex flex-col gap-4 md:flex-row md:items-end md:justify-between">
        <div>
          <p class="text-sm font-semibold uppercase tracking-wide text-blue-700">{{ eyebrow }}</p>
          <h1 class="mt-1 text-3xl font-bold text-slate-950">{{ title }}</h1>
          <p class="mt-2 text-sm text-slate-500">{{ subtitle }}</p>
        </div>
        <button class="rounded-md bg-blue-700 px-4 py-2 text-sm font-semibold text-white hover:bg-blue-800" @click="openCreate">
          Create user
        </button>
      </header>

      <div v-if="store.success" class="rounded-md bg-emerald-50 px-4 py-3 text-sm text-emerald-700">
        {{ store.success }}
      </div>
      <div v-if="store.error" class="rounded-md bg-red-50 px-4 py-3 text-sm text-red-700">
        {{ store.error }}
      </div>

      <section class="rounded-lg border border-slate-200 bg-white shadow-sm">
        <div class="flex flex-col gap-3 border-b border-slate-200 p-4 md:flex-row md:items-center md:justify-between">
          <input
            v-model.trim="search"
            class="w-full rounded-md border border-slate-300 px-3 py-2 text-sm outline-none focus:border-blue-600 focus:ring-2 focus:ring-blue-100 md:max-w-sm"
            placeholder="Search by name, email, or profile number"
          />
          <span class="text-sm text-slate-500">{{ filteredUsers.length }} record{{ filteredUsers.length === 1 ? '' : 's' }}</span>
        </div>

        <div v-if="store.loading" class="p-8 text-center text-sm text-slate-500">Loading users...</div>

        <div v-else-if="filteredUsers.length === 0" class="p-8 text-center">
          <h2 class="text-base font-semibold text-slate-950">No users found</h2>
          <p class="mt-1 text-sm text-slate-500">Create a user or adjust your search.</p>
        </div>

        <div v-else class="overflow-x-auto">
          <table class="min-w-full divide-y divide-slate-200 text-sm">
            <thead class="bg-slate-50 text-left text-xs font-semibold uppercase tracking-wide text-slate-500">
              <tr>
                <th class="px-4 py-3">User</th>
                <th class="px-4 py-3">Role</th>
                <th class="px-4 py-3">Profile</th>
                <th class="px-4 py-3">Status</th>
                <th class="px-4 py-3 text-right">Actions</th>
              </tr>
            </thead>
            <tbody class="divide-y divide-slate-100">
              <tr v-for="user in filteredUsers" :key="user.id">
                <td class="px-4 py-3">
                  <div class="font-medium text-slate-950">{{ user.name }}</div>
                  <div class="text-slate-500">{{ user.email }}</div>
                </td>
                <td class="px-4 py-3">
                  <span class="rounded-full bg-slate-100 px-2.5 py-1 text-xs font-semibold text-slate-700">{{ user.role }}</span>
                </td>
                <td class="px-4 py-3 text-slate-600">{{ profileLabel(user) }}</td>
                <td class="px-4 py-3">
                  <span :class="user.active ? 'bg-emerald-50 text-emerald-700' : 'bg-slate-100 text-slate-600'" class="rounded-full px-2.5 py-1 text-xs font-semibold">
                    {{ user.active ? 'Active' : 'Inactive' }}
                  </span>
                </td>
                <td class="px-4 py-3">
                  <div class="flex justify-end gap-2">
                    <button class="rounded-md border border-slate-300 px-3 py-1.5 font-medium text-slate-700 hover:bg-slate-100" @click="openEdit(user)">
                      Edit
                    </button>
                    <button
                      class="rounded-md border border-slate-300 px-3 py-1.5 font-medium text-slate-700 hover:bg-slate-100"
                      :disabled="store.saving"
                      @click="toggleActive(user)"
                    >
                      {{ user.active ? 'Deactivate' : 'Activate' }}
                    </button>
                  </div>
                </td>
              </tr>
            </tbody>
          </table>
        </div>
      </section>
    </div>

    <UserFormModal
      v-if="showModal"
      :mode="modalMode"
      :user="selectedUser"
      :saving="store.saving"
      @close="closeModal"
      @submit="saveUser"
    />
  </AdminLayout>
</template>

<script setup>
import { computed, onMounted, ref, watch } from 'vue'
import { useRoute } from 'vue-router'
import AdminLayout from '../layouts/AdminLayout.vue'
import UserFormModal from '../components/UserFormModal.vue'
import { useAdminUsersStore } from '../stores/adminUsersStore'

const route = useRoute()
const store = useAdminUsersStore()

const search = ref('')
const showModal = ref(false)
const modalMode = ref('create')
const selectedUser = ref(null)

const roleFilter = computed(() => route.meta.roleFilter)
const title = computed(() => route.meta.title || 'Users')
const eyebrow = computed(() => (roleFilter.value ? roleFilter.value.toLowerCase() : 'admin'))
const subtitle = computed(() => route.meta.subtitle || 'Manage system users and role profiles.')

const filteredUsers = computed(() => {
  const term = search.value.toLowerCase()
  return store.users
    .filter((user) => !roleFilter.value || user.role === roleFilter.value)
    .filter((user) => {
      if (!term) return true
      return [user.name, user.email, user.studentNumber, user.employeeNumber, user.role]
        .filter(Boolean)
        .some((value) => value.toLowerCase().includes(term))
    })
})

onMounted(() => {
  store.fetchUsers()
})

watch(
  () => route.fullPath,
  () => {
    search.value = ''
    store.clearMessages()
  },
)

function profileLabel(user) {
  if (user.role === 'STUDENT') return user.studentNumber || 'Missing student number'
  if (user.role === 'LECTURER') return user.employeeNumber || 'Missing employee number'
  return 'Admin profile'
}

function openCreate() {
  modalMode.value = 'create'
  selectedUser.value = roleFilter.value ? { role: roleFilter.value, active: true } : null
  showModal.value = true
  store.clearMessages()
}

function openEdit(user) {
  modalMode.value = 'edit'
  selectedUser.value = user
  showModal.value = true
  store.clearMessages()
}

function closeModal() {
  showModal.value = false
  selectedUser.value = null
}

async function saveUser(payload) {
  if (modalMode.value === 'create') {
    await store.createUser(payload)
  } else {
    await store.updateUser(selectedUser.value.id, payload)
  }
  closeModal()
}

async function toggleActive(user) {
  const nextState = !user.active
  const action = nextState ? 'activate' : 'deactivate'
  if (!window.confirm(`Are you sure you want to ${action} ${user.name}?`)) {
    return
  }
  await store.setActive(user.id, nextState)
}
</script>
