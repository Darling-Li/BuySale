import { defineStore } from 'pinia'
import { http } from '../api/http'

export const useAuthStore = defineStore('auth', {
  state: () => ({
    token: localStorage.getItem('rice_trade_auth_token') || '',
    user: JSON.parse(localStorage.getItem('rice_trade_user') || 'null'),
    loaded: false
  }),
  getters: {
    isAuthenticated: (state) => Boolean(state.token),
    isAdmin: (state) => Boolean(state.user?.admin),
    roleLabel: (state) => state.user?.admin ? '管理员' : '普通角色'
  },
  actions: {
    async login(username, password) {
      this.token = window.btoa(`${username}:${password}`)
      localStorage.setItem('rice_trade_auth_token', this.token)
      await this.loadMe()
    },
    async loadMe() {
      const response = await http.get('/auth/me')
      this.user = response.data
      this.loaded = true
      localStorage.setItem('rice_trade_user', JSON.stringify(this.user))
      return this.user
    },
    logout() {
      this.token = ''
      this.user = null
      this.loaded = false
      localStorage.removeItem('rice_trade_auth_token')
      localStorage.removeItem('rice_trade_user')
    }
  }
})
