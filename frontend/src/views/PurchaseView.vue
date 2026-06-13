<template>
  <section class="page-stack">
    <div v-if="!auth.isAdmin" class="readonly-note">普通角色仅可查询采购记录。</div>

    <UiPanel v-if="auth.isAdmin" as="form" class="form-panel" title="新增采购" tag="自动入库" @submit.prevent="submit">
      <div class="form-grid">
        <UiField label="商品类型">
          <select v-model="form.productType" required @change="applyDefaultName">
            <option v-for="item in reference.productTypes" :key="item.value" :value="item.value">
              {{ item.label }}
            </option>
          </select>
        </UiField>
        <UiField label="商品名称">
          <input v-model.trim="form.productName" required placeholder="如：稻谷、杂交稻种、复合肥" />
        </UiField>
        <UiField label="二维码内容" wide>
          <div class="scan-input-row">
            <input v-model.trim="form.qrContent" placeholder="识别后自动填入，可手动调整" />
            <QrScanButton @detected="handleQrDetected" />
          </div>
        </UiField>
        <UiField label="入库仓库">
          <select v-model="form.warehouseId" required>
            <option value="" disabled>请选择仓库</option>
            <option v-for="item in reference.warehouses" :key="item.id" :value="item.id">
              {{ item.name }}
            </option>
          </select>
        </UiField>
        <UiField label="采购日期">
          <input v-model="form.purchasedAt" type="date" required />
        </UiField>
        <UiField label="客户姓名">
          <input v-model.trim="form.counterpartyName" required />
        </UiField>
        <UiField label="电话">
          <input v-model.trim="form.counterpartyPhone" />
        </UiField>
        <LocationAddressFields
          v-model:province="form.counterpartyProvince"
          v-model:city="form.counterpartyCity"
          v-model:county="form.counterpartyCounty"
          v-model:address-detail="form.counterpartyAddressDetail"
        />
        <UiField label="数量">
          <input v-model.number="form.quantity" type="number" min="0.01" step="0.01" required />
        </UiField>
        <UiField label="单位">
          <select v-model="form.unitName" required @change="applyUnitPreset">
            <option v-for="item in unitOptions" :key="item.label" :value="item.label">
              {{ item.label }}
            </option>
          </select>
        </UiField>
        <UiField label="每单位折合斤">
          <input v-model.number="form.unitToJin" disabled type="number" min="0.0001" step="0.0001" required />
        </UiField>
        <UiField label="单价模式">
          <select v-model="form.priceMode">
            <option value="YUAN_PER_JIN">一斤多少元</option>
            <option value="MAO_PER_JIN">一斤多少毛</option>
          </select>
        </UiField>
        <UiField :label="form.priceMode === 'MAO_PER_JIN' ? '每斤毛价' : '每斤元价'">
          <input v-model.number="form.priceInput" type="number" min="0.0001" step="0.0001" required />
        </UiField>
        <UiField label="换算结果" wide>
          <input :value="conversionText" disabled />
        </UiField>
        <UiField label="备注" wide>
          <input v-model.trim="form.remark" />
        </UiField>
      </div>

      <div class="actions">
        <UiButton variant="primary" type="submit" :disabled="saving">
          <Save :size="17" />
          保存采购
        </UiButton>
        <UiButton @click="resetForm">
          <RotateCcw :size="17" />
          重置
        </UiButton>
        <ResultMessage :error="!!error">{{ error || message }}</ResultMessage>
      </div>
    </UiPanel>

    <UiToolbar>
      <UiField label="商品类型">
        <select v-model="filters.productType">
          <option value="">全部品类</option>
          <option v-for="item in reference.productTypes" :key="item.value" :value="item.value">
            {{ item.label }}
          </option>
        </select>
      </UiField>
      <UiField label="仓库">
        <select v-model="filters.warehouseId">
          <option value="">全部仓库</option>
          <option v-for="item in reference.warehouses" :key="item.id" :value="item.id">
            {{ item.name }}
          </option>
        </select>
      </UiField>
      <UiField label="关键词">
        <input v-model.trim="filters.keyword" placeholder="商品、姓名、电话" @keyup.enter="loadPurchases" />
      </UiField>
      <UiButton @click="loadPurchases">
        <Search :size="17" />
        查询
      </UiButton>
    </UiToolbar>

    <DataTable title="采购记录" :tag="`${records.length} 条`" min-width="1420px">
      <thead>
        <tr>
          <th>日期</th>
          <th>品类</th>
          <th>商品</th>
          <th>仓库</th>
          <th>客户</th>
          <th>电话</th>
          <th>重量</th>
          <th>单价</th>
          <th>金额</th>
          <th>省</th>
          <th>市</th>
          <th>县</th>
          <th>详细地址</th>
          <th>地址展示</th>
        </tr>
      </thead>
      <tbody>
        <tr v-if="records.length === 0">
          <td class="empty-row" colspan="14">暂无采购记录</td>
        </tr>
        <tr v-for="item in records" :key="item.id">
          <td>{{ item.purchasedAt }}</td>
          <td><UiTag>{{ item.productTypeLabel }}</UiTag></td>
          <td>{{ item.productName }}</td>
          <td>{{ item.warehouseName }}</td>
          <td>{{ item.counterpartyName }}</td>
          <td>{{ item.counterpartyPhone || '-' }}</td>
          <td>{{ number(item.quantity || item.weightJin) }} {{ item.unitName || '斤' }}</td>
          <td>¥{{ money(item.unitPrice || item.pricePerJin) }} / {{ item.unitName || '斤' }}</td>
          <td>¥{{ money(item.totalAmount) }}</td>
          <td>{{ item.counterpartyProvince || '-' }}</td>
          <td>{{ item.counterpartyCity || '-' }}</td>
          <td>{{ item.counterpartyCounty || '-' }}</td>
          <td>{{ item.counterpartyAddressDetail || '-' }}</td>
          <td>{{ item.counterpartyAddressDisplay || purchaseAddress(item) }}</td>
        </tr>
      </tbody>
    </DataTable>
  </section>
