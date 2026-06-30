import axios from 'axios'

// Instance axios commune à toute l'application
const api = axios.create({
  baseURL: '/api',
  headers: { 'Content-Type': 'application/json' }
})

// Avant chaque requête, j'ajoute le token JWT du barmaker si il est connecté
api.interceptors.request.use(config => {
  const token = localStorage.getItem('token')
  if (token) {
    config.headers.Authorization = `Bearer ${token}`
  }
  return config
})

export default api