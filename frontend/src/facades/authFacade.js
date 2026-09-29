import { ref } from 'vue'
import { api } from '../api/client'
import { useAuthStore } from '../stores/auth'

/**
 * Provides the login use case to views without exposing the HTTP client or Pinia mutations there.
 *
 * @returns {{ isSubmitting: import('vue').Ref<boolean>, iniciarSesion: (payload: {email: string, password: string}) => Promise<object> }} login state and operation
 */
export function useAuthFacade() {
  const auth = useAuthStore()
  const isSubmitting = ref(false)

  /**
   * Authenticates credentials through the API and updates the session state only on success.
   *
   * @param {{email: string, password: string}} payload credentials validated by the view and validated again by Spring Boot
   * @returns {Promise<object>} safe authenticated-user DTO
   * @throws {import('axios').AxiosError} when authentication, account state or connectivity fails
   */
  async function iniciarSesion(payload) {
    isSubmitting.value = true
    try {
      const { data } = await api.post('/auth/login', payload)
      auth.establecerSesion(data.user)
      return data.user
    } finally {
      isSubmitting.value = false
    }
  }

  return { isSubmitting, iniciarSesion }
}
