<template>
  <section class="page-stack">
    <div v-if="!auth.isAdmin" class="readonly-note">普通角色仅可查询销售记录。</div>

    <UiPanel v-if="auth.isAdmin" as="form" class="form-panel" title="新增销售" tag="自动出库" tag-variant="amber" @submit.prevent="submit">
      <div class="form-grid">
        <UiField label="商品类型">
          <select v-model="form.productType" required @change="handleProductTypeChange">
            <option v-for="item in reference.productTypes" :key="item.value" :value="item.value">
              {{ item.label }}
            </option>
          </select>
        </UiField>
        <UiField label="库存来源" wide>
          <select v-model="form.inventoryItemId" required @change="applyInventorySelection">
            <option value="" disabled>{{ inventoryOptions.length ? '请选择库存来源' : '暂无可用库存' }}</option>
            <option v-for="item in inventoryOptions" :key="item.id" :value="item.id">
              {{ item.warehouseName }} / {{ item.productName }} / 库存 {{ number(item.quantityJin) }} 斤
            </option>
          </select>
        </UiField>
        <UiField label="销售日期">
          <input v-model="form.soldAt" type="date" required />
        </UiField>
        <UiField label="客户姓名">
          <input v-model.trim="form.buyerName" required />
        </UiField>
        <UiField label="电话">
          <input v-model.trim="form.buyerPhone" />
        </UiField>
        <LocationAddressFields
          v-model:province="form.buyerProvince"
          v-model:city="form.buyerCity"
          v-model:county="form.buyerCounty"
          v-model:address-detail="form.buyerAddressDetail"
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
        <UiField label="单位价格">
          <input v-model.number="form.unitPrice" type="number" min="0.0001" step="0.0001" required />
        </UiField>
        <UiField label="换算结果" wide>
          <input :value="conversionText" disabled />
        </UiField>
        <UiField label="备注">
          <input v-model.trim="form.remark" />
        </UiField>
      </div>

      <div class="actions">
        <UiButton variant="primary" type="submit" :disabled="saving">
          <Save :size="17" />
          保存销售
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
        <input v-model.trim="filters.keyword" placeholder="商品、姓名、电话" @keyup.enter="loadSales" />
      </UiField>
      <UiButton @click="loadSales">
        <Search :size="17" />
        查询
      </UiButton>
    </UiToolbar>

    <UiPanel v-if="auth.isAdmin && settlementForm.saleId" as="form" class="form-panel" title="登记结账" :tag="settlementForm.saleLabel" tag-variant="amber" @submit.prevent="submitSettlement">
      <div class="form-grid">
        <UiField label="结账金额">
          <input v-model.number="settlementForm.amount" type="number" min="0.01" step="0.01" required />
        </UiField>
        <UiField label="结账渠道">
          <select v-model="settlementForm.channel" required>
            <option v-for="item in settlementChannels" :key="item.value" :value="item.value">
              {{ item.label }}
            </option>
          </select>
        </UiField>
        <UiField label="结账时间">
          <input v-model="settlementForm.settledAt" type="datetime-local" required />
        </UiField>
        <UiField label="备注" wide>
          <input v-model.trim="settlementForm.remark" />
        </UiField>
      </div>
      <div class="actions">
        <UiButton variant="primary" type="submit" :disabled="settlementSaving">
          <Save :size="17" />
          保存结账
        </UiButton>
        <UiButton @click="resetSettlement">
          <RotateCcw :size="17" />
          取消
        </UiButton>
        <ResultMessage :error="!!settlementError">{{ settlementError || settlementMessage }}</ResultMessage>
      </div>
    </UiPanel>

    <DataTable title="销售记录" :tag="`${records.length} 条`" min-width="1660px">
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
          <th>结账金额</th>
          <th>未结账</th>
          <th>结账渠道</th>
          <th v-if="auth.isAdmin">操作</th>
          <th>省</th>
          <th>市</th>
          <th>县</th>
          <th>详细地址</th>
          <th>地址展示</th>
        </tr>
      </thead>
      <tbody>
        <tr v-if="records.length === 0">
          <td class="empty-row" :colspan="auth.isAdmin ? 18 : 17">暂无销售记录</td>
        </tr>
        <tr v-for="item in records" :key="item.id">
          <td>{{ item.soldAt }}</td>
          <td><UiTag>{{ item.productTypeLabel }}</UiTag></td>
          <td>{{ item.productName }}</td>
          <td>{{ item.warehouseName }}</td>
          <td>{{ item.buyerName }}</td>
          <td>{{ item.buyerPhone || '-' }}</td>
          <td>{{ number(item.quantity || item.weightJin) }} {{ item.unitName || '斤' }}</td>
          <td>¥{{ money(item.unitPrice || item.pricePerJin) }} / {{ item.unitName || '斤' }}</td>
          <td>¥{{ money(item.totalAmount) }}</td>
          <td>¥{{ money(item.settledAmount) }}</td>
          <td>¥{{ money(item.unsettledAmount) }}</td>
          <td>{{ item.settlementChannels || '-' }}</td>
          <td v-if="auth.isAdmin">
            <UiButton variant="ghost" :disabled="Number(item.unsettledAmount || 0) <= 0" @click="startSettlement(item)">
              <Save :size="16" />
              登记
            </UiButton>
          </td>
          <td>{{ item.buyerProvince || '-' }}</td>
          <td>{{ item.buyerCity || '-' }}</td>
          <td>{{ item.buyerCounty || '-' }}</td>
          <td>{{ item.buyerAddressDetail || '-' }}</td>
          <td>{{ item.buyerAddressDisplay || saleAddress(item) }}</td>
        </tr>
      </tbody>
    </DataTable>
  </section>
