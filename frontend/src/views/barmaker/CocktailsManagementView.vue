<template>
  <div class="mgmt-page">

    <!-- Navigation barmaker -->
    <header class="bm-nav">
      <div class="nav-brand">
        <div class="brand-icon">🍸</div>
        <div>
          <p class="brand-name">Bar'app</p>
          <span class="bm-badge">ESPACE BARMAKER</span>
        </div>
      </div>
      <nav class="nav-links">
        <RouterLink to="/barmaker/orders" class="nav-link">Commandes</RouterLink>
        <span class="nav-link active">La carte</span>
      </nav>
      <button class="logout-btn" @click="handleLogout" title="Déconnexion">⎋</button>
    </header>

    <main class="mgmt-main">
      <h1 class="mgmt-title">Composer la carte</h1>
      <p class="mgmt-sub">Organisez vos catégories et ajoutez des cocktails avec leurs tailles et leurs prix.</p>

      <div class="mgmt-layout">

        <!-- Panneau gauche : catégories -->
        <aside class="cat-panel">
          <h2 class="panel-title">Catégories</h2>
          <div class="cat-list">
            <button
              v-for="cat in menuStore.categories"
              :key="cat.id"
              :class="['cat-row', { selected: selectedCat?.id === cat.id }]"
              @click="selectCat(cat)"
            >
              <span class="cat-row-name">{{ cat.name }}</span>
              <span class="cat-row-count">{{ cocktailsForCat(cat.id).length }}</span>
              <button class="cat-delete" @click.stop="deleteCategory(cat.id)" title="Supprimer">✕</button>
            </button>
          </div>

          <!-- Ajouter une catégorie -->
          <form class="add-cat-form" @submit.prevent="createCategory">
            <input v-model="newCatName" placeholder="+ Nouvelle catégorie" />
          </form>
        </aside>

        <!-- Panneau droit : cocktails de la catégorie sélectionnée -->
        <section class="cocktails-panel">
          <div v-if="!selectedCat" class="panel-empty">
            Sélectionne une catégorie pour voir ses cocktails.
          </div>

          <template v-else>
            <div class="panel-head">
              <h2 class="panel-title">{{ selectedCat.name }}</h2>
              <span class="panel-count">{{ cocktailsForCat(selectedCat.id).length }} cocktail(s)</span>
            </div>

            <div class="cocktail-list">
              <div
                v-for="cocktail in cocktailsForCat(selectedCat.id)"
                :key="cocktail.id"
                class="cocktail-row"
              >
                <img
                  v-if="cocktail.imageUrl"
                  :src="cocktail.imageUrl"
                  :alt="cocktail.name"
                  class="cocktail-thumb"
                />
                <div v-else class="cocktail-thumb-placeholder">🍸</div>

                <div class="cocktail-info">
                  <p class="cocktail-name">{{ cocktail.name }}</p>
                  <div class="size-tags">
                    <span v-for="s in cocktail.sizes" :key="s.id" class="size-tag">
                      {{ s.size }} · {{ s.price }}€
                    </span>
                  </div>
                </div>

                <button class="delete-cocktail" @click="deleteCocktail(cocktail.id)" title="Supprimer">🗑</button>
              </div>

              <div v-if="cocktailsForCat(selectedCat.id).length === 0" class="list-empty">
                Aucun cocktail dans cette catégorie.
              </div>
            </div>
          </template>
        </section>
      </div>
    </main>
  </div>
</template>

