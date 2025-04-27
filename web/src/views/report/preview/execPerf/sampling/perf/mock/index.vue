<script setup lang="ts">
import { computed, defineAsyncComponent, ref } from 'vue';

const CSV = defineAsyncComponent(() => import('./csv.vue'));
const Custom = defineAsyncComponent(() => import('./custom.vue'));
const Json = defineAsyncComponent(() => import('./json.vue'));
const Excel = defineAsyncComponent(() => import('./excel.vue'));
const Sql = defineAsyncComponent(() => import('./sql.vue'));
const Tab = defineAsyncComponent(() => import('./tab.vue'));
const Xml = defineAsyncComponent(() => import('./xml.vue'));

interface Props {
  dataSource: Record<string, any>
}

const props = withDefaults(defineProps<Props>(), {
  dataSource: undefined
});

const pluginType = computed(() => {
  return props.dataSource.plugin;
});

const mockExecDetailRef = ref();

defineExpose({
  restart: () => mockExecDetailRef.value && mockExecDetailRef.value.restart()
});
</script>
<template>
  <CSV
    v-if="pluginType==='MockCsv'"
    ref="mockExecDetailRef"
    :execInfo="props.dataSource" />
  <Json
    v-if="pluginType==='MockJson'"
    ref="mockExecDetailRef"
    :execInfo="props.dataSource" />
  <Custom
    v-if="pluginType==='MockCustom'"
    ref="mockExecDetailRef"
    :execInfo="props.dataSource" />
  <Excel
    v-if="pluginType==='MockExcel'"
    ref="mockExecDetailRef"
    :execInfo="props.dataSource" />
  <Sql
    v-if="pluginType==='MockSql'"
    ref="mockExecDetailRef"
    :execInfo="props.dataSource" />
  <Tab
    v-if="pluginType==='MockTab'"
    ref="mockExecDetailRef"
    :execInfo="props.dataSource" />
  <Xml
    v-if="pluginType==='MockXml'"
    ref="mockExecDetailRef"
    :execInfo="props.dataSource" />
</template>
