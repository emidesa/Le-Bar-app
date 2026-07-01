<template>
  <div class="mgmt-page">

    <!-- Modale de confirmation suppression -->
    <div v-if="confirmModal.visible" class="modal-backdrop" @click.self="confirmModal.visible = false">
      <div class="modal-box">
        <p class="modal-title">{{ confirmModal.title }}</p>
        <p class="modal-msg">{{ confirmModal.message }}</p>
        <div class="modal-actions">
          <button class="modal-cancel" @click="confirmModal.visible = false">Annuler</button>
          <button class="modal-confirm" @click="runConfirm">Supprimer</button>
        </div>
      </div>
    </div>

    <header class="bm-nav">
      <RouterLink to="/barmaker/orders" class="nav-brand">
        <div class="brand-icon">🍸</div>
        <div>
          <p class="brand-name">Bar'app</p>
          <span class="bm-badge">ESPACE BARMAKER</span>
        </div>
      </RouterLink>
      <nav class="nav-links">
        <RouterLink to="/barmaker/orders" class="nav-link">Commandes</RouterLink>
        <span class="nav-link active">La carte</span>
      </nav>
      <button class="logout-btn" @click="handleLogout" title="Déconnexion">⎋</button>
    </header>

    <main class="mgmt-main">
      <p class="eyebrow">PARAMÉTRAGE</p>
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
              <span class="cat-delete" @click.stop="deleteCategory(cat.id)">✕</span>
            </button>
          </div>

          <button class="add-cat-btn" @click="addingCat = !addingCat; catError = ''">+ Nouvelle catégorie</button>
          <form v-if="addingCat" class="add-cat-form" @submit.prevent="createCategory">
            <input v-model="newCatName" placeholder="Nom de la catégorie" autofocus />
            <button type="submit">Ajouter</button>
          </form>
          <p v-if="catError" class="cat-error">{{ catError }}</p>
        </aside>

        <!-- Panneau droit : liste ou formulaire -->
        <section class="right-panel">

          <!-- Formulaire d'ajout -->
          <div v-if="showForm" class="cocktail-form">
            <h2 class="form-title">Nouveau cocktail</h2>

            <p v-if="formError" class="form-error">{{ formError }}</p>

            <div class="field">
              <label class="field-label">NOM DU COCKTAIL</label>
              <input v-model="form.name" class="field-input" placeholder="Ex : Mojito, Spritz..." />
            </div>

            <div class="field">
              <label class="field-label">DESCRIPTION</label>
              <textarea v-model="form.description" class="field-input field-textarea" rows="3" placeholder="Décris le cocktail, ses saveurs..."></textarea>
            </div>

            <div class="field">
              <label class="field-label">PHOTO</label>
              <label class="upload-zone" :class="{ 'has-image': form.imageUrl }">
                <img v-if="form.imageUrl" :src="form.imageUrl" class="upload-preview" alt="aperçu" />
                <span v-else class="upload-placeholder">
                  <span class="upload-icon">📷</span>
                  <span>Cliquer pour ajouter une photo</span>
                </span>
                <input type="file" accept="image/*" class="upload-input" @change="uploadPhoto" :disabled="uploading" />
              </label>
              <p v-if="uploading" class="field-hint">Envoi en cours...</p>
              <p v-if="uploadError" class="form-error">{{ uploadError }}</p>
            </div>

            <div class="field">
              <label class="field-label">CATÉGORIE</label>
              <div class="cat-pills">
                <button
                  v-for="cat in menuStore.categories"
                  :key="cat.id"
                  :class="['cat-pill', { active: form.categoryId === cat.id }]"
                  type="button"
                  @click="form.categoryId = cat.id"
                >{{ cat.name }}</button>
              </div>
            </div>

            <div class="field">
              <label class="field-label">INGRÉDIENTS</label>
              <div class="tags-input">
                <span v-for="(ing, i) in form.ingredients" :key="i" class="tag">
                  {{ ing.name }}
                  <button type="button" class="tag-remove" @click="removeIngredient(i)">×</button>
                </span>
                <input
                  v-model="ingInput"
                  class="tags-text"
                  placeholder="Ajouter un ingrédient..."
                  @keydown.enter.prevent="addIngredient"
                  @keydown.","="addIngredient"
                />
              </div>
              <p class="field-hint">Appuie sur Entrée pour ajouter</p>
            </div>

            <div class="field">
              <label class="field-label">TAILLES & PRIX</label>
              <div class="sizes-list">
                <div v-for="s in form.sizes" :key="s.size" class="size-row">
                  <div :class="['size-badge', s.size.toLowerCase()]">{{ s.size }}</div>
                  <span class="size-cl">{{ sizeCl(s.size) }}</span>
                  <input v-model="s.price" type="number" min="0" step="0.5" class="price-input" placeholder="Prix" />
                  <span class="size-euro">€</span>
                </div>
              </div>
            </div>

            <div class="form-actions">
              <button class="btn-cancel" type="button" @click="cancelForm">Annuler</button>
              <button class="btn-save" type="button" :disabled="saving" @click="saveCocktail">
                {{ saving ? 'Enregistrement...' : 'Enregistrer le cocktail' }}
              </button>
            </div>
          </div>

          <!-- Liste des cocktails -->
          <template v-else-if="selectedCat">
            <div class="panel-head">
              <h2 class="panel-title">{{ selectedCat.name }}</h2>
              <button class="btn-new-cocktail" @click="openForm()">+ Nouveau cocktail</button>
            </div>

            <div class="cocktail-list">
              <div
                v-for="cocktail in cocktailsForCat(selectedCat.id)"
                :key="cocktail.id"
                class="cocktail-row"
              >
                <div class="cocktail-thumb-placeholder">🍸</div>
                <div class="cocktail-info">
                  <p class="cocktail-name">{{ cocktail.name }}</p>
                  <div class="size-tags">
                    <span v-for="s in cocktail.sizes" :key="s.id" class="size-tag">
                      {{ s.size }} · {{ s.price }}€
                    </span>
                  </div>
                  <p v-if="cocktail.ingredients?.length" class="cocktail-ings">
                    {{ cocktail.ingredients.map((i: any) => i.ingredientName).join(' · ') }}
                  </p>
                </div>
                <button class="delete-cocktail" @click="deleteCocktail(cocktail.id)" title="Supprimer">✕</button>
              </div>

              <div v-if="cocktailsForCat(selectedCat.id).length === 0" class="list-empty">
                Aucun cocktail dans cette catégorie.
              </div>
            </div>
          </template>

          <div v-else class="panel-empty">
            Sélectionne une catégorie pour voir ses cocktails.
          </div>

        </section>
      </div>
    </main>
  </div>
