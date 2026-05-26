import axios from 'axios'

export const http = axios.create({
  baseURL: import.meta.env.VITE_API_BASE_URL || '/api',
  timeout: 15000
})

http.interceptors.request.use((config) => {
  const token = localStorage.getItem('rice_trade_auth_token')
  if (token) {
    config.headers.Authorization = `Basic ${token}`
  }
  return config
})
