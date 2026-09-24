<script setup>
import { computed, reactive, ref } from 'vue'
import { CheckCircle2, Gauge, ShieldCheck, UserPlus } from 'lucide-vue-next'
import UiAlert from '../components/UiAlert.vue'
import { api, apiError } from '../api/client'

const form = reactive({ fullName: '', email: '', phone: '', password: '', confirmation: '' })
const error = ref('')
const success = ref('')
const sending = ref(false)
const passwordValid = computed(() => form.password.length >= 8 && /[A-Z]/.test(form.password) && /[a-z]/.test(form.password) && /\d/.test(form.password) && /[^A-Za-z0-9]/.test(form.password) && form.password === form.confirmation)

function resetForm() { Object.assign(form, { fullName: '', email: '', phone: '', password: '', confirmation: '' }) }
async function submit() {
  error.value = ''; success.value = ''
  if (!form.fullName || !form.email) { error.value = 'Complete nombre y correo electrónico.'; return }
  if (!passwordValid.value) { error.value = 'La contraseña debe cumplir todos los requisitos y coincidir.'; return }
  sending.value = true
  try { const { data } = await api.post('/public/register', { ...form, phone: form.phone || null }); success.value = data.message; resetForm() } catch (e) { error.value = apiError(e) } finally { sending.value = false }
}
</script>

<template>
  <div class="auth-page auth-pane flex min-h-screen items-center justify-center p-5 sm:p-8">
    <form class="card w-full max-w-2xl p-7 shadow-[0_24px_80px_-36px_rgba(8,47,73,.45)] sm:p-10" @submit.prevent="submit">
      <div class="mb-8 flex items-center gap-3"><div class="brand-mark"><Gauge :size="22" /></div><div><p class="text-xl font-black text-slate-950">Auto<span class="text-cyan-600">Manager</span></p><p class="text-xs text-slate-500">Gestión inteligente del taller</p></div></div>
      <div class="flex flex-col gap-5 border-b border-slate-100 pb-6 sm:flex-row sm:items-start sm:justify-between"><div><p class="text-xs font-black uppercase tracking-[.18em] text-cyan-700">Registro de usuario</p><h1 class="mt-2 text-3xl font-black tracking-[-.035em] text-slate-950">Solicita tu cuenta</h1><p class="mt-2 max-w-xl text-sm leading-6 text-slate-500">Tu solicitud quedará pendiente. Un Superadministrador deberá aprobarla y asignarte un rol antes de que puedas iniciar sesión.</p></div><div class="rounded-2xl bg-cyan-50 p-3 text-cyan-700"><UserPlus :size="24" /></div></div>
      <UiAlert class="mt-5" type="success" :message="success"/><UiAlert class="mt-5" :message="error"/>
      <div class="mt-6 grid gap-5 sm:grid-cols-2"><label class="sm:col-span-2"><span class="label">Nombre completo *</span><input v-model.trim="form.fullName" class="field" autocomplete="name" placeholder="Tu nombre completo" /></label><label><span class="label">Correo electrónico *</span><input v-model.trim="form.email" class="field" autocomplete="email" type="email" placeholder="nombre@correo.com" /></label><label><span class="label">Teléfono</span><input v-model.trim="form.phone" class="field" autocomplete="tel" placeholder="555 123 4567" /></label><label><span class="label">Contraseña *</span><input v-model="form.password" class="field" type="password" autocomplete="new-password" /></label><label><span class="label">Confirmar contraseña *</span><input v-model="form.confirmation" class="field" type="password" autocomplete="new-password" /></label></div>
      <div class="mt-5 flex gap-3 rounded-xl border border-cyan-100 bg-cyan-50/70 p-4 text-sm text-cyan-900"><CheckCircle2 :size="19" class="mt-0.5 shrink-0 text-cyan-700"/><p>Usa al menos 8 caracteres con mayúscula, minúscula, número y símbolo. Tus datos no otorgan permisos hasta ser aprobados.</p></div>
      <div class="mt-7 flex flex-col-reverse gap-3 sm:flex-row sm:items-center sm:justify-between"><RouterLink class="text-center text-sm font-bold text-slate-500 hover:text-cyan-700 hover:underline" to="/login">Ya tengo una cuenta</RouterLink><button class="btn-primary" :disabled="sending"><UserPlus :size="17" />{{ sending ? 'Enviando…' : 'Enviar solicitud' }}</button></div>
      <p class="mt-6 flex items-center justify-center gap-2 text-center text-xs font-medium text-slate-400"><ShieldCheck :size="14" class="text-emerald-500"/> Registro protegido y pendiente de aprobación</p>
    </form>
  </div>
</template>
