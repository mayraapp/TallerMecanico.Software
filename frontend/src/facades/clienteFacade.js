import { computed, reactive, ref } from 'vue'
import { api, apiError } from '../api/client'
import { notificacionFacade } from './notificacionFacade'

const NAME_PATTERN = /^[\p{L}](?:[\p{L}\s'’-]*[\p{L}])?$/u
const EMAIL_PATTERN = /^[^\s@]+@[^\s@]+\.[^\s@]{2,}$/
const PHONE_PATTERN = /^\d{10,15}$/
const MAX_PHOTO_BYTES = 15 * 1024 * 1024
const allowedPhotoTypes = ['image/jpeg', 'image/png', 'image/webp']
const PHOTO_ERROR = 'La fotografía debe ser JPEG, PNG o WebP y no puede superar los 15 MB.'
const emptyForm = () => ({ nombreCompleto: '', contactoAlternativo: '', fechaNacimiento: '', telefonoPersonal: '', telefonoTrabajo: '', emailPersonal: '', emailTrabajo: '', direccion: { calleNumero: '', colonia: '', municipio: '', estado: '', codigoPostal: '' } })

/**
 * Coordinates client form state, local validation, multipart submission and user-facing errors.
 *
 * @returns {object} reactive form state plus operations used by ClientRegistrationView
 */
export function useClienteFacade() {
  const form = reactive(emptyForm())
  const errors = reactive({})
  const fotografia = ref(null)
  const vistaPrevia = ref(null)
  const errorFotografia = ref('')
  const isSubmitting = ref(false)
  const today = new Date().toISOString().slice(0, 10)
  const edad = computed(() => calcularEdad(form.fechaNacimiento))
  const tieneCambios = computed(() => Boolean(form.nombreCompleto || form.contactoAlternativo || form.fechaNacimiento || form.telefonoPersonal || form.telefonoTrabajo || form.emailPersonal || form.emailTrabajo || Object.values(form.direccion).some(Boolean) || fotografia.value))

  /** Normalizes visible values before local validation and before creating the DTO payload. */
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

  /**
   * Validates the captured DTO and selected photo locally; Spring Boot remains authoritative.
   *
   * @returns {boolean} true only when the form can be submitted once
   */
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

  /**
   * Validates MIME, size and binary signature before creating a local preview.
   *
   * @param {File|undefined} file file chosen from the protected form input
   * @returns {Promise<boolean>} whether the photo can be sent to the backend
   */
  async function seleccionarFotografia(file) {
    revocarVistaPrevia()
    fotografia.value = file || null
    errorFotografia.value = ''
    delete errors.fotografia
    if (!file) return true
    errorFotografia.value = await validarArchivoFotografia(file)
    if (errorFotografia.value) {
      errors.fotografia = errorFotografia.value
      return false
    }
    vistaPrevia.value = URL.createObjectURL(file)
    return true
  }

  /** Removes the selected photo and its browser preview without touching any stored file. */
  function quitarFotografia() {
    revocarVistaPrevia()
    fotografia.value = null
    errorFotografia.value = ''
    delete errors.fotografia
  }

  /** Formats bytes for display without altering the value submitted to the backend. */
  function formatoTamano(bytes) {
    if (!Number.isFinite(bytes)) return '0 B'
    if (bytes < 1024) return `${bytes} B`
    if (bytes < 1024 * 1024) return `${(bytes / 1024).toFixed(1)} KB`
    return `${(bytes / (1024 * 1024)).toFixed(1)} MB`
  }

  /**
   * Builds the multipart DTO, prevents repeated submissions and delegates persistence to the API.
   *
   * @returns {Promise<boolean>} true after a 201 response; false after a controlled error
   */
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
      const { data } = await api.post('/clientes', payload)
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

  /**
   * Maps safe API status codes to field errors and centralized toast notifications.
   *
   * @param {import('axios').AxiosError} error API or connectivity error
   */
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

  /** Clears all local client capture state after a successful operation or confirmed cancellation. */
  function limpiarFormulario() {
    Object.assign(form, emptyForm())
    limpiarErrores()
    quitarFotografia()
  }

  /** Cancels capture after the view-level confirmation modal is accepted. */
  function cancelarCaptura() {
    limpiarFormulario()
    notificacionFacade.mostrarInformacion('La captura del cliente fue cancelada.', 'Operación cancelada')
  }

  /** Reapplies a remembered photo validation failure while the rest of the form is revalidated. */
  function validarFotografia() {
    if (fotografia.value && errorFotografia.value) errors.fotografia = errorFotografia.value
  }

  /** Removes reactive field validation messages. */
  function limpiarErrores() { Object.keys(errors).forEach((key) => delete errors[key]) }

  /** Releases a preview URL once it is replaced, removed or the form is cleared. */
  function revocarVistaPrevia() {
    if (vistaPrevia.value) URL.revokeObjectURL(vistaPrevia.value)
    vistaPrevia.value = null
  }

  /**
   * Checks claimed MIME type, maximum bytes and initial binary signature in the browser.
   * Spring Boot repeats the same security checks and remains the source of truth.
   *
   * @param {File} file selected image candidate
   * @returns {Promise<string>} an empty string for a valid candidate or the standard safe error message
   */
  async function validarArchivoFotografia(file) {
    if (!allowedPhotoTypes.includes(file.type) || file.size === 0 || file.size > MAX_PHOTO_BYTES) return PHOTO_ERROR
    try {
      const bytes = new Uint8Array(await file.slice(0, 12).arrayBuffer())
      const detectedType = detectarTipoFotografia(bytes)
      return detectedType === file.type ? '' : PHOTO_ERROR
    } catch {
      return PHOTO_ERROR
    }
  }

  /** Detects only the permitted binary image signatures; it does not trust a file extension. */
  function detectarTipoFotografia(bytes) {
    if (bytes.length >= 3 && bytes[0] === 0xFF && bytes[1] === 0xD8 && bytes[2] === 0xFF) return 'image/jpeg'
    if (bytes.length >= 8 && bytes[0] === 0x89 && bytes[1] === 0x50 && bytes[2] === 0x4E && bytes[3] === 0x47 && bytes[4] === 0x0D && bytes[5] === 0x0A && bytes[6] === 0x1A && bytes[7] === 0x0A) return 'image/png'
    if (bytes.length >= 12 && bytes[0] === 0x52 && bytes[1] === 0x49 && bytes[2] === 0x46 && bytes[3] === 0x46 && bytes[8] === 0x57 && bytes[9] === 0x45 && bytes[10] === 0x42 && bytes[11] === 0x50) return 'image/webp'
    return ''
  }

  /** Trims and compacts user-visible whitespace. */
  function limpiar(value) { return (value || '').trim().replace(/\s+/g, ' ') }
  /** Removes allowed telephone decoration before client-side comparison. */
  function normalizarTelefono(value) { return limpiar(value).replace(/[\s()\-]/g, '') }
  /** Calculates a non-persisted age from the ISO date selected by the user. */
  function calcularEdad(fecha) {
    if (!fecha || Number.isNaN(new Date(`${fecha}T00:00:00`).getTime())) return null
    const birth = new Date(`${fecha}T00:00:00`); const current = new Date(); let years = current.getFullYear() - birth.getFullYear()
    const beforeBirthday = current.getMonth() < birth.getMonth() || (current.getMonth() === birth.getMonth() && current.getDate() < birth.getDate())
    return beforeBirthday ? years - 1 : years
  }

  return { form, errors, fotografia, vistaPrevia, isSubmitting, today, edad, tieneCambios, seleccionarFotografia, quitarFotografia, formatoTamano, registrarCliente, validarFormulario, normalizarFormulario, procesarError, limpiarFormulario, cancelarCaptura }
}