<script setup lang="ts">
import { ref, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import { useMenuStore } from '@/stores/menuStore'
import { useAuthStore } from '@/stores/authStore'
import api from '@/services/api'

const router    = useRouter()
const menuStore = useMenuStore()
const authStore = useAuthStore()

const selectedCat = ref<any>(null)
const newCatName  = ref('')

onMounted(async () => {
  await menuStore.fetchCategories()
  await menuStore.fetchCocktails()
  if (menuStore.categories.length) selectedCat.value = menuStore.categories[0]
})

function cocktailsForCat(catId: number) {
  return menuStore.cocktails.filter((c: any) => c.category?.id === catId)
}

function selectCat(cat: any) {
  selectedCat.value = cat
}

async function createCategory() {
  if (!newCatName.value.trim()) return
  await api.post('/categories', { name: newCatName.value.trim(), description: '' })
  newCatName.value = ''
  await menuStore.fetchCategories()
}

async function deleteCategory(id: number) {
  if (!confirm('Supprimer cette catégorie ?')) return
  await api.delete(`/categories/${id}`)
  if (selectedCat.value?.id === id) selectedCat.value = null
  await menuStore.fetchCategories()
}

async function deleteCocktail(id: number) {
  if (!confirm('Supprimer ce cocktail ?')) return
  await api.delete(`/cocktails/${id}`)
  await menuStore.fetchCocktails()
}

function handleLogout() {
  authStore.logout()
  router.push('/barmaker/login')
}
</script>

<style scoped>
.mgmt-page {
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
.bm-nav {
  display: flex; align-items: center; justify-content: space-between;
  padding: 0.75rem 2rem; background: var(--c-nav);
  border-bottom: 1px solid var(--c-border); position: sticky; top: 0; z-index: 20;
}
.nav-brand { display: flex; align-items: center; gap: 0.75rem; }
.brand-icon { font-size: 1.6rem; }
.brand-name { font-size: 1.1rem; font-weight: bold; color: var(--c-text); margin: 0; line-height: 1.4; }
.bm-badge {
  font-family: sans-serif; font-size: 0.6rem; letter-spacing: 0.1em;
  background: var(--c-text); color: white; padding: 0.15rem 0.5rem; border-radius: 3px;
}
.nav-links { display: flex; gap: 2rem; }
.nav-link { font-family: sans-serif; font-size: 0.9rem; color: var(--c-muted); text-decoration: none; cursor: pointer; }
.nav-link.active { color: var(--c-text); border-bottom: 2px solid var(--c-text); font-weight: 600; padding-bottom: 2px; }
.logout-btn {
  background: var(--c-card); border: 1px solid var(--c-border);
  color: var(--c-text); width: 34px; height: 34px; border-radius: 6px; cursor: pointer; font-size: 1rem;
}

/* Contenu */
.mgmt-main { padding: 2rem; max-width: 1100px; margin: 0 auto; }
.mgmt-title { font-size: 1.8rem; font-weight: 700; margin: 0 0 0.4rem; }
.mgmt-sub { font-family: sans-serif; font-size: 0.88rem; color: var(--c-muted); margin: 0 0 2rem; }

/* Layout 2 colonnes */
.mgmt-layout {
  display: grid; grid-template-columns: 220px 1fr; gap: 1.5rem;
}

/* Panneau catégories */
.cat-panel {
  background: white; border: 1px solid var(--c-border);
  border-radius: 12px; padding: 1.25rem; align-self: start;
}
.panel-title { font-size: 0.95rem; font-weight: 700; margin: 0 0 0.75rem; }

.cat-list { display: flex; flex-direction: column; gap: 0.25rem; margin-bottom: 0.75rem; }
.cat-row {
  display: flex; align-items: center; gap: 0.5rem;
  padding: 0.55rem 0.75rem; border-radius: 8px;
  border: 1px solid transparent; background: none;
  width: 100%; text-align: left; cursor: pointer; color: var(--c-text);
}
.cat-row:hover { background: var(--c-bg); }
.cat-row.selected { background: #fdf4f4; border-color: var(--c-accent); }
.cat-row-name { flex: 1; font-family: sans-serif; font-size: 0.88rem; }
.cat-row-count {
  font-family: sans-serif; font-size: 0.72rem; color: var(--c-muted);
  background: var(--c-card); padding: 0.1rem 0.4rem; border-radius: 8px;
}
.cat-delete {
  background: none; border: none; color: var(--c-muted); cursor: pointer;
  font-size: 0.75rem; padding: 0.1rem 0.2rem;
}
.cat-delete:hover { color: var(--c-accent); }

.add-cat-form input {
  width: 100%; padding: 0.55rem 0.75rem;
  border: 1px dashed var(--c-border); border-radius: 8px;
  background: none; font-family: sans-serif; font-size: 0.82rem;
  color: var(--c-muted); box-sizing: border-box;
}
.add-cat-form input:focus { outline: none; border-color: var(--c-accent); color: var(--c-text); }

/* Panneau cocktails */
.cocktails-panel {
  background: white; border: 1px solid var(--c-border);
  border-radius: 12px; padding: 1.25rem;
}
.panel-empty {
  text-align: center; padding: 3rem;
  font-family: sans-serif; color: var(--c-muted); font-size: 0.9rem;
}
.panel-head {
  display: flex; align-items: baseline; gap: 0.75rem; margin-bottom: 1rem;
  padding-bottom: 0.75rem; border-bottom: 1px solid var(--c-border);
}
.panel-count { font-family: sans-serif; font-size: 0.78rem; color: var(--c-muted); }

.cocktail-list { display: flex; flex-direction: column; gap: 0.5rem; }
.cocktail-row {
  display: flex; align-items: center; gap: 1rem;
  padding: 0.75rem; border-radius: 8px; border: 1px solid var(--c-border);
}
.cocktail-thumb { width: 48px; height: 48px; border-radius: 6px; object-fit: cover; flex-shrink: 0; }
.cocktail-thumb-placeholder {
  width: 48px; height: 48px; border-radius: 6px; background: var(--c-card);
  display: flex; align-items: center; justify-content: center; font-size: 1.2rem; flex-shrink: 0;
}
.cocktail-info { flex: 1; }
.cocktail-name { font-weight: 600; font-size: 0.9rem; margin: 0 0 0.3rem; }
.size-tags { display: flex; gap: 0.35rem; }
.size-tag {
  font-family: sans-serif; font-size: 0.7rem; color: var(--c-muted);
  background: var(--c-bg); padding: 0.15rem 0.4rem; border-radius: 6px;
}
.delete-cocktail {
  background: none; border: 1px solid var(--c-border);
  color: var(--c-muted); width: 30px; height: 30px;
  border-radius: 6px; cursor: pointer; flex-shrink: 0;
}
.delete-cocktail:hover { border-color: var(--c-accent); color: var(--c-accent); }
.list-empty {
  text-align: center; padding: 2rem;
  font-family: sans-serif; color: var(--c-muted); font-size: 0.85rem;
}
</style>
