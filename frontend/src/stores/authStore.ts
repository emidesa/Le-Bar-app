import { defineStore } from 'pinia'
import { ref, computed } from 'vue'
import api from '@/services/api'

export const useAuthStore = defineStore('auth', () => {
  // Je récupère le token et l'email depuis le localStorage au chargement
  const token = ref<string | null>(localStorage.getItem('token'))
  const email = ref<string | null>(localStorage.getItem('email'))

  // isLoggedIn est recalculé automatiquement quand token change
  const isLoggedIn = computed(() => !!token.value)

  async function login(emailInput: string, password: string) {
    const response = await api.post('/auth/login', { email: emailInput, password })
    token.value = response.data.token
    email.value = response.data.email
    // Je persiste le token pour ne pas être déconnecté au refresh
    localStorage.setItem('token', response.data.token)
    localStorage.setItem('email', response.data.email)
  }

  async function register(emailInput: string, password: string) {
    const response = await api.post('/auth/register', { email: emailInput, password })
    token.value = response.data.token
    email.value = response.data.email
    localStorage.setItem('token', response.data.token)
    localStorage.setItem('email', response.data.email)
  }

  function logout() {
    token.value = null
    email.value = null
    localStorage.removeItem('token')
    localStorage.removeItem('email')
  }

  return { token, email, isLoggedIn, login, register, logout }
})