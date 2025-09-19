<script setup lang="ts">
import { defineAsyncComponent, ref } from 'vue';

interface Props {
  type:'configs'|'detail';
}

const props = withDefaults(defineProps<Props>(), {
  type: 'configs'
});

const Configs = defineAsyncComponent(() => import('./Configs.vue'));
const ExecDetail = defineAsyncComponent(() => import('./ExecDetail.vue'));

const detailRef = ref();

defineExpose({
  restartNode: () => {
    if (detailRef.value) {
      detailRef.value.restartNode();
    }
  }
});
</script>

<template>
  <template v-if="props.type === 'configs'">
    <Configs v-bind="props" />
  </template>
  <template v-else>
    <ExecDetail v-bind="props" />
  </template>
</template>
