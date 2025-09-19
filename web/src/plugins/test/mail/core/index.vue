<script setup lang="ts">
import { defineAsyncComponent, ref } from 'vue';

interface Props {
  type:'configs'|'detail';
}

const props = withDefaults(defineProps<Props>(), {
  type: 'configs'
});

const Configs = defineAsyncComponent(() => import('./Configs.vue'));
const Detail = defineAsyncComponent(() => import('./Detail.vue'));

const emit = defineEmits<{(e:'setCountTabKey', value:string):void;}>();
const setCountTabKey = (value:string) => {
  emit('setCountTabKey', value);
};

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
  <template v-if="props.type==='configs'">
    <Configs v-bind="props" />
  </template>
  <template v-else>
    <Detail
      ref="detailRef"
      v-bind="props"
      @setCountTabKey="setCountTabKey" />
  </template>
</template>
