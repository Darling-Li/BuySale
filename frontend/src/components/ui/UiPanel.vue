<template>
  <component v-bind="panelAttrs" :is="as" class="ui-panel" :class="[tone, attrs.class]">
    <div v-if="hasHeader" class="panel-title">
      <slot name="title">
        <h2>{{ title }}</h2>
      </slot>
      <div v-if="tag || $slots.actions" class="panel-actions">
        <slot name="actions">
          <UiTag :variant="tagVariant">{{ tag }}</UiTag>
        </slot>
      </div>
    </div>
    <slot />
  </component>
</template>

<script setup>
import { computed, useAttrs, useSlots } from 'vue'
import UiTag from './UiTag.vue'

defineOptions({ inheritAttrs: false })

const props = defineProps({
  as: {
    type: String,
    default: 'div'
  },
  title: {
    type: String,
    default: ''
  },
  tag: {
    type: [String, Number],
    default: ''
  },
  tagVariant: {
    type: String,
    default: ''
  },
  tone: {
    type: String,
    default: ''
  }
})

const attrs = useAttrs()
const slots = useSlots()
const panelAttrs = computed(() => {
  const { class: _class, ...rest } = attrs
  return rest
})
const hasHeader = computed(() => props.title || props.tag || slots.title || slots.actions)
</script>
