<template>
  <div class="metric-item" :class="tone">
    <span>{{ label }}</span>
    <strong><slot>{{ value }}</slot></strong>
  </div>
</template>

<script setup>
defineProps({
  label: {
    type: String,
    required: true
  },
  value: {
    type: [String, Number],
    default: ''
  },
  tone: {
    type: String,
    default: ''
  }
})
</script>
