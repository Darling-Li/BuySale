import axios from 'axios'
import { canEncryptBody, decryptPayload, encryptPayload } from '../utils/cryptoEnvelope'

const encryptedHeader = 'X-Encrypted'

export const http = axios.create({
  baseURL: import.meta.env.VITE_API_BASE_URL || '/api',
  timeout: 15000
})

http.interceptors.request.use(async (config) => {
  config.headers ||= {}
  const token = localStorage.getItem('rice_trade_auth_token')
  if (token) {
    config.headers.Authorization = `Basic ${token}`
  }
  if (shouldEncryptRequest(config)) {
    config.data = await encryptPayload(config.data)
    config.headers[encryptedHeader] = '1'
    config.headers['Content-Type'] = 'application/json'
  }
  return config
})

http.interceptors.response.use(async (response) => {
  response = await decryptResponse(response)
  response.data = unwrapApiResponse(response.data)
  return response
}, async (error) => {
  if (error.response) {
    error.response = await decryptResponse(error.response)
  }
  return Promise.reject(error)
})

async function decryptResponse(response) {
  const encrypted = response.headers?.['x-encrypted'] || response.headers?.[encryptedHeader]
  if (encrypted && response.data) {
    response.data = await decryptPayload(response.data)
  }
  return response
}

function unwrapApiResponse(data) {
  if (!isApiResponse(data)) {
    return data
  }
  if (data.code === 200) {
    return data.data
  }
  return data
}

function isApiResponse(data) {
  return data
    && typeof data === 'object'
    && Object.prototype.hasOwnProperty.call(data, 'code')
    && Object.prototype.hasOwnProperty.call(data, 'data')
    && Object.prototype.hasOwnProperty.call(data, 'msg')
}

function shouldEncryptRequest(config) {
  const method = (config.method || 'get').toLowerCase()
  return ['post', 'put', 'patch'].includes(method) && canEncryptBody(config.data)
}
