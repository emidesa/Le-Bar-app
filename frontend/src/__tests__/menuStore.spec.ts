import { describe, it, expect, beforeEach, vi } from 'vitest'
import { setActivePinia, createPinia } from 'pinia'
import { useMenuStore } from '@/stores/menuStore'

vi.mock('@/services/api', () => ({
  default: {
    get: vi.fn(),
  },
}))

import api from '@/services/api'

describe('menuStore', () => {
  beforeEach(() => {
    setActivePinia(createPinia())
    vi.clearAllMocks()
  })

  it('état initial vide', () => {
    const menu = useMenuStore()
    expect(menu.categories).toHaveLength(0)
    expect(menu.cocktails).toHaveLength(0)
    expect(menu.selectedCategory).toBeNull()
    expect(menu.loading).toBe(false)
  })

  it('fetchCategories charge les catégories depuis l\'API', async () => {
    const menu = useMenuStore()
    vi.mocked(api.get).mockResolvedValueOnce({
      data: [
        { id: 1, name: 'Classiques', description: '' },
        { id: 2, name: 'Tropicaux', description: '' },
      ],
    })

    await menu.fetchCategories()

    expect(menu.categories).toHaveLength(2)
    expect(menu.categories[0].name).toBe('Classiques')
  })

  it('fetchCocktails sans filtre appelle /cocktails', async () => {
    const menu = useMenuStore()
    vi.mocked(api.get).mockResolvedValueOnce({
      data: [{ id: 1, name: 'Mojito' }],
    })

    await menu.fetchCocktails()

    expect(api.get).toHaveBeenCalledWith('/cocktails')
    expect(menu.cocktails).toHaveLength(1)
  })

  it('fetchCocktails avec catégorie appelle /cocktails/category/:id', async () => {
    const menu = useMenuStore()
    menu.selectedCategory = 3
    vi.mocked(api.get).mockResolvedValueOnce({
      data: [{ id: 2, name: 'Spritz' }],
    })

    await menu.fetchCocktails()

    expect(api.get).toHaveBeenCalledWith('/cocktails/category/3')
  })

  it('selectCategory met à jour selectedCategory et recharge les cocktails', async () => {
    const menu = useMenuStore()
    vi.mocked(api.get).mockResolvedValueOnce({ data: [] })

    menu.selectCategory(2)

    expect(menu.selectedCategory).toBe(2)
    expect(api.get).toHaveBeenCalledWith('/cocktails/category/2')
  })

  it('selectCategory avec null recharge tous les cocktails', async () => {
    const menu = useMenuStore()
    menu.selectedCategory = 1
    vi.mocked(api.get).mockResolvedValueOnce({ data: [] })

    menu.selectCategory(null)

    expect(menu.selectedCategory).toBeNull()
    expect(api.get).toHaveBeenCalledWith('/cocktails')
  })
})
