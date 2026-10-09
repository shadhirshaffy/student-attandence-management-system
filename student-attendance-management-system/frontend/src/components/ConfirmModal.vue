<template>
  <div class="fixed inset-0 z-50 flex items-center justify-center bg-slate-950/40 px-4 py-6" @keydown.esc="$emit('cancel')">
    <section class="w-full max-w-md rounded-lg bg-white p-6 shadow-xl" role="dialog" aria-modal="true" :aria-labelledby="titleId">
      <h2 :id="titleId" class="text-lg font-semibold text-slate-950">{{ title }}</h2>
      <p class="mt-2 text-sm text-slate-600">{{ message }}</p>
      <div class="mt-6 flex justify-end gap-3">
        <button
          ref="cancelButton"
          type="button"
          class="rounded-md border border-slate-300 px-4 py-2 text-sm font-semibold text-slate-700 hover:bg-slate-100 focus:outline-none focus:ring-2 focus:ring-blue-200"
          @click="$emit('cancel')"
        >
          {{ cancelLabel }}
        </button>
        <button
          type="button"
          :class="danger ? 'bg-red-700 hover:bg-red-800' : 'bg-blue-700 hover:bg-blue-800'"
          class="rounded-md px-4 py-2 text-sm font-semibold text-white focus:outline-none focus:ring-2 focus:ring-blue-200"
          @click="$emit('confirm')"
        >
          {{ confirmLabel }}
        </button>
      </div>
    </section>
  </div>
</template>

<script setup>
import { onMounted, ref } from 'vue'

defineProps({
  title: {
    type: String,
    default: 'Confirm action',
  },
  message: {
    type: String,
    required: true,
  },
  confirmLabel: {
    type: String,
    default: 'Confirm',
  },
  cancelLabel: {
    type: String,
    default: 'Cancel',
  },
  danger: {
    type: Boolean,
    default: false,
  },
})

defineEmits(['confirm', 'cancel'])

const titleId = `confirm-modal-${Math.random().toString(36).slice(2)}`
const cancelButton = ref(null)

onMounted(() => {
  cancelButton.value?.focus()
})
</script>
