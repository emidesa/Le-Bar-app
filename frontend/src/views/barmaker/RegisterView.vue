<template>
  <div class="auth-page">
    <div class="auth-card">
      <h1>Bar'app</h1>
      <h2>Créer un compte Barmaker</h2>

      <form @submit.prevent="handleRegister">
        <div class="field">
          <label>Email</label>
          <input v-model="email" type="email" placeholder="barmaker@barapp.fr" required />
        </div>
        <div class="field">
          <label>Mot de passe</label>
          <input v-model="password" type="password" placeholder="••••••••" required />
        </div>
        <p v-if="error" class="error-msg">{{ error }}</p>
        <button type="submit" :disabled="loading">
          {{ loading ? 'Création...' : 'Créer le compte' }}
        </button>
      </form>

      <p class="auth-link">
        Déjà un compte ?
        <RouterLink to="/barmaker/login">Se connecter</RouterLink>
      </p>
    </div>
  </div>
</template>

<script setup lang="ts">
import { ref } from 'vue'
import { useRouter } from 'vue-router'
import { useAuthStore } from '@/stores/authStore'

const router = useRouter()
const authStore = useAuthStore()

const email = ref('')
const password = ref('')
const error = ref('')
const loading = ref(false)

async function handleRegister() {
  error.value = ''
  loading.value = true
  try {
    await authStore.register(email.value, password.value)
    router.push('/barmaker/orders')
  } catch {
    error.value = 'Erreur lors de la création du compte'
  } finally {
    loading.value = false
  }
}
</script>