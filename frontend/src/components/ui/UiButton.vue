<template>
  <button v-bind="buttonAttrs" class="btn" :class="[variant, { 'icon-only': iconOnly }, attrs.class]" :type="type">
    <slot />
  </button>
</template>

<script setup>
import { computed, useAttrs } from 'vue'

defineOptions({ inheritAttrs: false })

defineProps({
  variant: {
    type: String,
    default: 'secondary'
  },
  type: {
    type: String,
    default: 'button'
  },
  iconOnly: {
    type: Boolean,
    default: false
  }
})

const attrs = useAttrs()
const buttonAttrs = computed(() => {
  const { class: _class, ...rest } = attrs
  return rest
})
</script>