</template>

<script setup lang="ts">
import { ref, reactive, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import { useMenuStore } from '@/stores/menuStore'
import { useAuthStore } from '@/stores/authStore'
import api from '@/services/api'

const router    = useRouter()
const menuStore = useMenuStore()
const authStore = useAuthStore()

const selectedCat     = ref<any>(null)
const showForm        = ref(false)

const addingCat       = ref(false)
const newCatName      = ref('')
const catError        = ref('')
const ingInput        = ref('')
const saving          = ref(false)
const formError       = ref('')
const uploading       = ref(false)
const uploadError     = ref('')

// Modale de confirmation personnalisée
const confirmModal = reactive({
  visible: false,
  title: '',
  message: '',
  onConfirm: null as (() => Promise<void>) | null,
})

function askConfirm(title: string, message: string, onConfirm: () => Promise<void>) {
  confirmModal.title = title
  confirmModal.message = message
  confirmModal.onConfirm = onConfirm
  confirmModal.visible = true
}

async function runConfirm() {
  confirmModal.visible = false
  if (confirmModal.onConfirm) await confirmModal.onConfirm()
}

const allIngredients = ref<any[]>([])

const form = reactive({
  name: '',
  description: '',
  imageUrl: '' as string,
  categoryId: null as number | null,
  ingredients: [] as { name: string; id: number | null }[],
  sizes: [
    { size: 'S', price: '' as string | number },
    { size: 'M', price: '' as string | number },
    { size: 'L', price: '' as string | number },
  ]
})

function sizeCl(size: string) {
  return { S: '25 cl', M: '35 cl', L: '50 cl' }[size] ?? ''
}

onMounted(async () => {
  await menuStore.fetchCategories()
  await menuStore.fetchCocktails()
  const res = await api.get('/ingredients')
  allIngredients.value = res.data
  if (menuStore.categories.length) selectedCat.value = menuStore.categories[0]
})

function cocktailsForCat(catId: number) {
  return menuStore.cocktails.filter((c: any) => c.category?.id === catId)
}

function selectCat(cat: any) {
  selectedCat.value = cat
  showForm.value = false
}

function openForm() {
  form.name        = ''
  form.description = ''
  form.imageUrl    = ''
  form.categoryId  = selectedCat.value?.id ?? null
  form.ingredients = []
  form.sizes = [
    { size: 'S', price: '' },
    { size: 'M', price: '' },
    { size: 'L', price: '' },
  ]
  formError.value = ''
  showForm.value = true
}

function cancelForm() {
  showForm.value = false
  formError.value = ''
}

async function uploadPhoto(event: Event) {
  const file = (event.target as HTMLInputElement).files?.[0]
  if (!file) return
  uploading.value = true
  uploadError.value = ''
  try {
    const data = new FormData()
    data.append('file', file)
    const res = await api.post('/upload', data, { headers: { 'Content-Type': 'multipart/form-data' } })
    form.imageUrl = res.data.url
  } catch {
    uploadError.value = 'Erreur lors de l\'envoi de la photo.'
  } finally {
    uploading.value = false
  }
}

async function addIngredient() {
  const name = ingInput.value.trim().replace(/,$/, '')
  if (!name) return
  form.ingredients.push({ name, id: null })
  ingInput.value = ''
}

function removeIngredient(i: number) {
  form.ingredients.splice(i, 1)
}

async function saveCocktail() {
  formError.value = ''
  if (!form.name.trim()) { formError.value = 'Le nom est requis.'; return }
  if (!form.categoryId)  { formError.value = 'Sélectionne une catégorie.'; return }

  const duplicate = menuStore.cocktails.find(
    (c: any) => c.name.toLowerCase() === form.name.trim().toLowerCase()
      && c.category?.id === form.categoryId
  )
  if (duplicate) { formError.value = 'Un cocktail avec ce nom existe déjà dans cette catégorie.'; return }

  const validSizes = form.sizes.filter(s => s.price !== '' && Number(s.price) > 0)
  if (!validSizes.length) { formError.value = 'Renseigne au moins un prix.'; return }

  saving.value = true
  try {
    // Résolution des ingrédients : création si inexistant
    const ingredientPayload = []
    for (const ing of form.ingredients) {
      let existing = allIngredients.value.find(
        (i: any) => i.name.toLowerCase() === ing.name.toLowerCase()
      )
      if (!existing) {
        const res = await api.post(`/ingredients?name=${encodeURIComponent(ing.name)}`)
        existing = res.data
        allIngredients.value.push(existing)
      }
      ingredientPayload.push({ ingredientId: existing.id, quantity: 1, unit: 'cl' })
    }

    await api.post('/cocktails', {
      name: form.name.trim(),
      description: form.description.trim() || null,
      imageUrl: form.imageUrl || null,
      categoryId: form.categoryId,
      sizes: validSizes.map(s => ({ size: s.size, price: Number(s.price) })),
      ingredients: ingredientPayload,
    })

    await menuStore.fetchCocktails()
    showForm.value = false
  } catch (e: any) {
    formError.value = `Erreur lors de l'enregistrement (${e?.response?.status ?? 'réseau'})`
  } finally {
    saving.value = false
  }
}

async function createCategory() {
  const name = newCatName.value.trim()
  if (!name) return
  const duplicate = menuStore.categories.find(
    (c: any) => c.name.toLowerCase() === name.toLowerCase()
  )
  if (duplicate) { catError.value = 'Cette catégorie existe déjà.'; return }
  catError.value = ''
  await api.post('/categories', { name, description: '' })
  newCatName.value = ''
  addingCat.value = false
  await menuStore.fetchCategories()
}

function deleteCategory(id: number) {
  askConfirm(
    'Supprimer la catégorie',
    'Cette action est irréversible. Les cocktails associés seront aussi supprimés.',
    async () => {
      await api.delete(`/categories/${id}`)
      if (selectedCat.value?.id === id) { selectedCat.value = null; showForm.value = false }
      await menuStore.fetchCategories()
      await menuStore.fetchCocktails()
    }
  )
}

function deleteCocktail(id: number) {
  const cocktail = menuStore.cocktails.find((c: any) => c.id === id)
  askConfirm(
    'Supprimer le cocktail',
    `« ${cocktail?.name ?? 'ce cocktail'} » sera définitivement retiré de la carte.`,
    async () => {
      await api.delete(`/cocktails/${id}`)
      await menuStore.fetchCocktails()
    }
  )
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

/* Nav — identique aux autres vues barmaker */
.bm-nav {
  display: flex; align-items: center; justify-content: space-between;
  padding: 0.75rem 2rem; background: var(--c-nav);
  border-bottom: 1px solid var(--c-border); position: sticky; top: 0; z-index: 20;
}
.nav-brand { display: flex; align-items: center; gap: 0.75rem; text-decoration: none; color: inherit; }
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
.eyebrow {
  font-family: sans-serif; font-size: 0.7rem; letter-spacing: 0.12em;
  color: var(--c-muted); margin: 0 0 0.4rem;
}
.mgmt-title { font-size: 1.8rem; font-weight: 700; margin: 0 0 0.3rem; color: var(--c-text); }
.mgmt-sub { font-family: sans-serif; font-size: 0.88rem; color: var(--c-muted); margin: 0 0 2rem; }

/* Layout */
.mgmt-layout { display: grid; grid-template-columns: 220px 1fr; gap: 1.5rem; }

/* Panneau catégories */
.cat-panel {
  background: white; border: 1px solid var(--c-border);
  border-radius: 12px; padding: 1.25rem; align-self: start;
}
.panel-title { font-size: 0.95rem; font-weight: 700; margin: 0 0 0.75rem; color: var(--c-text); }
.cat-list { display: flex; flex-direction: column; gap: 0.25rem; margin-bottom: 0.75rem; }
.cat-row {
  display: flex; align-items: center; gap: 0.5rem;
  padding: 0.55rem 0.75rem; border-radius: 8px;
  border: 1px solid transparent; background: none;
  width: 100%; text-align: left; cursor: pointer; color: var(--c-text);
}
.cat-row:hover { background: var(--c-bg); }
.cat-row.selected { background: #fdf4f4; border-color: var(--c-accent); }
.cat-row-name { flex: 1; font-family: sans-serif; font-size: 0.88rem; color: var(--c-text); }
.cat-row-count {
  font-family: sans-serif; font-size: 0.72rem; color: var(--c-muted);
  background: var(--c-card); padding: 0.1rem 0.45rem; border-radius: 8px;
}
.cat-delete { color: var(--c-muted); cursor: pointer; font-size: 0.7rem; padding: 0.1rem 0.3rem; }
.cat-delete:hover { color: var(--c-accent); }

.add-cat-btn {
  width: 100%; padding: 0.55rem 0.75rem;
  border: 1px dashed var(--c-border); border-radius: 8px;
  background: none; font-family: sans-serif; font-size: 0.82rem;
  color: var(--c-muted); cursor: pointer; text-align: left; box-sizing: border-box;
}
.add-cat-btn:hover { background: var(--c-bg); color: var(--c-text); }
.cat-error {
  font-family: sans-serif; font-size: 0.75rem; color: var(--c-accent);
  margin: 0.4rem 0 0; padding: 0;
}
.add-cat-form { margin-top: 0.5rem; display: flex; gap: 0.4rem; }
.add-cat-form input {
  flex: 1; padding: 0.45rem 0.65rem;
  border: 1px solid var(--c-border); border-radius: 6px;
  font-family: sans-serif; font-size: 0.82rem; background: white; color: var(--c-text);
}
.add-cat-form input:focus { outline: none; border-color: var(--c-accent); }
.add-cat-form button {
  padding: 0.45rem 0.75rem; border-radius: 6px;
  background: var(--c-accent); color: white; border: none;
  font-family: sans-serif; font-size: 0.78rem; cursor: pointer;
}

/* Panneau droit */
.right-panel {
  background: white; border: 1px solid var(--c-border);
  border-radius: 12px; padding: 1.5rem;
}
.panel-empty, .list-empty {
  text-align: center; padding: 3rem;
  font-family: sans-serif; color: var(--c-muted); font-size: 0.9rem;
}
.panel-head {
  display: flex; align-items: center; justify-content: space-between;
  margin-bottom: 1rem; padding-bottom: 0.75rem; border-bottom: 1px solid var(--c-border);
}
.btn-new-cocktail {
  font-family: sans-serif; font-size: 0.82rem; font-weight: 600;
  color: white; background: var(--c-accent); border: none;
  padding: 0.45rem 1rem; border-radius: 6px; cursor: pointer;
}
.btn-new-cocktail:hover { opacity: 0.88; }

/* Liste cocktails */
.cocktail-list { display: flex; flex-direction: column; gap: 0.5rem; }
.cocktail-row {
  display: flex; align-items: center; gap: 1rem;
  padding: 0.75rem; border-radius: 8px; border: 1px solid var(--c-border);
}
.cocktail-thumb-placeholder {
  width: 44px; height: 44px; border-radius: 6px; background: var(--c-card);
  display: flex; align-items: center; justify-content: center; font-size: 1.1rem; flex-shrink: 0;
}
.cocktail-info { flex: 1; }
.cocktail-name { font-weight: 600; font-size: 0.9rem; margin: 0 0 0.25rem; color: var(--c-text); }
.size-tags { display: flex; gap: 0.35rem; flex-wrap: wrap; margin-bottom: 0.2rem; }
.size-tag {
  font-family: sans-serif; font-size: 0.68rem; color: var(--c-muted);
  background: var(--c-bg); padding: 0.1rem 0.4rem; border-radius: 6px;
}
.cocktail-ings { font-family: sans-serif; font-size: 0.72rem; color: var(--c-muted); margin: 0; }
.edit-cocktail {
  background: none; border: none; color: #bbb;
  font-size: 1rem; cursor: pointer; flex-shrink: 0; padding: 0.3rem;
}
.edit-cocktail:hover { color: var(--c-text); }
.delete-cocktail {
  background: none; border: none; color: #ccc;
  font-size: 0.9rem; cursor: pointer; flex-shrink: 0; padding: 0.3rem;
}
.delete-cocktail:hover { color: var(--c-accent); }

/* Formulaire */
.cocktail-form { max-width: 580px; }
.form-title { font-size: 1.2rem; font-weight: 700; margin: 0 0 1.5rem; color: var(--c-text); }
.form-error {
  font-family: sans-serif; font-size: 0.82rem; color: var(--c-accent);
  margin-bottom: 1rem; background: #fdf4f4; padding: 0.5rem 0.75rem; border-radius: 6px;
}

.field { margin-bottom: 1.25rem; }
.field-label {
  display: block; font-family: sans-serif; font-size: 0.68rem;
  letter-spacing: 0.1em; color: var(--c-muted); font-weight: 600; margin-bottom: 0.5rem;
}
.field-input {
  width: 100%; padding: 0.7rem 1rem; box-sizing: border-box;
  border: 1px solid var(--c-border); border-radius: 8px;
  font-family: 'Georgia', serif; font-size: 0.95rem;
  background: var(--c-bg); color: var(--c-text);
}
.field-input:focus { outline: none; border-color: var(--c-accent); }
.field-textarea { resize: vertical; font-family: sans-serif; font-size: 0.88rem; line-height: 1.5; }
.field-hint { font-family: sans-serif; font-size: 0.72rem; color: var(--c-muted); margin: 0.3rem 0 0; }

.upload-zone {
  display: flex; align-items: center; justify-content: center;
  width: 100%; height: 200px; border-radius: 10px;
  border: 2px dashed var(--c-border); background: var(--c-card);
  cursor: pointer; overflow: hidden; position: relative;
  transition: border-color 0.15s;
}
.upload-zone:hover { border-color: var(--c-accent); }
.upload-zone.has-image { border-style: solid; background: #000; }
.upload-input {
  position: absolute; inset: 0; opacity: 0; cursor: pointer; width: 100%; height: 100%;
}
.upload-preview { width: 100%; height: 100%; object-fit: contain; }
.upload-placeholder {
  display: flex; flex-direction: column; align-items: center; gap: 0.5rem;
  color: var(--c-muted); font-family: sans-serif; font-size: 0.82rem; pointer-events: none;
}
.upload-icon { font-size: 1.8rem; }

/* Pills catégorie */
.cat-pills { display: flex; gap: 0.5rem; flex-wrap: wrap; }
.cat-pill {
  padding: 0.4rem 1rem; border-radius: 6px;
  border: 1px solid var(--c-border); background: white;
  font-family: sans-serif; font-size: 0.82rem; color: var(--c-text); cursor: pointer;
}
.cat-pill.active { background: var(--c-accent); color: white; border-color: var(--c-accent); }
.cat-pill:not(.active):hover { background: var(--c-bg); }

/* Tags ingrédients */
.tags-input {
  display: flex; flex-wrap: wrap; gap: 0.4rem; align-items: center;
  padding: 0.55rem 0.75rem; border: 1px solid var(--c-border); border-radius: 8px;
  background: var(--c-bg); min-height: 44px;
}
.tags-input:focus-within { border-color: var(--c-accent); }
.tag {
  display: flex; align-items: center; gap: 0.3rem;
  background: white; border: 1px solid var(--c-border);
  padding: 0.2rem 0.55rem; border-radius: 20px;
  font-family: sans-serif; font-size: 0.8rem; color: var(--c-text);
}
.tag-remove {
  background: none; border: none; color: var(--c-muted);
  cursor: pointer; font-size: 1rem; line-height: 1; padding: 0;
}
.tag-remove:hover { color: var(--c-accent); }
.tags-text {
  border: none; background: none; outline: none;
  font-family: sans-serif; font-size: 0.82rem; color: var(--c-text);
  min-width: 160px; flex: 1;
}
.tags-text::placeholder { color: var(--c-muted); }

/* Tailles & prix */
.sizes-list { display: flex; flex-direction: column; gap: 0.5rem; }
.size-row {
  display: flex; align-items: center; gap: 0.75rem;
  background: var(--c-bg); border: 1px solid var(--c-border);
  border-radius: 8px; padding: 0.6rem 1rem;
}
.size-badge {
  width: 26px; height: 26px; border-radius: 5px;
  font-family: sans-serif; font-size: 0.72rem; font-weight: 700;
  display: flex; align-items: center; justify-content: center; flex-shrink: 0;
  background: var(--c-card); color: var(--c-text); border: 1px solid var(--c-border);
}
.size-cl { font-family: sans-serif; font-size: 0.8rem; color: var(--c-muted); min-width: 40px; }
.price-input {
  flex: 1; border: 1px solid var(--c-border); background: white; border-radius: 6px;
  padding: 0.4rem 0.75rem; font-family: sans-serif; font-size: 0.9rem;
  text-align: right; color: var(--c-text);
}
.price-input:focus { outline: none; border-color: var(--c-accent); }
.size-euro { font-family: sans-serif; font-size: 0.88rem; color: var(--c-muted); }

/* Actions formulaire */
.form-actions { display: flex; gap: 0.75rem; margin-top: 1.75rem; }
.btn-cancel {
  padding: 0.8rem 1.5rem; border-radius: 8px;
  border: 1px solid var(--c-border); background: white;
  font-family: sans-serif; font-size: 0.88rem; color: var(--c-text); cursor: pointer;
}
.btn-cancel:hover { background: var(--c-bg); }
.btn-save {
  flex: 1; padding: 0.8rem 1.5rem; border-radius: 8px;
  background: var(--c-accent); color: white; border: none;
  font-family: sans-serif; font-size: 0.88rem; font-weight: 600; cursor: pointer;
}
.btn-save:disabled { opacity: 0.5; cursor: not-allowed; }
.btn-save:hover:not(:disabled) { opacity: 0.88; }

/* Modale de confirmation */
.modal-backdrop {
  position: fixed; inset: 0; background: rgba(44,37,32,0.45);
  display: flex; align-items: center; justify-content: center; z-index: 100;
}
.modal-box {
  background: white; border-radius: 14px; padding: 2rem;
  max-width: 380px; width: 90%; box-shadow: 0 8px 32px rgba(44,37,32,0.18);
}
.modal-title {
  font-size: 1.1rem; font-weight: 700; color: var(--c-text); margin: 0 0 0.6rem;
}
.modal-msg {
  font-family: sans-serif; font-size: 0.88rem; color: var(--c-muted);
  margin: 0 0 1.5rem; line-height: 1.5;
}
.modal-actions { display: flex; gap: 0.75rem; justify-content: flex-end; }
.modal-cancel {
  padding: 0.6rem 1.25rem; border-radius: 8px;
  border: 1px solid var(--c-border); background: white;
  font-family: sans-serif; font-size: 0.85rem; color: var(--c-text); cursor: pointer;
}
.modal-cancel:hover { background: var(--c-bg); }
.modal-confirm {
  padding: 0.6rem 1.25rem; border-radius: 8px;
  background: var(--c-accent); color: white; border: none;
  font-family: sans-serif; font-size: 0.85rem; font-weight: 600; cursor: pointer;
}
.modal-confirm:hover { opacity: 0.88; }

@media (max-width: 768px) {
  .bm-nav { padding: 0.75rem 1rem; }
  .nav-links { display: none; }
  .mgmt-main { padding: 1.25rem 1rem; }
  .mgmt-layout { grid-template-columns: 1fr; }
  .cat-panel { position: static; }
}
</style>
