<template>
  <section class="page-stack">
    <div v-if="!auth.isAdmin" class="readonly-note">普通角色仅可查询销售记录。</div>

    <form v-if="auth.isAdmin" class="form-panel" @submit.prevent="submit">
      <div class="form-title">
        <h2>新增销售</h2>
        <span class="tag amber">自动出库</span>
      </div>

      <div class="form-grid">
        <label class="field">
          <span>商品类型</span>
          <select v-model="form.productType" required @change="handleProductTypeChange">
            <option v-for="item in reference.productTypes" :key="item.value" :value="item.value">
              {{ item.label }}
            </option>
          </select>
        </label>
        <label class="field wide">
          <span>库存来源</span>
          <select v-model="form.inventoryItemId" required @change="applyInventorySelection">
            <option value="" disabled>{{ inventoryOptions.length ? '请选择库存来源' : '暂无可用库存' }}</option>
            <option v-for="item in inventoryOptions" :key="item.id" :value="item.id">
              {{ item.warehouseName }} / {{ item.productName }} / 库存 {{ number(item.quantityJin) }} 斤
            </option>
          </select>
        </label>
        <label class="field">
          <span>销售日期</span>
          <input v-model="form.soldAt" type="date" required />
        </label>
        <label class="field">
          <span>客户姓名</span>
          <input v-model.trim="form.buyerName" required />
        </label>
        <label class="field">
          <span>电话</span>
          <input v-model.trim="form.buyerPhone" />
        </label>
        <label class="field wide">
          <span>家庭住址</span>
          <input v-model.trim="form.buyerAddress" />
        </label>
        <label class="field">
          <span>数量</span>
          <input v-model.number="form.quantity" type="number" min="0.01" step="0.01" required />
        </label>
        <label class="field">
          <span>单位</span>
          <select v-model="form.unitName" required @change="applyUnitPreset">
            <option v-for="item in unitOptions" :key="item.label" :value="item.label">
              {{ item.label }}
            </option>
          </select>
        </label>
        <label class="field">
          <span>每单位折合斤</span>
          <input v-model.number="form.unitToJin" type="number" min="0.0001" step="0.0001" required />
        </label>
        <label class="field">
          <span>单位价格</span>
          <input v-model.number="form.unitPrice" type="number" min="0.0001" step="0.0001" required />
        </label>
        <label class="field wide">
          <span>换算结果</span>
          <input :value="conversionText" disabled />
        </label>
        <label class="field">
          <span>是否结账</span>
          <select v-model="form.settled">
            <option :value="false">未结账</option>
            <option :value="true">已结账</option>
          </select>
        </label>
        <label class="field">
          <span>备注</span>
          <input v-model.trim="form.remark" />
        </label>
      </div>

      <div class="actions">
        <button class="btn primary" type="submit" :disabled="saving">
          <Save :size="17" />
          保存销售
        </button>
        <button class="btn secondary" type="button" @click="resetForm">
          <RotateCcw :size="17" />
          重置
        </button>
        <span class="message" :class="{ error: !!error }">{{ error || message }}</span>
      </div>
    </form>

    <div class="toolbar">
      <label class="field">
        <span>商品类型</span>
        <select v-model="filters.productType">
          <option value="">全部品类</option>
          <option v-for="item in reference.productTypes" :key="item.value" :value="item.value">
            {{ item.label }}
          </option>
        </select>
      </label>
      <label class="field">
        <span>仓库</span>
        <select v-model="filters.warehouseId">
          <option value="">全部仓库</option>
          <option v-for="item in reference.warehouses" :key="item.id" :value="item.id">
            {{ item.name }}
          </option>
        </select>
      </label>
      <label class="field">
        <span>结算</span>
        <select v-model="filters.settled">
          <option value="">全部</option>
          <option :value="true">已结账</option>
          <option :value="false">未结账</option>
        </select>
      </label>
      <label class="field">
        <span>关键词</span>
        <input v-model.trim="filters.keyword" placeholder="商品、姓名、电话" @keyup.enter="loadSales" />
      </label>
      <button class="btn secondary" type="button" @click="loadSales">
        <Search :size="17" />
        查询
      </button>
    </div>

    <div class="table-panel">
      <div class="table-title">
        <h2>销售记录</h2>
        <span class="tag blue">{{ records.length }} 条</span>
      </div>
      <div class="table-wrap">
        <table>
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
              <th>结算</th>
              <th>住址</th>
            </tr>
          </thead>
          <tbody>
            <tr v-if="records.length === 0">
              <td class="empty-row" colspan="11">暂无销售记录</td>
            </tr>
            <tr v-for="item in records" :key="item.id">
              <td>{{ item.soldAt }}</td>
              <td><span class="tag">{{ item.productTypeLabel }}</span></td>
              <td>{{ item.productName }}</td>
              <td>{{ item.warehouseName }}</td>
              <td>{{ item.buyerName }}</td>
              <td>{{ item.buyerPhone || '-' }}</td>
              <td>{{ number(item.quantity || item.weightJin) }} {{ item.unitName || '斤' }}</td>
              <td>¥{{ money(item.unitPrice || item.pricePerJin) }} / {{ item.unitName || '斤' }}</td>
              <td>¥{{ money(item.totalAmount) }}</td>
              <td>
                <label class="settlement">
                  <input :checked="item.settled" :disabled="!auth.isAdmin" type="checkbox" @change="toggleSettlement(item, $event)" />
                  <span :class="['tag', item.settled ? '' : 'red']">{{ item.settled ? '已结账' : '未结账' }}</span>
                </label>
              </td>
              <td>{{ item.buyerAddress || '-' }}</td>
            </tr>
          </tbody>
        </table>
      </div>
    </div>
  </section>
