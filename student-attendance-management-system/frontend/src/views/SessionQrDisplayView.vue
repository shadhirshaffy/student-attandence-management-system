<template>
  <AppLayout>
    <section class="space-y-6">
      <RouterLink :to="`/lecturer/sessions/${route.params.id}`" class="text-sm font-semibold text-blue-700 hover:text-blue-800">
        Back to session
      </RouterLink>

      <div v-if="store.error" class="rounded-md bg-red-50 px-4 py-3 text-sm text-red-700">{{ store.error }}</div>

      <section v-if="store.loading" class="rounded-lg border border-slate-200 bg-white p-8 text-center text-sm text-slate-500 shadow-sm">
        Loading QR...
      </section>

      <section v-else-if="qr" class="ui-panel mx-auto max-w-3xl p-5 text-center sm:p-10">
        <p class="page-eyebrow">{{ qr.moduleCode }}</p>
        <h1 class="mt-3 break-words text-2xl font-bold tracking-tight text-slate-950 sm:text-4xl">{{ qr.moduleName }}</h1>
        <p class="mt-2 text-lg text-slate-600">Room: {{ qr.room || 'Not set' }}</p>

        <p class="mt-5 text-sm text-slate-500">Students: open Scan QR, then point your camera at this code.</p>
        <div class="mt-6 flex justify-center">
          <img v-if="qrImage" :src="qrImage" alt="Attendance QR code" class="aspect-square w-full max-w-80 rounded-2xl border border-slate-200 bg-white p-3 shadow-sm" />
        </div>

        <div class="mt-6 flex flex-wrap justify-center gap-3">
          <span class="rounded-full bg-emerald-50 px-3 py-1 text-sm font-semibold text-emerald-700">{{ qr.status }}</span>
          <span class="rounded-full bg-blue-50 px-3 py-1 text-sm font-semibold text-blue-700">Expires: {{ formatExpiry(qr.tokenExpiry) }}</span>
        </div>
      </section>
    </section>
  </AppLayout>
</template>

<script setup>
import { computed, onMounted, ref } from 'vue'
import { useRoute } from 'vue-router'
import AppLayout from '../layouts/AppLayout.vue'
import { useSessionsStore } from '../stores/sessionsStore'

const route = useRoute()
const store = useSessionsStore()
const qrImage = ref('')

const qr = computed(() => store.sessionQr)

onMounted(async () => {
  store.clearMessages()
  try {
  const data = await store.fetchSessionQr(route.params.id)
  const { default: QRCode } = await import('qrcode')
  qrImage.value = await QRCode.toDataURL(data.attendanceUrl, {
    width: 720,
    margin: 2,
    errorCorrectionLevel: 'M',
  })
  } catch {
    if (!store.error) store.error = 'Could not display the QR code. Return to the session and try again.'
  }
})

function formatExpiry(value) {
  const date = new Date(value)
  return Number.isNaN(date.getTime()) ? value : date.toLocaleString([], { dateStyle: 'medium', timeStyle: 'short' })
}
</script>
