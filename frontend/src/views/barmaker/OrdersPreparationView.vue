<template>
  <div class="prep-page">

    <header class="bm-nav">
      <div class="nav-brand">
        <div class="brand-icon">🍸</div>
        <div>
          <p class="brand-name">Tsuki</p>
          <span class="bm-badge">ESPACE BARMAKER</span>
        </div>
      </div>
      <nav class="nav-links">
        <RouterLink to="/barmaker/orders" class="nav-link active">Commandes</RouterLink>
        <RouterLink to="/barmaker/cocktails" class="nav-link">La carte</RouterLink>
      </nav>
      <button class="logout-btn" @click="handleLogout">⎋</button>
    </header>

    <main class="prep-main">
      <RouterLink to="/barmaker/orders" class="breadcrumb">← COMMANDES</RouterLink>

      <div v-if="loading" class="state-msg">Chargement...</div>
      <div v-else-if="error" class="error-msg">{{ error }}</div>

      <template v-else-if="order">

        <div class="prep-head">
          <h1>Table {{ order.tableNumber }}
            <span class="sep">·</span>
            {{ order.items?.length }} cocktail{{ order.items?.length > 1 ? 's' : '' }}
          </h1>
          <span :class="['status-pill', order.status.toLowerCase()]">{{ statusLabel(order.status) }}</span>
        </div>

        <!-- Liste des items -->
        <div class="items-list">
          <div v-for="item in order.items" :key="item.id" class="item-row">

            <div class="item-left">
              <div class="item-icon">🍸</div>
              <div>
                <p class="item-name">{{ item.cocktailName }}</p>
                <p class="item-meta">{{ item.size }} · {{ item.quantity }}x</p>
              </div>
            </div>

            <!-- Stepper visuel -->
            <div class="stepper">
              <div v-for="(step, idx) in steps" :key="step.key" class="step-wrap">
                <div :class="['step-dot', stepClass(item.status, idx)]"></div>
                <span class="step-lbl">{{ step.label }}</span>
                <div v-if="idx < steps.length - 1" :class="['step-line', stepLineClass(item.status, idx)]"></div>
              </div>
            </div>

            <div class="item-right">
              <span :class="['item-badge', item.status.toLowerCase()]">{{ itemLabel(item.status) }}</span>
              <button
                v-if="item.status !== 'TERMINEE'"
                class="next-btn"
                :disabled="advancingItem === item.id"
                @click="nextStep(item)"
              >{{ advancingItem === item.id ? '...' : '→' }}</button>
              <span v-else class="done-check">✓</span>
            </div>

          </div>
        </div>

        <!-- Bouton Tout finaliser -->
        <div class="finish-section">
          <button
            class="finish-all-btn"
            :disabled="!canFinish || finishing"
            @click="finishAll"
          >
            {{ finishing ? 'En cours...' : '✓ Tout finaliser' }}
          </button>
          <p class="finish-hint">Marque tous les cocktails comme terminés et clôture la commande</p>
        </div>

      </template>
    </main>
  </div>
</template>

<script setup lang="ts">
import { ref, computed, watch, onMounted } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { useOrderStore } from '@/stores/orderStore'
import { useAuthStore } from '@/stores/authStore'

const route      = useRoute()
const router     = useRouter()
const orderStore = useOrderStore()
const authStore  = useAuthStore()

const orderId      = Number(route.params.id)
const loading      = ref(true)
const error        = ref('')
const finishing    = ref(false)
const advancingItem = ref<number | null>(null)
const order        = ref<any>(null)

const steps = [
  { key: 'ATTENTE',                 label: 'Attente' },
  { key: 'PREPARATION_INGREDIENTS', label: 'Prép.' },
  { key: 'ASSEMBLAGE',              label: 'Assemblage' },
  { key: 'DRESSAGE',                label: 'Dressage' },
  { key: 'TERMINEE',                label: 'Terminé' },
]
const statusSeq = ['ATTENTE', 'PREPARATION_INGREDIENTS', 'ASSEMBLAGE', 'DRESSAGE', 'TERMINEE']

