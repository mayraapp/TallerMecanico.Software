import { reactive } from 'vue'

const state = reactive({ items: [] })
let nextId = 1
const timers = new Map()

/** Creates a timed, accessible toast record and returns its identifier for manual dismissal. */
function mostrar(type, title, message, duration = 5500) {
  const item = { id: nextId++, type, title, message, duration, remaining: duration }
  state.items.push(item)
  const startedAt = Date.now()
  const interval = window.setInterval(() => {
    item.remaining = Math.max(0, duration - (Date.now() - startedAt))
  }, 80)
  const timeout = window.setTimeout(() => cerrar(item.id), duration)
  timers.set(item.id, { interval, timeout })
  return item.id
}

/** Stops a toast timer and removes the notification from the reactive queue. */
function cerrar(id) {
  const timer = timers.get(id)
  if (timer) { window.clearInterval(timer.interval); window.clearTimeout(timer.timeout); timers.delete(id) }
  const index = state.items.findIndex((item) => item.id === id)
  if (index >= 0) state.items.splice(index, 1)
}

/** Centralizes success, error, warning and information notifications for protected views. */
export const notificacionFacade = {
  state,
  cerrar,
  mostrarExito: (message, title = 'Operación completada') => mostrar('success', title, message),
  mostrarError: (message, title = 'No fue posible completar la operación') => mostrar('error', title, message, 7000),
  mostrarAdvertencia: (message, title = 'Revisa la información') => mostrar('warning', title, message, 6500),
  mostrarInformacion: (message, title = 'Información') => mostrar('info', title, message, 4000),
}
