<script setup lang="ts">
import { defineAsyncComponent, computed } from 'vue';

interface Props {
  userInfo: { [key: string]: any };
  appInfo: { [key: string]: any };
  execInfo: { [key: string]: any };
  updateTabPane: (data: any) => void;
  getTabPane: (data: any) => any;
  replaceTabPane: (key:string, data: {_id:string;name:string;value:'Jdbc', sceneInfo:{id:string;}}) => any;
}

const props = withDefaults(defineProps<Props>(), {
  userInfo: undefined,
  appInfo: undefined,
  execInfo: undefined,
  updateTabPane: undefined,
  getTabPane: undefined,
  replaceTabPane: undefined
});

const emit = defineEmits<{(e:'setCountTabKey', value:string):void;}>();

const FunctionTestDetail = defineAsyncComponent(() => import('./FunctionTestDetail/index.vue'));
const PerformanceTestDetail = defineAsyncComponent(() => import('./PerformanceTestDetail/index.vue'));

const scriptType = computed(() => {
  return props.execInfo?.scriptType?.value;
});

const setCountTabKey = (value:string) => {
  emit('setCountTabKey', value);
};
</script>

<template>
  <template v-if="scriptType==='TEST_FUNCTIONALITY'">
    <FunctionTestDetail v-bind="props" />
  </template>
  <template v-else-if="['TEST_PERFORMANCE','TEST_STABILITY','TEST_CUSTOMIZATION'].includes(scriptType)">
    <PerformanceTestDetail v-bind="props" @setCountTabKey="setCountTabKey" />
  </template>
</template>
