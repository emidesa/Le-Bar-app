<template>
  <div class="tracking-page">

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
        <span class="nav-link active">Mes commandes</span>
      </nav>
      <button class="refresh-btn" @click="loadOrders" title="Actualiser">↻</button>
    </header>

    <div v-if="loading" class="state-msg">Chargement...</div>
    <div v-else-if="orderStore.orders.length === 0" class="state-msg">
      Aucune commande pour cette table.
    </div>

    <main v-else class="tracking-main">
      <!-- On affiche la commande la plus récente en premier plan -->
      <template v-for="order in orderStore.orders" :key="order.id">
        <div class="order-block">

          <!-- En-tête commande -->
          <p class="order-eyebrow">COMMANDE #{{ order.id }} · TABLE {{ tableNumber }}</p>
          <div class="order-head">
            <h2 class="order-status-title">{{ statusTitle(order.status) }}</h2>
            <span :class="['status-pill', order.status.toLowerCase()]">{{ statusLabel(order.status) }}</span>
          </div>

          <!-- Barre de progression -->
          <div class="progress-bar">
            <div v-for="(step, idx) in orderSteps" :key="step.key" class="progress-step">
              <div :class="['prog-dot', progressClass(order.status, idx)]"></div>
              <span :class="['prog-label', { active: isCurrentStep(order.status, idx) }]">{{ step.label }}</span>
              <div v-if="idx < orderSteps.length - 1" :class="['prog-line', progressLineClass(order.status, idx)]"></div>
            </div>
          </div>

          <!-- Détail des items -->
          <p class="detail-label">DÉTAIL DE LA PRÉPARATION</p>
          <div class="items-grid">
            <div v-for="item in order.items" :key="item.id" class="item-card">
              <div class="item-icon">🍸</div>
              <div class="item-body">
                <p class="item-name">{{ item.cocktailName }}</p>
                <p class="item-size">Taille {{ item.size }}</p>
              </div>
              <span :class="['item-badge', item.status.toLowerCase()]">{{ itemLabel(item.status) }}</span>
            </div>
          </div>

          <!-- Total -->
          <div class="order-footer">
            <span class="order-total">Total : {{ order.totalPrice }}€</span>
          </div>
        </div>
      </template>
    </main>

  </div>
</template>

<script setup lang="ts">
import { ref, onMounted } from 'vue'
import { useRoute } from 'vue-router'
import { useOrderStore } from '@/stores/orderStore'

const route      = useRoute()
const orderStore = useOrderStore()
const tableNumber = Number(route.query.table) || 1
const loading    = ref(false)

const orderSteps = [
  { key: 'COMMANDEE', label: 'Commandée' },
  { key: 'EN_COURS',  label: 'En préparation' },
  { key: 'TERMINEE',  label: 'Terminée' },
]
const orderOrder = ['COMMANDEE', 'EN_COURS', 'TERMINEE']

function progressClass(status: string, idx: number) {
  const cur = orderOrder.indexOf(status)
  if (idx < cur)  return 'done'
  if (idx === cur) return 'active'
  return 'pending'
}
function progressLineClass(status: string, idx: number) {
  return orderOrder.indexOf(status) > idx ? 'done' : 'pending'
}
function isCurrentStep(status: string, idx: number) {
  return orderOrder.indexOf(status) === idx
}

function statusTitle(s: string) {
  const m: Record<string,string> = {
    COMMANDEE: 'En attente de préparation',
    EN_COURS:  'En préparation',
    TERMINEE:  'Prête à être servie !'
  }
  return m[s] ?? s
}
function statusLabel(s: string) {
  const m: Record<string,string> = { COMMANDEE: 'Commandée', EN_COURS: 'En cours', TERMINEE: 'Terminée' }
  return m[s] ?? s
}
function itemLabel(s: string) {
  const m: Record<string,string> = {
    ATTENTE:                '⏸ Attente',
    PREPARATION_INGREDIENTS:'🧪 Préparation',
    ASSEMBLAGE:             '🔀 Assemblage',
    DRESSAGE:               '🎨 Dressage',
    TERMINEE:               '✅ Prêt'
  }
  return m[s] ?? s
}

async function loadOrders() {
  loading.value = true
  await orderStore.fetchOrdersByTable(tableNumber)
  loading.value = false
}
onMounted(loadOrders)
</script>

