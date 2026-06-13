<template>
  <span class="tag" :class="variant">
    <slot />
  </span>
</template>

<script setup>
defineProps({
  variant: {
    type: String,
    default: ''
  }
})
</script>
