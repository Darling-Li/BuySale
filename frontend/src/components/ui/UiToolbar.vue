<template>
  <component :is="as" class="toolbar">
    <slot />
  </component>
</template>

<script setup>
defineProps({
  as: {
    type: String,
    default: 'div'
  }
})
</script>
