/* @vitest-environment jsdom */
import { afterEach, beforeEach, describe, expect, it, vi } from 'vitest'
import { notificacionFacade } from './notificacionFacade'

describe('notificacionFacade', () => {
  beforeEach(() => {
    vi.useFakeTimers()
    notificacionFacade.state.items.splice(0)
  })

  afterEach(() => vi.useRealTimers())

  it('muestra una notificación de éxito y la cierra al terminar su tiempo', () => {
    notificacionFacade.mostrarExito('Cliente registrado correctamente.', 'Cliente registrado')
    expect(notificacionFacade.state.items).toHaveLength(1)
    expect(notificacionFacade.state.items[0]).toMatchObject({ type: 'success', title: 'Cliente registrado' })
    vi.advanceTimersByTime(5500)
    expect(notificacionFacade.state.items).toHaveLength(0)
  })

  it('permite cerrar una notificación manualmente', () => {
    const id = notificacionFacade.mostrarAdvertencia('Cliente duplicado.', 'Revisa la información')
    notificacionFacade.cerrar(id)
    expect(notificacionFacade.state.items).toHaveLength(0)
  })
})