</template>

<script setup>
import { computed, onMounted, ref, watch } from 'vue'
import { RotateCcw, Save, Search } from 'lucide-vue-next'
import LocationAddressFields from '../components/LocationAddressFields.vue'
import QrScanButton from '../components/QrScanButton.vue'
import { tradeApi } from '../api/trade'
import { useAuthStore } from '../stores/auth'
import { useReferenceStore } from '../stores/reference'
import { extractError, money, number, today } from '../utils/format'
import {
  convertedPricePerJin,
  convertedWeightJin,
  defaultUnit,
  fallbackUnitOptions,
  unitByLabel,
  unitOptionFromResponse,
  unitPriceFromJinPrice
} from '../utils/units'

const reference = useReferenceStore()
const auth = useAuthStore()
const records = ref([])
const saving = ref(false)
const message = ref('')
const error = ref('')
const filters = ref({
  productType: '',
  warehouseId: '',
  keyword: ''
})
const unitOptions = computed(() => {
  return reference.productUnits.length > 0
    ? reference.productUnits.map(unitOptionFromResponse)
    : fallbackUnitOptions
})
const form = ref(defaultForm())
const weightJin = computed(() => convertedWeightJin(form.value.quantity, form.value.unitToJin))
const unitPrice = computed(() => unitPriceFromJinPrice(form.value.priceInput, form.value.unitToJin, form.value.priceMode))
const pricePerJin = computed(() => convertedPricePerJin(unitPrice.value, form.value.unitToJin))
const conversionText = computed(() => {
  return `${number(weightJin.value)} 斤 / ¥${money(pricePerJin.value)} 每斤 / ¥${money(unitPrice.value)} 每${form.value.unitName || '单位'}`
})

function defaultForm() {
  const unit = defaultUnit(unitOptions.value)
  const productType = reference.productTypes[0]?.value || 'RICE'
  const productName = reference.productTypes[0]?.label || '稻谷'
  return {
    productType,
    productName,
    qrContent: '',
    warehouseId: '',
    counterpartyName: '',
    counterpartyPhone: '',
    counterpartyProvince: '',
    counterpartyCity: '',
    counterpartyCounty: '',
    counterpartyAddressDetail: '',
    quantity: '',
    unitName: unit.label,
    unitToJin: unit.unitToJin,
    priceMode: 'YUAN_PER_JIN',
    priceInput: '',
    purchasedAt: today(),
    remark: ''
  }
}

function cleanParams(values) {
  return Object.fromEntries(Object.entries(values).filter(([, value]) => value !== '' && value !== null))
}

function applyDefaultName() {
  const selected = reference.productTypes.find((item) => item.value === form.value.productType)
  form.value.productName = selected?.label || ''
}

function applyUnitPreset() {
  form.value.unitToJin = unitByLabel(form.value.unitName, unitOptions.value).unitToJin
}

function resetForm() {
  const warehouseId = form.value.warehouseId || reference.warehouses[0]?.id || ''
  form.value = { ...defaultForm(), warehouseId }
  error.value = ''
  message.value = ''
}

