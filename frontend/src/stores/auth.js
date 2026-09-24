import { defineStore } from 'pinia'
import { api } from '../api/client'

export const useAuthStore = defineStore('auth', {
  state: () => ({ user: null, ready: false, loading: false }),
  getters: { authenticated: (state) => Boolean(state.user), permissions: (state) => state.user?.permissions || [], hasPermission: (state) => (permission) => state.user?.permissions?.includes(permission) },
  actions: {
    async bootstrap() { if (this.ready) return; try { const { data } = await api.get('/auth/me'); this.user = data } catch { this.user = null } finally { this.ready = true } },
    async login(payload) { this.loading = true; try { const { data } = await api.post('/auth/login', payload); this.user = data.user; this.ready = true; return data.user } finally { this.loading = false } },
    async logout() { try { await api.post('/auth/logout') } finally { this.user = null; this.ready = true } },
    async refresh() { const { data } = await api.get('/auth/me'); this.user = data; this.ready = true; return data },
  },
})
