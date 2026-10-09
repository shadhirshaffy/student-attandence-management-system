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

      <section v-else-if="qr" class="min-h-[70vh] rounded-lg border border-slate-200 bg-white p-6 text-center shadow-sm">
        <p class="text-sm font-semibold uppercase tracking-wide text-blue-700">{{ qr.moduleCode }}</p>
        <h1 class="mt-2 text-4xl font-bold text-slate-950">{{ qr.moduleName }}</h1>
        <p class="mt-2 text-lg text-slate-600">Room: {{ qr.room || 'Not set' }}</p>

        <div class="mt-6 flex justify-center">
          <img v-if="qrImage" :src="qrImage" alt="Attendance QR code" class="h-80 w-80 rounded-lg border border-slate-200 bg-white p-3 shadow-sm" />
        </div>

        <div class="mt-6 flex flex-wrap justify-center gap-3">
          <span class="rounded-full bg-emerald-50 px-3 py-1 text-sm font-semibold text-emerald-700">{{ qr.status }}</span>
          <span class="rounded-full bg-blue-50 px-3 py-1 text-sm font-semibold text-blue-700">Expires: {{ qr.tokenExpiry }}</span>
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
  const data = await store.fetchSessionQr(route.params.id)
  const { default: QRCode } = await import('qrcode')
  qrImage.value = await QRCode.toDataURL(data.attendanceUrl, {
    width: 720,
    margin: 2,
    errorCorrectionLevel: 'M',
  })
})
</script>
