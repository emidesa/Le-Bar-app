<template>
  <div class="cart-page">

    <!-- Navigation -->
    <header class="nav">
      <div class="nav-brand">
        <div class="brand-icon">🍸</div>
        <div>
          <p class="brand-name">Bar'app</p>
          <p class="brand-tag">BAR À COCKTAILS</p>
        </div>
      </div>
      <nav class="nav-links">
        <RouterLink :to="`/menu?table=${tableNumber}`" class="nav-link">La carte</RouterLink>
        <RouterLink :to="`/orders?table=${tableNumber}`" class="nav-link">Mes commandes</RouterLink>
      </nav>
      <div class="step-indicator">ÉTAPE 1 / 3</div>
    </header>

    <!-- Panier vide -->
    <div v-if="cartStore.items.length === 0" class="empty-state">
      <p class="empty-title">Votre panier est vide</p>
      <RouterLink :to="`/menu?table=${tableNumber}`" class="back-link">← Retour à la carte</RouterLink>
    </div>

    <!-- Contenu panier -->
    <main v-else class="cart-main">

      <!-- Colonne gauche : items -->
      <section class="cart-items-col">
        <h1 class="cart-title">
          Votre panier
          <span class="cart-sub">· {{ cartStore.itemCount }} cocktail{{ cartStore.itemCount > 1 ? 's' : '' }}</span>
        </h1>

        <div class="items-list">
          <div
            v-for="item in cartStore.items"
            :key="`${item.cocktailId}-${item.sizeId}`"
            class="cart-item"
          >
            <div class="item-icon">🍸</div>
            <div class="item-info">
              <p class="item-name">{{ item.cocktailName }}</p>
              <p class="item-meta">Taille {{ item.sizeName }} · Table {{ tableNumber }}</p>
            </div>
            <div class="item-controls">
              <button class="ctrl-btn" @click="cartStore.updateQuantity(item.cocktailId, item.sizeId, item.quantity - 1)">−</button>
              <span class="ctrl-qty">{{ item.quantity }}</span>
              <button class="ctrl-btn" @click="cartStore.updateQuantity(item.cocktailId, item.sizeId, item.quantity + 1)">+</button>
            </div>
            <span class="item-price">{{ (item.price * item.quantity).toFixed(2) }}€</span>
            <button class="remove-btn" @click="cartStore.removeItem(item.cocktailId, item.sizeId)">🗑</button>
          </div>
        </div>
      </section>

      <!-- Colonne droite : récapitulatif -->
      <aside class="recap-col">
        <div class="recap-card">
          <h2 class="recap-title">Récapitulatif</h2>

          <div class="recap-row">
            <span>Sous-total</span>
            <span>{{ cartStore.total.toFixed(2) }}€</span>
          </div>
          <div class="recap-row muted">
            <span>{{ cartStore.itemCount }} cocktail{{ cartStore.itemCount > 1 ? 's' : '' }}</span>
            <span>Table {{ tableNumber }}</span>
          </div>

          <hr class="recap-divider" />

          <div class="recap-total">
            <span>Total</span>
            <span class="total-amount">{{ cartStore.total.toFixed(2) }}€</span>
          </div>

          <p v-if="error" class="error-msg">{{ error }}</p>

          <button class="order-btn" :disabled="loading" @click="placeOrder">
            {{ loading ? 'Envoi...' : 'Lancer la commande' }}
          </button>

          <p class="recap-note">Votre commande sera préparée et servie à votre table.</p>
        </div>
      </aside>

    </main>
  </div>
</template>

<script setup lang="ts">
import { ref } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { useCartStore } from '@/stores/cartStore'
import { useOrderStore } from '@/stores/orderStore'

const route      = useRoute()
const router     = useRouter()
const cartStore  = useCartStore()
const orderStore = useOrderStore()

const tableNumber = Number(route.query.table) || 1
const loading = ref(false)
const error   = ref('')

async function placeOrder() {
  loading.value = true
  error.value   = ''
  try {
    await orderStore.placeOrder(tableNumber)
    router.push(`/orders?table=${tableNumber}`)
  } catch {
    error.value = 'Erreur lors de la commande, réessaie.'
  } finally {
    loading.value = false
  }
}
</script>

<style scoped>
.cart-page {
  --c-bg:     #ede8df;
  --c-card:   #e0dbd0;
  --c-nav:    #ffffff;
  --c-text:   #2c2520;
  --c-muted:  #8a7f74;
  --c-accent: #8b3230;
  --c-border: #ccc8bc;

  min-height: 100vh;
  background: var(--c-bg);
  color: var(--c-text);
  font-family: 'Georgia', serif;
}

