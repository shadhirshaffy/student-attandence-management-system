<template>
  <div
    :class="toneClasses"
    class="rounded-lg border px-4 py-3 text-sm"
    role="status"
    aria-live="polite"
  >
    <div class="flex items-start justify-between gap-3">
      <p class="font-medium">{{ message }}</p>
      <button
        v-if="dismissible"
        type="button"
        class="rounded px-1 text-lg leading-none opacity-70 hover:opacity-100 focus:outline-none focus:ring-2 focus:ring-current"
        aria-label="Dismiss message"
        @click="$emit('dismiss')"
      >
        x
      </button>
    </div>
  </div>
</template>

<script setup>
import { computed } from 'vue'

const props = defineProps({
  type: {
    type: String,
    default: 'info',
  },
  message: {
    type: String,
    required: true,
  },
  dismissible: {
    type: Boolean,
    default: false,
  },
})

defineEmits(['dismiss'])

const toneClasses = computed(() => ({
  success: 'border-emerald-200 bg-emerald-50 text-emerald-800',
  error: 'border-red-200 bg-red-50 text-red-800',
  warning: 'border-amber-200 bg-amber-50 text-amber-800',
  info: 'border-blue-200 bg-blue-50 text-blue-800',
}[props.type] || 'border-blue-200 bg-blue-50 text-blue-800'))
</script>