<style scoped>
.tracking-page {
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
.nav-link   { font-family: sans-serif; font-size: 0.9rem; color: var(--c-muted); text-decoration: none; cursor: pointer; }
.nav-link.active { color: var(--c-text); border-bottom: 2px solid var(--c-text); font-weight: 600; padding-bottom: 2px; }
.refresh-btn {
  background: white; border: 1px solid var(--c-border);
  color: var(--c-muted); width: 34px; height: 34px;
  border-radius: 50%; cursor: pointer; font-size: 1rem;
}

.state-msg { text-align: center; padding: 4rem; color: var(--c-muted); font-family: sans-serif; }

/* Contenu */
.tracking-main { padding: 2rem; max-width: 800px; margin: 0 auto; display: flex; flex-direction: column; gap: 2rem; }

.order-block {
  background: white; border: 1px solid var(--c-border);
  border-radius: 14px; padding: 1.75rem;
}

.order-eyebrow {
  font-family: sans-serif; font-size: 0.7rem; letter-spacing: 0.12em;
  color: var(--c-muted); margin: 0 0 0.4rem;
}
.order-head { display: flex; align-items: center; gap: 1rem; margin-bottom: 1.5rem; }
.order-status-title { font-size: 1.5rem; font-weight: 700; margin: 0; }
.status-pill {
  font-family: sans-serif; font-size: 0.72rem; font-weight: 600;
  padding: 0.25rem 0.7rem; border-radius: 20px;
}
.status-pill.commandee { background: #f5f0e8; border: 1px solid var(--c-border); color: var(--c-muted); }
.status-pill.en_cours  { background: #fdf4f4; border: 1px solid var(--c-accent); color: var(--c-accent); }
.status-pill.terminee  { background: #f0fdf4; border: 1px solid #27ae60; color: #27ae60; }

/* Progress bar */
.progress-bar {
  display: flex; align-items: flex-start;
  margin-bottom: 2rem; padding: 0 0.5rem;
}
.progress-step { display: flex; flex-direction: column; align-items: center; flex: 1; position: relative; }
.prog-dot {
  width: 16px; height: 16px; border-radius: 50%;
  border: 2px solid var(--c-border); background: white; z-index: 1;
}
.prog-dot.active  { border-color: var(--c-text); background: var(--c-text); }
.prog-dot.done    { border-color: var(--c-accent); background: var(--c-accent); }
.prog-label {
  font-family: sans-serif; font-size: 0.68rem; color: var(--c-muted);
  margin-top: 0.35rem; text-align: center;
}
.prog-label.active { color: var(--c-text); font-weight: 600; }
.prog-line {
  position: absolute; top: 8px; left: 50%; width: 100%;
  height: 2px; background: var(--c-border); z-index: 0;
}
.prog-line.done { background: var(--c-accent); }

/* Détail */
.detail-label {
  font-family: sans-serif; font-size: 0.68rem; letter-spacing: 0.1em;
  color: var(--c-muted); margin: 0 0 0.75rem;
}
.items-grid {
  display: grid; grid-template-columns: repeat(auto-fill, minmax(220px, 1fr));
  gap: 0.75rem; margin-bottom: 1.5rem;
}
.item-card {
  display: flex; align-items: center; gap: 0.75rem;
  background: var(--c-bg); border: 1px solid var(--c-border);
  border-radius: 8px; padding: 0.75rem;
}
.item-icon {
  width: 32px; height: 32px; border-radius: 50%;
  background: var(--c-card); display: flex; align-items: center;
  justify-content: center; font-size: 0.9rem; flex-shrink: 0;
}
.item-body { flex: 1; }
.item-name { font-size: 0.85rem; font-weight: 600; margin: 0; }
.item-size { font-family: sans-serif; font-size: 0.72rem; color: var(--c-muted); margin: 0.1rem 0 0; }
.item-badge {
  font-family: sans-serif; font-size: 0.65rem; font-weight: 600;
  padding: 0.2rem 0.5rem; border-radius: 10px; white-space: nowrap;
}
.item-badge.attente                 { background: #f5f0e8; color: var(--c-muted); }
.item-badge.preparation_ingredients { background: #f3e8fd; color: #7b3fa0; }
.item-badge.assemblage              { background: #e8f0fd; color: #2a5ea8; }
.item-badge.dressage                { background: #fdf4e8; color: #a06b10; }
.item-badge.terminee                { background: #f0fdf4; color: #27ae60; }

/* Footer */
.order-footer {
  border-top: 1px solid var(--c-border);
  padding-top: 1rem; text-align: right;
}
.order-total { font-family: sans-serif; font-size: 0.9rem; font-weight: 600; color: var(--c-text); }
</style>
