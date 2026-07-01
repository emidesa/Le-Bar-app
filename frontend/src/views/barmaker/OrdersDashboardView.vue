<template>
  <div class="dashboard-page">

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
        <span class="nav-link active">Commandes</span>
        <RouterLink to="/barmaker/cocktails" class="nav-link">La carte</RouterLink>
      </nav>
      <button class="logout-btn" @click="handleLogout" title="Déconnexion">⎋</button>
    </header>

    <main class="dashboard-main">
      <p class="service-eyebrow">SERVICE DU SOIR · BAR CENTRAL</p>
      <div class="dashboard-head">
        <h1>Commandes en attente</h1>
        <button class="refresh-icon" @click="() => loadOrders()" title="Actualiser">↻</button>
      </div>

      <!-- Onglets -->
      <div class="tabs">
        <button :class="['tab', { active: activeTab === 'toProcess' }]" @click="activeTab = 'toProcess'">
          À traiter <span class="tab-count">{{ toProcess.length }}</span>
        </button>
        <button :class="['tab', { active: activeTab === 'inProgress' }]" @click="activeTab = 'inProgress'">
          En cours <span class="tab-count">{{ inProgress.length }}</span>
        </button>
        <button :class="['tab', { active: activeTab === 'done' }]" @click="activeTab = 'done'">
          Terminées <span class="tab-count">{{ done.length }}</span>
        </button>
      </div>

      <div v-if="loading" class="state-msg">Chargement...</div>

      <!-- Grille de commandes -->
      <div v-else class="orders-grid">
        <div
          v-for="order in currentOrders"
          :key="order.id"
          class="order-card"
        >
          <div class="card-top">
            <div>
              <p class="card-id">#T-{{ order.id }}</p>
              <p class="card-meta">Table {{ order.tableNumber }} · {{ order.items?.length }} cocktail{{ order.items?.length > 1 ? 's' : '' }}</p>
            </div>
            <span :class="['order-badge', order.status.toLowerCase()]">
              {{ order.status === 'COMMANDEE' ? 'Nouvelle' : 'En préparation' }}
            </span>
          </div>

          <!-- Mini-timer basé sur createdAt -->
          <div :class="['card-timer', orderTimerClass(order)]">
            <span class="timer-icon">⏱</span>
            <span class="timer-val">{{ orderTimer(order) }}</span>
            <span class="timer-lbl">{{ orderTimerDone(order) ? '— temps écoulé' : 'restant' }}</span>
          </div>

          <div class="card-actions">
            <RouterLink
              v-if="order.status === 'COMMANDEE'"
              :to="`/barmaker/orders/${order.id}`"
              class="action-btn primary"
            >Commencer la préparation</RouterLink>
            <RouterLink
              v-else
              :to="`/barmaker/orders/${order.id}`"
              class="action-btn outline"
            >Ouvrir la commande</RouterLink>
          </div>
        </div>

        <div v-if="currentOrders.length === 0" class="empty-tab">
          Aucune commande dans cet onglet.
        </div>
      </div>
    </main>

  </div>
</template>

<script setup lang="ts">
import { ref, computed, onMounted, onUnmounted } from 'vue'
import { useRouter } from 'vue-router'
import { useOrderStore } from '@/stores/orderStore'
import { useAuthStore } from '@/stores/authStore'

const router     = useRouter()
const orderStore = useOrderStore()
const authStore  = useAuthStore()

const loading   = ref(false)
const activeTab = ref<'toProcess' | 'inProgress' | 'done'>('toProcess')
const now       = ref(Date.now())

// Tick toutes les secondes pour mettre à jour les timers des cartes
let ticker: ReturnType<typeof setInterval> | null = null

function orderElapsed(order: any): number {
  return Math.floor((now.value - new Date(order.createdAt).getTime()) / 1000)
}
function orderTimerDone(order: any): boolean {
  return orderElapsed(order) >= 300
}
function orderTimer(order: any): string {
  const remaining = Math.max(0, 300 - orderElapsed(order))
  const m = Math.floor(remaining / 60)
  const s = remaining % 60
  return `${m}:${s.toString().padStart(2, '0')}`
}
function orderTimerClass(order: any): string {
  if (orderTimerDone(order)) return 'urgent'
  if (orderElapsed(order) >= 200) return 'warning'
  return ''
}

const toProcess  = computed(() => orderStore.orders.filter(o => o.status === 'COMMANDEE'))
const inProgress = computed(() => orderStore.orders.filter(o => o.status === 'EN_COURS'))
const done       = computed(() => orderStore.orders.filter(o => o.status === 'TERMINEE'))

const currentOrders = computed(() => {
  if (activeTab.value === 'toProcess')  return toProcess.value
  if (activeTab.value === 'inProgress') return inProgress.value
  return done.value
})

async function loadOrders(showLoading = true) {
  if (showLoading) loading.value = true
  await orderStore.fetchPendingOrders()
  loading.value = false
}


function handleLogout() {
  authStore.logout()
  router.push('/barmaker/login')
}

let pollTimer: ReturnType<typeof setInterval> | null = null

onMounted(() => {
  loadOrders()
  ticker    = setInterval(() => { now.value = Date.now() }, 1000)
  pollTimer = setInterval(() => loadOrders(false), 5000)
})

