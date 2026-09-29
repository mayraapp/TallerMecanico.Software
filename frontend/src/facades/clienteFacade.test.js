/* @vitest-environment jsdom */
import { afterEach, describe, expect, it, vi } from 'vitest'
import { useClienteFacade } from './clienteFacade'

/** Builds a file-like browser object without persisting test images. */
function archivo(bytes, type, reportedSize = bytes.length) {
  const content = new Uint8Array(bytes)
  return { type, size: reportedSize, slice: () => ({ arrayBuffer: async () => content.buffer }) }
}

describe('clienteFacade fotografía', () => {
  afterEach(() => vi.unstubAllGlobals())

  it('acepta una imagen PNG de exactamente 15 MB tras validar MIME y firma', async () => {
    vi.stubGlobal('URL', { createObjectURL: vi.fn(() => 'blob:foto'), revokeObjectURL: vi.fn() })
    const bytes = new Uint8Array(15 * 1024 * 1024)
    bytes.set([0x89, 0x50, 0x4E, 0x47, 0x0D, 0x0A, 0x1A, 0x0A])
    const facade = useClienteFacade()

    await expect(facade.seleccionarFotografia(archivo(bytes, 'image/png'))).resolves.toBe(true)
    expect(facade.vistaPrevia.value).toBe('blob:foto')
  })

  it('rechaza tipos falsos, archivos vacíos y tamaños mayores de 15 MB', async () => {
    const facade = useClienteFacade()
    await expect(facade.seleccionarFotografia(archivo([0x89, 0x50, 0x4E, 0x47], 'text/plain'))).resolves.toBe(false)
    await expect(facade.seleccionarFotografia(archivo([], 'image/png'))).resolves.toBe(false)
    await expect(facade.seleccionarFotografia(archivo([0x89, 0x50, 0x4E, 0x47], 'image/png', 15 * 1024 * 1024 + 1))).resolves.toBe(false)
    expect(facade.errors.fotografia).toBe('La fotografía debe ser JPEG, PNG o WebP y no puede superar los 15 MB.')
  })
})
