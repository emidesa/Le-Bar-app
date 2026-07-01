import { defineStore } from 'pinia'
import { ref } from 'vue'
import api from '@/services/api'
import { useCartStore } from './cartStore'

export const useOrderStore = defineStore('order', () => {
  const orders = ref<any[]>([])
  const currentOrder = ref<any>(null)
  const cartStore = useCartStore()

  // Le client passe sa commande avec son numéro de table (venu du QR code)
  async function placeOrder(tableNumber: number) {
    const payload = {
      tableNumber,
      items: cartStore.items.map(i => ({
        cocktailId: i.cocktailId,
        sizeId: i.sizeId,
        quantity: i.quantity
      }))
    }
    await api.post('/orders', payload)
    cartStore.clearCart()
  }

  // Le client suit ses commandes via son numéro de table
  async function fetchOrdersByTable(tableNumber: number) {
    const response = await api.get(`/orders/table/${tableNumber}`)
    orders.value = response.data
  }

  // Le barmaker voit toutes les commandes en cours
  async function fetchPendingOrders() {
    const response = await api.get('/orders')
    orders.value = response.data
  }

  async function fetchOrderById(id: number) {
    const response = await api.get(`/orders/${id}`)
    currentOrder.value = response.data
  }

  // Le barmaker fait avancer un cocktail à l'étape suivante
  async function advanceItem(itemId: number) {
    const response = await api.patch(`/orders/items/${itemId}/next-step`)
    return response.data
  }

  // Suppression d'une commande terminée (côté client ou barmaker)
  async function deleteOrder(orderId: number) {
    await api.delete(`/orders/${orderId}`)
    orders.value = orders.value.filter(o => o.id !== orderId)
  }

  return { orders, currentOrder, placeOrder, fetchOrdersByTable, fetchPendingOrders, fetchOrderById, advanceItem, deleteOrder }
})