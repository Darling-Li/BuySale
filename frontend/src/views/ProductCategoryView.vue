<template>
  <section class="page-stack">
    <div v-if="!auth.isAdmin" class="readonly-note">普通角色仅可查询基础数据。</div>

    <form v-if="auth.isAdmin" class="form-panel" @submit.prevent="submitCategory">
      <div class="form-title">
        <h2>{{ categoryEditingId ? '编辑种类' : '新增种类' }}</h2>
        <span class="tag">商品种类</span>
      </div>

      <div class="form-grid">
        <label class="field">
          <span>种类编码</span>
          <input v-model.trim="categoryForm.code" :disabled="!!categoryEditingId" required placeholder="如：RICE" @input="normalizeCategoryCode" />
        </label>
        <label class="field">
          <span>种类名称</span>
          <input v-model.trim="categoryForm.name" required placeholder="如：稻谷" />
        </label>
        <label class="field">
          <span>排序</span>
          <input v-model.number="categoryForm.sortOrder" type="number" step="1" />
        </label>
        <label class="field">
          <span>状态</span>
          <select v-model="categoryForm.enabled">
            <option :value="true">启用</option>
            <option :value="false">禁用</option>
          </select>
        </label>
        <label class="field wide">
          <span>备注</span>
          <input v-model.trim="categoryForm.remark" />
        </label>
      </div>

      <div class="actions">
        <button class="btn primary" type="submit" :disabled="categorySaving">
          <Save :size="17" />
          {{ categoryEditingId ? '保存修改' : '保存种类' }}
        </button>
        <button class="btn secondary" type="button" @click="resetCategory">
          <RotateCcw :size="17" />
          重置
        </button>
        <span class="message" :class="{ error: !!categoryError }">{{ categoryError || categoryMessage }}</span>
      </div>
    </form>

    <div class="table-panel">
      <div class="table-title">
        <h2>商品种类</h2>
        <span class="tag blue">{{ categories.length }} 个</span>
      </div>
      <div class="table-wrap">
        <table>
          <thead>
            <tr>
              <th>编码</th>
              <th>名称</th>
              <th>排序</th>
              <th>状态</th>
              <th>备注</th>
              <th>更新时间</th>
              <th v-if="auth.isAdmin">操作</th>
            </tr>
          </thead>
          <tbody>
            <tr v-if="categories.length === 0">
              <td class="empty-row" :colspan="auth.isAdmin ? 7 : 6">暂无商品种类</td>
            </tr>
            <tr v-for="item in categories" :key="item.id">
              <td>{{ item.code }}</td>
              <td>{{ item.name }}</td>
              <td>{{ item.sortOrder }}</td>
              <td><span :class="['tag', item.enabled ? '' : 'red']">{{ item.enabled ? '启用' : '禁用' }}</span></td>
              <td>{{ item.remark || '-' }}</td>
              <td>{{ dateTime(item.updatedAt) }}</td>
              <td v-if="auth.isAdmin">
                <button class="btn ghost" type="button" @click="editCategory(item)">
                  <Pencil :size="16" />
                  编辑
                </button>
              </td>
            </tr>
          </tbody>
        </table>
      </div>
    </div>

    <form v-if="auth.isAdmin" class="form-panel" @submit.prevent="submitUnit">
      <div class="form-title">
        <h2>{{ unitEditingId ? '编辑单位' : '新增单位' }}</h2>
        <span class="tag amber">单位换算</span>
      </div>

      <div class="form-grid">
        <label class="field">
          <span>单位名称</span>
          <input v-model.trim="unitForm.name" required placeholder="如：袋、包" />
        </label>
        <label class="field">
          <span>每单位折合斤</span>
          <input v-model.number="unitForm.unitToJin" type="number" min="0.0001" step="0.0001" required />
        </label>
        <label class="field">
          <span>排序</span>
          <input v-model.number="unitForm.sortOrder" type="number" step="1" />
        </label>
        <label class="field">
          <span>状态</span>
          <select v-model="unitForm.enabled">
            <option :value="true">启用</option>
            <option :value="false">禁用</option>
          </select>
        </label>
        <label class="field wide">
          <span>备注</span>
          <input v-model.trim="unitForm.remark" />
        </label>
      </div>

      <div class="actions">
        <button class="btn primary" type="submit" :disabled="unitSaving">
          <Save :size="17" />
          {{ unitEditingId ? '保存修改' : '保存单位' }}
        </button>
        <button class="btn secondary" type="button" @click="resetUnit">
          <RotateCcw :size="17" />
          重置
        </button>
        <span class="message" :class="{ error: !!unitError }">{{ unitError || unitMessage }}</span>
      </div>
    </form>

    <div class="table-panel">
      <div class="table-title">
        <h2>单位换算</h2>
        <span class="tag blue">{{ units.length }} 个</span>
      </div>
      <div class="table-wrap">
        <table>
          <thead>
            <tr>
              <th>单位</th>
              <th>每单位折合斤</th>
              <th>类型</th>
              <th>排序</th>
              <th>状态</th>
              <th>备注</th>
              <th>更新时间</th>
              <th v-if="auth.isAdmin">操作</th>
            </tr>
          </thead>
          <tbody>
            <tr v-if="units.length === 0">
              <td class="empty-row" :colspan="auth.isAdmin ? 8 : 7">暂无单位</td>
            </tr>
            <tr v-for="item in units" :key="item.id">
              <td>{{ item.name }}</td>
              <td>{{ number(item.unitToJin, 4) }}</td>
              <td><span :class="['tag', item.systemBuiltin ? 'blue' : '']">{{ item.systemBuiltin ? '系统固定' : '自定义' }}</span></td>
              <td>{{ item.sortOrder }}</td>
              <td><span :class="['tag', item.enabled ? '' : 'red']">{{ item.enabled ? '启用' : '禁用' }}</span></td>
              <td>{{ item.remark || '-' }}</td>
              <td>{{ dateTime(item.updatedAt) }}</td>
              <td v-if="auth.isAdmin">
                <button v-if="!item.systemBuiltin" class="btn ghost" type="button" @click="editUnit(item)">
                  <Pencil :size="16" />
                  编辑
                </button>
                <span v-else class="tag blue">不可修改</span>
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
import { dateTime, extractError, number } from '../utils/format'

