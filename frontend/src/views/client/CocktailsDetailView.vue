<template>
  <div class="detail-page">

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
      <RouterLink :to="`/cart?table=${tableNumber}`" class="cart-wrap">
        <span class="cart-icon">🛒</span>
        <span v-if="cartStore.itemCount > 0" class="cart-badge">{{ cartStore.itemCount }}</span>
      </RouterLink>
    </header>

    <div v-if="loading" class="state-msg">Chargement...</div>

    <main v-else-if="cocktail" class="detail-main">
      <!-- Image gauche -->
      <div class="detail-img-wrap">
        <div class="img-badges">
          <span v-if="cocktail.category?.name" class="img-badge">{{ cocktail.category.name.toUpperCase() }}</span>
        </div>
        <img v-if="cocktail.imageUrl" :src="cocktail.imageUrl" :alt="cocktail.name" class="detail-img" />
        <div v-else class="detail-img-placeholder">🍸</div>
      </div>

      <!-- Infos droite -->
      <div class="detail-info">
        <p class="cat-eyebrow">{{ cocktail.category?.name?.toUpperCase() }}</p>
        <h1 class="cocktail-title">{{ cocktail.name }}</h1>
        <p class="cocktail-desc">{{ cocktail.description }}</p>

        <!-- Ingrédients -->
        <div class="ings-row" v-if="cocktail.ingredients?.length">
          <span
            v-for="ing in cocktail.ingredients"
            :key="ing.ingredientName"
            class="ing-chip"
          >{{ ing.ingredientName }}</span>
        </div>

        <!-- Taille -->
        <p class="field-label">CHOISISSEZ LA TAILLE</p>
        <div class="size-cards">
          <button
            v-for="size in cocktail.sizes"
            :key="size.id"
            :class="['size-card', { selected: selectedSize?.id === size.id }]"
            @click="selectedSize = size"
          >
            <span class="size-letter">{{ size.size }}</span>
            <span class="size-price">{{ size.price }}€</span>
          </button>
        </div>

        <!-- Quantité -->
        <p class="field-label">QUANTITÉ</p>
        <div class="qty-row">
          <button class="qty-btn" @click="qty = Math.max(1, qty - 1)">−</button>
          <span class="qty-val">{{ qty }}</span>
          <button class="qty-btn" @click="qty++">+</button>
        </div>

        <!-- CTA -->
        <button
          class="cta-btn"
          :disabled="!selectedSize"
          @click="addToCart"
        >
          Ajouter au panier{{ selectedSize ? ' · ' + (selectedSize.price * qty).toFixed(2) + '€' : '' }}
        </button>
      </div>
    </main>

  </div>
</template>

<script setup lang="ts">
import { ref, onMounted } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { useCartStore } from '@/stores/cartStore'
import api from '@/services/api'

const route  = useRoute()
const router = useRouter()
const cartStore = useCartStore()

const tableNumber = Number(route.query.table) || 1
const cocktailId  = Number(route.params.id)

const cocktail    = ref<any>(null)
const loading     = ref(true)
const selectedSize = ref<any>(null)
const qty         = ref(1)

onMounted(async () => {
  const { data } = await api.get(`/cocktails/${cocktailId}`)
  cocktail.value = data
  if (data.sizes?.length) selectedSize.value = data.sizes[0]
  loading.value = false
})

function addToCart() {
  if (!selectedSize.value || !cocktail.value) return
  cartStore.addItem({
    cocktailId:   cocktail.value.id,
    sizeId:       selectedSize.value.id,
    cocktailName: cocktail.value.name,
    sizeName:     selectedSize.value.size,
    price:        selectedSize.value.price,
    quantity:     qty.value
  })
  router.push(`/menu?table=${tableNumber}`)
}
</script>

