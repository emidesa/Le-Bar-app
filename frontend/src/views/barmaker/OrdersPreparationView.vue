<template>
  <div class="prep-page">

    <header class="bm-nav">
      <div class="nav-brand">
        <div class="brand-icon">🍸</div>
        <div>
          <p class="brand-name">Bar'app</p>
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

        <!-- Timer (s'arrête à DRESSAGE) -->
        <div :class="['timer-card', timerClass]">
          <div class="timer-top">
            <div>
              <p class="timer-label">{{ timerLabel }}</p>
              <p class="timer-display">{{ timerDisplay }}</p>
            </div>
            <div class="timer-phases">
              <div v-for="(ph, i) in phases" :key="ph.key" :class="['ph-item', phaseClass(i + 1)]">
                <div class="ph-dot"></div>
                <span class="ph-lbl">{{ ph.label }}</span>
              </div>
            </div>
          </div>
          <div class="bar-bg">
            <div class="bar-fill" :style="{ width: progressPercent + '%' }"></div>
          </div>
          <p class="timer-hint">{{ timerHint }}</p>
        </div>

        <!-- Liste des items avec bouton par item -->
        <div class="items-list">
          <div v-for="item in order.items" :key="item.id" class="item-row">
            <div class="item-left">
              <div class="item-icon">🍸</div>
              <div>
                <p class="item-name">{{ item.cocktailName }}</p>
                <p class="item-meta">{{ item.size }} · {{ item.quantity }}x</p>
              </div>
            </div>

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
                class="finish-item-btn"
                :disabled="finishingItem === item.id"
                @click="finishItem(item)"
                title="Terminer ce cocktail"
              >✓</button>
              <span v-else class="done-check">✓</span>
            </div>
          </div>
        </div>

        <!-- Bouton Tout finaliser -->
        <div class="finish-section">
          <button
            class="finish-all-btn"
            :disabled="!canFinish || finishing"
            @click="validateFinished"
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
import { ref, computed, watch, onMounted, onUnmounted } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { useOrderStore } from '@/stores/orderStore'
import { useAuthStore } from '@/stores/authStore'

const route      = useRoute()
const router     = useRouter()
const orderStore = useOrderStore()
const authStore  = useAuthStore()

const orderId     = Number(route.params.id)
const loading     = ref(true)
const error       = ref('')
const finishing   = ref(false)
const finishingItem = ref<number | null>(null)
const validated   = ref(false)
const order       = ref<any>(null)

// ── Timer ─────────────────────────────────────────────
const TOTAL        = 300  // 5 min
const PHASE        = 100  // 100s par phase auto

const timerStart    = ref(0)
const elapsed       = ref(0)
const advancedPhase = ref(0)
let   iv: ReturnType<typeof setInterval> | null = null

// Timer s'arrête à DRESSAGE (phase 3 = 200s), pas à TERMINEE
const timerDone = computed(() => elapsed.value >= PHASE * 2)

const remaining = computed(() => Math.max(0, PHASE * 2 - elapsed.value))

const timerDisplay = computed(() => {
  if (timerDone.value) return 'DRESSAGE'
  const m = Math.floor(remaining.value / 60)
  const s = remaining.value % 60
  return `${m}:${s.toString().padStart(2, '0')}`
})

// Barre : 0→80% pendant le timer auto (jusqu'à DRESSAGE), 100% après validation
const progressPercent = computed(() => {
  if (validated.value || order.value?.status === 'TERMINEE') return 100
  return Math.min(80, (elapsed.value / (PHASE * 2)) * 80)
})

const currentPhase = computed(() => {
  if (elapsed.value === 0)       return 0
  if (elapsed.value < PHASE)     return 1
  if (elapsed.value < PHASE * 2) return 2
  return 3
})

const timerClass = computed(() => {
  if (validated.value || order.value?.status === 'TERMINEE') return 'done'
  if (timerDone.value) return 'ready'
  return ''
})

const timerLabel = computed(() => {
  if (validated.value || order.value?.status === 'TERMINEE') return 'Commande terminée'
  if (timerDone.value) return 'Prêt pour finalisation'
  return 'Préparation automatique'
})

const timerHint = computed(() => {
  if (validated.value || order.value?.status === 'TERMINEE') return 'La commande a été transmise au client.'
  if (timerDone.value) return 'Tous les cocktails sont au dressage. Finalisez manuellement.'
  return 'Les étapes s\'enchaînent automatiquement jusqu\'au dressage.'
})

const phases = [
  { key: 'PREPARATION_INGREDIENTS', label: 'Préparation' },
  { key: 'ASSEMBLAGE',              label: 'Assemblage' },
  { key: 'DRESSAGE',                label: 'Dressage' },
]

function phaseClass(i: number) {
  if (currentPhase.value > i) return 'done'
  if (currentPhase.value === i) return 'active'
  return 'pending'
}

// ── Steppers ──────────────────────────────────────────
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
  if (idx < cur) return 'done'
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
    ATTENTE: 'Attente',
    PREPARATION_INGREDIENTS: 'Préparation',
    ASSEMBLAGE: 'Assemblage',
    DRESSAGE: 'Dressage',
    TERMINEE: 'Prêt',
  }[s] ?? s
}

// ── Timer logic ───────────────────────────────────────
function initTimer() {
  // Le timer part de la création de la commande → toutes les commandes tournent en parallèle
  const created = order.value?.createdAt ? new Date(order.value.createdAt).getTime() : Date.now()
  timerStart.value = created
  iv = setInterval(tick, 1000)
  tick()
}

