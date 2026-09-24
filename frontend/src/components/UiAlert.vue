<script setup>
defineProps({ type: { type: String, default: 'error' }, message: String })
</script>
<template><div v-if="message" :class="type === 'success' ? 'border-emerald-200 bg-emerald-50 text-emerald-800' : 'border-rose-200 bg-rose-50 text-rose-800'" class="rounded-xl border px-3 py-2 text-sm">{{ message }}</div></template>