<style scoped>
.detail-page {
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

/* Nav (identique MenuView) */
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
.nav-link:hover { color: var(--c-text); }
.cart-wrap  { position: relative; text-decoration: none; color: var(--c-text); font-size: 1.4rem; }
.cart-badge {
  position: absolute; top: -6px; right: -8px;
  background: var(--c-accent); color: white; border-radius: 50%;
  font-size: 0.7rem; font-family: sans-serif; min-width: 18px; height: 18px;
  display: flex; align-items: center; justify-content: center;
}

.state-msg { text-align: center; padding: 4rem; color: var(--c-muted); font-family: sans-serif; }

/* Layout 2 colonnes */
.detail-main {
  display: grid;
  grid-template-columns: 1fr 1fr;
  min-height: calc(100vh - 57px);
}

/* Colonne image */
.detail-img-wrap {
  position: relative;
  background: var(--c-card);
}
.img-badges {
  position: absolute; top: 1.5rem; left: 1.5rem;
  display: flex; gap: 0.5rem; z-index: 2;
}
.img-badge {
  background: rgba(255,255,255,0.9); border: 1px solid var(--c-border);
  font-family: sans-serif; font-size: 0.65rem; letter-spacing: 0.1em;
  font-weight: 700; padding: 0.3rem 0.7rem; border-radius: 4px; color: var(--c-text);
}
.detail-img {
  width: 100%; height: 100%; object-fit: cover; display: block;
}
.detail-img-placeholder {
  display: flex; align-items: center; justify-content: center;
  height: 100%; font-size: 5rem;
}

/* Colonne infos */
.detail-info {
  padding: 3rem 2.5rem;
  display: flex; flex-direction: column; gap: 0;
}
.cat-eyebrow {
  font-family: sans-serif; font-size: 0.72rem; letter-spacing: 0.12em;
  color: var(--c-accent); text-transform: uppercase; margin: 0 0 0.5rem;
}
.cocktail-title {
  font-size: 2.2rem; font-weight: 700; color: var(--c-text);
  margin: 0 0 1rem; line-height: 1.15;
}
.cocktail-desc {
  font-family: sans-serif; font-size: 0.9rem; color: var(--c-muted);
  line-height: 1.6; margin: 0 0 1.5rem;
}

/* Ingrédients */
.ings-row { display: flex; flex-wrap: wrap; gap: 0.4rem; margin-bottom: 1.75rem; }
.ing-chip {
  background: white; border: 1px solid var(--c-border);
  font-family: sans-serif; font-size: 0.78rem; color: var(--c-text);
  padding: 0.3rem 0.75rem; border-radius: 20px;
}

/* Labels de section */
.field-label {
  font-family: sans-serif; font-size: 0.68rem; letter-spacing: 0.12em;
  color: var(--c-muted); text-transform: uppercase; margin: 0 0 0.6rem;
}

/* Cartes de taille */
.size-cards { display: flex; gap: 0.75rem; margin-bottom: 1.75rem; }
.size-card {
  flex: 1; padding: 0.85rem 0.5rem;
  background: white; border: 1px solid var(--c-border);
  border-radius: 10px; cursor: pointer; text-align: center;
  display: flex; flex-direction: column; gap: 0.3rem;
  transition: border-color 0.15s;
}
.size-card.selected { border-color: var(--c-accent); border-width: 2px; background: #fdf4f4; }
.size-letter { font-size: 1.2rem; font-weight: 700; color: var(--c-text); }
.size-price  { font-family: sans-serif; font-size: 0.85rem; color: var(--c-muted); }

/* Quantité */
.qty-row { display: flex; align-items: center; gap: 1rem; margin-bottom: 2rem; }
.qty-btn {
  width: 32px; height: 32px; border-radius: 50%;
  border: 1px solid var(--c-border); background: white;
  color: var(--c-text); font-size: 1.1rem; cursor: pointer;
  display: flex; align-items: center; justify-content: center;
}
.qty-val { font-size: 1.1rem; font-weight: 600; min-width: 24px; text-align: center; }

/* CTA */
.cta-btn {
  width: 100%; padding: 1rem;
  background: var(--c-accent); color: white;
  border: none; border-radius: 8px;
  font-family: sans-serif; font-size: 1rem; font-weight: 600;
  cursor: pointer; margin-top: auto;
}
.cta-btn:disabled { opacity: 0.5; cursor: not-allowed; }
.cta-btn:hover:not(:disabled) { opacity: 0.9; }
</style>
