<template>
  <span :class="classes" class="inline-flex items-center rounded-full px-2.5 py-1 text-xs font-semibold">
    {{ label || value }}
  </span>
</template>

<script setup>
import { computed } from 'vue'

const props = defineProps({
  value: {
    type: [String, Boolean],
    required: true,
  },
  label: {
    type: String,
    default: '',
  },
})

const classes = computed(() => {
  const key = String(props.value).toUpperCase()
  return {
    ACTIVE: 'bg-emerald-50 text-emerald-700',
    PRESENT: 'bg-emerald-50 text-emerald-700',
    HEALTHY: 'bg-emerald-50 text-emerald-700',
    TRUE: 'bg-emerald-50 text-emerald-700',
    SCHEDULED: 'bg-slate-100 text-slate-700',
    INACTIVE: 'bg-slate-100 text-slate-600',
    FALSE: 'bg-slate-100 text-slate-600',
    LOCKED: 'bg-amber-50 text-amber-700',
    WARNING: 'bg-amber-50 text-amber-700',
    COMPLETED: 'bg-blue-50 text-blue-700',
    ADMIN: 'bg-blue-50 text-blue-700',
    LECTURER: 'bg-indigo-50 text-indigo-700',
    STUDENT: 'bg-cyan-50 text-cyan-700',
    ABSENT: 'bg-red-50 text-red-700',
    LOW: 'bg-red-50 text-red-700',
  }[key] || 'bg-slate-100 text-slate-700'
})
</script>
