<template>
  <AppLayout>
    <section class="space-y-6">
      <header class="flex flex-col justify-between gap-4 sm:flex-row sm:items-center">
        <div><p class="page-eyebrow">Lecturer workspace</p><h1 class="mt-2 text-3xl font-bold tracking-tight text-slate-950">Absence requests</h1><p class="mt-2 text-sm text-slate-500">Review student explanations for modules assigned to you.</p></div>
        <button class="ui-button ui-button-secondary" :disabled="store.loading || store.saving" @click="refresh">{{ store.loading ? 'Refreshing...' : 'Refresh requests' }}</button>
      </header>
      <AppAlert v-if="store.success" type="success" :message="store.success" />
      <AppAlert v-if="store.error" type="error" :message="store.error" />
      <div class="grid gap-3 sm:grid-cols-3"><article v-for="item in counts" :key="item.label" class="ui-panel flex items-center justify-between p-5"><p class="text-sm text-slate-500">{{ item.label }}</p><p class="text-2xl font-bold">{{ item.count }}</p></article></div>
      <section class="ui-panel p-5">
        <div class="grid gap-3 sm:grid-cols-[1fr_200px]">
          <label class="text-xs font-semibold text-slate-600">Search requests<input v-model.trim="search" type="search" class="ui-input mt-2" placeholder="Student name, number, or module" /></label>
          <label class="text-xs font-semibold text-slate-600">Review status<select v-model="filter" class="ui-input mt-2"><option value="PENDING">Pending review</option><option value="APPROVED">Approved</option><option value="REJECTED">Rejected</option><option value="ALL">All requests</option></select></label>
        </div>
        <p class="mt-4 text-xs leading-5 text-slate-500">Approving a reason records your decision. It does not change the student's attendance to Present.</p>
      </section>
      <p v-if="store.loading" class="ui-panel p-10 text-center text-sm text-slate-500" role="status">Loading absence requests...</p>
      <section v-else-if="filtered.length === 0" class="ui-panel p-10 text-center"><h2 class="font-semibold">No matching requests</h2><p class="mt-2 text-sm text-slate-500">Try another status or search. New submissions appear under Pending review.</p></section>
      <div v-else class="grid items-start gap-4 xl:grid-cols-2">
        <article v-for="item in filtered" :key="item.id" class="ui-panel min-w-0 p-5 sm:p-6">
          <div class="flex flex-wrap items-start justify-between gap-3"><div><p class="text-xs font-semibold text-blue-700">{{ item.moduleCode }}</p><h2 class="mt-1 break-words text-lg font-semibold">{{ item.studentName }}</h2><p class="mt-1 text-xs text-slate-500">{{ item.studentNumber }} &middot; Session #{{ item.sessionId }}</p></div><StatusBadge :value="item.status" :label="statusLabel(item.status)" /></div>
          <p class="mt-3 break-words text-sm text-slate-500">{{ item.moduleName }}</p>
          <div class="mt-4 rounded-xl bg-slate-50 p-4"><h3 class="text-xs font-semibold uppercase tracking-wide text-slate-500">Student's explanation</h3><p class="mt-2 whitespace-pre-wrap break-words text-sm leading-6 text-slate-800">{{ item.reason }}</p></div>
          <p class="mt-3 text-xs text-slate-500">Submitted {{ formatDate(item.submittedAt) }}</p>
          <div v-if="item.status === 'PENDING'" class="mt-5 flex flex-wrap gap-3"><button class="ui-button ui-button-primary" :disabled="store.saving" @click="openReview(item, 'APPROVED')">Approve</button><button class="ui-button ui-button-secondary text-red-700" :disabled="store.saving" @click="openReview(item, 'REJECTED')">Reject</button></div>
          <div v-else class="mt-4 border-t border-slate-100 pt-4 text-sm"><p v-if="item.reviewNote" class="whitespace-pre-wrap break-words text-slate-700">{{ item.reviewNote }}</p><p class="mt-2 text-xs text-slate-500">Reviewed by {{ item.reviewedByName || 'Lecturer' }} &middot; {{ formatDate(item.reviewedAt) }}</p></div>
        </article>
      </div>
    </section>
    <FormModal v-if="selected" :title="decision === 'APPROVED' ? 'Approve absence request' : 'Reject absence request'" :subtitle="`${selected.studentName} - ${selected.moduleCode}`" @close="closeReview">
      <form @submit.prevent="saveReview">
        <AppAlert v-if="store.error" type="error" :message="store.error" />
        <p class="my-4 whitespace-pre-wrap break-words rounded-lg bg-slate-50 p-4 text-sm leading-6 text-slate-700">{{ selected.reason }}</p>
        <label class="block text-sm font-medium text-slate-700" for="review-note">Review note (optional)</label><textarea id="review-note" v-model.trim="note" :disabled="store.saving" maxlength="1000" rows="3" class="ui-input mt-2" placeholder="Explain your decision to the student"></textarea><p class="mt-2 text-right text-xs text-slate-500">{{ note.length }}/1000</p>
        <div class="mt-5 flex flex-wrap justify-end gap-3"><button type="button" class="ui-button ui-button-secondary" :disabled="store.saving" @click="closeReview">Cancel</button><button type="submit" class="ui-button" :class="decision === 'APPROVED' ? 'ui-button-primary' : 'bg-red-700 text-white hover:bg-red-800'" :disabled="store.saving">{{ store.saving ? 'Saving...' : decision === 'APPROVED' ? 'Confirm approval' : 'Confirm rejection' }}</button></div>
      </form>
    </FormModal>
  </AppLayout>
</template>

<script setup>
import { computed, onMounted, ref } from 'vue'
import AppLayout from '../layouts/AppLayout.vue'
import AppAlert from '../components/AppAlert.vue'
import StatusBadge from '../components/StatusBadge.vue'
import FormModal from '../components/FormModal.vue'
import { useJustificationsStore } from '../stores/justificationsStore'
const store = useJustificationsStore()
const search = ref('')
const filter = ref('PENDING')
const selected = ref(null)
const decision = ref('APPROVED')
const note = ref('')
const counts = computed(() => ['PENDING', 'APPROVED', 'REJECTED'].map((status) => ({ label: statusLabel(status), count: store.requests.filter((r) => r.status === status).length })))
const filtered = computed(() => store.requests.filter((item) => (filter.value === 'ALL' || item.status === filter.value) && [item.studentName, item.studentNumber, item.moduleCode, item.moduleName].some((value) => String(value || '').toLowerCase().includes(search.value.toLowerCase()))).sort((a, b) => String(b.submittedAt).localeCompare(String(a.submittedAt))))
function statusLabel(status) { return { PENDING: 'Pending review', APPROVED: 'Approved', REJECTED: 'Rejected' }[status] || status }
function formatDate(value) { if (!value) return 'Not recorded'; const date = new Date(value); return Number.isNaN(date.getTime()) ? value : date.toLocaleString([], { dateStyle: 'medium', timeStyle: 'short' }) }
async function refresh() { try { await store.fetchRequests() } catch { /* Error shown above. */ } }
function openReview(item, status) { store.error = ''; store.success = ''; selected.value = item; decision.value = status; note.value = '' }
function closeReview() { if (!store.saving) selected.value = null }
async function saveReview() { if (!selected.value || store.saving) return; try { await store.review(selected.value.id, decision.value, note.value); selected.value = null } catch { /* Keep note and dialog for retry. */ } }
onMounted(() => { store.success = ''; return refresh() })
</script>
