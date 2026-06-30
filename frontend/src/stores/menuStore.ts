import { defineStore } from 'pinia'
import { ref } from 'vue'
import api from '@/services/api'

export const useMenuStore = defineStore('menu', () => {
  const categories = ref<any[]>([])
  const cocktails = ref<any[]>([])
  const selectedCategory = ref<number | null>(null)
  const loading = ref(false)

  async function fetchCategories() {
    const response = await api.get('/categories')
    categories.value = response.data
  }

  async function fetchCocktails() {
    loading.value = true
    // Si une catégorie est sélectionnée je filtre, sinon je prends tout
    const url = selectedCategory.value
      ? `/cocktails/category/${selectedCategory.value}`
      : '/cocktails'
    const response = await api.get(url)
    cocktails.value = response.data
    loading.value = false
  }

  function selectCategory(id: number | null) {
    selectedCategory.value = id
    fetchCocktails()
  }

  return { categories, cocktails, selectedCategory, loading, fetchCategories, fetchCocktails, selectCategory }
})