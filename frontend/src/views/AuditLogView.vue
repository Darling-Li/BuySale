<template>
  <section class="page-stack">
    <Transition name="copy-toast">
      <div v-if="copyNotice" class="copy-toast">复制成功</div>
    </Transition>

    <div class="toolbar">
      <button class="btn secondary" type="button" @click="loadLogs">
        <RefreshCcw :size="17" />
        刷新
      </button>
      <span class="message error">{{ error }}</span>
    </div>

    <div class="table-panel">
      <div class="table-title">
        <h2>管理员操作日志</h2>
        <span class="tag blue">{{ records.length }} 条</span>
      </div>
      <div class="table-wrap">
        <table>
          <thead>
            <tr>
              <th>时间</th>
              <th>用户</th>
              <th>角色</th>
              <th>动作</th>
              <th>参数</th>
              <th>状态</th>
              <th>IP</th>
              <th>User Agent</th>
            </tr>
          </thead>
          <tbody>
            <tr v-if="records.length === 0">
              <td class="empty-row" colspan="8">暂无操作日志</td>
            </tr>
            <tr v-for="item in records" :key="item.id">
              <td>{{ item.occurredAt }}</td>
              <td>{{ item.username }}</td>
              <td><span class="tag">{{ item.roleName }}</span></td>
              <td>{{ item.actionName }}</td>
              <td class="params-cell">
                <button
                  class="params-copy"
                  type="button"
                  :title="formatParams(item.requestParams)"
                  @click="copyParams(item.requestParams)"
                >
                  {{ formatParams(item.requestParams) }}
                </button>
              </td>
              <td>
                <span :class="['tag', item.statusCode >= 400 ? 'red' : '']">{{ item.statusCode }}</span>
              </td>
              <td>{{ item.ipAddress || '-' }}</td>
              <td>{{ item.userAgent || '-' }}</td>
            </tr>
          </tbody>
        </table>
      </div>
    </div>
  </section>
</template>

<script setup>
import { onMounted, ref } from 'vue'
import { RefreshCcw } from 'lucide-vue-next'
import { tradeApi } from '../api/trade'
import { extractError } from '../utils/format'

const records = ref([])
const error = ref('')
const copyNotice = ref(false)
let copyNoticeTimer = 0

async function loadLogs() {
  error.value = ''
  try {
    records.value = await tradeApi.auditLogs()
  } catch (err) {
    error.value = extractError(err)
  }
}

function formatParams(value) {
  if (!value) {
    return '{}'
  }

  try {
    return JSON.stringify(JSON.parse(value))
  } catch {
    return value
  }
}

async function copyParams(value) {
  const text = formatParams(value)
  error.value = ''
  try {
    await navigator.clipboard.writeText(text)
    showCopyNotice()
  } catch {
    fallbackCopy(text)
    showCopyNotice()
  }
}

function showCopyNotice() {
  copyNotice.value = true
  window.clearTimeout(copyNoticeTimer)
  copyNoticeTimer = window.setTimeout(() => {
    copyNotice.value = false
  }, 1600)
}

function fallbackCopy(text) {
  const textarea = document.createElement('textarea')
  textarea.value = text
  textarea.setAttribute('readonly', '')
  textarea.style.position = 'fixed'
  textarea.style.left = '-9999px'
  document.body.appendChild(textarea)
  textarea.select()
  document.execCommand('copy')
  document.body.removeChild(textarea)
}

onMounted(loadLogs)
</script>

<style scoped>
.copy-toast {
  position: fixed;
  top: 50%;
  left: 50%;
  transform: translate(-50%, -50%);
  z-index: 50;
  padding: 8px 14px;
  border: 1px solid #86efac;
  border-radius: 8px;
  background: #dcfce7;
  color: #166534;
  font-size: 13px;
  font-weight: 700;
  box-shadow: 0 12px 28px rgba(22, 101, 52, 0.14);
}

.copy-toast-enter-active,
.copy-toast-leave-active {
  transition: opacity 0.22s ease, transform 0.22s ease;
}

.copy-toast-enter-from,
.copy-toast-leave-to {
  opacity: 0;
  transform: translate(-50%, -46%);
}

.params-cell {
  width: 360px;
  max-width: 360px;
}

.params-copy {
  display: block;
  width: 100%;
  margin: 0;
  padding: 0;
  overflow: hidden;
  border: 0;
  background: transparent;
  color: #334155;
  font-family: ui-monospace, SFMono-Regular, Menlo, Monaco, Consolas, monospace;
  font-size: 12px;
  line-height: 1.4;
  text-align: left;
  text-overflow: ellipsis;
  white-space: nowrap;
  cursor: copy;
}

.params-copy:hover {
  color: #256342;
}
</style>
