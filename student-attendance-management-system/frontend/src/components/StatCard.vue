<template>
  <article class="rounded-lg border border-slate-200 bg-white p-5 shadow-sm">
    <p class="text-sm font-medium text-slate-500">{{ label }}</p>
    <p class="mt-2 text-3xl font-bold tracking-tight text-slate-950">{{ value }}</p>
    <p v-if="hint" class="mt-2 text-xs text-slate-500">{{ hint }}</p>
    <slot />
  </article>
</template>

<script setup>
defineProps({
  label: {
    type: String,
    required: true,
  },
  value: {
    type: [String, Number],
    required: true,
  },
  hint: {
    type: String,
    default: '',
  },
})
</script>