</template>

<script setup>
import { computed, onMounted, ref } from 'vue'
import { RotateCcw, Save, Search } from 'lucide-vue-next'
import { tradeApi } from '../api/trade'
import { useAuthStore } from '../stores/auth'
import { useReferenceStore } from '../stores/reference'
import { extractError, money, number, today } from '../utils/format'
import { convertedPricePerJin, convertedWeightJin, defaultUnit, unitByLabel, unitOptions } from '../utils/units'

const reference = useReferenceStore()
const auth = useAuthStore()
const defaultNames = {
  RICE: '稻谷',
  SEED: '稻种',
  FERTILIZER: '化肥'
}

const records = ref([])
const inventoryOptions = ref([])
const saving = ref(false)
const message = ref('')
const error = ref('')
const filters = ref({
  productType: '',
  warehouseId: '',
  settled: '',
  keyword: ''
})
const form = ref(defaultForm())
const weightJin = computed(() => convertedWeightJin(form.value.quantity, form.value.unitToJin))
const pricePerJin = computed(() => convertedPricePerJin(form.value.unitPrice, form.value.unitToJin))
const conversionText = computed(() => {
  return `${number(weightJin.value)} 斤 / ¥${money(pricePerJin.value)} 每斤`
})

function defaultForm() {
  const unit = defaultUnit()
  return {
    productType: 'RICE',
    inventoryItemId: '',
    productName: '',
    warehouseId: '',
    buyerName: '',
    buyerPhone: '',
    buyerAddress: '',
    quantity: '',
    unitName: unit.label,
    unitToJin: unit.unitToJin,
    unitPrice: '',
    soldAt: today(),
    settled: false,
    remark: ''
  }
}

function cleanParams(values) {
  return Object.fromEntries(Object.entries(values).filter(([, value]) => value !== '' && value !== null))
}

async function handleProductTypeChange() {
  form.value.inventoryItemId = ''
  form.value.productName = defaultNames[form.value.productType] || ''
  form.value.warehouseId = ''
  await loadInventoryOptions()
  applyFirstInventory()
}

function applyUnitPreset() {
  form.value.unitToJin = unitByLabel(form.value.unitName).unitToJin
}

async function resetForm() {
  const productType = form.value.productType || 'RICE'
  form.value = { ...defaultForm(), productType }
  error.value = ''
  message.value = ''
  await loadInventoryOptions()
  applyFirstInventory()
}

async function loadSales() {
  records.value = await tradeApi.sales(cleanParams(filters.value))
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
    form.value.productName = defaultNames[form.value.productType] || ''
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

async function toggleSettlement(item, event) {
  const settled = event.target.checked
  try {
    const updated = await tradeApi.updateSettlement(item.id, settled)
    Object.assign(item, updated)
  } catch (err) {
    event.target.checked = item.settled
    error.value = extractError(err)
  }
}

onMounted(async () => {
  await reference.loadAll()
  await loadInventoryOptions()
  applyFirstInventory()
  await loadSales()
})
</script>
