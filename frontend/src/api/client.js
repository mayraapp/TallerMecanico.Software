import axios from 'axios'

export const api = axios.create({ baseURL: import.meta.env.VITE_API_URL || 'http://localhost:8080/api', withCredentials: true, headers: { 'Content-Type': 'application/json' } })
export const apiError = (error) => error?.response?.data?.message || 'No fue posible completar la solicitud.'
