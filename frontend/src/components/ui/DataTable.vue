<template>
  <UiPanel class="table-panel" :title="title" :tag="tag" :tag-variant="tagVariant">
    <div class="table-wrap">
      <table :style="{ '--table-min-width': minWidth }">
        <slot />
      </table>
    </div>
  </UiPanel>
</template>

<script setup>
import UiPanel from './UiPanel.vue'

defineProps({
  title: {
    type: String,
    required: true
  },
  tag: {
    type: [String, Number],
    default: ''
  },
  tagVariant: {
    type: String,
    default: 'blue'
  },
  minWidth: {
    type: String,
    default: '980px'
  }
})
</script>
