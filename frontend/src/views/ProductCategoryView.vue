<template>
  <section class="page-stack">
    <div v-if="!auth.isAdmin" class="readonly-note">普通角色仅可查询基础数据。</div>

    <UiToolbar class="submodule-toolbar">
      <div class="segmented">
        <button v-for="item in modules" :key="item.key" type="button" :class="{ active: activeModule === item.key }" @click="switchModule(item.key)">
          <component :is="item.icon" :size="16" />
          {{ item.label }}
        </button>
      </div>
      <UiButton v-if="auth.isAdmin" variant="primary" @click="openCreate">
        <Plus :size="17" />
        新增
      </UiButton>
      <UiButton v-if="auth.isAdmin" variant="warning" :disabled="selectedIds.length === 0" @click="batchDelete">
        <Trash2 :size="17" />
        批量删除
      </UiButton>
      <ResultMessage :error="!!error">{{ error || message }}</ResultMessage>
    </UiToolbar>

    <UiPanel v-if="auth.isAdmin && formVisible && activeModule === 'categories'" as="form" class="form-panel" :title="categoryEditingId ? '编辑种类' : '新增种类'" tag="种类管理" @submit.prevent="submitCategory">
      <div class="form-grid">
        <UiField label="种类编码">
          <input v-model.trim="categoryForm.code" :disabled="!!categoryEditingId" required placeholder="如：WHEAT" @input="normalizeCategoryCode" />
        </UiField>
        <UiField label="种类名称">
          <input v-model.trim="categoryForm.name" required placeholder="如：小麦" />
        </UiField>
        <UiField label="排序">
          <input v-model.number="categoryForm.sortOrder" type="number" step="1" />
        </UiField>
        <UiField label="状态">
          <select v-model="categoryForm.enabled">
            <option :value="true">启用</option>
            <option :value="false">禁用</option>
          </select>
        </UiField>
        <UiField label="备注" wide>
          <input v-model.trim="categoryForm.remark" />
        </UiField>
      </div>
      <FormActions :saving="saving" :editing="!!categoryEditingId" @cancel="closeForm" />
    </UiPanel>

    <UiPanel v-if="auth.isAdmin && formVisible && activeModule === 'units'" as="form" class="form-panel" :title="unitEditingId ? '编辑单位' : '新增单位'" tag="单位管理" tag-variant="amber" @submit.prevent="submitUnit">
      <div class="form-grid">
        <UiField label="单位名称">
          <input v-model.trim="unitForm.name" required placeholder="如：袋、包" />
        </UiField>
        <UiField label="每单位折合斤">
          <input v-model.number="unitForm.unitToJin" type="number" min="0.0001" step="0.0001" required />
        </UiField>
        <UiField label="排序">
          <input v-model.number="unitForm.sortOrder" type="number" step="1" />
        </UiField>
        <UiField label="状态">
          <select v-model="unitForm.enabled">
            <option :value="true">启用</option>
            <option :value="false">禁用</option>
          </select>
        </UiField>
        <UiField label="备注" wide>
          <input v-model.trim="unitForm.remark" />
        </UiField>
      </div>
      <FormActions :saving="saving" :editing="!!unitEditingId" @cancel="closeForm" />
    </UiPanel>

    <UiPanel v-if="auth.isAdmin && formVisible && activeModule === 'users'" as="form" class="form-panel" :title="userEditingId ? '编辑用户' : '新增用户'" tag="用户管理" tag-variant="blue" @submit.prevent="submitUser">
      <div class="form-grid">
        <UiField label="用户名">
          <input v-model.trim="userForm.username" required placeholder="登录账号" />
        </UiField>
        <UiField label="姓名">
          <input v-model.trim="userForm.displayName" placeholder="显示名称" />
        </UiField>
        <UiField :label="userEditingId ? '密码' : '初始密码'">
          <div class="password-input-row">
            <input v-model="userForm.password" :required="!userEditingId" :type="passwordVisible ? 'text' : 'password'" placeholder="编辑时为空则不修改" />
            <UiButton :title="passwordVisible ? '隐藏密码' : '显示密码'" icon-only @click="passwordVisible = !passwordVisible">
              <EyeOff v-if="passwordVisible" :size="17" />
              <Eye v-else :size="17" />
            </UiButton>
          </div>
        </UiField>
        <UiField label="确认密码">
          <div class="password-input-row">
            <input v-model="userForm.confirmPassword" :required="!userEditingId" :type="passwordVisible ? 'text' : 'password'" placeholder="再次输入密码" />
            <UiButton :title="passwordVisible ? '隐藏密码' : '显示密码'" icon-only @click="passwordVisible = !passwordVisible">
              <EyeOff v-if="passwordVisible" :size="17" />
              <Eye v-else :size="17" />
            </UiButton>
          </div>
        </UiField>
        <UiField label="状态">
          <select v-model="userForm.enabled">
            <option :value="true">启用</option>
            <option :value="false">禁用</option>
          </select>
        </UiField>
        <UiField label="角色" wide>
          <div class="checkbox-row">
            <label v-for="role in roles" :key="role.code" class="check-item">
              <input v-model="userForm.roleCodes" type="checkbox" :value="role.code" />
              <span>{{ role.name }}</span>
            </label>
          </div>
        </UiField>
      </div>
      <FormActions :saving="saving" :editing="!!userEditingId" @cancel="closeForm" />
    </UiPanel>

    <DataTable v-if="activeModule === 'categories'" title="种类管理" :tag="`${categories.length} 个`" min-width="920px">
      <thead>
        <tr>
          <th v-if="auth.isAdmin"><input type="checkbox" :checked="allSelected" @change="toggleAll($event.target.checked)" /></th>
          <th>编码</th>
          <th>名称</th>
          <th>类型</th>
          <th>排序</th>
          <th>状态</th>
          <th>备注</th>
          <th>更新时间</th>
          <th v-if="auth.isAdmin">操作</th>
        </tr>
      </thead>
      <tbody>
        <tr v-if="categories.length === 0">
          <td class="empty-row" :colspan="auth.isAdmin ? 9 : 8">暂无种类</td>
        </tr>
        <tr v-for="item in categories" :key="item.id">
          <td v-if="auth.isAdmin"><input v-model="selectedIds" type="checkbox" :value="item.id" /></td>
          <td>{{ item.code }}</td>
          <td>{{ item.name }}</td>
          <td><UiTag :variant="item.systemBuiltin ? 'blue' : ''">{{ item.systemBuiltin ? '系统默认' : '自定义' }}</UiTag></td>
          <td>{{ item.sortOrder }}</td>
          <td><UiTag :variant="item.enabled ? '' : 'red'">{{ item.enabled ? '启用' : '禁用' }}</UiTag></td>
          <td>{{ item.remark || '-' }}</td>
          <td>{{ dateTime(item.updatedAt) }}</td>
          <td v-if="auth.isAdmin">
            <UiButton variant="ghost" @click="editCategory(item)">
              <Pencil :size="16" />
              编辑
            </UiButton>
          </td>
        </tr>
      </tbody>
    </DataTable>

    <DataTable v-if="activeModule === 'units'" title="单位管理" :tag="`${units.length} 个`" min-width="900px">
      <thead>
        <tr>
          <th v-if="auth.isAdmin"><input type="checkbox" :checked="allSelected" @change="toggleAll($event.target.checked)" /></th>
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
          <td class="empty-row" :colspan="auth.isAdmin ? 9 : 8">暂无单位</td>
        </tr>
        <tr v-for="item in units" :key="item.id">
          <td v-if="auth.isAdmin"><input v-model="selectedIds" type="checkbox" :value="item.id" /></td>
          <td>{{ item.name }}</td>
          <td>{{ number(item.unitToJin, 4) }}</td>
          <td><UiTag :variant="item.systemBuiltin ? 'blue' : ''">{{ item.systemBuiltin ? '系统默认' : '自定义' }}</UiTag></td>
          <td>{{ item.sortOrder }}</td>
          <td><UiTag :variant="item.enabled ? '' : 'red'">{{ item.enabled ? '启用' : '禁用' }}</UiTag></td>
          <td>{{ item.remark || '-' }}</td>
          <td>{{ dateTime(item.updatedAt) }}</td>
          <td v-if="auth.isAdmin">
            <UiButton :disabled="item.systemBuiltin" variant="ghost" @click="editUnit(item)">
              <Pencil :size="16" />
              编辑
            </UiButton>
          </td>
        </tr>
      </tbody>
    </DataTable>

    <DataTable v-if="activeModule === 'users'" title="用户管理" :tag="`${users.length} 个`" min-width="900px">
      <thead>
        <tr>
          <th v-if="auth.isAdmin"><input type="checkbox" :checked="allSelected" @change="toggleAll($event.target.checked)" /></th>
          <th>用户名</th>
          <th>姓名</th>
          <th>角色</th>
          <th>状态</th>
          <th>更新时间</th>
          <th v-if="auth.isAdmin">操作</th>
        </tr>
      </thead>
      <tbody>
        <tr v-if="users.length === 0">
          <td class="empty-row" :colspan="auth.isAdmin ? 7 : 6">暂无用户</td>
        </tr>
        <tr v-for="item in users" :key="item.id">
          <td v-if="auth.isAdmin"><input v-model="selectedIds" type="checkbox" :value="item.id" /></td>
          <td>{{ item.username }}</td>
          <td>{{ item.displayName || '-' }}</td>
          <td>{{ roleText(item.roleCodes) }}</td>
          <td><UiTag :variant="item.enabled ? '' : 'red'">{{ item.enabled ? '启用' : '禁用' }}</UiTag></td>
          <td>{{ dateTime(item.updatedAt) }}</td>
          <td v-if="auth.isAdmin">
            <UiButton variant="ghost" @click="editUser(item)">
              <Pencil :size="16" />
              编辑
            </UiButton>
          </td>
        </tr>
      </tbody>
    </DataTable>
  </section>
