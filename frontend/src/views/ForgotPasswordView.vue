<script setup>
import { ref } from 'vue'
import { useRouter } from 'vue-router'
import { api, apiError } from '../api/client'
import UiAlert from '../components/UiAlert.vue'
const router = useRouter(); const email = ref(''); const loading = ref(false); const message = ref(''); const error = ref('')
async function submit() { error.value=''; message.value=''; if (!email.value) { error.value='Ingrese un correo válido.'; return } loading.value=true; try { const { data } = await api.post('/auth/forgot-password',{ email:email.value }); message.value=data.message; setTimeout(()=>router.push({path:'/verify-code',query:{email:email.value}}),900) } catch(e){error.value=apiError(e)} finally {loading.value=false} }
</script>
<template><div class="flex min-h-screen items-center justify-center bg-slate-100 p-5"><form class="card w-full max-w-md p-8" @submit.prevent="submit"><p class="text-sm font-bold text-amber-600">RECUPERACIÓN</p><h1 class="mt-2 text-3xl font-black">Recupera tu acceso</h1><p class="mt-2 text-sm text-slate-500">Si la cuenta está activa, recibirás un código de seis dígitos.</p><label class="mt-6 block"><span class="label">Correo electrónico</span><input v-model.trim="email" class="field" type="email" autocomplete="email" /></label><div class="mt-4"><UiAlert type="success" :message="message" /><UiAlert :message="error" /></div><button class="btn-primary mt-5 w-full" :disabled="loading">{{ loading ? 'Procesando…' : 'Solicitar código' }}</button><RouterLink class="mt-5 block text-center text-sm font-bold text-slate-600 hover:underline" to="/login">Volver al inicio de sesión</RouterLink></form></div></template>
