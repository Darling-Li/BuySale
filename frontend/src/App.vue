<template>
  <div :class="route.name === 'login' ? 'login-shell' : 'app-shell'">
    <aside v-if="route.name !== 'login'" class="sidebar">
      <div class="brand">
        <div class="brand-mark">米</div>
        <div>
          <strong>稻谷进销存</strong>
          <span>采购 · 入库 · 销售</span>
        </div>
      </div>

      <nav class="nav-list">
        <RouterLink v-for="item in visibleNavItems" :key="item.name" :to="item.to">
          <component :is="item.icon" :size="18" stroke-width="2" />
          <span>{{ item.label }}</span>
        </RouterLink>
      </nav>
    </aside>

    <main :class="route.name === 'login' ? 'login-content' : 'content-shell'">
      <header class="topbar">
        <div>
          <p>当前模块</p>
          <h1>{{ route.meta.title }}</h1>
        </div>
        <div v-if="route.name !== 'login'" class="topbar-actions">
          <div class="topbar-badge">{{ auth.user?.username }} · {{ auth.roleLabel }}</div>
          <UiButton variant="secondary" @click="logout">
            <LogOut :size="17" />
            退出
          </UiButton>
        </div>
      </header>
      <RouterView />
    </main>
  </div>
</template>

<script setup>
import { computed } from 'vue'
import { RouterLink, RouterView, useRoute, useRouter } from 'vue-router'
import { BarChart3, Boxes, ClipboardList, Home, LogOut, PhoneCall, ShoppingBag, Tags, Truck } from 'lucide-vue-next'
import { useAuthStore } from './stores/auth'

const route = useRoute()
const router = useRouter()
const auth = useAuthStore()

const navItems = [
  { name: 'dashboard', to: '/dashboard', label: '经营看板', icon: BarChart3 },
  { name: 'purchases', to: '/purchases', label: '采购入库', icon: Truck },
  { name: 'sales', to: '/sales', label: '销售出库', icon: ShoppingBag },
  { name: 'phoneTransactions', to: '/phone-transactions', label: '手机号查询', icon: PhoneCall },
  { name: 'inventory', to: '/inventory', label: '库存汇总', icon: Boxes },
  { name: 'warehouses', to: '/warehouses', label: '仓库管理', icon: Home },
  { name: 'baseData', to: '/base-data', label: '基础数据', icon: Tags },
  { name: 'auditLogs', to: '/audit-logs', label: '操作日志', icon: ClipboardList, adminOnly: true }
]

const visibleNavItems = computed(() => navItems.filter((item) => !item.adminOnly || auth.isAdmin))

function logout() {
  auth.logout()
  router.push('/login')
}
</script>