</template>

<script setup>
import { computed, h, onMounted, ref, resolveComponent } from 'vue'
import { Boxes, Eye, EyeOff, Pencil, Plus, RotateCcw, Save, Tags, Trash2, Users } from 'lucide-vue-next'
import { tradeApi } from '../api/trade'
import { useAuthStore } from '../stores/auth'
import { useReferenceStore } from '../stores/reference'
import { dateTime, extractError, number } from '../utils/format'

const FormActions = {
  props: {
    saving: Boolean,
    editing: Boolean
  },
  emits: ['cancel'],
  setup(props, { emit }) {
    return () => h('div', { class: 'actions' }, [
      h(resolveComponent('UiButton'), { variant: 'primary', type: 'submit', disabled: props.saving }, () => [
        h(Save, { size: 17 }),
        props.editing ? '保存修改' : '保存新增'
      ]),
      h(resolveComponent('UiButton'), { onClick: () => emit('cancel') }, () => [
        h(RotateCcw, { size: 17 }),
        '取消'
      ])
    ])
  }
}

const auth = useAuthStore()
const reference = useReferenceStore()
const moduleOptions = [
  { key: 'categories', label: '种类管理', icon: Tags },
  { key: 'units', label: '单位管理', icon: Boxes },
  { key: 'users', label: '用户管理', icon: Users }
]
const modules = computed(() => moduleOptions.filter((item) => auth.isAdmin || item.key !== 'users'))
const activeModule = ref('categories')
const categories = ref([])
const units = ref([])
const users = ref([])
const roles = ref([])
const selectedIds = ref([])
const saving = ref(false)
const formVisible = ref(false)
const passwordVisible = ref(false)
const message = ref('')
const error = ref('')
const categoryEditingId = ref(null)
const unitEditingId = ref(null)
const userEditingId = ref(null)
const categoryForm = ref(defaultCategoryForm())
const unitForm = ref(defaultUnitForm())
const userForm = ref(defaultUserForm())
const currentItems = computed(() => {
  if (activeModule.value === 'categories') return categories.value
  if (activeModule.value === 'units') return units.value
  return users.value
})
const allSelected = computed(() => currentItems.value.length > 0 && selectedIds.value.length === currentItems.value.length)

