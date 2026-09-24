<script setup>
import { ref } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { Eye, EyeOff, Gauge, ShieldCheck, KeyRound, UsersRound, UserPlus } from 'lucide-vue-next'
import { useAuthStore } from '../stores/auth'
import { apiError } from '../api/client'
import UiAlert from '../components/UiAlert.vue'

const router = useRouter()
const route = useRoute()
const auth = useAuthStore()
const email = ref('')
const password = ref('')
const show = ref(false)
const error = ref('')
async function submit() {
  error.value = ''
  if (!email.value || !password.value) { error.value = 'Escriba correo y contraseña.'; return }
  try { const user = await auth.login({ email: email.value, password: password.value }); router.push(user.requiresPasswordChange ? '/change-password' : (route.query.redirect || '/')) } catch (e) { error.value = apiError(e) }
}
</script>

<template>
  <div class="auth-page grid lg:grid-cols-[1.08fr_.92fr]">
    <section class="auth-hero hidden p-14 text-white lg:flex lg:flex-col">
      <div class="flex items-center gap-3"><div class="brand-mark h-14 w-14 rounded-2xl"><Gauge :size="28" /></div><div><p class="brand-wordmark text-2xl">Auto<span>Manager</span></p><p class="text-sm text-cyan-100/70">Gestión inteligente del taller</p></div></div>
      <div class="my-auto max-w-xl"><p class="mb-5 text-sm font-bold uppercase tracking-[.22em] text-cyan-300">Control operativo seguro</p><h1 class="text-5xl font-black leading-[1.08] tracking-[-.045em]">La operación de tu taller, en una sola dirección.</h1><p class="mt-6 max-w-lg text-lg leading-8 text-slate-300">Administra accesos, usuarios y permisos con una experiencia clara, ágil y protegida.</p><div class="mt-10 grid max-w-lg gap-3"><div class="security-pill"><ShieldCheck :size="19" class="text-cyan-300"/>Acceso validado y sesión protegida</div><div class="security-pill"><KeyRound :size="19" class="text-cyan-300"/>Permisos específicos para cada rol</div><div class="security-pill"><UsersRound :size="19" class="text-cyan-300"/>Administración centralizada de usuarios</div></div></div>
      <p class="text-sm font-medium text-slate-400">AutoManager · M01 Seguridad y usuarios</p>
    </section>
    <main class="auth-pane flex items-center justify-center p-5 sm:p-8">
      <form class="card w-full max-w-md p-7 shadow-[0_24px_80px_-36px_rgba(8,47,73,.45)] sm:p-10" @submit.prevent="submit">
        <div class="mb-8 lg:hidden"><div class="flex items-center gap-3"><div class="brand-mark"><Gauge :size="22"/></div><div><p class="text-xl font-black text-slate-950">Auto<span class="text-cyan-600">Manager</span></p><p class="text-xs text-slate-500">Gestión inteligente del taller</p></div></div></div>
        <div class="mb-7"><p class="text-xs font-black uppercase tracking-[.18em] text-cyan-700">Acceso seguro</p><h1 class="mt-2 text-3xl font-black tracking-[-.035em] text-slate-950">Bienvenido de nuevo</h1><p class="mt-2 text-sm leading-6 text-slate-500">Ingresa tus credenciales para continuar a tu espacio de trabajo.</p></div>
        <div v-if="route.query.mode === 'register'" class="mb-5 flex gap-3 rounded-xl border border-cyan-100 bg-cyan-50/80 p-4 text-sm text-cyan-900"><UserPlus :size="19" class="mt-0.5 shrink-0 text-cyan-700"/><p><b>Registro de usuarios:</b> inicia sesión como Superadministrador para abrir el formulario de registro.</p></div>
        <div class="space-y-5"><label><span class="label">Correo electrónico</span><input v-model.trim="email" class="field" autocomplete="email" type="email" placeholder="nombre@taller.com" /></label><label><span class="label">Contraseña</span><div class="relative"><input v-model="password" class="field pr-11" autocomplete="current-password" :type="show ? 'text' : 'password'" placeholder="••••••••" /><button class="absolute right-2 top-2 rounded-lg p-1.5 text-slate-500 hover:bg-cyan-50 hover:text-cyan-700" type="button" :aria-label="show ? 'Ocultar contraseña' : 'Mostrar contraseña'" @click="show=!show"><EyeOff v-if="show" :size="19"/><Eye v-else :size="19"/></button></div></label><div class="text-right"><RouterLink class="text-sm font-bold text-cyan-700 hover:underline" to="/recover">¿Olvidaste tu contraseña?</RouterLink></div><UiAlert :message="error"/><button class="btn-primary w-full py-3" :disabled="auth.loading">{{ auth.loading ? 'Verificando…' : 'Iniciar sesión' }}</button></div>
        <div class="mt-7 border-t border-slate-100 pt-5 text-center"><p class="text-sm text-slate-500">¿Aún no tienes una cuenta?</p><RouterLink to="/register" class="mt-2 inline-flex items-center gap-2 text-sm font-bold text-cyan-700 hover:text-cyan-600 hover:underline"><UserPlus :size="17"/>Registrarme</RouterLink></div><p class="mt-5 flex items-center justify-center gap-2 text-center text-xs font-medium text-slate-400"><ShieldCheck :size="14" class="text-emerald-500"/> Conexión local protegida</p>
      </form>
    </main>
  </div>
</template>
