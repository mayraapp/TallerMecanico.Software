<script setup>
import { computed, onMounted, reactive, ref } from 'vue'
import { ArrowLeft, CheckCircle2, KeyRound, UserPlus } from 'lucide-vue-next'
import AppShell from '../components/AppShell.vue'
import UiAlert from '../components/UiAlert.vue'
import { api, apiError } from '../api/client'

const roles = ref([])
const error = ref('')
const success = ref('')
const loadingRoles = ref(true)
const saving = ref(false)
const form = reactive({ fullName: '', email: '', phone: '', temporaryPassword: '', confirmation: '', roleCode: '', status: 'ACTIVE' })
const passwordValid = computed(() => form.temporaryPassword.length >= 8 && /[A-Z]/.test(form.temporaryPassword) && /[a-z]/.test(form.temporaryPassword) && /\d/.test(form.temporaryPassword) && /[^A-Za-z0-9]/.test(form.temporaryPassword) && form.temporaryPassword === form.confirmation)

function resetForm() {
  Object.assign(form, { fullName: '', email: '', phone: '', temporaryPassword: '', confirmation: '', roleCode: roles.value.find((role) => !['SUPERADMIN', 'PENDING'].includes(role.code))?.code || '', status: 'ACTIVE' })
}
async function loadRoles() {
  loadingRoles.value = true
  try { const { data } = await api.get('/roles'); roles.value = data; resetForm() } catch (e) { error.value = apiError(e) } finally { loadingRoles.value = false }
}
async function submit() {
  error.value = ''; success.value = ''
  if (!form.fullName || !form.email || !form.roleCode) { error.value = 'Complete nombre, correo y rol.'; return }
  if (!passwordValid.value) { error.value = 'La contraseña temporal debe cumplir todos los requisitos y coincidir.'; return }
  saving.value = true
  try {
    const { data } = await api.post('/users', { ...form, phone: form.phone || null })
    success.value = `La cuenta de ${data.fullName} fue registrada. Deberá cambiar su contraseña al ingresar.`
    resetForm()
  } catch (e) { error.value = apiError(e) } finally { saving.value = false }
}
onMounted(loadRoles)
</script>

<template>
  <AppShell title="Registrar usuario" subtitle="Crea cuentas con rol, estado y contraseña temporal">
    <div class="mx-auto max-w-4xl">
      <section class="control-hero rounded-3xl p-6 text-white sm:p-8"><div class="flex flex-col gap-5 sm:flex-row sm:items-center sm:justify-between"><div><p class="text-xs font-black uppercase tracking-[.18em] text-cyan-200">Administración segura</p><h2 class="mt-2 text-2xl font-black tracking-tight">Registra una cuenta para tu equipo.</h2><p class="mt-2 max-w-2xl text-sm leading-6 text-slate-200">El nuevo usuario recibirá únicamente los permisos de su rol y deberá establecer una contraseña propia al iniciar sesión.</p></div><div class="rounded-2xl border border-white/15 bg-white/10 p-3 text-cyan-100"><UserPlus :size="28" /></div></div></section>
      <UiAlert class="mt-5" type="success" :message="success"/><UiAlert class="mt-5" :message="error"/>
      <form class="card mt-5 p-6 sm:p-8" @submit.prevent="submit">
        <div class="flex items-center gap-3 border-b border-slate-100 pb-5"><div class="rounded-xl bg-cyan-50 p-2.5 text-cyan-700"><KeyRound :size="20"/></div><div><h3 class="font-black text-slate-950">Datos de la cuenta</h3><p class="text-sm text-slate-500">Los campos marcados con * son obligatorios.</p></div></div>
        <div v-if="loadingRoles" class="py-10 text-center text-slate-500">Cargando roles disponibles…</div>
        <div v-else class="mt-6 grid gap-5 sm:grid-cols-2"><label class="sm:col-span-2"><span class="label">Nombre completo *</span><input v-model.trim="form.fullName" class="field" autocomplete="name" placeholder="Nombre de la persona"/></label><label><span class="label">Correo electrónico *</span><input v-model.trim="form.email" class="field" autocomplete="email" type="email" placeholder="nombre@taller.com"/></label><label><span class="label">Teléfono</span><input v-model.trim="form.phone" class="field" autocomplete="tel" placeholder="555 123 4567"/></label><label><span class="label">Rol *</span><select v-model="form.roleCode" class="field"><option v-for="role in roles.filter((role) => !['SUPERADMIN', 'PENDING'].includes(role.code))" :key="role.code" :value="role.code">{{ role.name }}</option></select></label><label><span class="label">Estado inicial</span><select v-model="form.status" class="field"><option value="ACTIVE">Activo</option><option value="INACTIVE">Inactivo</option></select></label><label><span class="label">Contraseña temporal *</span><input v-model="form.temporaryPassword" class="field" type="password" autocomplete="new-password"/></label><label><span class="label">Confirmar contraseña *</span><input v-model="form.confirmation" class="field" type="password" autocomplete="new-password"/></label></div>
        <div v-if="!loadingRoles" class="mt-5 flex gap-3 rounded-xl border border-cyan-100 bg-cyan-50/70 p-4 text-sm text-cyan-900"><CheckCircle2 :size="19" class="mt-0.5 shrink-0 text-cyan-700"/><p>La contraseña temporal requiere 8 caracteres, mayúscula, minúscula, número y símbolo. No se muestra ni se guarda en auditoría.</p></div>
        <div class="mt-7 flex flex-col-reverse gap-3 sm:flex-row sm:justify-end"><RouterLink class="btn-ghost" to="/users"><ArrowLeft :size="17"/>Volver a usuarios</RouterLink><button class="btn-primary" :disabled="saving || loadingRoles"><UserPlus :size="17"/>{{ saving ? 'Registrando…' : 'Registrar usuario' }}</button></div>
      </form>
    </div>
  </AppShell>
</template>
