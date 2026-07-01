import axios from 'axios'

const api = axios.create({
  baseURL: '/api',
  headers: { 'Content-Type': 'application/json' }
})

// Avant chaque requête, ajoute le token JWT si disponible
api.interceptors.request.use(config => {
  try {
    const stored = localStorage.getItem('auth')
    const token = stored ? JSON.parse(stored).token : null
    if (token) config.headers.Authorization = `Bearer ${token}`
  } catch {}
  return config
})


export default api