import { defineStore } from 'pinia'
import { tradeApi } from '../api/trade'

export const useReferenceStore = defineStore('reference', {
  state: () => ({
    productTypes: [],
    warehouses: [],
    loading: false
  }),
  actions: {
    async loadAll() {
      this.loading = true
      try {
        const [productTypes, warehouses] = await Promise.all([
          tradeApi.productTypes(),
          tradeApi.warehouses()
        ])
        this.productTypes = productTypes
        this.warehouses = warehouses
      } finally {
        this.loading = false
      }
    },
    async loadWarehouses() {
      this.warehouses = await tradeApi.warehouses()
    }
  }
})

