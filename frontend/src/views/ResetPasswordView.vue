<script setup>
import { computed, ref } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { api, apiError } from '../api/client'
import UiAlert from '../components/UiAlert.vue'
const route=useRoute(), router=useRouter(); const email=ref(route.query.email||''); const code=ref(route.query.code||''); const password=ref(''); const confirmation=ref(''); const error=ref(''); const loading=ref(false)
const valid=computed(()=>password.value.length>=8&&/[A-Z]/.test(password.value)&&/[a-z]/.test(password.value)&&/\d/.test(password.value)&&/[^A-Za-z0-9]/.test(password.value)&&password.value===confirmation.value)
async function submit(){error.value='';if(!valid.value){error.value='Use 8 caracteres, mayúscula, minúscula, número, símbolo y la misma confirmación.';return}loading.value=true;try{await api.post('/auth/reset-password',{email:email.value,code:code.value,newPassword:password.value,confirmation:confirmation.value});router.push('/login')}catch(e){error.value=apiError(e)}finally{loading.value=false}}
</script>
<template><div class="flex min-h-screen items-center justify-center bg-slate-100 p-5"><form class="card w-full max-w-md p-8" @submit.prevent="submit"><p class="text-sm font-bold text-amber-600">PASO 3 DE 3</p><h1 class="mt-2 text-3xl font-black">Nueva contraseña</h1><p class="mt-2 text-sm text-slate-500">Debe tener mayúscula, minúscula, número y símbolo.</p><label class="mt-6 block"><span class="label">Nueva contraseña</span><input v-model="password" class="field" type="password" autocomplete="new-password" /></label><label class="mt-4 block"><span class="label">Confirmar contraseña</span><input v-model="confirmation" class="field" type="password" autocomplete="new-password" /></label><UiAlert class="mt-4" :message="error"/><button class="btn-primary mt-5 w-full" :disabled="loading">{{loading?'Guardando…':'Actualizar contraseña'}}</button></form></div></template>
