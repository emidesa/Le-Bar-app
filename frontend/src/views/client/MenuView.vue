<template>
  <div class="menu-page">

    <!-- Pétales de sakura -->
    <div class="sakura-overlay" aria-hidden="true">
      <div
        v-for="petal in petals"
        :key="petal.id"
        class="petal"
        :style="{
          left: petal.left + '%',
          animationDelay: petal.delay + 's',
          animationDuration: petal.duration + 's',
          width: petal.size + 'px',
          height: petal.size * 1.3 + 'px',
          '--drift': petal.drift + 'px',
          '--spin': petal.spin + 'deg',
        }"
      ></div>
    </div>

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
        <span class="nav-link active">La carte</span>
        <RouterLink :to="`/orders?table=${tableNumber}`" class="nav-link">Mes commandes</RouterLink>
      </nav>
      <RouterLink :to="`/cart?table=${tableNumber}`" class="cart-wrap">
        <span class="cart-icon">🛒</span>
        <span v-if="cartStore.itemCount > 0" class="cart-badge">{{ cartStore.itemCount }}</span>
      </RouterLink>
    </header>

    <!-- Hero -->
    <div class="hero">
      <p class="hero-eyebrow">BAR À COCKTAILS · TABLE {{ tableNumber }}</p>
      <h1 class="hero-title">Notre carte <em>du soir</em></h1>
    </div>

    <!-- Recherche + filtres -->
    <div class="controls">
      <label class="search-wrap">
        <span class="search-icon">🔍</span>
        <input
          v-model="search"
          class="search-input"
          placeholder="Rechercher un cocktail, un ingrédient..."
        />
      </label>
      <div class="filter-row">
        <button
          :class="['pill', { active: menuStore.selectedCategory === null }]"
          @click="menuStore.selectCategory(null)"
        >Tout</button>
        <button
          v-for="cat in menuStore.categories"
          :key="cat.id"
          :class="['pill', { active: menuStore.selectedCategory === cat.id }]"
          @click="menuStore.selectCategory(cat.id)"
        >{{ cat.name }}</button>
      </div>
    </div>

    <!-- État chargement / vide -->
    <div v-if="menuStore.loading" class="state-msg">Chargement de la carte...</div>
    <div v-else-if="groupedCocktails.length === 0" class="state-msg">Aucun résultat pour cette recherche.</div>

    <!-- Sections par catégorie -->
    <main v-else class="menu-body">
      <section
        v-for="group in groupedCocktails"
        :key="group.category.id"
        class="cat-section"
      >
        <div class="cat-head">
          <h2 class="cat-title">{{ group.category.name }}</h2>
          <span class="cat-count">{{ group.cocktails.length }} cocktail{{ group.cocktails.length > 1 ? 's' : '' }}</span>
        </div>

        <div class="cards-grid">
          <article
            v-for="cocktail in group.cocktails"
            :key="cocktail.id"
            class="cocktail-card"
          >
            <div class="card-img">
              <span class="card-label">{{ cocktail.category?.name?.toUpperCase() }}</span>
              <img v-if="cocktail.imageUrl" :src="cocktail.imageUrl" :alt="cocktail.name" />
              <div v-else class="card-placeholder">🍸</div>
            </div>
            <div class="card-body">
              <h3 class="card-name">{{ cocktail.name }}</h3>
              <p class="card-desc">{{ cocktail.description?.slice(0, 90) }}</p>
              <div class="card-foot">
                <span class="card-price">dès {{ minPrice(cocktail.sizes) }}€</span>
                <RouterLink
                  :to="`/cocktail/${cocktail.id}?table=${tableNumber}`"
                  class="btn-add"
                >+</RouterLink>
              </div>
            </div>
          </article>
        </div>
      </section>
    </main>

  </div>
</template>

<script setup lang="ts">
import { computed, onMounted, ref } from 'vue'
import { useRoute } from 'vue-router'
import { useMenuStore } from '@/stores/menuStore'
import { useCartStore } from '@/stores/cartStore'

const route = useRoute()
const menuStore = useMenuStore()
const cartStore = useCartStore()

const tableNumber = ref(Number(route.query.table) || 1)
const search = ref('')

const petals = ref<{ id: number; left: number; delay: number; duration: number; size: number; drift: number; spin: number }[]>([])

onMounted(async () => {
  petals.value = Array.from({ length: 22 }, (_, i) => ({
    id: i,
    left:     Math.random() * 100,
    delay:    Math.random() * 12,
    duration: 7 + Math.random() * 7,
    size:     8 + Math.random() * 10,
    drift:    -50 + Math.random() * 100,
    spin:     Math.random() * 360,
  }))
  await menuStore.fetchCategories()
  await menuStore.fetchCocktails()
})