async function loadPurchases() {
  records.value = await tradeApi.purchases(cleanParams(filters.value))
}

async function submit() {
  saving.value = true
  error.value = ''
  message.value = ''
  try {
    await tradeApi.createPurchase({
      productType: form.value.productType,
      productName: form.value.productName,
      warehouseId: Number(form.value.warehouseId),
      counterpartyName: form.value.counterpartyName,
      counterpartyPhone: form.value.counterpartyPhone,
      counterpartyProvince: form.value.counterpartyProvince,
      counterpartyCity: form.value.counterpartyCity,
      counterpartyCounty: form.value.counterpartyCounty,
      counterpartyAddressDetail: form.value.counterpartyAddressDetail,
      quantity: form.value.quantity,
      unitName: form.value.unitName,
      unitToJin: form.value.unitToJin,
      unitPrice: unitPrice.value,
      weightJin: weightJin.value,
      pricePerJin: pricePerJin.value,
      purchasedAt: form.value.purchasedAt,
      remark: form.value.remark
    })
    message.value = '采购已保存，库存已入库'
    resetForm()
    await Promise.all([loadPurchases(), reference.loadWarehouses()])
  } catch (err) {
    error.value = extractError(err)
  } finally {
    saving.value = false
  }
}

function handleQrDetected(value) {
  form.value.qrContent = value
  const data = parseQrContent(value)
  applyQrField(data, ['productType', 'type', 'category', '种类', '商品类型'], (matched) => {
    const option = reference.productTypes.find((item) => item.value === matched || item.label === matched)
    if (option) {
      form.value.productType = option.value
      form.value.productName = option.label
    }
  })
  applyQrField(data, ['productName', 'name', 'goodsName', '商品名称', '名称'], (matched) => {
    form.value.productName = matched
  })
  applyQrField(data, ['quantity', '数量'], (matched) => {
    form.value.quantity = Number(matched) || form.value.quantity
  })
  applyQrField(data, ['unitName', 'unit', '单位'], (matched) => {
    const option = unitOptions.value.find((item) => item.label === matched)
    if (option) {
      form.value.unitName = option.label
      form.value.unitToJin = option.unitToJin
    }
  })
  applyQrField(data, ['remark', '备注'], (matched) => {
    form.value.remark = matched
  })
  if (!Object.keys(data).length && !form.value.remark) {
    form.value.remark = `二维码：${value}`
  }
}

function parseQrContent(value) {
  const text = String(value || '').trim()
  if (!text) return {}
  try {
    const parsed = JSON.parse(text)
    return parsed && typeof parsed === 'object' ? parsed : {}
  } catch {
    const queryText = text.includes('?') ? text.slice(text.indexOf('?') + 1) : text
    const separator = queryText.includes('&') ? '&' : queryText.includes(';') ? ';' : null
    if (!separator || !queryText.includes('=')) return {}
    return Object.fromEntries(queryText.split(separator).map((part) => {
      const [key, ...rest] = part.split('=')
      return [decodeURIComponent(key || '').trim(), decodeURIComponent(rest.join('=') || '').trim()]
    }).filter(([key]) => key))
  }
}

function applyQrField(data, keys, action) {
  const key = keys.find((item) => data[item] !== undefined && data[item] !== null && String(data[item]).trim() !== '')
  if (key) action(String(data[key]).trim())
}

function purchaseAddress(item) {
  return [item.counterpartyProvince, item.counterpartyCity, item.counterpartyCounty, item.counterpartyAddressDetail]
    .filter(Boolean)
    .join('') || '-'
}

watch(() => reference.warehouses, (warehouses) => {
  if (!form.value.warehouseId && warehouses.length > 0) {
    form.value.warehouseId = warehouses[0].id
  }
}, { deep: true })

onMounted(async () => {
  await reference.loadAll()
  if (!reference.productTypes.some((item) => item.value === form.value.productType)) {
    form.value.productType = reference.productTypes[0]?.value || ''
    applyDefaultName()
  }
  if (!unitOptions.value.some((item) => item.label === form.value.unitName)) {
    const unit = defaultUnit(unitOptions.value)
    form.value.unitName = unit.label
    form.value.unitToJin = unit.unitToJin
  } else {
    applyUnitPreset()
  }
  if (!form.value.warehouseId && reference.warehouses.length > 0) {
    form.value.warehouseId = reference.warehouses[0].id
  }
  await loadPurchases()
})
</script>
