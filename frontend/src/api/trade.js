import { http } from './http'

export const tradeApi = {
  me: () => http.get('/auth/me').then((res) => res.data),
  auditLogs: () => http.get('/audit-logs').then((res) => res.data),
  productTypes: () => http.get('/reference/product-types').then((res) => res.data),
  productCategories: () => http.get('/product-categories').then((res) => res.data),
  createProductCategory: (payload) => http.post('/product-categories', payload).then((res) => res.data),
  updateProductCategory: (id, payload) => http.put(`/product-categories/${id}`, payload).then((res) => res.data),
  productUnits: () => http.get('/product-units').then((res) => res.data),
  referenceUnits: () => http.get('/reference/units').then((res) => res.data),
  createProductUnit: (payload) => http.post('/product-units', payload).then((res) => res.data),
  updateProductUnit: (id, payload) => http.put(`/product-units/${id}`, payload).then((res) => res.data),
  warehouses: () => http.get('/warehouses').then((res) => res.data),
  createWarehouse: (payload) => http.post('/warehouses', payload).then((res) => res.data),
  updateWarehouse: (id, payload) => http.put(`/warehouses/${id}`, payload).then((res) => res.data),
  purchases: (params) => http.get('/purchases', { params }).then((res) => res.data),
  createPurchase: (payload) => http.post('/purchases', payload).then((res) => res.data),
  sales: (params) => http.get('/sales', { params }).then((res) => res.data),
  createSale: (payload) => http.post('/sales', payload).then((res) => res.data),
  createSaleSettlement: (id, payload) => http.post(`/sales/${id}/settlements`, payload).then((res) => res.data),
  updateSettlement: (id, settled) => http.patch(`/sales/${id}/settlement`, { settled }).then((res) => res.data),
  phoneTransactions: (params) => http.get('/phone-transactions', { params }).then((res) => res.data),
  inventory: (params) => http.get('/inventory', { params }).then((res) => res.data),
  monthlyTrend: (params) => http.get('/dashboard/monthly-trend', { params }).then((res) => res.data)
}