function defaultCategoryForm() {
  return { code: '', name: '', sortOrder: 0, enabled: true, remark: '' }
}

function defaultUnitForm() {
  return { name: '', unitToJin: '', sortOrder: 0, enabled: true, remark: '' }
}

function defaultUserForm() {
  return { username: '', password: '', confirmPassword: '', displayName: '', enabled: true, roleCodes: ['USER'] }
}

function switchModule(key) {
  activeModule.value = key
  closeForm()
  selectedIds.value = []
  message.value = ''
  error.value = ''
}

function openCreate() {
  categoryEditingId.value = null
  unitEditingId.value = null
  userEditingId.value = null
  categoryForm.value = defaultCategoryForm()
  unitForm.value = defaultUnitForm()
  userForm.value = defaultUserForm()
  passwordVisible.value = false
  formVisible.value = true
  message.value = ''
  error.value = ''
}

function closeForm() {
  formVisible.value = false
  categoryEditingId.value = null
  unitEditingId.value = null
  userEditingId.value = null
}

function normalizeCategoryCode() {
  categoryForm.value.code = categoryForm.value.code.replace(/[^a-zA-Z0-9_]/g, '').toUpperCase()
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
  formVisible.value = true
}

function editUnit(item) {
  if (item.systemBuiltin) return
  unitEditingId.value = item.id
  unitForm.value = {
    name: item.name || '',
    unitToJin: item.unitToJin || '',
    sortOrder: item.sortOrder ?? 0,
    enabled: item.enabled,
    remark: item.remark || ''
  }
  formVisible.value = true
}

