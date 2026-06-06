import { defineStore } from 'pinia'
import { tradeApi } from '../api/trade'

export const useReferenceStore = defineStore('reference', {
  state: () => ({
    productTypes: [],
    productUnits: [],
    warehouses: [],
    loading: false
  }),
  actions: {
    async loadAll() {
      this.loading = true
      try {
        const [productTypes, productUnits, warehouses] = await Promise.all([
          tradeApi.productTypes(),
          tradeApi.referenceUnits(),
          tradeApi.warehouses()
        ])
        this.productTypes = productTypes
        this.productUnits = productUnits
        this.warehouses = warehouses
      } finally {
        this.loading = false
      }
    },
    async loadProductTypes() {
      this.productTypes = await tradeApi.productTypes()
    },
    async loadProductUnits() {
      this.productUnits = await tradeApi.referenceUnits()
    },
    async loadWarehouses() {
      this.warehouses = await tradeApi.warehouses()
    }
  }
})