function stepClass(status: string, idx: number) {
  const cur = statusSeq.indexOf(status)
  if (idx < cur)  return 'done'
  if (idx === cur) return 'active'
  return 'pending'
}
function stepLineClass(status: string, idx: number) {
  return statusSeq.indexOf(status) > idx ? 'done' : 'pending'
}
function statusLabel(s: string) {
  return { COMMANDEE: 'Nouvelle', EN_COURS: 'En préparation', TERMINEE: 'Terminée' }[s] ?? s
}
function itemLabel(s: string) {
  return {
    ATTENTE:                'Attente',
    PREPARATION_INGREDIENTS:'Préparation',
    ASSEMBLAGE:             'Assemblage',
    DRESSAGE:               'Dressage',
    TERMINEE:               'Prêt',
  }[s] ?? s
}

const canFinish = computed(() =>
  !!order.value && order.value.items?.some((i: any) => i.status !== 'TERMINEE')
)

// Avance d'un seul cran
async function nextStep(item: any) {
  advancingItem.value = item.id
  error.value = ''
  try {
    await orderStore.advanceItem(item.id)
    await load(false)
  } catch (e: any) {
    error.value = `Erreur (${e?.response?.status ?? 'réseau'})`
  } finally {
    advancingItem.value = null
  }
}

// Finalise tous les items d'un coup
async function finishAll() {
  if (!order.value) return
  finishing.value = true
  error.value = ''
  try {
    for (const item of order.value.items) {
      let status = item.status
      while (status !== 'TERMINEE') {
        await orderStore.advanceItem(item.id)
        const idx = statusSeq.indexOf(status)
        status = statusSeq[Math.min(idx + 1, statusSeq.length - 1)]
      }
    }
    await load(false)
  } catch (e: any) {
    error.value = `Erreur (${e?.response?.status ?? 'réseau'})`
  } finally {
    finishing.value = false
  }
}

// Redirection automatique quand la commande est terminée
watch(
  () => order.value?.status,
  (status) => {
    if (status === 'TERMINEE') router.push('/barmaker/orders')
  }
)

async function load(showLoading = true) {
  if (showLoading) loading.value = true
  error.value = ''
  try {
    await orderStore.fetchOrderById(orderId)
    order.value = orderStore.currentOrder
  } catch (e: any) {
    error.value = `Erreur ${e?.response?.status ?? 'réseau'}`
  } finally {
    if (showLoading) loading.value = false
  }
}

function handleLogout() {
  authStore.logout()
  router.push('/barmaker/login')
}

onMounted(() => load())
</script>

