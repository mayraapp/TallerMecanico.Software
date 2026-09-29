import { api } from '../api/client'

/**
 * Centralizes protected internal-user HTTP use cases for administrative views.
 * Views receive DTOs only and never access repositories or persistence concerns.
 */
export const usuarioFacade = {
  /** Loads roles available for protected internal registration. @returns {Promise<Array>} safe role DTOs. */
  async cargarRoles() { const { data } = await api.get('/roles'); return data },

  /** Loads a paged account list. @param {object} params protected filters and page values. @returns {Promise<object>} user page DTO. */
  async listarUsuarios(params) { const { data } = await api.get('/users', { params }); return data },

  /** Creates an internal account; the backend hashes the temporary password with BCrypt. @param {object} datos validated account DTO. @returns {Promise<object>} safe created user DTO. */
  async registrarUsuario(datos) { const { data } = await api.post('/users', datos); return data },

  /** Updates editable account fields. @param {number} id account identifier. @param {object} datos safe update DTO. @returns {Promise<object>} updated user DTO. */
  async actualizarUsuario(id, datos) { const { data } = await api.put(`/users/${id}`, datos); return data },

  /** Assigns an allowed role. @param {number} id account identifier. @param {{roleCode: string}} datos role DTO. @returns {Promise<object>} updated user DTO. */
  async cambiarRol(id, datos) { const { data } = await api.patch(`/users/${id}/role`, datos); return data },

  /** Changes only active/inactive status. @param {number} id account identifier. @param {{status: string}} datos status DTO. @returns {Promise<object>} updated user DTO. */
  async cambiarEstado(id, datos) { const { data } = await api.patch(`/users/${id}/status`, datos); return data },

  /** Unlocks a blocked account. @param {number} id account identifier. @returns {Promise<object>} unlocked user DTO. */
  async desbloquearUsuario(id) { const { data } = await api.post(`/users/${id}/unlock`); return data },
}