</template>

<script setup>
import { computed, onMounted, ref } from 'vue'
import { RotateCcw, Save, Search } from 'lucide-vue-next'
import LocationAddressFields from '../components/LocationAddressFields.vue'
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
  unitOptionFromResponse
} from '../utils/units'

const reference = useReferenceStore()
const auth = useAuthStore()
const records = ref([])
const inventoryOptions = ref([])
const saving = ref(false)
const settlementSaving = ref(false)
const message = ref('')
const error = ref('')
const settlementMessage = ref('')
const settlementError = ref('')
const filters = ref({
  productType: '',
  warehouseId: '',
  keyword: ''
})
const settlementChannels = [
  { value: 'BANK_CARD', label: '银行卡' },
  { value: 'TRANSFER', label: '微信/支付宝转账' },
  { value: 'CASH', label: '现金' }
]
const unitOptions = computed(() => {
  return reference.productUnits.length > 0
    ? reference.productUnits.map(unitOptionFromResponse)
    : fallbackUnitOptions
})
const form = ref(defaultForm())
const settlementForm = ref(defaultSettlementForm())
const weightJin = computed(() => convertedWeightJin(form.value.quantity, form.value.unitToJin))
const pricePerJin = computed(() => convertedPricePerJin(form.value.unitPrice, form.value.unitToJin))
const conversionText = computed(() => {
  return `${number(weightJin.value)} 斤 / ¥${money(pricePerJin.value)} 每斤`
})

function defaultForm() {
  const unit = defaultUnit(unitOptions.value)
  const productType = reference.productTypes[0]?.value || 'RICE'
  const productName = reference.productTypes[0]?.label || ''
  return {
    productType,
    inventoryItemId: '',
    productName,
    warehouseId: '',
    buyerName: '',
    buyerPhone: '',
    buyerProvince: '',
    buyerCity: '',
    buyerCounty: '',
    buyerAddressDetail: '',
    quantity: '',
    unitName: unit.label,
    unitToJin: unit.unitToJin,
    unitPrice: '',
    soldAt: today(),
    remark: ''
  }
}

function defaultSettlementForm() {
  return {
    saleId: null,
    saleLabel: '',
    amount: '',
    channel: 'TRANSFER',
    settledAt: nowDateTimeLocal(),
    remark: ''
  }
}

function cleanParams(values) {
  return Object.fromEntries(Object.entries(values).filter(([, value]) => value !== '' && value !== null))
}

async function handleProductTypeChange() {
  form.value.inventoryItemId = ''
  form.value.productName = productTypeLabel(form.value.productType)
  form.value.warehouseId = ''
  await loadInventoryOptions()
  applyFirstInventory()
}

function applyUnitPreset() {
  form.value.unitToJin = unitByLabel(form.value.unitName, unitOptions.value).unitToJin
}

