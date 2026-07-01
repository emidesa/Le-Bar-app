import { defineStore } from 'pinia'
import { ref, computed } from 'vue'
import api from '@/services/api'

export const useAuthStore = defineStore('auth', () => {
  const token = ref<string | null>(null)
  const email = ref<string | null>(null)

  const isLoggedIn = computed(() => !!token.value)

  async function login(emailInput: string, password: string) {
    const response = await api.post('/auth/login', { email: emailInput, password })
    token.value = response.data.token
    email.value = response.data.email
  }

  async function register(emailInput: string, password: string) {
    const response = await api.post('/auth/register', { email: emailInput, password })
    token.value = response.data.token
    email.value = response.data.email
  }

  function logout() {
    token.value = null
    email.value = null
  }

  return { token, email, isLoggedIn, login, register, logout }
}, {
  persist: true  // token et email survivent au rechargement de page
})
