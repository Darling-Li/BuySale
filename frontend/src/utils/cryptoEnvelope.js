const textEncoder = new TextEncoder()
const textDecoder = new TextDecoder()
const defaultSharedKey = 'lmbcddzxj1314'

const cryptoConfig = {
  enabled: import.meta.env.VITE_CRYPTO_ENABLED !== 'false',
  sharedKey: import.meta.env.VITE_CRYPTO_SHARED_KEY || defaultSharedKey
}

let keyPromise = null

export function apiCryptoEnabled() {
  return cryptoConfig.enabled && Boolean(cryptoConfig.sharedKey) && Boolean(window.crypto?.subtle)
}

export function canEncryptBody(data) {
  return apiCryptoEnabled()
    && data !== undefined
    && data !== null
    && !(data instanceof FormData)
    && !(data instanceof Blob)
    && !(data instanceof ArrayBuffer)
}

export async function encryptPayload(payload) {
  const iv = window.crypto.getRandomValues(new Uint8Array(12))
  const plain = textEncoder.encode(JSON.stringify(payload))
  const encrypted = await window.crypto.subtle.encrypt(
    { name: 'AES-GCM', iv },
    await cryptoKey(),
    plain
  )

  return {
    iv: bytesToBase64(iv),
    data: bytesToBase64(new Uint8Array(encrypted))
  }
}

export async function decryptPayload(envelope) {
  if (!envelope?.iv || !envelope?.data) {
    return envelope
  }

  const plain = await window.crypto.subtle.decrypt(
    { name: 'AES-GCM', iv: base64ToBytes(envelope.iv) },
    await cryptoKey(),
    base64ToBytes(envelope.data)
  )
  return JSON.parse(textDecoder.decode(plain))
}

async function cryptoKey() {
  keyPromise ||= window.crypto.subtle.digest('SHA-256', textEncoder.encode(cryptoConfig.sharedKey))
    .then((digest) => window.crypto.subtle.importKey(
      'raw',
      digest,
      { name: 'AES-GCM' },
      false,
      ['encrypt', 'decrypt']
    ))
  return keyPromise
}

function bytesToBase64(bytes) {
  let binary = ''
  for (let index = 0; index < bytes.length; index += 0x8000) {
    binary += String.fromCharCode(...bytes.subarray(index, index + 0x8000))
  }
  return window.btoa(binary)
}

function base64ToBytes(value) {
  const binary = window.atob(value)
  const bytes = new Uint8Array(binary.length)
  for (let index = 0; index < binary.length; index += 1) {
    bytes[index] = binary.charCodeAt(index)
  }
  return bytes
}
