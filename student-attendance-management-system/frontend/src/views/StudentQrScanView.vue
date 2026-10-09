<template>
  <AppLayout>
    <section class="mx-auto max-w-3xl space-y-6">
      <header>
        <p class="text-sm font-semibold uppercase tracking-wide text-blue-700">Student</p>
        <h1 class="mt-1 text-3xl font-bold text-slate-950">Scan attendance QR</h1>
        <p class="mt-2 text-sm text-slate-500">Scan the QR code shown by your lecturer to mark yourself present.</p>
      </header>

      <div v-if="store.error" class="rounded-md bg-red-50 px-4 py-3 text-sm text-red-700">{{ store.error }}</div>
      <div v-if="store.success" class="rounded-md bg-emerald-50 px-4 py-3 text-sm text-emerald-700">{{ store.success }}</div>
      <div v-if="cameraError" class="rounded-md bg-amber-50 px-4 py-3 text-sm text-amber-700">{{ cameraError }}</div>

      <section class="rounded-lg border border-slate-200 bg-white p-6 shadow-sm">
        <div id="qr-reader" class="overflow-hidden rounded-lg border border-slate-200"></div>

        <form class="mt-5 flex flex-col gap-3 sm:flex-row" @submit.prevent="submitToken">
          <input
            v-model.trim="token"
            placeholder="Attendance token"
            class="flex-1 rounded-md border border-slate-300 px-3 py-2 text-sm"
          />
          <button class="rounded-md bg-blue-700 px-4 py-2 text-sm font-semibold text-white hover:bg-blue-800" :disabled="store.saving || !token">
            {{ store.saving ? 'Submitting...' : 'Submit' }}
          </button>
        </form>
      </section>

      <section v-if="result" class="rounded-lg border border-slate-200 bg-white p-5 shadow-sm">
        <p class="text-sm font-semibold uppercase tracking-wide text-emerald-700">Recorded</p>
        <h2 class="mt-1 text-xl font-bold text-slate-950">{{ result.moduleCode }} - {{ result.moduleName }}</h2>
        <p class="mt-2 text-sm text-slate-500">{{ result.status }} by {{ result.markingMethod }} at {{ result.markedAt }}</p>
      </section>
    </section>
  </AppLayout>
</template>

<script setup>
import { onBeforeUnmount, onMounted, ref } from 'vue'
import { useRoute } from 'vue-router'
import AppLayout from '../layouts/AppLayout.vue'
import { useAttendanceStore } from '../stores/attendanceStore'

const route = useRoute()
const store = useAttendanceStore()
const token = ref('')
const result = ref(null)
const cameraError = ref('')
let scanner = null
let scannerRunning = false
let submitted = false

onMounted(async () => {
  store.clearMessages()
  token.value = normalizeToken(route.query.token || '')
  if (token.value) {
    await submitToken()
    return
  }
  startScanner()
})

onBeforeUnmount(async () => {
  if (scanner && scannerRunning) {
    await scanner.stop()
    scannerRunning = false
  }
})

async function startScanner() {
  if (!navigator.mediaDevices?.getUserMedia) {
    cameraError.value = 'Camera scanning is not supported in this browser.'
    return
  }
  try {
    const { Html5Qrcode } = await import('html5-qrcode')
    scanner = new Html5Qrcode('qr-reader')
    await scanner.start(
      { facingMode: 'environment' },
      { fps: 10, qrbox: { width: 260, height: 260 } },
      async (decodedText) => {
        if (submitted) return
        submitted = true
        token.value = normalizeToken(decodedText)
        if (scanner && scannerRunning) {
          await scanner.stop()
          scannerRunning = false
        }
        await submitToken()
      },
    )
    scannerRunning = true
  } catch (error) {
    cameraError.value = 'Camera permission is required, or no usable camera was found.'
  }
}

async function submitToken() {
  const cleanToken = normalizeToken(token.value)
  if (!cleanToken) {
    store.error = 'Invalid QR code.'
    return
  }
  token.value = cleanToken
  try {
    result.value = await store.submitQrAttendance(cleanToken)
  } catch {
    // The store presents rejection details, including duplicate/expired codes.
    result.value = null
  }
}

function normalizeToken(value) {
  const text = String(value || '').trim()
  if (!text) return ''
  try {
    const url = new URL(text)
    return url.searchParams.get('token') || text
  } catch {
    return text
  }
}
</script>
