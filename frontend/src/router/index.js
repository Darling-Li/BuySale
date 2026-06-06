import { createRouter, createWebHistory } from 'vue-router'
import DashboardView from '../views/DashboardView.vue'
import PurchaseView from '../views/PurchaseView.vue'
import SaleView from '../views/SaleView.vue'
import PhoneTransactionView from '../views/PhoneTransactionView.vue'
import InventoryView from '../views/InventoryView.vue'
import WarehouseView from '../views/WarehouseView.vue'
import ProductCategoryView from '../views/ProductCategoryView.vue'
import AuditLogView from '../views/AuditLogView.vue'
import LoginView from '../views/LoginView.vue'
import { useAuthStore } from '../stores/auth'

const routes = [
  { path: '/', redirect: '/dashboard' },
  { path: '/login', name: 'login', component: LoginView, meta: { title: '登录' } },
  { path: '/dashboard', name: 'dashboard', component: DashboardView, meta: { title: '经营看板' } },
  { path: '/purchases', name: 'purchases', component: PurchaseView, meta: { title: '采购入库' } },
  { path: '/sales', name: 'sales', component: SaleView, meta: { title: '销售出库' } },
  { path: '/phone-transactions', name: 'phoneTransactions', component: PhoneTransactionView, meta: { title: '手机号查询' } },
  { path: '/inventory', name: 'inventory', component: InventoryView, meta: { title: '库存汇总' } },
  { path: '/warehouses', name: 'warehouses', component: WarehouseView, meta: { title: '仓库管理' } },
  { path: '/base-data', name: 'baseData', component: ProductCategoryView, meta: { title: '基础数据' } },
  { path: '/audit-logs', name: 'auditLogs', component: AuditLogView, meta: { title: '操作日志', adminOnly: true } }
]

const router = createRouter({
  history: createWebHistory(),
  routes
})

router.beforeEach(async (to) => {
  const auth = useAuthStore()

  if (to.name === 'login') {
    return true
  }

  if (!auth.isAuthenticated) {
    return { name: 'login', query: { redirect: to.fullPath } }
  }

  if (!auth.loaded) {
    try {
      await auth.loadMe()
    } catch {
      auth.logout()
      return { name: 'login', query: { redirect: to.fullPath } }
    }
  }

  if (to.meta.adminOnly && !auth.isAdmin) {
    return { name: 'dashboard' }
  }

  return true
})

export default router
