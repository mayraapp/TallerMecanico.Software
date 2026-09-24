<script setup>
import { ref } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { api, apiError } from '../api/client'
import UiAlert from '../components/UiAlert.vue'
const route=useRoute(), router=useRouter(); const email=ref(route.query.email||''); const code=ref(''); const error=ref(''); const loading=ref(false)
async function submit(){error.value=''; if(!/^\d{6}$/.test(code.value)){error.value='Escriba los seis dígitos del código.';return} loading.value=true;try{await api.post('/auth/verify-code',{email:email.value,code:code.value});router.push({path:'/reset-password',query:{email:email.value,code:code.value}})}catch(e){error.value=apiError(e)}finally{loading.value=false}}
</script>
<template><div class="flex min-h-screen items-center justify-center bg-slate-100 p-5"><form class="card w-full max-w-md p-8" @submit.prevent="submit"><p class="text-sm font-bold text-amber-600">PASO 2 DE 3</p><h1 class="mt-2 text-3xl font-black">Verifica el código</h1><p class="mt-2 text-sm text-slate-500">El código vence en 10 minutos y permite hasta cinco intentos.</p><label class="mt-6 block"><span class="label">Correo</span><input v-model.trim="email" class="field" type="email" /></label><label class="mt-4 block"><span class="label">Código de seis dígitos</span><input v-model.trim="code" class="field text-center text-2xl tracking-[.45em]" inputmode="numeric" maxlength="6" /></label><UiAlert class="mt-4" :message="error"/><button class="btn-primary mt-5 w-full" :disabled="loading">{{loading?'Verificando…':'Verificar código'}}</button></form></div></template>
