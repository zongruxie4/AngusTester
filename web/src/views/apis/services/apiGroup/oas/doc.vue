<script lang="ts" setup>
import { ref, defineAsyncComponent, onMounted } from 'vue';
import '@xcan-angus/rapidoc';
import { cookie, site } from '@xcan-angus/tools';
import { Button } from 'ant-design-vue';
import YAML from 'yaml';
import { AsyncComponent } from '@xcan-angus/vue-ui';
import { services } from '@/api/tester';

interface Props {
  mode: 'UI' | 'code',
  serviceId: string;
}
const props = withDefaults(defineProps<Props>(), {
  mode: 'UI'
});

const loading = ref(false);
const openapiMetaDataStr = ref();
const openapiMetaData = ref();
const openapiYamlData = ref();

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
  openapiMetaDataStr.value = data;
  window.specUrl = openapiMetaDataStr;

  openapiMetaData.value = JSON.parse(data || '{}');
  openapiYamlData.value = YAML.stringify(openapiMetaData.value, null, 2);
  loading.value = false;
};

const accessToken = ref();
const docOrigin = ref();
onMounted(async () => {
  accessToken.value = cookie.get('access_token');
  docOrigin.value = await site.getUrl('apis');
  loadData();
});

const exportVisible = ref(false);

const openExportModal = () => {
  exportVisible.value = true;
};

const CodeView = defineAsyncComponent(() => import('./CodeView.vue'));
const ExportDoc = defineAsyncComponent(() => import('@/views/apis/services/sidebar/components/exportServices/pureExport.vue'));

defineExpose({
  loadData
});

</script>
<template>
  <div v-if="props.mode === 'UI'">
    <rapi-doc
      v-if="docOrigin"
      :spec-url="openapiMetaDataStr"
      spec-is-content="true"
      render-style="read"
      theme="light"
      header-color="#fff"
      update-route="false"
      nav-bg-color="#fff"
      show-info="true"
      bg-color="#fff"
      allow-advanced-search="false"
      allow-spec-url-load="false"
      allow-spec-file-load="false"
      allow-spec-file-download="false"
      allow-try="false"
      schema-style="table"
      show-header="false"
      schema-expand-level="20">
      <Button
        v-if="docOrigin"
        slot="extra"
        class="text-3 ml-2"
        @click="openExportModal">
        导出
      </Button>
    </rapi-doc>
  </div>
  <CodeView
    v-else
    :value="openapiYamlData" />

  <AsyncComponent :visible="exportVisible">
    <ExportDoc
      :id="props.serviceId"
      v-model:visible="exportVisible" />
  </AsyncComponent>
</template>
