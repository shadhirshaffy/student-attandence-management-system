<template>
  <div class="px-6 py-10 text-center">
    <div class="mx-auto flex h-12 w-12 items-center justify-center rounded-full bg-slate-100 text-lg font-semibold text-slate-500">
      {{ symbol }}
    </div>
    <h2 class="mt-4 text-base font-semibold text-slate-950">{{ title }}</h2>
    <p v-if="message" class="mx-auto mt-1 max-w-md text-sm text-slate-500">{{ message }}</p>
    <div v-if="$slots.default" class="mt-5">
      <slot />
    </div>
  </div>
</template>

<script setup>
defineProps({
  title: {
    type: String,
    required: true,
  },
  message: {
    type: String,
    default: '',
  },
  symbol: {
    type: String,
    default: '-',
  },
})
</script>
