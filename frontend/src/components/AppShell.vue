<script setup>
import { computed, ref } from 'vue'
import { useRouter } from 'vue-router'
import { LayoutDashboard, Users, UserPlus, ShieldCheck, ClipboardList, Menu, X, LogOut, Gauge, CircleCheck } from 'lucide-vue-next'
import { useAuthStore } from '../stores/auth'

defineProps({ title: String, subtitle: String })
const router = useRouter()
const auth = useAuthStore()
const mobileOpen = ref(false)
const items = [
  { to: '/', label: 'Centro de control', icon: LayoutDashboard, permission: 'ADMIN_PANEL_VIEW' },
  { to: '/users', label: 'Usuarios', icon: Users, permission: 'USERS_VIEW' },
  { to: '/register-user', label: 'Registrar usuario', icon: UserPlus, permission: 'USERS_CREATE' },
  { to: '/roles', label: 'Roles y permisos', icon: ShieldCheck, permission: 'PERMISSIONS_MANAGE' },
  { to: '/audit', label: 'Auditoría', icon: ClipboardList, permission: 'AUDIT_VIEW' },
]
const visibleItems = computed(() => items.filter((item) => auth.hasPermission(item.permission)))
async function logout() { await auth.logout(); router.push('/login') }
</script>

<template>
  <div class="app-canvas">
    <aside :class="mobileOpen ? 'translate-x-0' : '-translate-x-full'" class="sidebar fixed inset-y-0 left-0 z-40 flex w-72 flex-col text-slate-200 transition-transform lg:translate-x-0">
      <div class="flex items-center gap-3 border-b border-white/10 px-6 py-6"><div class="brand-mark"><Gauge :size="23" /></div><div><p class="brand-wordmark text-lg">Auto<span>Manager</span></p><p class="text-xs font-medium text-slate-400">Gestión inteligente del taller</p></div><button class="ml-auto rounded-lg p-2 text-slate-300 hover:bg-white/10 lg:hidden" aria-label="Cerrar menú" @click="mobileOpen=false"><X /></button></div>
      <div class="mx-4 mt-5 flex items-center gap-2 rounded-xl border border-white/10 bg-white/5 px-3 py-2.5 text-xs text-slate-300"><CircleCheck :size="15" class="text-emerald-400"/><span>Entorno protegido</span></div>
      <nav class="flex-1 space-y-1 px-3 py-5"><RouterLink v-for="item in visibleItems" :key="item.to" :to="item.to" class="nav-item" @click="mobileOpen=false"><component :is="item.icon" :size="19" />{{ item.label }}</RouterLink></nav>
      <div class="m-4 rounded-2xl border border-white/10 bg-white/5 p-4"><p class="truncate text-sm font-bold text-white">{{ auth.user?.fullName }}</p><p class="mb-3 truncate text-xs text-slate-400">{{ auth.user?.primaryRole }}</p><button class="btn w-full bg-white/8 text-white hover:bg-white/14" @click="logout"><LogOut :size="16"/>Cerrar sesión</button></div>
    </aside>
    <div class="lg:pl-72"><header class="topbar sticky top-0 z-30 flex min-h-20 items-center gap-4 px-5 lg:px-8"><button class="btn-ghost p-2 lg:hidden" aria-label="Abrir menú" @click="mobileOpen=true"><Menu /></button><div class="min-w-0"><p class="mb-0.5 text-xs font-bold uppercase tracking-[.16em] text-cyan-700">AutoManager</p><h1 class="truncate text-xl font-black text-slate-950">{{ title }}</h1><p v-if="subtitle" class="text-sm text-slate-500">{{ subtitle }}</p></div><div class="ml-auto hidden items-center gap-2 rounded-full border border-cyan-100 bg-cyan-50 px-3 py-2 text-xs font-bold text-cyan-800 sm:flex"><span class="h-2 w-2 rounded-full bg-emerald-500"></span>{{ auth.user?.primaryRole }}</div></header><main class="p-5 lg:p-8"><slot /></main></div>
    <div v-if="mobileOpen" class="fixed inset-0 z-30 bg-slate-950/40 lg:hidden" @click="mobileOpen=false" />
  </div>
</template>
