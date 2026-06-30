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
      existing.quantity += item.quantity
    } else {
      items.value.push({ ...item })
    }
  }

  function removeItem(cocktailId: number, sizeId: number) {
    items.value = items.value.filter(
      i => !(i.cocktailId === cocktailId && i.sizeId === sizeId)
    )
  }

  function updateQuantity(cocktailId: number, sizeId: number, qty: number) {
    if (qty <= 0) { removeItem(cocktailId, sizeId); return }
    const item = items.value.find(i => i.cocktailId === cocktailId && i.sizeId === sizeId)
    if (item) item.quantity = qty
  }

  function clearCart() {
    items.value = []
  }

  return { items, total, itemCount, addItem, removeItem, updateQuantity, clearCart }
})