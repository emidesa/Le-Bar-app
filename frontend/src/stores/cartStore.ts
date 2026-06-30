import { defineStore } from 'pinia'
import { ref, computed } from 'vue'

interface CartItem {
  cocktailId: number
  sizeId: number
  cocktailName: string
  sizeName: string
  price: number
  quantity: number
}

export const useCartStore = defineStore('cart', () => {
  const items = ref<CartItem[]>([])

  // Le total est recalculé automatiquement à chaque changement du panier
  const total = computed(() =>
    items.value.reduce((sum, item) => sum + item.price * item.quantity, 0)
  )

  const itemCount = computed(() =>
    items.value.reduce((sum, item) => sum + item.quantity, 0)
  )

  function addItem(item: CartItem) {
    // Si le même cocktail dans la même taille existe déjà, j'augmente juste la quantité
    const existing = items.value.find(
      i => i.cocktailId === item.cocktailId && i.sizeId === item.sizeId
    )
    if (existing) {
      existing.quantity++
    } else {
      items.value.push({ ...item, quantity: 1 })
    }
  }

  function removeItem(cocktailId: number, sizeId: number) {
    items.value = items.value.filter(
      i => !(i.cocktailId === cocktailId && i.sizeId === sizeId)
    )
  }

  function clearCart() {
    items.value = []
  }

  return { items, total, itemCount, addItem, removeItem, clearCart }
})