onUnmounted(() => {
  if (ticker)    clearInterval(ticker)
  if (pollTimer) clearInterval(pollTimer)
})
</script>

<style scoped>
.dashboard-page {
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

/* Nav barmaker */
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
  background: var(--c-text); color: white;
  padding: 0.15rem 0.5rem; border-radius: 3px;
}
.nav-links { display: flex; gap: 2rem; }
.nav-link { font-family: sans-serif; font-size: 0.9rem; color: var(--c-muted); text-decoration: none; cursor: pointer; }
.nav-link.active { color: var(--c-text); border-bottom: 2px solid var(--c-text); font-weight: 600; padding-bottom: 2px; }
.logout-btn {
  background: var(--c-card); border: 1px solid var(--c-border);
  color: var(--c-text); width: 34px; height: 34px;
  border-radius: 6px; cursor: pointer; font-size: 1rem;
}

/* Contenu */
.dashboard-main { padding: 2rem; max-width: 1100px; margin: 0 auto; }
.service-eyebrow {
  font-family: sans-serif; font-size: 0.7rem; letter-spacing: 0.12em;
  color: var(--c-muted); margin: 0 0 0.4rem;
}
.dashboard-head { display: flex; align-items: center; justify-content: space-between; margin-bottom: 1.5rem; }
.dashboard-head h1 { font-size: 1.8rem; font-weight: 700; margin: 0; }
.head-actions { display: flex; align-items: center; gap: 0.75rem; }
.refresh-icon {
  background: white; border: 1px solid var(--c-border);
  color: var(--c-muted); width: 34px; height: 34px;
  border-radius: 50%; cursor: pointer; font-size: 1rem;
}
.finish-all-btn {
  background: var(--c-accent); color: white;
  border: none; border-radius: 8px;
  padding: 0.5rem 1.1rem;
  font-family: sans-serif; font-size: 0.85rem; font-weight: 600;
  cursor: pointer;
}
.finish-all-btn:disabled { opacity: 0.4; cursor: not-allowed; }
.finish-all-btn:hover:not(:disabled) { opacity: 0.88; }

/* Onglets */
.tabs { display: flex; gap: 0; margin-bottom: 1.5rem; border-bottom: 2px solid var(--c-border); }
.tab {
  background: none; border: none; border-bottom: 2px solid transparent; margin-bottom: -2px;
  padding: 0.6rem 1.25rem; font-family: sans-serif; font-size: 0.88rem;
  color: var(--c-muted); cursor: pointer; display: flex; align-items: center; gap: 0.5rem;
}
.tab.active { color: var(--c-text); border-bottom-color: var(--c-text); font-weight: 600; }
.tab-count {
  background: var(--c-card); border-radius: 10px;
  font-size: 0.72rem; padding: 0.1rem 0.45rem;
}
.tab.active .tab-count { background: var(--c-text); color: white; }

.state-msg { text-align: center; padding: 3rem; color: var(--c-muted); font-family: sans-serif; }

/* Grille de commandes */
.orders-grid {
  display: grid;
  grid-template-columns: repeat(auto-fill, minmax(300px, 1fr));
  gap: 1rem;
}
.empty-tab {
  grid-column: 1 / -1; text-align: center;
  padding: 3rem; color: var(--c-muted); font-family: sans-serif;
}

.order-card {
  background: white; border: 1px solid var(--c-border);
  border-radius: 12px; padding: 1.25rem;
  display: flex; flex-direction: column; gap: 1rem;
}
.card-top { display: flex; justify-content: space-between; align-items: flex-start; }
.card-id { font-size: 1rem; font-weight: 700; margin: 0 0 0.2rem; }
.card-meta { font-family: sans-serif; font-size: 0.78rem; color: var(--c-muted); margin: 0; }

.order-badge {
  font-family: sans-serif; font-size: 0.7rem; font-weight: 600;
  padding: 0.25rem 0.7rem; border-radius: 20px;
}
.order-badge.commandee { background: #f5f0e8; color: var(--c-muted); border: 1px solid var(--c-border); }
.order-badge.en_cours  { background: #fdf4f4; color: var(--c-accent); border: 1px solid var(--c-accent); }

/* Mini-timer */
.card-timer {
  display: flex; align-items: center; gap: 0.4rem;
  font-family: sans-serif; font-size: 0.78rem; color: var(--c-muted);
  background: var(--c-bg); border-radius: 6px; padding: 0.35rem 0.65rem;
}
.card-timer.warning { color: #a06b10; background: #fdf4e8; }
.card-timer.urgent  { color: var(--c-accent); background: #fdf4f4; font-weight: 600; }
.timer-icon { font-size: 0.85rem; }
.timer-val  { font-weight: 700; }
.timer-lbl  { color: inherit; opacity: 0.8; }

.card-actions { display: flex; }
.action-btn {
  width: 100%; padding: 0.6rem 1rem; border-radius: 8px;
  font-family: sans-serif; font-size: 0.82rem; font-weight: 600;
  text-align: center; text-decoration: none; cursor: pointer;
}
.action-btn.primary { background: var(--c-accent); color: white; border: none; }
.action-btn.outline { background: white; color: var(--c-text); border: 1px solid var(--c-border); }
.action-btn:hover { opacity: 0.85; }
</style>
