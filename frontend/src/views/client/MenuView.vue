<template>
  <div class="menu-page">

    <!-- Navigation -->
    <header class="nav">
      <div class="nav-brand">
        <div class="brand-logo">🍸</div>
        <div class="brand-text">
          <p class="brand-name">Tsuki</p>
          <p class="brand-tag">BAR À COCKTAILS</p>
        </div>
      </div>

      <nav class="nav-links">
        <span class="nav-link active">La carte</span>
        <RouterLink :to="`/orders?table=${tableNumber}`" class="nav-link">Mes commandes</RouterLink>
      </nav>

      <div class="nav-right">
        <RouterLink :to="`/cart?table=${tableNumber}`" class="cart-btn">
          <svg width="20" height="20" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2">
            <circle cx="9" cy="21" r="1"/><circle cx="20" cy="21" r="1"/>
            <path d="M1 1h4l2.68 13.39a2 2 0 0 0 2 1.61h9.72a2 2 0 0 0 2-1.61L23 6H6"/>
          </svg>
          <span v-if="cartStore.itemCount > 0" class="cart-badge">{{ cartStore.itemCount }}</span>
        </RouterLink>
        <div class="user-avatar">T{{ tableNumber }}</div>
      </div>
    </header>

    <!-- Hero -->
    <section class="hero">
      <p class="hero-eyebrow">BAR À COCKTAILS · OUVERT JUSQU'À 2H</p>
      <h1 class="hero-title">Notre carte <em>du soir</em></h1>
    </section>

    <!-- Recherche + filtres sur la même ligne -->
    <div class="controls">
      <label class="search-wrap">
        <svg class="search-icon" width="16" height="16" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2">
          <circle cx="11" cy="11" r="8"/><path d="m21 21-4.35-4.35"/>
        </svg>
        <input v-model="search" class="search-input" placeholder="Rechercher un cocktail, un ingrédient..." />
      </label>

      <div class="filter-row">
        <button :class="['pill', { active: menuStore.selectedCategory === null }]" @click="menuStore.selectCategory(null)">Tout</button>
        <button
          v-for="cat in menuStore.categories"
          :key="cat.id"
          :class="['pill', { active: menuStore.selectedCategory === cat.id }]"
          @click="menuStore.selectCategory(cat.id)"
        >{{ cat.name }}</button>
      </div>
    </div>

    <!-- Chargement -->
    <div v-if="menuStore.loading" class="state-msg">Chargement de la carte...</div>
    <div v-else-if="groupedCocktails.length === 0" class="state-msg">Aucun résultat.</div>

    <!-- Sections par catégorie -->
    <main v-else class="menu-body">
      <section v-for="group in groupedCocktails" :key="group.category.id" class="cat-section">

        <div class="cat-head">
          <h2 class="cat-title">{{ group.category.name }}</h2>
          <span class="cat-count">{{ group.cocktails.length }} cocktail{{ group.cocktails.length > 1 ? 's' : '' }}</span>
        </div>

        <div class="cards-grid">
          <article v-for="cocktail in group.cocktails" :key="cocktail.id" class="cocktail-card">

            <!-- Zone image -->
            <div class="card-img">
              <img v-if="cocktail.imageUrl" :src="cocktail.imageUrl" :alt="cocktail.name" class="card-photo" />
              <div v-else class="card-illus">
                <svg viewBox="0 0 60 80" fill="none" xmlns="http://www.w3.org/2000/svg">
                  <path d="M10 8 L50 8 L35 40 L35 68 L25 68 L25 40 Z" stroke="#8a7f74" stroke-width="1.5" fill="none" stroke-linejoin="round"/>
                  <line x1="20" y1="68" x2="40" y2="68" stroke="#8a7f74" stroke-width="1.5"/>
                  <ellipse cx="30" cy="30" rx="10" ry="5" fill="#ccc8bc" opacity="0.5"/>
                </svg>
              </div>
            </div>

            <!-- Corps de la carte -->
            <div class="card-body">
              <h3 class="card-name">{{ cocktail.name }}</h3>
              <p class="card-ingredients">{{ ingredientsLine(cocktail) }}</p>
              <div class="card-foot">
                <span class="card-price">dès {{ minPrice(cocktail.sizes) }}€</span>
                <RouterLink :to="`/cocktail/${cocktail.id}?table=${tableNumber}`" class="btn-add">+</RouterLink>
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

const route      = useRoute()
const menuStore  = useMenuStore()
const cartStore  = useCartStore()

