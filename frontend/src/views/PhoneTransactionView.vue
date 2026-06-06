<template>
  <section class="page-stack">
    <form class="toolbar" @submit.prevent="loadRecords">
      <label class="field">
        <span>手机号</span>
        <input v-model.trim="phone" placeholder="请输入完整手机号" required />
      </label>
      <button class="btn secondary" type="submit" :disabled="loading">
        <Search :size="17" />
        查询
      </button>
      <span class="message" :class="{ error: !!error }">{{ error || summaryText }}</span>
    </form>

    <div class="table-panel">
      <div class="table-title">
        <h2>关联交易记录</h2>
        <span class="tag blue">{{ records.length }} 条</span>
      </div>
      <div class="table-wrap">
        <table>
          <thead>
            <tr>
              <th>日期</th>
              <th>类型</th>
              <th>品类</th>
              <th>商品</th>
              <th>仓库</th>
              <th>客户</th>
              <th>电话</th>
              <th>重量</th>
              <th>单价</th>
              <th>金额</th>
              <th>结账金额</th>
              <th>结账渠道</th>
              <th>住址</th>
              <th>备注</th>
            </tr>
          </thead>
          <tbody>
            <tr v-if="records.length === 0">
              <td class="empty-row" colspan="14">{{ emptyText }}</td>
            </tr>
            <tr v-for="item in records" :key="`${item.businessType}-${item.businessId}`">
              <td>{{ dateTime(item.transactionDate) }}</td>
              <td>
                <span :class="['tag', item.businessType === 'SALE' ? 'amber' : '']">
                  {{ item.businessTypeLabel }}
                </span>
              </td>
              <td><span class="tag">{{ item.productTypeLabel }}</span></td>
              <td>{{ item.productName }}</td>
              <td>{{ item.warehouseName }}</td>
              <td>{{ item.contactName }}</td>
              <td>{{ item.contactPhone || '-' }}</td>
              <td>{{ number(item.quantity || item.weightJin) }} {{ item.unitName || '斤' }}</td>
              <td>¥{{ money(item.unitPrice || item.pricePerJin) }} / {{ item.unitName || '斤' }}</td>
              <td>¥{{ money(item.totalAmount) }}</td>
              <td>
                <span v-if="item.businessType === 'SALE'">¥{{ money(item.settledAmount) }}</span>
                <span v-else>-</span>
              </td>
              <td>{{ item.businessType === 'SALE' ? item.settlementChannels || '-' : '-' }}</td>
              <td>{{ item.contactAddress || '-' }}</td>
              <td>{{ item.remark || '-' }}</td>
            </tr>
          </tbody>
        </table>
      </div>
    </div>
  </section>
</template>

<script setup>
import { computed, ref } from 'vue'
import { Search } from 'lucide-vue-next'
import { tradeApi } from '../api/trade'
import { dateTime, extractError, money, number } from '../utils/format'

const phone = ref('')
const records = ref([])
const loading = ref(false)
const searched = ref(false)
const error = ref('')

const summaryText = computed(() => {
  if (!searched.value) {
    return ''
  }
  return records.value.length > 0 ? `已查询到 ${records.value.length} 条关联交易` : '未查询到关联交易'
})

const emptyText = computed(() => {
  return searched.value ? '暂无关联交易记录' : '请输入手机号后查询'
})

async function loadRecords() {
  if (!phone.value) {
    records.value = []
    searched.value = false
    error.value = '手机号不能为空'
    return
  }

  loading.value = true
  error.value = ''
  try {
    records.value = await tradeApi.phoneTransactions({ phone: phone.value })
    searched.value = true
  } catch (err) {
    records.value = []
    searched.value = true
    error.value = extractError(err)
  } finally {
    loading.value = false
  }
}
</script>
