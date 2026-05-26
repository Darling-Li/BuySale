<template>
  <section class="page-stack">
    <div v-if="!auth.isAdmin" class="readonly-note">普通角色仅可查询采购记录。</div>

    <form v-if="auth.isAdmin" class="form-panel" @submit.prevent="submit">
      <div class="form-title">
        <h2>新增采购</h2>
        <span class="tag">自动入库</span>
      </div>

      <div class="form-grid">
        <label class="field">
          <span>商品类型</span>
          <select v-model="form.productType" required @change="applyDefaultName">
            <option v-for="item in reference.productTypes" :key="item.value" :value="item.value">
              {{ item.label }}
            </option>
          </select>
        </label>
        <label class="field">
          <span>商品名称</span>
          <input v-model.trim="form.productName" required placeholder="如：稻谷、杂交稻种、复合肥" />
        </label>
        <label class="field">
          <span>入库仓库</span>
          <select v-model="form.warehouseId" required>
            <option value="" disabled>请选择仓库</option>
            <option v-for="item in reference.warehouses" :key="item.id" :value="item.id">
              {{ item.name }}
            </option>
          </select>
        </label>
        <label class="field">
          <span>采购日期</span>
          <input v-model="form.purchasedAt" type="date" required />
        </label>
        <label class="field">
          <span>客户姓名</span>
          <input v-model.trim="form.counterpartyName" required />
        </label>
        <label class="field">
          <span>电话</span>
          <input v-model.trim="form.counterpartyPhone" />
        </label>
        <label class="field wide">
          <span>家庭住址</span>
          <input v-model.trim="form.counterpartyAddress" />
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
        <label class="field wide">
          <span>备注</span>
          <input v-model.trim="form.remark" />
        </label>
      </div>

      <div class="actions">
        <button class="btn primary" type="submit" :disabled="saving">
          <Save :size="17" />
          保存采购
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
        <span>关键词</span>
        <input v-model.trim="filters.keyword" placeholder="商品、姓名、电话" @keyup.enter="loadPurchases" />
      </label>
      <button class="btn secondary" type="button" @click="loadPurchases">
        <Search :size="17" />
        查询
      </button>
    </div>

    <div class="table-panel">
      <div class="table-title">
        <h2>采购记录</h2>
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
              <th>住址</th>
            </tr>
          </thead>
          <tbody>
            <tr v-if="records.length === 0">
              <td class="empty-row" colspan="10">暂无采购记录</td>
            </tr>
            <tr v-for="item in records" :key="item.id">
              <td>{{ item.purchasedAt }}</td>
              <td><span class="tag">{{ item.productTypeLabel }}</span></td>
              <td>{{ item.productName }}</td>
              <td>{{ item.warehouseName }}</td>
              <td>{{ item.counterpartyName }}</td>
              <td>{{ item.counterpartyPhone || '-' }}</td>
              <td>{{ number(item.quantity || item.weightJin) }} {{ item.unitName || '斤' }}</td>
              <td>¥{{ money(item.unitPrice || item.pricePerJin) }} / {{ item.unitName || '斤' }}</td>
              <td>¥{{ money(item.totalAmount) }}</td>
              <td>{{ item.counterpartyAddress || '-' }}</td>
            </tr>
          </tbody>
        </table>
      </div>
    </div>
  </section>
</template>

<script setup>
import { computed, onMounted, ref, watch } from 'vue'
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
const saving = ref(false)
const message = ref('')
const error = ref('')
const filters = ref({
  productType: '',
  warehouseId: '',
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
    productName: '稻谷',
    warehouseId: '',
    counterpartyName: '',
    counterpartyPhone: '',
    counterpartyAddress: '',
    quantity: '',
    unitName: unit.label,
    unitToJin: unit.unitToJin,
    unitPrice: '',
    purchasedAt: today(),
    remark: ''
  }
}

function cleanParams(values) {
  return Object.fromEntries(Object.entries(values).filter(([, value]) => value !== '' && value !== null))
}

function applyDefaultName() {
  form.value.productName = defaultNames[form.value.productType] || ''
}

function applyUnitPreset() {
  form.value.unitToJin = unitByLabel(form.value.unitName).unitToJin
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
      ...form.value,
      warehouseId: Number(form.value.warehouseId),
      weightJin: weightJin.value,
      pricePerJin: pricePerJin.value
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

watch(() => reference.warehouses, (warehouses) => {
  if (!form.value.warehouseId && warehouses.length > 0) {
    form.value.warehouseId = warehouses[0].id
  }
}, { deep: true })

onMounted(async () => {
  await reference.loadAll()
  if (!form.value.warehouseId && reference.warehouses.length > 0) {
    form.value.warehouseId = reference.warehouses[0].id
  }
  await loadPurchases()
})
</script>