<style scoped>
.prep-page {
  --c-bg:     #ede8df;
  --c-nav:    #ffffff;
  --c-text:   #2c2520;
  --c-muted:  #8a7f74;
  --c-accent: #8b3230;
  --c-border: #ccc8bc;
  --c-green:  #27ae60;
  --c-card:   #e0dbd0;

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
.nav-link { font-family: sans-serif; font-size: 0.9rem; color: var(--c-muted); text-decoration: none; }
.nav-link.active { color: var(--c-text); border-bottom: 2px solid var(--c-text); font-weight: 600; padding-bottom: 2px; }
.logout-btn {
  background: var(--c-card); border: 1px solid var(--c-border);
  color: var(--c-text); width: 34px; height: 34px; border-radius: 6px; cursor: pointer;
}

/* Contenu */
.prep-main { padding: 2rem; max-width: 900px; margin: 0 auto; }
.breadcrumb {
  font-family: sans-serif; font-size: 0.72rem; letter-spacing: 0.1em;
  color: var(--c-muted); text-decoration: none; display: block; margin-bottom: 1.5rem;
}
.breadcrumb:hover { color: var(--c-text); }
.state-msg { text-align: center; padding: 4rem; color: var(--c-muted); font-family: sans-serif; }
.error-msg { text-align: center; padding: 2rem; color: var(--c-accent); font-family: sans-serif; font-weight: 600; }

.prep-head { display: flex; align-items: center; gap: 1rem; margin-bottom: 1.5rem; }
.prep-head h1 { font-size: 1.8rem; font-weight: 700; margin: 0; }
.sep { color: var(--c-muted); font-weight: 300; }
.status-pill {
  font-family: sans-serif; font-size: 0.75rem; font-weight: 600;
  padding: 0.3rem 0.8rem; border-radius: 20px;
}
.status-pill.commandee { background: #f0ece3; border: 1px solid var(--c-border); color: var(--c-muted); }
.status-pill.en_cours  { background: #fdf4f4; border: 1px solid var(--c-accent); color: var(--c-accent); }
.status-pill.terminee  { background: #f0fdf4; border: 1px solid var(--c-green); color: var(--c-green); }

/* Items */
.items-list { display: flex; flex-direction: column; margin-bottom: 1.5rem; }
.item-row {
  display: flex; align-items: center; gap: 1.25rem;
  background: white; border: 1px solid var(--c-border);
  padding: 0.9rem 1.25rem; border-bottom: none;
}
.item-row:first-child { border-radius: 10px 10px 0 0; }
.item-row:last-child  { border-radius: 0 0 10px 10px; border-bottom: 1px solid var(--c-border); }
.item-row:only-child  { border-radius: 10px; border-bottom: 1px solid var(--c-border); }

.item-left { display: flex; align-items: center; gap: 0.75rem; min-width: 150px; }
.item-icon {
  width: 34px; height: 34px; border-radius: 50%;
  background: var(--c-card); display: flex; align-items: center;
  justify-content: center; font-size: 0.95rem; flex-shrink: 0;
}
.item-name { font-weight: 600; font-size: 0.88rem; margin: 0; }
.item-meta { font-family: sans-serif; font-size: 0.72rem; color: var(--c-muted); margin: 0.15rem 0 0; }

/* Stepper */
.stepper { display: flex; align-items: flex-start; flex: 1; }
.step-wrap { display: flex; flex-direction: column; align-items: center; position: relative; flex: 1; }
.step-dot {
  width: 12px; height: 12px; border-radius: 50%;
  border: 2px solid var(--c-border); background: white; z-index: 1;
}
.step-dot.active { border-color: var(--c-accent); background: var(--c-accent); }
.step-dot.done   { border-color: var(--c-green);  background: var(--c-green); }
.step-lbl {
  font-family: sans-serif; font-size: 0.58rem; color: var(--c-muted);
  text-align: center; margin-top: 0.25rem; white-space: nowrap;
}
.step-line {
  position: absolute; top: 6px; left: 50%; width: 100%;
  height: 2px; background: var(--c-border); z-index: 0;
}
.step-line.done { background: var(--c-green); }

/* Côté droit */
.item-right { display: flex; align-items: center; gap: 0.6rem; flex-shrink: 0; }
.item-badge {
  font-family: sans-serif; font-size: 0.68rem; font-weight: 600;
  padding: 0.2rem 0.55rem; border-radius: 10px; white-space: nowrap;
}
.item-badge.attente                 { background: #ede8df; color: var(--c-muted); }
.item-badge.preparation_ingredients { background: #e0dbd0; color: var(--c-text); }
.item-badge.assemblage              { background: #ccc8bc; color: var(--c-text); }
.item-badge.dressage                { background: #f0dbd8; color: var(--c-accent); }
.item-badge.terminee                { background: #e8f5ec; color: var(--c-green); }

.next-btn {
  width: 32px; height: 32px; border-radius: 6px;
  background: var(--c-accent); color: white; border: none;
  font-size: 1rem; font-weight: 700; cursor: pointer; flex-shrink: 0;
  display: flex; align-items: center; justify-content: center;
}
.next-btn:disabled { opacity: 0.4; cursor: not-allowed; }
.next-btn:hover:not(:disabled) { opacity: 0.85; }
.done-check { color: var(--c-green); font-weight: 700; font-size: 1rem; width: 32px; text-align: center; }

/* Bouton tout finaliser */
.finish-section { text-align: center; padding: 0.5rem 0 1rem; }
.finish-all-btn {
  padding: 0.85rem 2.5rem; border-radius: 10px;
  font-family: sans-serif; font-size: 1rem; font-weight: 600; cursor: pointer;
  background: var(--c-accent); color: white; border: none; transition: opacity 0.15s;
}
.finish-all-btn:hover:not(:disabled) { opacity: 0.88; }
.finish-all-btn:disabled { opacity: 0.35; cursor: not-allowed; }
.finish-hint {
  font-family: sans-serif; font-size: 0.75rem; color: var(--c-muted); margin: 0.5rem 0 0;
}
</style>