const tableNumber = ref(Number(route.query.table) || 1)
const search      = ref('')

onMounted(async () => {
  await menuStore.fetchCategories()
  await menuStore.fetchCocktails()
})

const groupedCocktails = computed(() => {
  const term = search.value.toLowerCase()
  const filtered = menuStore.cocktails.filter(c =>
    !term ||
    c.name.toLowerCase().includes(term) ||
    ingredientsLine(c).toLowerCase().includes(term)
  )
  if (menuStore.selectedCategory !== null) {
    const cat = menuStore.categories.find(c => c.id === menuStore.selectedCategory)
    if (!cat) return []
    return [{ category: cat, cocktails: filtered.filter(c => (c as any).category?.id === cat.id) }]
  }
  return menuStore.categories
    .map(cat => ({ category: cat, cocktails: filtered.filter(c => (c as any).category?.id === cat.id) }))
    .filter(g => g.cocktails.length > 0)
})

function ingredientsLine(cocktail: any): string {
  if (cocktail.ingredients?.length) {
    return cocktail.ingredients.map((i: any) => i.ingredientName).join(' · ')
  }
  return cocktail.description ?? ''
}


function minPrice(sizes: any[]): string {
  if (!sizes?.length) return '?'
  const min = Math.min(...sizes.map((s: any) => Number(s.price)))
  return Number.isInteger(min) ? String(min) : min.toFixed(2)
}
</script>

<style scoped>
.menu-page {
  --c-bg:     #ede8df;
  --c-card:   #e4dfd5;
  --c-nav:    #ffffff;
  --c-text:   #2c2520;
  --c-muted:  #8a7f74;
  --c-accent: #8b3230;
  --c-border: #d0cbbf;

  min-height: 100vh;
  background: var(--c-bg);
  color: var(--c-text);
  font-family: 'Georgia', serif;
}

/* ── Nav ── */
.nav {
  display: flex; align-items: center; justify-content: space-between;
  padding: 0.75rem 2rem; background: var(--c-nav);
  border-bottom: 1px solid var(--c-border);
  position: sticky; top: 0; z-index: 20;
}
.nav-brand { display: flex; align-items: center; gap: 0.6rem; }
.brand-logo {
  width: 34px; height: 34px; border-radius: 8px;
  background: var(--c-accent); color: white;
  display: flex; align-items: center; justify-content: center;
  font-size: 1.1rem;
}
.brand-name { font-size: 1.05rem; font-weight: 700; color: var(--c-text); margin: 0; line-height: 1.3; }
.brand-tag  { font-family: sans-serif; font-size: 0.6rem; letter-spacing: 0.1em; color: var(--c-muted); margin: 0; }

.nav-links { display: flex; gap: 2rem; }
.nav-link  { font-family: sans-serif; font-size: 0.9rem; color: var(--c-muted); text-decoration: none; cursor: pointer; }
.nav-link.active { color: var(--c-text); border-bottom: 2px solid var(--c-text); font-weight: 600; padding-bottom: 2px; }

.nav-right  { display: flex; align-items: center; gap: 0.75rem; }
.cart-btn {
  position: relative; display: flex; align-items: center;
  color: var(--c-text); text-decoration: none; padding: 0.25rem;
}
.cart-badge {
  position: absolute; top: -4px; right: -6px;
  background: var(--c-accent); color: white; border-radius: 50%;
  font-family: sans-serif; font-size: 0.65rem; font-weight: 700;
  min-width: 17px; height: 17px;
  display: flex; align-items: center; justify-content: center;
}
.user-avatar {
  width: 34px; height: 34px; border-radius: 10px;
  background: var(--c-accent); color: white;
  font-family: sans-serif; font-size: 0.72rem; font-weight: 700;
  display: flex; align-items: center; justify-content: center;
  letter-spacing: 0.02em;
}

