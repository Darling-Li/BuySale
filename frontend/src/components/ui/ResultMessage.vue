<template>
  <span class="message" :class="{ error }">
    <slot>{{ text }}</slot>
  </span>
</template>

<script setup>
defineProps({
  text: {
    type: String,
    default: ''
  },
  error: {
    type: Boolean,
    default: false
  }
})
</script>
