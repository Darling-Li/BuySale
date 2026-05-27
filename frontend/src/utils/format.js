export function today() {
  return new Date().toISOString().slice(0, 10)
}

export function money(value) {
  return Number(value || 0).toLocaleString('zh-CN', {
    minimumFractionDigits: 2,
    maximumFractionDigits: 2
  })
}

export function number(value, digits = 2) {
  return Number(value || 0).toLocaleString('zh-CN', {
    minimumFractionDigits: digits,
    maximumFractionDigits: digits
  })
}

export function percent(value) {
  if (value === null || value === undefined) {
    return '-'
  }
  return `${Number(value).toFixed(2)}%`
}

export function extractError(error) {
  const data = error?.response?.data
  if (!data) {
    return error?.message || '请求失败'
  }
  if (data.data && typeof data.data === 'object') {
    return Object.values(data.data).join('；')
  }
  return data.msg || data.message || '请求失败'
}