const groupedCocktails = computed(() => {
  const term = search.value.toLowerCase()
  const filtered = menuStore.cocktails.filter(c =>
    !term ||
    c.name.toLowerCase().includes(term) ||
    (c.description ?? '').toLowerCase().includes(term)
  )

  if (menuStore.selectedCategory !== null) {
    const cat = menuStore.categories.find(c => c.id === menuStore.selectedCategory)
    if (!cat) return []
    return [{ category: cat, cocktails: filtered }]
  }

  return menuStore.categories
    .map(cat => ({
      category: cat,
      cocktails: filtered.filter(c => (c as any).category?.id === cat.id)
    }))
    .filter(g => g.cocktails.length > 0)
})

function minPrice(sizes: any[]) {
  if (!sizes?.length) return '?'
  const min = Math.min(...sizes.map(s => Number(s.price)))
  return Number.isInteger(min) ? min : min.toFixed(2)
}


</script>

<style scoped>
/* ── Variables locales thème clair ── */
.menu-page {
  --c-bg:      #ede8df;
  --c-card:    #e0dbd0;
  --c-nav:     #ffffff;
  --c-text:    #2c2520;
  --c-muted:   #8a7f74;
  --c-accent:  #8b3230;
  --c-border:  #ccc8bc;

  min-height: 100vh;
  background: var(--c-bg);
  color: var(--c-text);
  font-family: 'Georgia', serif;
}

/* ── Navigation ── */
.nav {
  display: flex;
  align-items: center;
  justify-content: space-between;
  padding: 0.75rem 2rem;
  background: var(--c-nav);
  border-bottom: 1px solid var(--c-border);
  position: sticky;
  top: 0;
  z-index: 20;
}

.nav-brand { display: flex; align-items: center; gap: 0.75rem; }
.brand-icon { font-size: 1.6rem; }
.brand-name { font-size: 1.1rem; font-weight: bold; color: var(--c-text); margin: 0; line-height: 1.2; }
.brand-tag  { font-size: 0.65rem; letter-spacing: 0.1em; color: var(--c-muted); margin: 0; font-family: sans-serif; }

.nav-links { display: flex; gap: 2rem; }
.nav-link {
  font-family: sans-serif;
  font-size: 0.9rem;
  color: var(--c-muted);
  text-decoration: none;
  padding-bottom: 2px;
  cursor: pointer;
}
.nav-link.active {
  color: var(--c-text);
  border-bottom: 2px solid var(--c-text);
  font-weight: 600;
}

.cart-wrap {
  position: relative;
  display: flex;
  align-items: center;
  text-decoration: none;
  color: var(--c-text);
}
.cart-icon { font-size: 1.4rem; }
.cart-badge {
  position: absolute;
  top: -6px;
  right: -8px;
  background: var(--c-accent);
  color: white;
  border-radius: 50%;
  font-size: 0.7rem;
  font-family: sans-serif;
  min-width: 18px;
  height: 18px;
  display: flex;
  align-items: center;
  justify-content: center;
}

/* ── Hero ── */
.hero {
  padding: 2.5rem 2rem 1rem;
}
.hero-eyebrow {
  font-family: sans-serif;
  font-size: 0.75rem;
  letter-spacing: 0.12em;
  color: var(--c-muted);
  margin: 0 0 0.5rem;
  text-transform: uppercase;
}
.hero-title {
  font-size: 2.4rem;
  font-weight: 700;
  color: var(--c-text);
  margin: 0;
  line-height: 1.2;
}
.hero-title em {
  font-style: italic;
  font-weight: 300;
  color: var(--c-muted);
}

/* ── Recherche + filtres ── */
.controls { padding: 1rem 2rem 0.5rem; }

.search-wrap {
  display: flex;
  align-items: center;
  gap: 0.75rem;
  background: white;
  border: 1px solid var(--c-border);
  border-radius: 8px;
  padding: 0.65rem 1rem;
  margin-bottom: 1rem;
  cursor: text;
}
.search-icon { font-size: 0.9rem; color: var(--c-muted); }
.search-input {
  border: none;
  background: none;
  font-size: 0.95rem;
  color: var(--c-text);
  font-family: sans-serif;
  width: 100%;
  outline: none;
  padding: 0;
}
.search-input::placeholder { color: var(--c-muted); }

.filter-row { display: flex; gap: 0.5rem; flex-wrap: wrap; }
.pill {
  background: white;
  border: 1px solid var(--c-border);
  color: var(--c-text);
  padding: 0.4rem 1.1rem;
  border-radius: 20px;
  font-family: sans-serif;
  font-size: 0.85rem;
  cursor: pointer;
  transition: all 0.15s;
}
.pill.active {
  background: var(--c-text);
  color: white;
  border-color: var(--c-text);
  font-weight: 600;
}
.pill:not(.active):hover { background: var(--c-card); }