/* ── Hero ── */
.hero { padding: 2.5rem 2rem 0.75rem; text-align: center; }
.hero-eyebrow {
  font-family: sans-serif; font-size: 0.72rem; letter-spacing: 0.12em;
  color: var(--c-muted); margin: 0 0 0.5rem; text-transform: uppercase;
}
.hero-title { font-size: 2.4rem; font-weight: 700; color: #1a1410; margin: 0; }
.hero-title em { font-style: italic; font-weight: 300; color: #6b5e54; }

/* ── Contrôles ── */
.controls {
  padding: 1.5rem 2rem 1rem;
  display: flex; flex-direction: column; align-items: center; gap: 1rem;
}

.search-wrap {
  display: flex; align-items: center; gap: 0.65rem;
  width: 60%; box-sizing: border-box;
  background: white; border: 1px solid var(--c-border);
  border-radius: 8px; padding: 0.65rem 1rem;
  cursor: text;
  box-shadow: 0 1px 3px rgba(0,0,0,0.04);
}
.search-icon { color: var(--c-muted); flex-shrink: 0; }
.search-input {
  border: none; background: none; width: 100%; outline: none;
  font-size: 0.88rem; font-family: sans-serif; color: var(--c-text);
}
.search-input::placeholder { color: var(--c-muted); }

.filter-row { display: flex; gap: 0.5rem; flex-wrap: wrap; justify-content: center; }
.pill {
  background: white;
  border: 1.5px solid #ccc5b5;
  color: var(--c-text);
  padding: 0.45rem 1.25rem;
  border-radius: 10px;
  font-family: sans-serif;
  font-size: 0.85rem;
  font-weight: 400;
  cursor: pointer;
  transition: all 0.12s;
  white-space: nowrap;
  letter-spacing: 0.01em;
}
.pill.active {
  background: var(--c-accent);
  color: #fff;
  border-color: var(--c-accent);
  font-weight: 600;
}
.pill:not(.active):hover { background: #f0ece4; }

/* ── Corps ── */
.menu-body { padding: 1.75rem 2rem 3rem; }
.state-msg { text-align: center; padding: 4rem; color: var(--c-muted); font-family: sans-serif; }

/* ── Section ── */
.cat-section { margin-bottom: 2.75rem; }
.cat-head {
  display: flex; align-items: baseline; justify-content: space-between;
  margin-bottom: 1.25rem;
  padding-bottom: 0.75rem;
  border-bottom: 1px solid #d0cbbf;
}
.cat-title { font-size: 1.35rem; font-weight: 700; margin: 0; color: #1a1410; }
.cat-count  { font-family: sans-serif; font-size: 0.78rem; color: var(--c-muted); }

/* ── Grille ── */
.cards-grid {
  display: grid;
  grid-template-columns: repeat(auto-fill, minmax(230px, 1fr));
  gap: 1.25rem;
}

/* ── Carte ── */
.cocktail-card {
  background: var(--c-card); border-radius: 14px;
  overflow: hidden; display: flex; flex-direction: column;
  transition: transform 0.15s, box-shadow 0.15s;
}
.cocktail-card:hover { transform: translateY(-2px); box-shadow: 0 6px 20px rgba(44,37,32,0.1); }

/* Zone image */
.card-img {
  position: relative; height: 160px;
  background: #ddd8cc;
  display: flex; align-items: center; justify-content: center;
}
.card-photo { width: 100%; height: 100%; object-fit: cover; }
.card-illus { width: 70px; height: 90px; opacity: 0.7; }
.card-illus svg { width: 100%; height: 100%; }

.card-badge {
  position: absolute; top: 0.6rem; left: 0.6rem;
  background: rgba(255,255,255,0.88); border: 1px solid rgba(0,0,0,0.1);
  font-family: sans-serif; font-size: 0.6rem; font-weight: 700;
  letter-spacing: 0.08em; padding: 0.2rem 0.55rem; border-radius: 4px;
  color: var(--c-text);
}

/* Corps de la carte */
.card-body {
  padding: 1rem 1rem 1rem;
  display: flex; flex-direction: column; flex: 1;
}
.card-name {
  font-size: 1rem; font-weight: 700; color: #1a1410; margin: 0 0 0.3rem;
}
.card-ingredients {
  font-family: sans-serif; font-size: 0.78rem; color: var(--c-muted);
  margin: 0 0 0.9rem; line-height: 1.45; flex: 1;
}
.card-foot { display: flex; align-items: center; justify-content: space-between; margin-top: auto; }
.card-price { font-family: sans-serif; font-size: 0.88rem; font-weight: 600; color: var(--c-text); }
.btn-add {
  width: 36px; height: 36px; border-radius: 8px;
  background: var(--c-accent); color: white; border: none;
  font-size: 1.4rem; font-weight: 300; cursor: pointer;
  display: flex; align-items: center; justify-content: center;
  text-decoration: none; line-height: 1;
  transition: opacity 0.12s;
}
.btn-add:hover { opacity: 0.85; }
</style>