/* Nav */
.nav {
  display: flex; align-items: center; justify-content: space-between;
  padding: 0.75rem 2rem; background: var(--c-nav);
  border-bottom: 1px solid var(--c-border); position: sticky; top: 0; z-index: 20;
}
.nav-brand { display: flex; align-items: center; gap: 0.75rem; }
.brand-icon { font-size: 1.6rem; }
.brand-name { font-size: 1.1rem; font-weight: bold; color: var(--c-text); margin: 0; line-height: 1.2; }
.brand-tag  { font-size: 0.65rem; letter-spacing: 0.1em; color: var(--c-muted); margin: 0; font-family: sans-serif; }
.nav-links  { display: flex; gap: 2rem; }
.nav-link   { font-family: sans-serif; font-size: 0.9rem; color: var(--c-muted); text-decoration: none; }
.step-indicator {
  font-family: sans-serif; font-size: 0.72rem; letter-spacing: 0.1em;
  color: var(--c-muted);
}

/* Empty */
.empty-state { text-align: center; padding: 5rem 2rem; }
.empty-title { font-size: 1.4rem; color: var(--c-muted); margin-bottom: 1.5rem; font-family: sans-serif; }
.back-link { color: var(--c-accent); text-decoration: none; font-family: sans-serif; font-size: 0.9rem; }

/* Layout 2 cols */
.cart-main {
  display: grid;
  grid-template-columns: 1fr 360px;
  gap: 2rem;
  padding: 2.5rem 2rem;
  max-width: 1100px;
  margin: 0 auto;
}

/* Colonne items */
.cart-title {
  font-size: 1.6rem; font-weight: 700; color: var(--c-text);
  margin: 0 0 1.5rem;
}
.cart-sub { color: var(--c-muted); font-weight: 300; }

.items-list { display: flex; flex-direction: column; gap: 0; }
.cart-item {
  display: flex; align-items: center; gap: 1rem;
  background: white; border: 1px solid var(--c-border);
  padding: 1rem 1.25rem; border-bottom: none;
}
.cart-item:first-child { border-radius: 10px 10px 0 0; }
.cart-item:last-child  { border-radius: 0 0 10px 10px; border-bottom: 1px solid var(--c-border); }
.cart-item:only-child  { border-radius: 10px; border-bottom: 1px solid var(--c-border); }

.item-icon {
  width: 40px; height: 40px; border-radius: 50%;
  background: var(--c-card); display: flex; align-items: center;
  justify-content: center; font-size: 1.1rem; flex-shrink: 0;
}
.item-info { flex: 1; }
.item-name { font-weight: 600; font-size: 0.95rem; margin: 0; }
.item-meta { font-family: sans-serif; font-size: 0.78rem; color: var(--c-muted); margin: 0.2rem 0 0; }

.item-controls { display: flex; align-items: center; gap: 0.5rem; }
.ctrl-btn {
  width: 28px; height: 28px; border-radius: 50%;
  border: 1px solid var(--c-border); background: white;
  color: var(--c-text); font-size: 1rem; cursor: pointer;
  display: flex; align-items: center; justify-content: center;
}
.ctrl-qty { font-family: sans-serif; font-size: 0.9rem; font-weight: 600; min-width: 20px; text-align: center; }

.item-price { font-family: sans-serif; font-weight: 600; min-width: 50px; text-align: right; }
.remove-btn {
  background: none; border: none; cursor: pointer;
  color: var(--c-muted); font-size: 1rem; padding: 0.2rem;
}
.remove-btn:hover { color: var(--c-accent); }

/* Récapitulatif */
.recap-card {
  background: white; border: 1px solid var(--c-border);
  border-radius: 12px; padding: 1.5rem;
  position: sticky; top: 80px;
}
.recap-title { font-size: 1.1rem; font-weight: 700; margin: 0 0 1.25rem; }
.recap-row {
  display: flex; justify-content: space-between;
  font-family: sans-serif; font-size: 0.85rem; margin-bottom: 0.5rem;
}
.recap-row.muted { color: var(--c-muted); }
.recap-divider { border: none; border-top: 1px solid var(--c-border); margin: 1rem 0; }
.recap-total {
  display: flex; justify-content: space-between; align-items: baseline;
  font-family: sans-serif; font-weight: 600; margin-bottom: 1.25rem;
}
.total-amount { font-size: 1.4rem; font-weight: 700; color: var(--c-text); }

.order-btn {
  width: 100%; padding: 0.9rem;
  background: var(--c-accent); color: white;
  border: none; border-radius: 8px;
  font-family: sans-serif; font-size: 0.95rem; font-weight: 600;
  cursor: pointer; margin-bottom: 0.75rem;
}
.order-btn:disabled { opacity: 0.5; cursor: not-allowed; }
.order-btn:hover:not(:disabled) { opacity: 0.9; }

.recap-note { font-family: sans-serif; font-size: 0.75rem; color: var(--c-muted); text-align: center; margin: 0; }

.error-msg   { font-family: sans-serif; font-size: 0.82rem; color: var(--c-accent); margin-bottom: 0.75rem; }
.success-msg { font-family: sans-serif; font-size: 0.82rem; color: #27ae60; font-weight: 600; margin-bottom: 0.75rem; }
</style>
