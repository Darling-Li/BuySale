<template>
  <section class="page-stack">
    <div v-if="!auth.isAdmin" class="readonly-note">普通角色仅可查询仓库信息。</div>

    <form v-if="auth.isAdmin" class="form-panel" @submit.prevent="submit">
      <div class="form-title">
        <h2>{{ editingId ? '编辑仓库' : '新增仓库' }}</h2>
        <span class="tag">{{ editingId ? '编辑中' : '仓库存储' }}</span>
      </div>

      <div class="form-grid">
        <label class="field">
          <span>仓库名称</span>
          <input v-model.trim="form.name" required />
        </label>
        <label class="field">
          <span>联系人</span>
          <input v-model.trim="form.contactName" />
        </label>
        <label class="field">
          <span>联系电话</span>
          <input v-model.trim="form.contactPhone" />
        </label>
        <label class="field wide">
          <span>仓库地址</span>
          <input v-model.trim="form.address" />
        </label>
        <label class="field wide">
          <span>备注</span>
          <input v-model.trim="form.remark" />
        </label>
      </div>

      <div class="actions">
        <button class="btn primary" type="submit" :disabled="saving">
          <Save :size="17" />
          {{ editingId ? '保存修改' : '保存仓库' }}
        </button>
        <button class="btn secondary" type="button" @click="reset">
          <RotateCcw :size="17" />
          重置
        </button>
        <span class="message" :class="{ error: !!error }">{{ error || message }}</span>
      </div>
    </form>

    <div class="table-panel">
      <div class="table-title">
        <h2>仓库列表</h2>
        <span class="tag blue">{{ reference.warehouses.length }} 个</span>
      </div>
      <div class="table-wrap">
        <table>
          <thead>
            <tr>
              <th>名称</th>
              <th>地址</th>
              <th>联系人</th>
              <th>电话</th>
              <th>备注</th>
              <th>更新时间</th>
              <th v-if="auth.isAdmin">操作</th>
            </tr>
          </thead>
          <tbody>
            <tr v-if="reference.warehouses.length === 0">
              <td class="empty-row" :colspan="auth.isAdmin ? 7 : 6">暂无仓库</td>
            </tr>
            <tr v-for="item in reference.warehouses" :key="item.id">
              <td>{{ item.name }}</td>
              <td>{{ item.address || '-' }}</td>
              <td>{{ item.contactName || '-' }}</td>
              <td>{{ item.contactPhone || '-' }}</td>
              <td>{{ item.remark || '-' }}</td>
              <td>{{ item.updatedAt }}</td>
              <td v-if="auth.isAdmin">
                <button class="btn ghost" type="button" @click="edit(item)">
                  <Pencil :size="16" />
                  编辑
                </button>
              </td>
            </tr>
          </tbody>
        </table>
      </div>
    </div>
  </section>
</template>

<script setup>
import { onMounted, ref } from 'vue'
import { Pencil, RotateCcw, Save } from 'lucide-vue-next'
import { tradeApi } from '../api/trade'
import { useAuthStore } from '../stores/auth'
import { useReferenceStore } from '../stores/reference'
import { extractError } from '../utils/format'

const reference = useReferenceStore()
const auth = useAuthStore()
const saving = ref(false)
const editingId = ref(null)
const message = ref('')
const error = ref('')
const form = ref(defaultForm())

function defaultForm() {
  return {
    name: '',
    address: '',
    contactName: '',
    contactPhone: '',
    remark: ''
  }
}

function reset() {
  editingId.value = null
  form.value = defaultForm()
  error.value = ''
  message.value = ''
}

function edit(item) {
  editingId.value = item.id
  form.value = {
    name: item.name || '',
    address: item.address || '',
    contactName: item.contactName || '',
    contactPhone: item.contactPhone || '',
    remark: item.remark || ''
  }
  message.value = ''
  error.value = ''
}

async function submit() {
  saving.value = true
  error.value = ''
  message.value = ''
  try {
    if (editingId.value) {
      await tradeApi.updateWarehouse(editingId.value, form.value)
      message.value = '仓库已更新'
    } else {
      await tradeApi.createWarehouse(form.value)
      message.value = '仓库已创建'
    }
    reset()
    await reference.loadWarehouses()
  } catch (err) {
    error.value = extractError(err)
  } finally {
    saving.value = false
  }
}

onMounted(async () => {
  await reference.loadAll()
})
</script>
