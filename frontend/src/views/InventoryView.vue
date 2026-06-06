<template>
  <section class="page-stack">
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
        <span>商品名称</span>
        <input v-model.trim="filters.keyword" placeholder="输入商品名称" @keyup.enter="loadInventory" />
      </label>
      <button class="btn secondary" type="button" @click="loadInventory">
        <Search :size="17" />
        查询
      </button>
      <span class="message" :class="{ error: !!error }">{{ error }}</span>
    </div>

    <div class="metric-grid">
      <div class="metric-item">
        <span>库存品项</span>
        <strong>{{ records.length }}</strong>
      </div>
      <div class="metric-item">
        <span>库存重量</span>
        <strong>{{ number(totalQuantity) }} 斤</strong>
      </div>
      <div class="metric-item">
        <span>低库存品项</span>
        <strong>{{ lowStockCount }}</strong>
      </div>
      <div class="metric-item">
        <span>仓库数量</span>
        <strong>{{ reference.warehouses.length }}</strong>
      </div>
    </div>

    <div class="table-panel">
      <div class="table-title">
        <h2>库存汇总</h2>
        <span class="tag blue">{{ records.length }} 条</span>
      </div>
      <div class="table-wrap">
        <table>
          <thead>
            <tr>
              <th>仓库</th>
              <th>品类</th>
              <th>商品</th>
              <th>库存重量</th>
              <th>平均成本</th>
              <th>状态</th>
              <th>更新时间</th>
            </tr>
          </thead>
          <tbody>
            <tr v-if="records.length === 0">
              <td class="empty-row" colspan="7">暂无库存</td>
            </tr>
            <tr v-for="item in records" :key="item.id">
              <td>{{ item.warehouseName }}</td>
              <td><span class="tag">{{ item.productTypeLabel }}</span></td>
              <td>{{ item.productName }}</td>
              <td>{{ number(item.quantityJin) }} 斤</td>
              <td>¥{{ money(item.averageCostPerJin) }}</td>
              <td>
                <span :class="['tag', Number(item.quantityJin) <= 0 ? 'red' : Number(item.quantityJin) < 1000 ? 'amber' : '']">
                  {{ stockStatus(item.quantityJin) }}
                </span>
              </td>
              <td>{{ dateTime(item.updatedAt) }}</td>
            </tr>
          </tbody>
        </table>
      </div>
    </div>
  </section>
</template>

<script setup>
import { computed, onMounted, ref } from 'vue'
import { Search } from 'lucide-vue-next'
import { tradeApi } from '../api/trade'
import { useReferenceStore } from '../stores/reference'
import { dateTime, extractError, money, number } from '../utils/format'

const reference = useReferenceStore()
const records = ref([])
const error = ref('')
const filters = ref({
  productType: '',
  warehouseId: '',
  keyword: ''
})

const totalQuantity = computed(() => records.value.reduce((sum, item) => sum + Number(item.quantityJin || 0), 0))
const lowStockCount = computed(() => records.value.filter((item) => Number(item.quantityJin || 0) > 0 && Number(item.quantityJin || 0) < 1000).length)

function cleanParams(values) {
  return Object.fromEntries(Object.entries(values).filter(([, value]) => value !== '' && value !== null))
}

function stockStatus(quantity) {
  const value = Number(quantity || 0)
  if (value <= 0) {
    return '无库存'
  }
  if (value < 1000) {
    return '偏低'
  }
  return '正常'
}

async function loadInventory() {
  error.value = ''
  try {
    records.value = await tradeApi.inventory(cleanParams(filters.value))
  } catch (err) {
    error.value = extractError(err)
  }
}

onMounted(async () => {
  await reference.loadAll()
  await loadInventory()
})
</script>
