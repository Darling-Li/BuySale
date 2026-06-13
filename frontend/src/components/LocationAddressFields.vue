<template>
  <div class="address-fields">
    <UiField label="省份">
      <input :value="province" placeholder="定位后自动填入" @input="emitValue('province', $event.target.value)" />
    </UiField>
    <UiField label="城市">
      <input :value="city" placeholder="定位后自动填入" @input="emitValue('city', $event.target.value)" />
    </UiField>
    <UiField label="区县">
      <input :value="county" placeholder="定位后自动填入" @input="emitValue('county', $event.target.value)" />
    </UiField>
    <UiField label="详细地址" wide>
      <div class="address-detail-row">
        <input :value="addressDetail" placeholder="定位后自动填入，可继续补充门牌" @input="emitValue('addressDetail', $event.target.value)" />
        <UiButton :disabled="locating" @click="locate">
          <MapPin :size="17" />
          {{ locating ? '定位中' : '读取定位' }}
        </UiButton>
      </div>
    </UiField>
    <ResultMessage v-if="message || error" class="address-message" :error="!!error">
      {{ error || message }}
    </ResultMessage>
  </div>
</template>

<script setup>
import { ref } from 'vue'
import { MapPin } from 'lucide-vue-next'
import { tradeApi } from '../api/trade'
import { extractError } from '../utils/format'

defineProps({
  province: {
    type: String,
    default: ''
  },
  city: {
    type: String,
    default: ''
  },
  county: {
    type: String,
    default: ''
  },
  addressDetail: {
    type: String,
    default: ''
  }
})

const emit = defineEmits([
  'update:province',
  'update:city',
  'update:county',
  'update:addressDetail'
])
const locating = ref(false)
const message = ref('')
const error = ref('')

function emitValue(field, value) {
  emit(`update:${field}`, value)
}

async function locate() {
  error.value = ''
  message.value = ''
  if (!window.isSecureContext && window.location.hostname !== 'localhost' && window.location.hostname !== '127.0.0.1') {
    error.value = '手机定位需要 HTTPS 访问'
    return
  }
  if (!navigator.geolocation) {
    error.value = '当前浏览器不支持定位'
    return
  }

  locating.value = true
  try {
    const position = await currentPosition()
    const address = await tradeApi.reverseGeocode({
      latitude: position.coords.latitude,
      longitude: position.coords.longitude
    })
    emitValue('province', address.province || '')
    emitValue('city', address.city || '')
    emitValue('county', address.county || '')
    emitValue('addressDetail', address.addressDetail || '')
    message.value = '定位地址已填入'
  } catch (err) {
    error.value = extractError(err)
  } finally {
    locating.value = false
  }
}

function currentPosition() {
  return new Promise((resolve, reject) => {
    navigator.geolocation.getCurrentPosition(resolve, reject, {
      enableHighAccuracy: true,
      timeout: 12000,
      maximumAge: 30000
    })
  })
}
</script>
