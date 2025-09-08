<script setup lang="ts">
import { computed, defineAsyncComponent, ref } from 'vue';

interface Props {
  execInfo: { [key: string]: any };
  userInfo: { [key: string]: any };
  appInfo: { [key: string]: any };
  execContent: { [key: string]: any }[];
  updateTabPane: (data: any) => void;
  getTabPane: (data: any) => any;
  replaceTabPane: (key:string, data: {_id:string;name:string;value:'Mail', scenarioInfo:{id:string;}}) => any;
}

const props = withDefaults(defineProps<Props>(), {
  execInfo: () => ({}),
  execContent: () => ([]),
  userInfo: undefined,
  appInfo: undefined,
  updateTabPane: undefined,
  getTabPane: undefined,
  replaceTabPane: undefined
});

const FunctionTestDetail = defineAsyncComponent(() => import('./FunctionTestDetail/index.vue'));
// const PerformanceTestDetail = defineAsyncComponent(() => import('./PerformanceTestDetail/index.vue'));

const scriptType = computed(() => {
  return props.execInfo?.scriptType?.value;
});

const emit = defineEmits<{(e:'setCountTabKey', value:string):void;}>();
const setCountTabKey = (value:string) => {
  emit('setCountTabKey', value);
};
const performanceDetailRef = ref();
defineExpose({
  restartNode: () => {
    if (performanceDetailRef.value) {
      performanceDetailRef.value.restartNode();
    }
  }
});
</script>

<template>
  <template v-if="scriptType==='TEST_FUNCTIONALITY'">
    <FunctionTestDetail v-bind="props" />
  </template>
  <!-- <template v-else-if="['TEST_PERFORMANCE','TEST_STABILITY','TEST_CUSTOMIZATION'].includes(scriptType)">
    <PerformanceTestDetail
      ref="performanceDetailRef"
      v-bind="props"
      @setCountTabKey="setCountTabKey" />
  </template> -->
</template>
