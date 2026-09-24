<script setup>
import { computed, ref } from 'vue'
import { useRouter } from 'vue-router'
import { Gauge, LockKeyhole, ShieldCheck, LogOut } from 'lucide-vue-next'
import { useAuthStore } from '../stores/auth'
import { api, apiError } from '../api/client'
import UiAlert from '../components/UiAlert.vue'

const router = useRouter(); const auth = useAuthStore(); const currentPassword = ref(''); const newPassword = ref(''); const confirmation = ref(''); const error = ref(''); const loading = ref(false)
const valid = computed(() => newPassword.value.length >= 8 && /[A-Z]/.test(newPassword.value) && /[a-z]/.test(newPassword.value) && /\d/.test(newPassword.value) && /[^A-Za-z0-9]/.test(newPassword.value) && newPassword.value === confirmation.value)
async function submit() { error.value = ''; if (!valid.value) { error.value = 'La nueva contraseña debe incluir 8 caracteres, mayúscula, minúscula, número, símbolo y confirmación.'; return }; loading.value = true; try { await api.post('/auth/change-temporary-password', { currentPassword: currentPassword.value, newPassword: newPassword.value, confirmation: confirmation.value }); await auth.refresh(); router.push('/') } catch (e) { error.value = apiError(e) } finally { loading.value = false } }
async function cancelLogin() { try { await auth.logout() } finally { router.push('/login') } }
</script>

<template>
  <div class="auth-page auth-pane flex min-h-screen items-center justify-center p-5 sm:p-8">
    <form class="card w-full max-w-md p-7 shadow-[0_24px_80px_-36px_rgba(8,47,73,.45)] sm:p-10" @submit.prevent="submit">
      <div class="mb-8 flex items-center gap-3"><div class="brand-mark"><Gauge :size="22" /></div><div><p class="text-xl font-black tracking-tight text-slate-950">Auto<span class="text-cyan-600">Manager</span></p><p class="text-xs text-slate-500">Gestión inteligente del taller</p></div></div>
      <div class="mb-7 rounded-2xl border border-cyan-100 bg-cyan-50/70 p-4"><div class="flex gap-3"><div class="rounded-xl bg-cyan-100 p-2 text-cyan-700"><LockKeyhole :size="20" /></div><div><p class="text-xs font-black uppercase tracking-[.16em] text-cyan-700">Acción obligatoria</p><h1 class="mt-1 text-2xl font-black tracking-tight text-slate-950">Actualiza tu contraseña</h1><p class="mt-1 text-sm leading-5 text-slate-600">Antes de continuar, crea una contraseña que solo tú conozcas.</p></div></div></div>
      <div class="space-y-5"><label><span class="label">Contraseña temporal actual</span><input v-model="currentPassword" class="field" type="password" autocomplete="current-password" /></label><label><span class="label">Nueva contraseña</span><input v-model="newPassword" class="field" type="password" autocomplete="new-password" /></label><label><span class="label">Confirmar nueva contraseña</span><input v-model="confirmation" class="field" type="password" autocomplete="new-password" /></label></div>
      <UiAlert class="mt-4" :message="error"/><button class="btn-primary mt-6 w-full py-3" :disabled="loading">{{ loading ? 'Actualizando…' : 'Guardar contraseña segura' }}</button><button class="mt-4 flex w-full items-center justify-center gap-2 text-sm font-bold text-slate-500 hover:text-cyan-700" type="button" @click="cancelLogin"><LogOut :size="16"/>Volver al inicio de sesión</button><p class="mt-5 flex items-center justify-center gap-2 text-center text-xs font-medium text-slate-400"><ShieldCheck :size="14" class="text-emerald-500"/> Esta medida protege tu cuenta.</p>
    </form>
  </div>
</template>
