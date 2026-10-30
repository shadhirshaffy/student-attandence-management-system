<template>
  <AppLayout>
    <section class="mx-auto max-w-3xl space-y-6">
      <header>
        <p class="page-eyebrow">Student</p>
        <h1 class="mt-2 text-3xl font-bold tracking-tight text-slate-950">Scan attendance QR</h1>
        <p class="mt-2 text-sm text-slate-500">Scan the QR code shown by your lecturer to mark yourself present.</p>
      </header>

      <div role="alert" v-if="store.error" class="rounded-md bg-red-50 px-4 py-3 text-sm text-red-700">{{ store.error }}</div>
      <div role="status" v-if="store.success" class="rounded-md bg-emerald-50 px-4 py-3 text-sm text-emerald-700">{{ store.success }}</div>
      <div role="status" v-if="cameraError" class="rounded-md bg-amber-50 px-4 py-3 text-sm text-amber-700">{{ cameraError }}</div>

      <section class="ui-panel p-5 sm:p-7">
        <div class="mb-5 flex items-start gap-3"><span class="flex h-9 w-9 shrink-0 items-center justify-center rounded-lg bg-blue-50 font-bold text-blue-700" aria-hidden="true">1</span><div><h2 class="font-semibold text-slate-900">Scan your lecturer's QR code</h2><p class="mt-1 text-sm leading-6 text-slate-500">Allow camera access and keep the code inside the frame.</p></div></div>
        <div id="qr-reader" class="overflow-hidden rounded-lg border border-slate-200"></div>

        <div class="mb-4 mt-7 border-t border-slate-100 pt-5"><h2 class="text-sm font-semibold text-slate-900">Have an attendance token?</h2><p class="mt-1 text-xs leading-5 text-slate-500">If your camera is unavailable, paste the token or attendance link provided by your lecturer.</p></div>
        <form class="flex flex-col gap-3 sm:flex-row" @submit.prevent="submitToken">
          <input
            v-model.trim="token"
            aria-label="Attendance token"
            placeholder="Paste attendance token or link"
            class="min-w-0 flex-1 rounded-lg border border-slate-300 px-3 py-3 text-sm"
          />
          <button class="ui-button ui-button-primary" :disabled="store.saving || !token">
            {{ store.saving ? 'Submitting...' : 'Submit' }}
          </button>
        </form>
      </section>

      <section v-if="result" class="rounded-2xl border border-emerald-200 bg-emerald-50 p-6" role="status">
        <p class="text-sm font-semibold text-emerald-700">Attendance recorded successfully</p>
        <h2 class="mt-1 text-xl font-bold text-slate-950">{{ result.moduleCode }} - {{ result.moduleName }}</h2>
        <p class="mt-2 break-words text-sm text-slate-600">{{ result.status }} by {{ result.markingMethod }} at {{ result.markedAt }}</p>
        <RouterLink to="/student/attendance-history" class="mt-4 inline-flex text-sm font-semibold text-emerald-800 hover:underline">View attendance history &rarr;</RouterLink>
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
  if (store.saving) return
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
