import { http } from './http'

export const tradeApi = {
  me: () => http.get('/auth/me').then((res) => res.data),
  auditLogs: () => http.get('/audit-logs').then((res) => res.data),
  productTypes: () => http.get('/reference/product-types').then((res) => res.data),
  warehouses: () => http.get('/warehouses').then((res) => res.data),
  createWarehouse: (payload) => http.post('/warehouses', payload).then((res) => res.data),
  updateWarehouse: (id, payload) => http.put(`/warehouses/${id}`, payload).then((res) => res.data),
  purchases: (params) => http.get('/purchases', { params }).then((res) => res.data),
  createPurchase: (payload) => http.post('/purchases', payload).then((res) => res.data),
  sales: (params) => http.get('/sales', { params }).then((res) => res.data),
  createSale: (payload) => http.post('/sales', payload).then((res) => res.data),
  updateSettlement: (id, settled) => http.patch(`/sales/${id}/settlement`, { settled }).then((res) => res.data),
  inventory: (params) => http.get('/inventory', { params }).then((res) => res.data),
  monthlyTrend: (params) => http.get('/dashboard/monthly-trend', { params }).then((res) => res.data)
}