/* ── Corps du menu ── */
.menu-body { padding: 1.5rem 2rem 3rem; }

.state-msg {
  text-align: center;
  padding: 4rem;
  color: var(--c-muted);
  font-family: sans-serif;
}

/* ── Section par catégorie ── */
.cat-section { margin-bottom: 3rem; }

.cat-head {
  display: flex;
  align-items: baseline;
  gap: 1.5rem;
  margin-bottom: 1.25rem;
  padding-bottom: 0.75rem;
  border-bottom: 1px solid var(--c-border);
}
.cat-title {
  font-size: 1.4rem;
  font-weight: 700;
  color: var(--c-text);
  margin: 0;
}
.cat-count {
  font-family: sans-serif;
  font-size: 0.8rem;
  color: var(--c-muted);
  margin-left: auto;
}

/* ── Grille de cartes ── */
.cards-grid {
  display: grid;
  grid-template-columns: repeat(auto-fill, minmax(240px, 1fr));
  gap: 1.25rem;
}

/* ── Carte cocktail ── */
.cocktail-card {
  background: var(--c-card);
  border-radius: 12px;
  overflow: hidden;
  display: flex;
  flex-direction: column;
}

.card-img {
  position: relative;
  height: 180px;
  background: #d5d0c4;
  overflow: hidden;
}
.card-img img {
  width: 100%;
  height: 100%;
  object-fit: cover;
  display: block;
}
.card-placeholder {
  display: flex;
  align-items: center;
  justify-content: center;
  height: 100%;
  font-size: 3rem;
}
.card-label {
  position: absolute;
  top: 0.6rem;
  left: 0.6rem;
  background: rgba(255,255,255,0.85);
  border: 1px solid var(--c-border);
  font-family: sans-serif;
  font-size: 0.62rem;
  letter-spacing: 0.08em;
  padding: 0.2rem 0.5rem;
  border-radius: 4px;
  color: var(--c-text);
  font-weight: 600;
}

.card-body {
  padding: 1rem;
  display: flex;
  flex-direction: column;
  flex: 1;
}
.card-name {
  font-size: 1.05rem;
  font-weight: 700;
  color: var(--c-text);
  margin: 0 0 0.35rem;
}
.card-desc {
  font-family: sans-serif;
  font-size: 0.8rem;
  color: var(--c-muted);
  margin: 0 0 1rem;
  line-height: 1.4;
  flex: 1;
}

.card-foot {
  display: flex;
  align-items: center;
  gap: 0.5rem;
  margin-top: auto;
}
.card-price {
  font-family: sans-serif;
  font-size: 0.9rem;
  color: var(--c-text);
  font-weight: 600;
  flex: 1;
}
.btn-add {
  width: 36px;
  height: 36px;
  border-radius: 8px;
  background: var(--c-accent);
  color: white;
  border: none;
  font-size: 1.3rem;
  cursor: pointer;
  display: flex;
  align-items: center;
  justify-content: center;
  flex-shrink: 0;
}
.btn-add:hover { opacity: 0.85; }

/* Sélecteur de taille (affiché au clic) */
.btn-size {
  flex: 1;
  padding: 0.4rem 0.25rem;
  background: var(--c-accent);
  color: white;
  border: none;
  border-radius: 8px;
  font-family: sans-serif;
  font-size: 0.78rem;
  font-weight: 600;
  text-align: center;
  cursor: pointer;
  line-height: 1.3;
}
.btn-size small { font-weight: 400; opacity: 0.85; font-size: 0.72rem; }
.btn-size:hover { opacity: 0.85; }

.btn-close {
  width: 30px;
  height: 30px;
  background: none;
  border: 1px solid var(--c-border);
  color: var(--c-muted);
  border-radius: 50%;
  cursor: pointer;
  font-size: 0.7rem;
  flex-shrink: 0;
}

/* ── Sakura ── */
.sakura-overlay {
  position: fixed;
  inset: 0;
  pointer-events: none;
  z-index: 50;
  overflow: hidden;
}

.petal {
  position: absolute;
  top: -20px;
  background: radial-gradient(ellipse at 40% 35%, #ffd6e0, #ffb7c5 60%, #e8889a);
  border-radius: 150% 0 150% 0;
  opacity: 0;
  animation: sakura-fall linear infinite;
  transform-origin: center;
  filter: drop-shadow(0 1px 2px rgba(200, 100, 120, 0.25));
}

@keyframes sakura-fall {
  0% {
    transform: translateY(-20px) translateX(0) rotate(var(--spin, 0deg));
    opacity: 0;
  }
  8% {
    opacity: 0.75;
  }
  85% {
    opacity: 0.55;
  }
  100% {
    transform: translateY(105vh) translateX(var(--drift, 40px)) rotate(calc(var(--spin, 0deg) + 540deg));
    opacity: 0;
  }
}
</style>
