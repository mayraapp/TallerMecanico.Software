import { computed, reactive, ref } from 'vue'
import { api, apiError } from '../api/client'
import { notificacionFacade } from './notificacionFacade'

const NAME_PATTERN = /^[\p{L}](?:[\p{L}\s'’-]*[\p{L}])?$/u
const EMAIL_PATTERN = /^[^\s@]+@[^\s@]+\.[^\s@]{2,}$/
const PHONE_PATTERN = /^\d{10,15}$/
const allowedPhotoTypes = ['image/jpeg', 'image/png', 'image/webp']
const emptyForm = () => ({ nombreCompleto: '', contactoAlternativo: '', fechaNacimiento: '', telefonoPersonal: '', telefonoTrabajo: '', emailPersonal: '', emailTrabajo: '', direccion: { calleNumero: '', colonia: '', municipio: '', estado: '', codigoPostal: '' } })

export function useClienteFacade() {
  const form = reactive(emptyForm())
  const errors = reactive({})
  const fotografia = ref(null)
  const vistaPrevia = ref(null)
  const isSubmitting = ref(false)
  const today = new Date().toISOString().slice(0, 10)
  const edad = computed(() => calcularEdad(form.fechaNacimiento))
  const tieneCambios = computed(() => Boolean(form.nombreCompleto || form.contactoAlternativo || form.fechaNacimiento || form.telefonoPersonal || form.telefonoTrabajo || form.emailPersonal || form.emailTrabajo || Object.values(form.direccion).some(Boolean) || fotografia.value))

  function normalizarFormulario() {
    form.nombreCompleto = limpiar(form.nombreCompleto)
    form.contactoAlternativo = limpiar(form.contactoAlternativo)
    form.telefonoPersonal = normalizarTelefono(form.telefonoPersonal)
    form.telefonoTrabajo = normalizarTelefono(form.telefonoTrabajo)
    form.emailPersonal = limpiar(form.emailPersonal).toLowerCase()
    form.emailTrabajo = limpiar(form.emailTrabajo).toLowerCase()
    form.direccion.calleNumero = limpiar(form.direccion.calleNumero)
    form.direccion.colonia = limpiar(form.direccion.colonia)
    form.direccion.municipio = limpiar(form.direccion.municipio)
    form.direccion.estado = limpiar(form.direccion.estado)
    form.direccion.codigoPostal = limpiar(form.direccion.codigoPostal)
  }

  function validarFormulario() {
    limpiarErrores()
    normalizarFormulario()
    if (!NAME_PATTERN.test(form.nombreCompleto)) errors.nombreCompleto = 'Escribe un nombre válido, sin números ni símbolos.'
    if (form.contactoAlternativo && !NAME_PATTERN.test(form.contactoAlternativo)) errors.contactoAlternativo = 'El contacto alternativo tiene formato inválido.'
    if (!form.fechaNacimiento || form.fechaNacimiento >= today || edad.value === null || edad.value > 120) errors.fechaNacimiento = 'Selecciona una fecha de nacimiento válida y anterior a hoy.'
    if (!PHONE_PATTERN.test(form.telefonoPersonal)) errors.telefonoPersonal = 'El teléfono personal debe contener de 10 a 15 dígitos.'
    if (form.telefonoTrabajo && !PHONE_PATTERN.test(form.telefonoTrabajo)) errors.telefonoTrabajo = 'El teléfono de trabajo debe contener de 10 a 15 dígitos.'
    if (!EMAIL_PATTERN.test(form.emailPersonal)) errors.emailPersonal = 'Escribe un correo electrónico personal válido.'
    if (form.emailTrabajo && !EMAIL_PATTERN.test(form.emailTrabajo)) errors.emailTrabajo = 'El correo de trabajo tiene formato inválido.'
    if (!form.direccion.calleNumero) errors.calleNumero = 'La calle y número son obligatorios.'
    if (!form.direccion.colonia) errors.colonia = 'La colonia es obligatoria.'
    if (!NAME_PATTERN.test(form.direccion.municipio)) errors.municipio = 'Escribe un municipio válido.'
    if (!NAME_PATTERN.test(form.direccion.estado)) errors.estado = 'Escribe un estado válido.'
    if (!/^\d{5}$/.test(form.direccion.codigoPostal)) errors.codigoPostal = 'El código postal debe tener cinco dígitos.'
    validarFotografia()
    return Object.keys(errors).length === 0
  }

  function seleccionarFotografia(file) {
    if (vistaPrevia.value) URL.revokeObjectURL(vistaPrevia.value)
    fotografia.value = file || null
    vistaPrevia.value = file ? URL.createObjectURL(file) : null
    delete errors.fotografia
    validarFotografia()
  }

  async function registrarCliente() {
    if (!validarFormulario()) {
      notificacionFacade.mostrarAdvertencia('Corrige los campos marcados antes de guardar.', 'Información incompleta')
      return false
    }
    isSubmitting.value = true
    try {
      const payload = new FormData()
      payload.append('datos', new Blob([JSON.stringify({ ...form, telefonoTrabajo: form.telefonoTrabajo || null, emailTrabajo: form.emailTrabajo || null, contactoAlternativo: form.contactoAlternativo || null })], { type: 'application/json' }))
      if (fotografia.value) payload.append('fotografia', fotografia.value)
      const { data } = await api.post('/clientes', payload, { headers: { 'Content-Type': 'multipart/form-data' } })
      notificacionFacade.mostrarExito(data.mensaje || 'Cliente registrado correctamente.', 'Cliente registrado')
      limpiarFormulario()
      return true
    } catch (error) {
      procesarError(error)
      return false
    } finally {
      isSubmitting.value = false
    }
  }

  function procesarError(error) {
    const response = error?.response
    const data = response?.data || {}
    Object.entries(data.validationErrors || {}).forEach(([key, value]) => { errors[key.replace('direccion.', '')] = value })
    if (response?.status === 401) notificacionFacade.mostrarError('Tu sesión ha expirado. Inicia sesión nuevamente.', 'Sesión expirada')
    else if (response?.status === 403) notificacionFacade.mostrarError('No tienes permiso para registrar clientes.', 'Sin autorización')
    else if (data.code === 'CLIENTE_DUPLICADO') notificacionFacade.mostrarAdvertencia(data.message, 'Cliente duplicado')
    else if (data.code?.startsWith('FOTOGRAFIA_')) { errors.fotografia = data.message; notificacionFacade.mostrarError(data.message, 'Fotografía inválida') }
    else if (response?.status === 400) notificacionFacade.mostrarAdvertencia(data.message || 'Revisa los datos proporcionados.', 'Datos inválidos')
    else if (!response) notificacionFacade.mostrarError('No fue posible comunicarse con el servidor. Intenta nuevamente.', 'Error de conexión')
    else notificacionFacade.mostrarError(apiError(error), 'Error interno')
  }

  function limpiarFormulario() {
    Object.assign(form, emptyForm())
    limpiarErrores()
    if (vistaPrevia.value) URL.revokeObjectURL(vistaPrevia.value)
    fotografia.value = null
    vistaPrevia.value = null
  }

  function cancelarCaptura() { limpiarFormulario(); notificacionFacade.mostrarInformacion('La captura del cliente fue cancelada.', 'Operación cancelada') }
  function limpiarErrores() { Object.keys(errors).forEach((key) => delete errors[key]) }
  function validarFotografia() {
    if (!fotografia.value) return
    if (!allowedPhotoTypes.includes(fotografia.value.type)) errors.fotografia = 'Selecciona una imagen JPEG, PNG o WebP.'
    else if (fotografia.value.size > 5 * 1024 * 1024) errors.fotografia = 'La fotografía no debe superar 5 MB.'
  }
  function limpiar(value) { return (value || '').trim().replace(/\s+/g, ' ') }
  function normalizarTelefono(value) { return limpiar(value).replace(/[\s()\-]/g, '') }
  function calcularEdad(fecha) {
    if (!fecha || Number.isNaN(new Date(`${fecha}T00:00:00`).getTime())) return null
    const birth = new Date(`${fecha}T00:00:00`); const current = new Date(); let years = current.getFullYear() - birth.getFullYear()
    const beforeBirthday = current.getMonth() < birth.getMonth() || (current.getMonth() === birth.getMonth() && current.getDate() < birth.getDate())
    return beforeBirthday ? years - 1 : years
  }

  return { form, errors, fotografia, vistaPrevia, isSubmitting, today, edad, tieneCambios, seleccionarFotografia, registrarCliente, validarFormulario, normalizarFormulario, procesarError, limpiarFormulario, cancelarCaptura }
}