async function resetForm() {
  const productType = form.value.productType || reference.productTypes[0]?.value || ''
  form.value = { ...defaultForm(), productType }
  form.value.productName = productTypeLabel(productType)
  error.value = ''
  message.value = ''
  await loadInventoryOptions()
  applyFirstInventory()
}

async function loadSales() {
  records.value = await tradeApi.sales(cleanParams(filters.value))
}

function startSettlement(item) {
  settlementForm.value = {
    saleId: item.id,
    saleLabel: `${item.buyerName} / ${item.productName}`,
    amount: Number(item.unsettledAmount || 0),
    channel: 'TRANSFER',
    settledAt: nowDateTimeLocal(),
    remark: ''
  }
  settlementError.value = ''
  settlementMessage.value = ''
}

function resetSettlement() {
  settlementForm.value = defaultSettlementForm()
  settlementError.value = ''
  settlementMessage.value = ''
}

async function loadInventoryOptions() {
  if (!form.value.productType) {
    inventoryOptions.value = []
    return
  }
  const items = await tradeApi.inventory({ productType: form.value.productType })
  inventoryOptions.value = items.filter((item) => Number(item.quantityJin || 0) > 0)
}

function applyFirstInventory() {
  if (inventoryOptions.value.length === 1) {
    form.value.inventoryItemId = inventoryOptions.value[0].id
    applyInventorySelection()
  }
}

function applyInventorySelection() {
  const selected = inventoryOptions.value.find((item) => item.id === Number(form.value.inventoryItemId))
  if (!selected) {
    form.value.productName = productTypeLabel(form.value.productType)
    form.value.warehouseId = ''
    return
  }
  form.value.productName = selected.productName
  form.value.warehouseId = selected.warehouseId
}

async function submit() {
  saving.value = true
  error.value = ''
  message.value = ''
  try {
    applyInventorySelection()
    await tradeApi.createSale({
      ...form.value,
      inventoryItemId: form.value.inventoryItemId ? Number(form.value.inventoryItemId) : null,
      warehouseId: form.value.warehouseId ? Number(form.value.warehouseId) : null,
      weightJin: weightJin.value,
      pricePerJin: pricePerJin.value
    })
    message.value = '销售已保存，库存已出库'
    await resetForm()
    await Promise.all([loadSales(), loadInventoryOptions()])
  } catch (err) {
    error.value = extractError(err)
  } finally {
    saving.value = false
  }
}

async function submitSettlement() {
  settlementSaving.value = true
  settlementError.value = ''
  settlementMessage.value = ''
  try {
    const updated = await tradeApi.createSaleSettlement(settlementForm.value.saleId, {
      amount: settlementForm.value.amount,
      channel: settlementForm.value.channel,
      settledAt: settlementForm.value.settledAt,
      remark: settlementForm.value.remark
    })
    const index = records.value.findIndex((item) => item.id === updated.id)
    if (index >= 0) {
      records.value[index] = updated
    }
    settlementMessage.value = '结账已登记'
    resetSettlement()
  } catch (err) {
    settlementError.value = extractError(err)
  } finally {
    settlementSaving.value = false
  }
}

function productTypeLabel(value) {
  return reference.productTypes.find((item) => item.value === value)?.label || ''
}

function nowDateTimeLocal() {
  const date = new Date()
  date.setMinutes(date.getMinutes() - date.getTimezoneOffset())
  return date.toISOString().slice(0, 16)
}

function saleAddress(item) {
  return [item.buyerProvince, item.buyerCity, item.buyerCounty, item.buyerAddressDetail]
    .filter(Boolean)
    .join('') || '-'
}

onMounted(async () => {
  await reference.loadAll()
  if (!reference.productTypes.some((item) => item.value === form.value.productType)) {
    form.value.productType = reference.productTypes[0]?.value || ''
    form.value.productName = productTypeLabel(form.value.productType)
  }
  if (!unitOptions.value.some((item) => item.label === form.value.unitName)) {
    const unit = defaultUnit(unitOptions.value)
    form.value.unitName = unit.label
    form.value.unitToJin = unit.unitToJin
  } else {
    applyUnitPreset()
  }
  await loadInventoryOptions()
  applyFirstInventory()
  await loadSales()
})
</script>
