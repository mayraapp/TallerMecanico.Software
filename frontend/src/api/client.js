import axios from 'axios'

/**
 * Shared HTTP client for the protected API.
 *
 * <p>It deliberately does not force a {@code Content-Type}: Axios supplies JSON for object
 * payloads and the required boundary for {@link FormData} client-photo uploads.</p>
 */
export const api = axios.create({ baseURL: import.meta.env.VITE_API_URL || 'http://localhost:8080/api', withCredentials: true })

/**
 * Obtains the safe user-facing message supplied by the API.
 *
 * @param {import('axios').AxiosError} error failed HTTP operation
 * @returns {string} controlled server message or a generic fallback
 */
export const apiError = (error) => error?.response?.data?.message || 'No fue posible completar la solicitud.'
