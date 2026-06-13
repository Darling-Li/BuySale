<template>
  <label class="field" :class="{ wide, full }">
    <span v-if="label">{{ label }}</span>
    <slot />
  </label>
</template>

<script setup>
defineProps({
  label: {
    type: String,
    default: ''
  },
  wide: {
    type: Boolean,
    default: false
  },
  full: {
    type: Boolean,
    default: false
  }
})
</script>
