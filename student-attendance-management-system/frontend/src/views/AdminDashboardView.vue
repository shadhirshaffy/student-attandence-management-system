<template>
  <AdminLayout>
    <section class="space-y-6">
      <div>
        <p class="text-sm font-semibold uppercase tracking-wide text-blue-700">Admin</p>
        <h1 class="mt-1 text-3xl font-bold text-slate-950">Admin dashboard</h1>
        <p class="mt-2 max-w-2xl text-slate-600">
          Manage user accounts, role profiles, modules, lecturers, and enrollments for the Student Attendance Management System.
        </p>
      </div>

      <div class="grid gap-4 md:grid-cols-3">
        <div v-for="card in cards" :key="card.label" class="rounded-lg border border-slate-200 bg-white p-5 shadow-sm">
          <p class="text-sm font-medium text-slate-500">{{ card.label }}</p>
          <p class="mt-2 text-3xl font-bold text-slate-950">{{ card.value }}</p>
        </div>
      </div>

      <RouterLink
        to="/admin/modules"
        class="inline-flex rounded-md bg-blue-700 px-4 py-2 text-sm font-semibold text-white hover:bg-blue-800"
      >
        Manage modules
      </RouterLink>
    </section>
  </AdminLayout>
</template>

<script setup>
import { computed, onMounted } from 'vue'
import AdminLayout from '../layouts/AdminLayout.vue'
import { useAdminUsersStore } from '../stores/adminUsersStore'

const store = useAdminUsersStore()

const cards = computed(() => [
  { label: 'Total users', value: store.users.length },
  { label: 'Students', value: store.students.length },
  { label: 'Lecturers', value: store.lecturers.length },
])

onMounted(() => {
  store.fetchUsers()
})
</script>
