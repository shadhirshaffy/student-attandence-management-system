<template>
  <div class="fixed inset-0 z-50 flex items-center justify-center bg-slate-950/40 px-4 py-6" @keydown.esc="$emit('close')">
    <section class="max-h-[90vh] w-full max-w-3xl overflow-y-auto rounded-lg bg-white p-6 shadow-xl" role="dialog" aria-modal="true" :aria-labelledby="titleId">
      <div class="flex items-start justify-between gap-4">
        <div>
          <h2 :id="titleId" class="text-lg font-semibold text-slate-950">{{ title }}</h2>
          <p v-if="subtitle" class="mt-1 text-sm text-slate-500">{{ subtitle }}</p>
        </div>
        <button
          type="button"
          class="rounded-md px-2 py-1 text-sm text-slate-500 hover:bg-slate-100 focus:outline-none focus:ring-2 focus:ring-blue-200"
          aria-label="Close modal"
          @click="$emit('close')"
        >
          Close
        </button>
      </div>
      <div class="mt-5">
        <slot />
      </div>
    </section>
  </div>
</template>

<script setup>
defineProps({
  title: {
    type: String,
    required: true,
  },
  subtitle: {
    type: String,
    default: '',
  },
})

defineEmits(['close'])

const titleId = `form-modal-${Math.random().toString(36).slice(2)}`
</script>
