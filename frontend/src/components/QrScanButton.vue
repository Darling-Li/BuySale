<template>
  <div class="scan-row">
    <UiButton :disabled="scanning" @click="selectImage">
      <QrCode :size="17" />
      {{ scanning ? '识别中' : '识别二维码' }}
    </UiButton>
    <input ref="fileInput" class="hidden-file" type="file" accept="image/*" capture="environment" @change="handleFile" />
    <ResultMessage v-if="error" class="scan-message" :error="true">{{ error }}</ResultMessage>
  </div>
</template>

<script setup>
import { ref } from 'vue'
import { QrCode } from 'lucide-vue-next'

const emit = defineEmits(['detected'])
const fileInput = ref(null)
const scanning = ref(false)
const error = ref('')

function selectImage() {
  error.value = ''
  fileInput.value?.click()
}

async function handleFile(event) {
  const file = event.target.files?.[0]
  event.target.value = ''
  if (!file) return
  if (!('BarcodeDetector' in window)) {
    error.value = '当前浏览器不支持二维码识别'
    return
  }

  scanning.value = true
  try {
    const detector = new window.BarcodeDetector({ formats: ['qr_code'] })
    const bitmap = await createImageBitmap(file)
    const results = await detector.detect(bitmap)
    bitmap.close?.()
    const value = results[0]?.rawValue || ''
    if (!value) {
      error.value = '未识别到二维码'
      return
    }
    emit('detected', value)
  } catch (err) {
    error.value = err?.message || '二维码识别失败'
  } finally {
    scanning.value = false
  }
}
</script>