async function tick() {
  const el = Math.floor((Date.now() - timerStart.value) / 1000)
  elapsed.value = Math.min(el, TOTAL)

  if (elapsed.value >= 0 && advancedPhase.value < 1) {
    advancedPhase.value = 1
    await autoAdvance('ATTENTE')
  }
  if (elapsed.value >= PHASE && advancedPhase.value < 2) {
    advancedPhase.value = 2
    await autoAdvance('PREPARATION_INGREDIENTS')
  }
  // Arrêt automatique à DRESSAGE — la suite est manuelle
  if (elapsed.value >= PHASE * 2 && advancedPhase.value < 3) {
    advancedPhase.value = 3
    await autoAdvance('ASSEMBLAGE')
    if (iv) { clearInterval(iv); iv = null }
  }
}

async function autoAdvance(fromStatus: string) {
  if (!order.value?.items?.length) return
  const targets = order.value.items.filter((i: any) => i.status === fromStatus)
  for (const item of targets) {
    try { await orderStore.advanceItem(item.id) } catch { /* silencieux */ }
  }
  if (targets.length > 0) await load(false)
}

// ── Redirection automatique quand la commande est terminée ────────────────
watch(
  () => order.value?.status,
  (status) => {
    if (status === 'TERMINEE') {
      if (iv) { clearInterval(iv); iv = null }
      router.push('/barmaker/orders')
    }
  }
)

// ── Actions manuelles ─────────────────────────────────
const canFinish = computed(() =>
  !!order.value && order.value.items?.some((i: any) => i.status !== 'TERMINEE')
)

async function finishItem(item: any) {
  finishingItem.value = item.id
  error.value = ''
  try {
    // On avance l'item jusqu'à TERMINEE en se basant sur la réponse du serveur
    let result = await orderStore.advanceItem(item.id)
    while (result?.status !== 'TERMINEE') {
      result = await orderStore.advanceItem(item.id)
    }
    await load(false)
  } catch (e: any) {
    error.value = `Erreur lors de la mise à jour (${e?.response?.status ?? 'réseau'})`
  } finally {
    finishingItem.value = null
  }
}

async function validateFinished() {
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
    validated.value = true
    if (iv) { clearInterval(iv); iv = null }
    await load(false)
    // La redirection est gérée par le watcher sur order.value.status
  } catch (e: any) {
    error.value = `Erreur lors de la finalisation (${e?.response?.status ?? 'réseau'})`
  } finally {
    finishing.value = false
  }
}

// ── Chargement ────────────────────────────────────────
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

onMounted(async () => {
  await load()
  if (order.value) initTimer()
})

onUnmounted(() => {
  if (iv) clearInterval(iv)
})
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

/* Timer card */
.timer-card {
  background: white; border: 1px solid var(--c-border);
  border-radius: 14px; padding: 1.5rem 1.75rem; margin-bottom: 1.5rem;
}
.timer-card.ready { border-color: #e8a020; }
.timer-card.done  { border-color: var(--c-green); }

.timer-top { display: flex; justify-content: space-between; align-items: center; margin-bottom: 1rem; }
.timer-label { font-family: sans-serif; font-size: 0.7rem; letter-spacing: 0.1em; color: var(--c-muted); margin: 0 0 0.2rem; }
.timer-display {
  font-size: 2.4rem; font-weight: 700; color: var(--c-text); margin: 0; line-height: 1;
}
.timer-card.ready .timer-display { color: #a06b10; }
.timer-card.done  .timer-display { color: var(--c-green); font-size: 1.4rem; }

.timer-phases { display: flex; gap: 1.5rem; }
.ph-item { display: flex; flex-direction: column; align-items: center; gap: 0.35rem; }
.ph-dot {
  width: 12px; height: 12px; border-radius: 50%;
  border: 2px solid var(--c-border); background: white; transition: all 0.3s;
}
.ph-item.active .ph-dot { border-color: var(--c-accent); background: var(--c-accent); }
.ph-item.done   .ph-dot { border-color: var(--c-green);  background: var(--c-green); }
.ph-lbl { font-family: sans-serif; font-size: 0.68rem; color: var(--c-muted); }
.ph-item.active .ph-lbl { color: var(--c-accent); font-weight: 600; }
.ph-item.done   .ph-lbl { color: var(--c-green); }

.bar-bg { height: 6px; background: var(--c-card); border-radius: 3px; margin-bottom: 0.75rem; }
.bar-fill { height: 100%; background: var(--c-accent); border-radius: 3px; transition: width 1s linear; }
.timer-card.done .bar-fill { background: var(--c-green); }

.timer-hint { font-family: sans-serif; font-size: 0.78rem; color: var(--c-muted); margin: 0; }

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

/* Côté droit item */
.item-right { display: flex; align-items: center; gap: 0.6rem; flex-shrink: 0; }
.item-badge {
  font-family: sans-serif; font-size: 0.68rem; font-weight: 600;
  padding: 0.2rem 0.55rem; border-radius: 10px; white-space: nowrap;
}
.item-badge.attente                 { background: #f5f0e8; color: var(--c-muted); }
.item-badge.preparation_ingredients { background: #f3e8fd; color: #7b3fa0; }
.item-badge.assemblage              { background: #e8f0fd; color: #2a5ea8; }
.item-badge.dressage                { background: #fdf4e8; color: #a06b10; }
.item-badge.terminee                { background: #f0fdf4; color: var(--c-green); }

.finish-item-btn {
  width: 28px; height: 28px; border-radius: 6px;
  background: var(--c-green); color: white; border: none;
  font-size: 0.85rem; font-weight: 700; cursor: pointer; flex-shrink: 0;
}
.finish-item-btn:disabled { opacity: 0.4; cursor: not-allowed; }
.finish-item-btn:hover:not(:disabled) { opacity: 0.85; }
.done-check { color: var(--c-green); font-weight: 700; font-size: 1rem; width: 28px; text-align: center; }

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
