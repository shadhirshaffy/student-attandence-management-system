<template>
  <section class="ui-panel overflow-hidden">
    <div v-if="$slots.header" class="border-b border-slate-200 px-4 py-3">
      <slot name="header" />
    </div>
    <LoadingSpinner v-if="loading" :label="loadingLabel" />
    <EmptyState v-else-if="empty" :title="emptyTitle" :message="emptyMessage" />
    <div v-else class="overflow-x-auto" role="region" aria-label="Data table" tabindex="0">
      <table class="w-full min-w-[760px] divide-y divide-slate-200 text-sm">
        <thead class="bg-slate-50 text-left text-xs font-semibold uppercase tracking-wide text-slate-500">
          <tr>
            <th v-for="column in columns" :key="column.key" :class="column.class" class="px-4 py-3">
              {{ column.label }}
            </th>
          </tr>
        </thead>
        <tbody class="divide-y divide-slate-100">
          <slot />
        </tbody>
      </table>
    </div>
  </section>
</template>

<script setup>
import EmptyState from './EmptyState.vue'
import LoadingSpinner from './LoadingSpinner.vue'

defineProps({
  columns: {
    type: Array,
    required: true,
  },
  loading: {
    type: Boolean,
    default: false,
  },
  loadingLabel: {
    type: String,
    default: 'Loading...',
  },
  empty: {
    type: Boolean,
    default: false,
  },
  emptyTitle: {
    type: String,
    default: 'No records found',
  },
  emptyMessage: {
    type: String,
    default: '',
  },
})
</script>
