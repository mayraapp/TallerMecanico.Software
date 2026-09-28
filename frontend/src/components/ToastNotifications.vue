<script setup>
import { computed } from 'vue'
import { CheckCircle2, CircleAlert, Info, ShieldAlert, X } from 'lucide-vue-next'
import { notificacionFacade } from '../facades/notificacionFacade'

const icons = { success: CheckCircle2, error: ShieldAlert, warning: CircleAlert, info: Info }
const colorClasses = {
  success: 'border-emerald-200 bg-emerald-50 text-emerald-950',
  error: 'border-rose-200 bg-rose-50 text-rose-950',
  warning: 'border-amber-200 bg-amber-50 text-amber-950',
  info: 'border-cyan-200 bg-cyan-50 text-cyan-950',
}
const accentClasses = { success: 'bg-emerald-500', error: 'bg-rose-500', warning: 'bg-amber-500', info: 'bg-cyan-500' }
const progress = (item) => computed(() => `${(item.remaining / item.duration) * 100}%`)
</script>

<template>
  <div class="pointer-events-none fixed inset-x-4 bottom-4 z-100 flex flex-col items-end gap-3 sm:left-auto sm:w-105" aria-live="polite" aria-atomic="true">
    <TransitionGroup name="toast">
      <article v-for="item in notificacionFacade.state.items" :key="item.id" :class="colorClasses[item.type]" :role="item.type === 'error' ? 'alert' : 'status'" class="pointer-events-auto relative w-full overflow-hidden rounded-2xl border p-4 shadow-xl shadow-slate-950/12">
        <div class="flex gap-3"><component :is="icons[item.type]" :size="21" class="mt-0.5 shrink-0"/><div class="min-w-0 flex-1"><p class="font-black">{{ item.title }}</p><p class="mt-1 text-sm leading-5 opacity-85">{{ item.message }}</p></div><button type="button" class="-mt-1 -mr-1 rounded-lg p-1.5 opacity-65 transition hover:bg-black/5 hover:opacity-100 focus:outline-none focus:ring-2 focus:ring-current" aria-label="Cerrar notificación" @click="notificacionFacade.cerrar(item.id)"><X :size="17"/></button></div>
        <div class="absolute inset-x-0 bottom-0 h-1 bg-black/8"><div :class="accentClasses[item.type]" class="h-full transition-[width] duration-75" :style="{ width: progress(item).value }"/></div>
      </article>
    </TransitionGroup>
  </div>
</template>