function editUser(item) {
  userEditingId.value = item.id
  userForm.value = {
    username: item.username || '',
    password: '',
    confirmPassword: '',
    displayName: item.displayName || '',
    enabled: item.enabled,
    roleCodes: item.roleCodes?.length ? [...item.roleCodes] : ['USER']
  }
  formVisible.value = true
}

function toggleAll(checked) {
  selectedIds.value = checked ? currentItems.value.map((item) => item.id) : []
}

async function loadCategories() {
  categories.value = await tradeApi.productCategories()
}

async function loadUnits() {
  units.value = await tradeApi.productUnits()
}

async function loadUsers() {
  users.value = await tradeApi.systemUsers()
}

async function loadRoles() {
  roles.value = await tradeApi.systemRoles()
}

async function submitCategory() {
  await submit(async () => {
    normalizeCategoryCode()
    if (categoryEditingId.value) {
      await tradeApi.updateProductCategory(categoryEditingId.value, categoryForm.value)
    } else {
      await tradeApi.createProductCategory(categoryForm.value)
    }
    await Promise.all([loadCategories(), reference.loadProductTypes()])
    message.value = '种类已保存'
  })
}

async function submitUnit() {
  await submit(async () => {
    if (unitEditingId.value) {
      await tradeApi.updateProductUnit(unitEditingId.value, unitForm.value)
    } else {
      await tradeApi.createProductUnit(unitForm.value)
    }
    await Promise.all([loadUnits(), reference.loadProductUnits()])
    message.value = '单位已保存'
  })
}

async function submitUser() {
  if (userForm.value.password || userForm.value.confirmPassword || !userEditingId.value) {
    if (userForm.value.password !== userForm.value.confirmPassword) {
      error.value = '密码不一致'
      message.value = ''
      return
    }
  }
  await submit(async () => {
    const { confirmPassword: _confirmPassword, ...payload } = userForm.value
    if (userEditingId.value) {
      await tradeApi.updateSystemUser(userEditingId.value, payload)
    } else {
      await tradeApi.createSystemUser(payload)
    }
    await loadUsers()
    message.value = '用户已保存'
  })
}

async function submit(action) {
  saving.value = true
  error.value = ''
  message.value = ''
  try {
    await action()
    closeForm()
  } catch (err) {
    error.value = extractError(err)
  } finally {
    saving.value = false
  }
}

async function batchDelete() {
  if (selectedIds.value.length === 0) return
  if (!window.confirm(`确认删除选中的 ${selectedIds.value.length} 条数据？`)) return
  error.value = ''
  message.value = ''
  try {
    if (activeModule.value === 'categories') {
      await tradeApi.deleteProductCategories(selectedIds.value)
      await Promise.all([loadCategories(), reference.loadProductTypes()])
    } else if (activeModule.value === 'units') {
      await tradeApi.deleteProductUnits(selectedIds.value)
      await Promise.all([loadUnits(), reference.loadProductUnits()])
    } else {
      await tradeApi.deleteSystemUsers(selectedIds.value)
      await loadUsers()
    }
    selectedIds.value = []
    message.value = '已删除选中数据'
  } catch (err) {
    error.value = extractError(err)
  }
}

function roleText(roleCodes = []) {
  return roleCodes.map((code) => roles.value.find((role) => role.code === code)?.name || code).join('、') || '-'
}

onMounted(async () => {
  const tasks = [loadCategories(), loadUnits()]
  if (auth.isAdmin) {
    tasks.push(loadUsers(), loadRoles())
  }
  await Promise.all(tasks)
})
</script>
