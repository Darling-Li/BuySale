<template>
  <section class="page-stack">
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
      <UiField label="商品名称">
        <input v-model.trim="filters.keyword" placeholder="输入商品名称" @keyup.enter="loadInventory" />
      </UiField>
      <UiButton @click="loadInventory">
        <Search :size="17" />
        查询
      </UiButton>
      <ResultMessage :error="!!error">{{ error }}</ResultMessage>
    </UiToolbar>

    <MetricGrid>
      <MetricCard label="库存品项">{{ records.length }}</MetricCard>
      <MetricCard label="库存重量" tone="success">{{ number(totalQuantity) }} 斤</MetricCard>
      <MetricCard label="低库存品项" tone="warning">{{ lowStockCount }}</MetricCard>
      <MetricCard label="仓库数量" tone="info">{{ reference.warehouses.length }}</MetricCard>
    </MetricGrid>

    <DataTable title="库存汇总" :tag="`${records.length} 条`" min-width="820px">
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
          <td><UiTag>{{ item.productTypeLabel }}</UiTag></td>
          <td>{{ item.productName }}</td>
          <td>{{ number(item.quantityJin) }} 斤</td>
          <td>¥{{ money(item.averageCostPerJin) }}</td>
          <td>
            <UiTag :variant="Number(item.quantityJin) <= 0 ? 'red' : Number(item.quantityJin) < 1000 ? 'amber' : ''">
              {{ stockStatus(item.quantityJin) }}
            </UiTag>
          </td>
          <td>{{ dateTime(item.updatedAt) }}</td>
        </tr>
      </tbody>
    </DataTable>
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
