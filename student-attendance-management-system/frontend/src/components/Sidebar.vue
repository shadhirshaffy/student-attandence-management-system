<template>
  <aside class="flex h-full flex-col border-r border-slate-200 bg-white">
    <div class="border-b border-slate-200 px-5 py-5">
      <p class="text-xs font-semibold uppercase tracking-wide text-blue-700">{{ roleLabel }}</p>
      <p class="mt-1 text-sm font-medium text-slate-950">{{ title }}</p>
    </div>
    <nav class="flex-1 space-y-1 overflow-y-auto p-3" aria-label="Primary navigation">
      <RouterLink
        v-for="item in items"
        :key="item.to"
        :to="item.to"
        class="block rounded-md px-3 py-2 text-sm font-medium text-slate-600 transition hover:bg-slate-100 hover:text-slate-950 focus:outline-none focus:ring-2 focus:ring-blue-200"
        active-class="bg-blue-50 text-blue-700"
        @click="$emit('navigate')"
      >
        {{ item.label }}
      </RouterLink>
    </nav>
  </aside>
</template>

<script setup>
defineProps({
  items: {
    type: Array,
    required: true,
  },
  roleLabel: {
    type: String,
    default: 'Workspace',
  },
  title: {
    type: String,
    default: 'Academic dashboard',
  },
})

defineEmits(['navigate'])
</script>