const auth = useAuthStore()
const reference = useReferenceStore()
const categories = ref([])
const units = ref([])
const categorySaving = ref(false)
const unitSaving = ref(false)
const categoryEditingId = ref(null)
const unitEditingId = ref(null)
const categoryMessage = ref('')
const categoryError = ref('')
const unitMessage = ref('')
const unitError = ref('')
const categoryForm = ref(defaultCategoryForm())
const unitForm = ref(defaultUnitForm())

function defaultCategoryForm() {
  return {
    code: '',
    name: '',
    sortOrder: 0,
    enabled: true,
    remark: ''
  }
}

function defaultUnitForm() {
  return {
    name: '',
    unitToJin: '',
    sortOrder: 0,
    enabled: true,
    remark: ''
  }
}

function normalizeCategoryCode() {
  categoryForm.value.code = categoryForm.value.code.replace(/[^a-zA-Z0-9_]/g, '').toUpperCase()
}

function resetCategory() {
  categoryEditingId.value = null
  categoryForm.value = defaultCategoryForm()
  categoryError.value = ''
  categoryMessage.value = ''
}

function resetUnit() {
  unitEditingId.value = null
  unitForm.value = defaultUnitForm()
  unitError.value = ''
  unitMessage.value = ''
}

function editCategory(item) {
  categoryEditingId.value = item.id
  categoryForm.value = {
    code: item.code || '',
    name: item.name || '',
    sortOrder: item.sortOrder ?? 0,
    enabled: item.enabled,
    remark: item.remark || ''
  }
  categoryMessage.value = ''
  categoryError.value = ''
}

function editUnit(item) {
  unitEditingId.value = item.id
  unitForm.value = {
    name: item.name || '',
    unitToJin: item.unitToJin || '',
    sortOrder: item.sortOrder ?? 0,
    enabled: item.enabled,
    remark: item.remark || ''
  }
  unitMessage.value = ''
  unitError.value = ''
}

async function loadCategories() {
  categories.value = await tradeApi.productCategories()
}

async function loadUnits() {
  units.value = await tradeApi.productUnits()
}

async function submitCategory() {
  categorySaving.value = true
  categoryError.value = ''
  categoryMessage.value = ''
  try {
    normalizeCategoryCode()
    if (categoryEditingId.value) {
      await tradeApi.updateProductCategory(categoryEditingId.value, categoryForm.value)
      categoryMessage.value = '商品种类已更新'
    } else {
      await tradeApi.createProductCategory(categoryForm.value)
      categoryMessage.value = '商品种类已创建'
    }
    resetCategory()
    await Promise.all([loadCategories(), reference.loadProductTypes()])
  } catch (err) {
    categoryError.value = extractError(err)
  } finally {
    categorySaving.value = false
  }
}

async function submitUnit() {
  unitSaving.value = true
  unitError.value = ''
  unitMessage.value = ''
  try {
    if (unitEditingId.value) {
      await tradeApi.updateProductUnit(unitEditingId.value, unitForm.value)
      unitMessage.value = '单位已更新'
    } else {
      await tradeApi.createProductUnit(unitForm.value)
      unitMessage.value = '单位已创建'
    }
    resetUnit()
    await Promise.all([loadUnits(), reference.loadProductUnits()])
  } catch (err) {
    unitError.value = extractError(err)
  } finally {
    unitSaving.value = false
  }
}

onMounted(async () => {
  await Promise.all([loadCategories(), loadUnits()])
})
</script>
