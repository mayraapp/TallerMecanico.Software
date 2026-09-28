<script setup>
import { nextTick, onMounted, onUnmounted, ref } from 'vue'
import { AlertTriangle, X } from 'lucide-vue-next'

const props = defineProps({ title: { type: String, required: true }, message: { type: String, required: true }, confirmLabel: { type: String, default: 'Continuar' } })
const emit = defineEmits(['confirm', 'cancel'])
const cancelButton = ref(null)
const closeOnEscape = (event) => { if (event.key === 'Escape') emit('cancel') }
onMounted(async () => { window.addEventListener('keydown', closeOnEscape); await nextTick(); cancelButton.value?.focus() })
onUnmounted(() => window.removeEventListener('keydown', closeOnEscape))
</script>

<template>
  <div class="fixed inset-0 z-90 flex items-center justify-center bg-slate-950/55 p-4" role="presentation">
    <section class="w-full max-w-md rounded-3xl bg-white p-6 shadow-2xl" role="dialog" aria-modal="true" aria-labelledby="confirmation-title" aria-describedby="confirmation-message">
      <div class="flex items-start justify-between gap-4"><div class="flex items-center gap-3"><div class="rounded-2xl bg-amber-100 p-3 text-amber-700"><AlertTriangle :size="23"/></div><div><h2 id="confirmation-title" class="text-lg font-black text-slate-950">{{ props.title }}</h2><p id="confirmation-message" class="mt-1 text-sm leading-6 text-slate-600">{{ props.message }}</p></div></div><button type="button" class="rounded-xl p-2 text-slate-400 transition hover:bg-slate-100 hover:text-slate-700" aria-label="Cerrar confirmación" @click="emit('cancel')"><X :size="19"/></button></div>
      <div class="mt-7 flex flex-col-reverse gap-3 sm:flex-row sm:justify-end"><button ref="cancelButton" type="button" class="btn-ghost" @click="emit('cancel')">Cancelar</button><button type="button" class="btn bg-amber-500 text-slate-950 hover:bg-amber-400 focus:ring-amber-200" @click="emit('confirm')">{{ props.confirmLabel }}</button></div>
    </section>
  </div>
</template>
