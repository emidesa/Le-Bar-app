<template>
  <div class="login-page">

    <!-- Colonne gauche : visuel -->
    <div class="login-visual">
      <div class="visual-content">
        <div class="brand-icon">🍸</div>
        <h1 class="brand-title">Bar'app</h1>
        <p class="brand-sub">BAR À COCKTAILS</p>
        <p class="brand-desc">L'espace de travail de vos barmakers pour gérer les commandes en temps réel.</p>
      </div>
    </div>

    <!-- Colonne droite : formulaire -->
    <div class="login-form-col">
      <div class="login-card">
        <p class="login-eyebrow">ESPACE BARMAKER</p>
        <h2 class="login-title">Connexion</h2>
        <p class="login-hint">Connectez-vous pour accéder au tableau de bord.</p>

        <form @submit.prevent="handleLogin" class="form">
          <div class="field">
            <label>Email</label>
            <input v-model="email" type="email" placeholder="barmaker@barapp.fr" required autocomplete="username" />
          </div>
          <div class="field">
            <label>Mot de passe</label>
            <input v-model="password" type="password" placeholder="••••••••" required autocomplete="current-password" />
          </div>

          <p v-if="error" class="error-msg">{{ error }}</p>

          <button type="submit" class="submit-btn" :disabled="loading">
            {{ loading ? 'Connexion...' : 'Se connecter' }}
          </button>
        </form>

        <p class="register-link">
          Première connexion ?
          <RouterLink to="/barmaker/register">Créer un compte</RouterLink>
        </p>
      </div>
    </div>

  </div>
</template>

<script setup lang="ts">
import { ref } from 'vue'
import { useRouter } from 'vue-router'
import { useAuthStore } from '@/stores/authStore'

const router    = useRouter()
const authStore = useAuthStore()

const email    = ref('')
const password = ref('')
const error    = ref('')
const loading  = ref(false)

async function handleLogin() {
  error.value   = ''
  loading.value = true
  try {
    await authStore.login(email.value, password.value)
    router.push('/barmaker/orders')
  } catch {
    error.value = 'Email ou mot de passe incorrect'
  } finally {
    loading.value = false
  }
}
</script>

<style scoped>
.login-page {
  --c-bg:     #ede8df;
  --c-card:   #ffffff;
  --c-text:   #2c2520;
  --c-muted:  #8a7f74;
  --c-accent: #8b3230;
  --c-border: #ccc8bc;
  --c-visual: #2c2520;

  display: flex;
  min-height: 100vh;
  font-family: 'Georgia', serif;
}

/* Colonne gauche */
.login-visual {
  flex: 1;
  background: var(--c-visual);
  display: flex;
  align-items: center;
  justify-content: center;
  padding: 3rem;
}
.visual-content { text-align: center; color: white; }
.brand-icon { font-size: 3.5rem; margin-bottom: 1rem; }
.brand-title {
  font-size: 2.5rem; font-weight: 700;
  margin: 0 0 0.3rem; color: white;
}
.brand-sub {
  font-family: sans-serif; font-size: 0.7rem; letter-spacing: 0.18em;
  color: rgba(255,255,255,0.5); margin: 0 0 2.5rem;
}
.brand-desc {
  font-family: sans-serif; font-size: 0.9rem; color: rgba(255,255,255,0.65);
  max-width: 280px; line-height: 1.6; margin: 0 auto;
}

/* Colonne droite */
.login-form-col {
  width: 460px;
  background: var(--c-bg);
  display: flex;
  align-items: center;
  justify-content: center;
  padding: 3rem 2.5rem;
}
.login-card { width: 100%; max-width: 360px; }

.login-eyebrow {
  font-family: sans-serif; font-size: 0.68rem; letter-spacing: 0.14em;
  color: var(--c-accent); margin: 0 0 0.5rem;
}
.login-title {
  font-size: 2rem; font-weight: 700;
  color: var(--c-text); margin: 0 0 0.4rem;
}
.login-hint {
  font-family: sans-serif; font-size: 0.85rem;
  color: var(--c-muted); margin: 0 0 2rem;
}

/* Formulaire */
.form { display: flex; flex-direction: column; gap: 1rem; }
.field { display: flex; flex-direction: column; gap: 0.35rem; }
.field label {
  font-family: sans-serif; font-size: 0.78rem;
  font-weight: 600; color: var(--c-text);
}
.field input {
  padding: 0.75rem 1rem;
  border: 1px solid var(--c-border);
  border-radius: 8px;
  background: white;
  font-family: sans-serif; font-size: 0.9rem;
  color: var(--c-text);
  outline: none;
  transition: border-color 0.15s;
}
.field input:focus { border-color: var(--c-accent); }
.field input::placeholder { color: #bbb; }

.error-msg {
  font-family: sans-serif; font-size: 0.82rem;
  color: var(--c-accent); margin: 0;
}

.submit-btn {
  margin-top: 0.5rem;
  width: 100%; padding: 0.85rem;
  background: var(--c-accent); color: white;
  border: none; border-radius: 8px;
  font-family: sans-serif; font-size: 0.95rem; font-weight: 600;
  cursor: pointer;
  transition: opacity 0.15s;
}
.submit-btn:hover:not(:disabled) { opacity: 0.88; }
.submit-btn:disabled { opacity: 0.5; cursor: not-allowed; }

.register-link {
  font-family: sans-serif; font-size: 0.82rem;
  color: var(--c-muted); margin: 1.5rem 0 0; text-align: center;
}
.register-link a { color: var(--c-accent); text-decoration: none; font-weight: 600; }
.register-link a:hover { text-decoration: underline; }
</style>
