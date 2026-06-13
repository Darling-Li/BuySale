<template>
  <section class="page-stack">
    <UiToolbar>
      <UiField label="年份">
        <select v-model="year">
          <option v-for="item in yearOptions" :key="item" :value="item">{{ item }}</option>
        </select>
      </UiField>
      <UiField label="商品类型">
        <select v-model="productType">
          <option value="">全部品类</option>
          <option v-for="item in reference.productTypes" :key="item.value" :value="item.value">
            {{ item.label }}
          </option>
        </select>
      </UiField>
      <UiButton :disabled="loading" @click="loadTrend">
        <RefreshCcw :size="17" />
        刷新
      </UiButton>
      <ResultMessage :error="!!error">{{ error || statusText }}</ResultMessage>
    </UiToolbar>

    <MetricGrid>
      <MetricCard label="采购金额">¥{{ money(totals.purchaseAmount) }}</MetricCard>
      <MetricCard label="销售金额" tone="success">¥{{ money(totals.saleAmount) }}</MetricCard>
      <MetricCard label="采购重量" tone="info">{{ number(totals.purchaseWeight) }} 斤</MetricCard>
      <MetricCard label="销售重量" tone="warning">{{ number(totals.saleWeight) }} 斤</MetricCard>
    </MetricGrid>

    <UiPanel class="chart-panel" title="月度金额" :tag="trend?.productTypeLabel || '全部'" tag-variant="blue">
      <div ref="amountChartRef" class="chart"></div>
    </UiPanel>

    <UiPanel class="chart-panel" title="同比 / 环比" tag="按销售金额计算" tag-variant="amber">
      <div ref="rateChartRef" class="chart"></div>
    </UiPanel>

    <DataTable title="月度明细" min-width="760px">
      <thead>
        <tr>
          <th>月份</th>
          <th>采购重量</th>
          <th>采购金额</th>
          <th>销售重量</th>
          <th>销售金额</th>
          <th>同比</th>
          <th>环比</th>
        </tr>
      </thead>
      <tbody>
        <tr v-for="item in items" :key="item.month">
          <td>{{ item.month }}月</td>
          <td>{{ number(item.purchaseWeightJin) }} 斤</td>
          <td>¥{{ money(item.purchaseAmount) }}</td>
          <td>{{ number(item.saleWeightJin) }} 斤</td>
          <td>¥{{ money(item.saleAmount) }}</td>
          <td>{{ percent(item.saleYoYRate) }}</td>
          <td>{{ percent(item.saleMoMRate) }}</td>
        </tr>
      </tbody>
    </DataTable>
  </section>
</template>

<script setup>
import { computed, nextTick, onBeforeUnmount, onMounted, ref, watch } from 'vue'
import * as echarts from 'echarts'
import { RefreshCcw } from 'lucide-vue-next'
import { tradeApi } from '../api/trade'
import { useReferenceStore } from '../stores/reference'
import { extractError, money, number, percent } from '../utils/format'

const reference = useReferenceStore()
const currentYear = new Date().getFullYear()
const year = ref(currentYear)
const productType = ref('')
const trend = ref(null)
const loading = ref(false)
const error = ref('')
const amountChartRef = ref(null)
const rateChartRef = ref(null)

let amountChart = null
let rateChart = null

const yearOptions = Array.from({ length: 6 }, (_, index) => currentYear - 3 + index)
const items = computed(() => trend.value?.items || [])
const statusText = computed(() => (loading.value ? '加载中...' : ''))
const totals = computed(() => items.value.reduce((acc, item) => {
  acc.purchaseAmount += Number(item.purchaseAmount || 0)
  acc.saleAmount += Number(item.saleAmount || 0)
  acc.purchaseWeight += Number(item.purchaseWeightJin || 0)
  acc.saleWeight += Number(item.saleWeightJin || 0)
  return acc
}, {
  purchaseAmount: 0,
  saleAmount: 0,
  purchaseWeight: 0,
  saleWeight: 0
}))

async function loadTrend() {
  loading.value = true
  error.value = ''
  try {
    trend.value = await tradeApi.monthlyTrend({
      year: year.value,
      productType: productType.value || undefined
    })
    await nextTick()
    renderCharts()
  } catch (err) {
    error.value = extractError(err)
  } finally {
    loading.value = false
  }
}

function renderCharts() {
  const months = items.value.map((item) => `${item.month}月`)
  const purchaseAmount = items.value.map((item) => Number(item.purchaseAmount || 0))
  const saleAmount = items.value.map((item) => Number(item.saleAmount || 0))
  const yoy = items.value.map((item) => item.saleYoYRate == null ? null : Number(item.saleYoYRate))
  const mom = items.value.map((item) => item.saleMoMRate == null ? null : Number(item.saleMoMRate))

  amountChart ||= echarts.init(amountChartRef.value)
  rateChart ||= echarts.init(rateChartRef.value)

  amountChart.setOption({
    color: ['#0f766e', '#d97706'],
    tooltip: { trigger: 'axis' },
    legend: { top: 0 },
    grid: { top: 42, left: 56, right: 24, bottom: 36 },
    xAxis: { type: 'category', data: months },
    yAxis: { type: 'value', axisLabel: { formatter: '¥{value}' } },
    series: [
      { name: '采购金额', type: 'bar', data: purchaseAmount, barMaxWidth: 34 },
      { name: '销售金额', type: 'line', smooth: true, data: saleAmount }
    ]
  })

  rateChart.setOption({
    color: ['#2563eb', '#dc2626'],
    tooltip: { trigger: 'axis', valueFormatter: (value) => value == null ? '-' : `${value}%` },
    legend: { top: 0 },
    grid: { top: 42, left: 48, right: 24, bottom: 36 },
    xAxis: { type: 'category', data: months },
    yAxis: { type: 'value', axisLabel: { formatter: '{value}%' } },
    series: [
      { name: '同比', type: 'line', smooth: true, data: yoy },
      { name: '环比', type: 'line', smooth: true, data: mom }
    ]
  })
}

function resizeCharts() {
  amountChart?.resize()
  rateChart?.resize()
}

watch([year, productType], loadTrend)

onMounted(async () => {
  await reference.loadAll()
  await loadTrend()
  window.addEventListener('resize', resizeCharts)
})

onBeforeUnmount(() => {
  window.removeEventListener('resize', resizeCharts)
  amountChart?.dispose()
  rateChart?.dispose()
})
</script>
