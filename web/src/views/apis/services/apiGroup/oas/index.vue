<script setup lang="ts">
import { defineAsyncComponent, onMounted, ref } from 'vue';
import { Spin } from '@xcan-angus/vue-ui';
import YAML from 'yaml';
import { services } from '@/api/tester';
import { OpenApiInfo } from './PropsType';

interface Props {
  serviceId: string;
  mode:'UI'|'code'
}

const props = withDefaults(defineProps<Props>(), {
  serviceId: undefined,
  mode: 'UI'
});

const CodeView = defineAsyncComponent(() => import('./CodeView.vue'));

const loading = ref(false);
const openapiMetaData = ref<OpenApiInfo>();
const openapiYamlData = ref<string>('');

const loadData = async () => {
  if (loading.value) {
    return;
  }

  loading.value = true;
  const [error, { data }] = await services.getOpenapi(props.serviceId, { gzipCompression: false, format: 'json' });
  if (error) {
    loading.value = false;
    return;
  }

  openapiMetaData.value = JSON.parse(data || '{}');
  openapiYamlData.value = YAML.stringify(openapiMetaData.value, null, 2);

  loading.value = false;
};

const selectStr = ref();
const startKey = ref<string[]>([]);

onMounted(() => {
  loadData();
});

defineExpose({ loadData });
</script>

<template>
  <Spin :spinning="loading" class="flex h-full flex-nowrap text-3">
    <CodeView
      :value="openapiYamlData"
      :selectStr="selectStr"
      :startKey="startKey">
    </codeview>
  </Spin>
</template>
<style scoped>

</style>
