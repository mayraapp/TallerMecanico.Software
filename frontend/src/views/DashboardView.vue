<script setup>
import AppShell from '../components/AppShell.vue'
import { useAuthStore } from '../stores/auth'
import { ShieldCheck, Users, ClipboardCheck, Gauge, LockKeyhole } from 'lucide-vue-next'

const auth = useAuthStore()
const cards = [
  { label: 'Sesión protegida', value: 'Activa', detail: 'JWT y cookies seguras', icon: ShieldCheck, color: 'bg-emerald-50 text-emerald-700' },
  { label: 'Rol principal', value: auth.user?.primaryRole || '—', detail: 'Acceso según tu perfil', icon: Gauge, color: 'bg-amber-50 text-amber-700' },
  { label: 'Permisos disponibles', value: String(auth.permissions.length), detail: 'Autorizaciones vigentes', icon: ClipboardCheck, color: 'bg-cyan-50 text-cyan-700' },
  { label: 'Administración de usuarios', value: auth.hasPermission('USERS_VIEW') ? 'Disponible' : 'Restringida', detail: 'Gestión de cuentas', icon: Users, color: 'bg-violet-50 text-violet-700' },
]
</script>

<template>
  <AppShell title="Centro de control" subtitle="Resumen de acceso, permisos y seguridad">
    <section class="control-hero mb-7 rounded-3xl p-7 text-white lg:p-10"><div class="flex flex-col gap-7 sm:flex-row sm:items-end sm:justify-between"><div><p class="text-sm font-bold uppercase tracking-[.18em] text-cyan-200">Hola, {{ auth.user?.fullName }}</p><h2 class="mt-3 max-w-2xl text-3xl font-black leading-tight tracking-[-.035em] lg:text-4xl">Todo lo importante empieza con un acceso bien controlado.</h2><p class="mt-4 max-w-xl leading-7 text-slate-200">AutoManager valida cada acción sensible desde el servidor para cuidar la operación del taller.</p></div><div class="flex shrink-0 items-center gap-3 rounded-2xl border border-white/15 bg-white/10 px-4 py-3"><LockKeyhole :size="21" class="text-cyan-200"/><div><p class="text-xs font-semibold text-slate-300">Estado de seguridad</p><p class="font-black">Protegido</p></div></div></div></section>
    <section class="grid gap-4 sm:grid-cols-2 xl:grid-cols-4"><article v-for="card in cards" :key="card.label" class="metric-card"><div :class="card.color" class="mb-5 inline-flex rounded-xl p-2.5"><component :is="card.icon" :size="21"/></div><p class="text-sm font-semibold text-slate-500">{{ card.label }}</p><p class="mt-1 break-words text-xl font-black text-slate-950">{{ card.value }}</p><p class="mt-2 text-xs font-medium text-slate-400">{{ card.detail }}</p></article></section>
    <section class="card mt-7 overflow-hidden p-0"><div class="p-6"><p class="text-xs font-black uppercase tracking-[.16em] text-cyan-700">Próximas fases</p><h3 class="mt-2 text-xl font-black text-slate-950">La base está lista para crecer.</h3><p class="mt-2 max-w-2xl text-sm leading-6 text-slate-600">Clientes, vehículos, inventario, empleados, servicios, órdenes, proveedores y reportes se integrarán de forma modular sin exponer pantallas antes de tiempo.</p></div><div class="h-1 bg-gradient-to-r from-cyan-500 via-cyan-300 to-emerald-300"></div></section>
  </AppShell>
</template>
