<template>
  <section class="login-panel">
    <UiPanel class="login-card">
      <div class="brand login-brand">
        <div class="brand-mark">米</div>
        <div>
          <strong>稻谷进销存</strong>
          <span>账号登录</span>
        </div>
      </div>

      <form class="login-form" @submit.prevent="submit">
        <UiField label="用户名">
          <input v-model.trim="username" autocomplete="username" required />
        </UiField>
        <UiField label="密码">
          <input v-model="password" autocomplete="current-password" required type="password" />
        </UiField>
        <UiField label="管理员设备码">
          <input v-model.trim="adminDeviceToken" autocomplete="off" placeholder="普通用户可不填" type="password" />
        </UiField>
        <UiButton variant="primary" type="submit" :disabled="loading">
          <LogIn :size="17" />
          登录
        </UiButton>
        <ResultMessage :error="!!error">{{ error || '管理员：admin/admin123；普通角色：user/user123' }}</ResultMessage>
      </form>
    </UiPanel>
  </section>
</template>

<script setup>
import { ref } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { LogIn } from 'lucide-vue-next'
import { useAuthStore } from '../stores/auth'
import { extractError } from '../utils/format'

const auth = useAuthStore()
const route = useRoute()
const router = useRouter()
const username = ref('admin')
const password = ref('admin123')
const adminDeviceToken = ref(localStorage.getItem('rice_trade_admin_device_token') || '')
const loading = ref(false)
const error = ref('')

async function submit() {
  loading.value = true
  error.value = ''
  try {
    saveAdminDeviceToken()
    await auth.login(username.value, password.value)
    await router.push(route.query.redirect || '/dashboard')
  } catch (err) {
    auth.logout()
    error.value = extractError(err)
  } finally {
    loading.value = false
  }
}

function saveAdminDeviceToken() {
  if (adminDeviceToken.value) {
    localStorage.setItem('rice_trade_admin_device_token', adminDeviceToken.value)
  } else {
    localStorage.removeItem('rice_trade_admin_device_token')
  }
}
</script>
