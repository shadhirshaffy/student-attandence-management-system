
<template>
  <AppLayout>
    <section class="flex min-h-[65vh] items-center justify-center px-4 py-12">
      <div class="mx-auto max-w-4xl text-center">

        <p class="text-sm font-semibold uppercase tracking-wide text-blue-700">
          Welcome to Student Attendance Management
        </p>

        <h1
          class="mt-5 text-4xl font-bold tracking-tight text-slate-950 sm:text-5xl"
        >
          Student Attendance Management System
        </h1>

        <p class="mx-auto mt-6 max-w-2xl text-lg leading-8 text-slate-600">
          A simple and efficient platform for managing student attendance,
          monitoring attendance records, and keeping academic information
          organized in one place.
        </p>

        <RouterLink
          to="/login"
          class="mt-8 inline-flex rounded-md bg-blue-700 px-6 py-3 text-base font-semibold text-white shadow-sm transition hover:bg-blue-800"
        >
          Sign in
        </RouterLink>

      </div>
    </section>
  </AppLayout>
</template>

<script setup>
import AppLayout from '../layouts/AppLayout.vue'
</script>
