<template>
  <div class="flex items-center justify-center gap-3 py-8 text-sm text-slate-500" role="status">
    <span class="h-5 w-5 animate-spin rounded-full border-2 border-slate-200 border-t-blue-700"></span>
    <span>{{ label }}</span>
  </div>
</template>

<script setup>
defineProps({
  label: {
    type: String,
    default: 'Loading...',
  },
})
</script>
