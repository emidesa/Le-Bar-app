import { createRouter, createWebHistory } from 'vue-router'
import { useAuthStore } from '@/stores/authStore'

const router = createRouter({
  history: createWebHistory(import.meta.env.BASE_URL),
  routes: [
    // ── Routes CLIENT (accès via QR code, pas de login) ──
    {
      path: '/menu',
      name: 'menu',
      component: () => import('@/views/client/MenuView.vue')
    },
    {
      path: '/cart',
      name: 'cart',
      component: () => import('@/views/client/CartView.vue')
    },
    {
      path: '/orders',
      name: 'orders',
      component: () => import('@/views/client/OrderTrackingView.vue')
    },

    // ── Routes BARMAKER (login requis) ──
    {
      path: '/barmaker/login',
      name: 'barmaker-login',
      component: () => import('@/views/barmaker/LoginView.vue')
    },
    {
      path: '/barmaker/register',
      name: 'barmaker-register',
      component: () => import('@/views/barmaker/RegisterView.vue')
    },
    {
      path: '/barmaker/orders',
      name: 'barmaker-orders',
      component: () => import('@/views/barmaker/OrdersDashboardView.vue'),
      meta: { requiresAuth: true }
    },
    {
      path: '/barmaker/cocktails',
      name: 'barmaker-cocktails',
      component: () => import('@/views/barmaker/CocktailsManagementView.vue'),
      meta: { requiresAuth: true }
    },

    {
      path: '/cocktail/:id',
      name: 'cocktail-detail',
      component: () => import('@/views/client/CocktailsDetailView.vue')
    },
    {
      path: '/barmaker/orders/:id',
      name: 'barmaker-order-prep',
      component: () => import('@/views/barmaker/OrdersPreparationView.vue'),
      meta: { requiresAuth: true }
    },

    // Redirige la racine vers le menu client
    { path: '/', redirect: '/menu' }
  ]
})

// Guard : le store est persisté via pinia-plugin-persistedstate → token disponible dès le rechargement
router.beforeEach((to) => {
  const auth = useAuthStore()
  if (to.meta.requiresAuth && !auth.isLoggedIn) {
    return { name: 'barmaker-login' }
  }
})

export default router