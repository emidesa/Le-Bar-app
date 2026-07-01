import { describe, it, expect, beforeEach, vi } from 'vitest'
import { setActivePinia, createPinia } from 'pinia'
import { useAuthStore } from '@/stores/authStore'

vi.mock('@/services/api', () => ({
  default: {
    post: vi.fn(),
  },
}))

import api from '@/services/api'

describe('authStore', () => {
  beforeEach(() => {
    setActivePinia(createPinia())
    vi.clearAllMocks()
  })

  it('isLoggedIn est false au départ', () => {
    const auth = useAuthStore()
    expect(auth.isLoggedIn).toBe(false)
    expect(auth.token).toBeNull()
  })

  it('login met à jour token et email', async () => {
    const auth = useAuthStore()
    vi.mocked(api.post).mockResolvedValueOnce({
      data: { token: 'mon-token-jwt', email: 'bar@barapp.fr' },
    })

    await auth.login('bar@barapp.fr', 'password123')

    expect(auth.token).toBe('mon-token-jwt')
    expect(auth.email).toBe('bar@barapp.fr')
    expect(auth.isLoggedIn).toBe(true)
  })

  it('register met à jour token et email', async () => {
    const auth = useAuthStore()
    vi.mocked(api.post).mockResolvedValueOnce({
      data: { token: 'nouveau-token', email: 'nouveau@barapp.fr' },
    })

    await auth.register('nouveau@barapp.fr', 'password123')

    expect(auth.token).toBe('nouveau-token')
    expect(auth.isLoggedIn).toBe(true)
  })

  it('logout remet token et email à null', async () => {
    const auth = useAuthStore()
    vi.mocked(api.post).mockResolvedValueOnce({
      data: { token: 'mon-token-jwt', email: 'bar@barapp.fr' },
    })
    await auth.login('bar@barapp.fr', 'password123')

    auth.logout()

    expect(auth.token).toBeNull()
    expect(auth.email).toBeNull()
    expect(auth.isLoggedIn).toBe(false)
  })
})
