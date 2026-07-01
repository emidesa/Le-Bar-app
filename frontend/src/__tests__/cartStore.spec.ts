import { describe, it, expect, beforeEach } from 'vitest'
import { setActivePinia, createPinia } from 'pinia'
import { useCartStore } from '@/stores/cartStore'

const mojito = {
  cocktailId: 1,
  sizeId: 10,
  cocktailName: 'Mojito',
  sizeName: 'M',
  price: 9.5,
  quantity: 1,
}

const spritz = {
  cocktailId: 2,
  sizeId: 20,
  cocktailName: 'Spritz',
  sizeName: 'S',
  price: 7,
  quantity: 2,
}

describe('cartStore', () => {
  beforeEach(() => {
    setActivePinia(createPinia())
  })

  it('panier vide au départ', () => {
    const cart = useCartStore()
    expect(cart.items).toHaveLength(0)
    expect(cart.total).toBe(0)
    expect(cart.itemCount).toBe(0)
  })

  it('addItem ajoute un cocktail au panier', () => {
    const cart = useCartStore()
    cart.addItem(mojito)
    expect(cart.items).toHaveLength(1)
    expect(cart.items[0].cocktailName).toBe('Mojito')
  })

  it('addItem incrémente la quantité si même cocktail + taille', () => {
    const cart = useCartStore()
    cart.addItem(mojito)
    cart.addItem({ ...mojito, quantity: 2 })
    expect(cart.items).toHaveLength(1)
    expect(cart.items[0].quantity).toBe(3)
  })

  it('addItem crée un item séparé pour une taille différente', () => {
    const cart = useCartStore()
    cart.addItem(mojito)
    cart.addItem({ ...mojito, sizeId: 11, sizeName: 'L', price: 12 })
    expect(cart.items).toHaveLength(2)
  })

  it('total est calculé correctement', () => {
    const cart = useCartStore()
    cart.addItem(mojito)   // 9.5 x 1
    cart.addItem(spritz)   // 7 x 2
    expect(cart.total).toBeCloseTo(23.5)
  })

  it('itemCount retourne le nombre total de cocktails', () => {
    const cart = useCartStore()
    cart.addItem(mojito)
    cart.addItem(spritz)
    expect(cart.itemCount).toBe(3) // 1 + 2
  })

  it('removeItem supprime le bon article', () => {
    const cart = useCartStore()
    cart.addItem(mojito)
    cart.addItem(spritz)
    cart.removeItem(1, 10)
    expect(cart.items).toHaveLength(1)
    expect(cart.items[0].cocktailName).toBe('Spritz')
  })

  it('updateQuantity met à jour la quantité', () => {
    const cart = useCartStore()
    cart.addItem(mojito)
    cart.updateQuantity(1, 10, 5)
    expect(cart.items[0].quantity).toBe(5)
  })

  it('updateQuantity avec qty <= 0 supprime l\'article', () => {
    const cart = useCartStore()
    cart.addItem(mojito)
    cart.updateQuantity(1, 10, 0)
    expect(cart.items).toHaveLength(0)
  })

  it('clearCart vide le panier', () => {
    const cart = useCartStore()
    cart.addItem(mojito)
    cart.addItem(spritz)
    cart.clearCart()
    expect(cart.items).toHaveLength(0)
    expect(cart.total).toBe(0)
  })
})
