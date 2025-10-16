<script lang="ts" setup>
import { ref, defineAsyncComponent, onMounted } from 'vue';
import '@xcan-angus/rapidoc';
import { cookieUtils, DomainManager, AppOrServiceRoute } from '@xcan-angus/infra';
import { Button } from 'ant-design-vue';
import YAML from 'yaml';
import { AsyncComponent } from '@xcan-angus/vue-ui';
import { useI18n } from 'vue-i18n';
import { services } from '@/api/tester';

const CodeView = defineAsyncComponent(() => import('@/views/apis/services/apis/doc/CodeView.vue'));
const ExportDoc = defineAsyncComponent(() => import('@/views/apis/services/services/SimpleExport.vue'));

interface Props {
  mode: 'UI' | 'code',
  serviceId: string;
}
const props = withDefaults(defineProps<Props>(), {
  mode: 'UI'
});

const { t } = useI18n();

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
  window.specUrl = openapiMetaDataStr.value;

  openapiMetaData.value = JSON.parse(data || '{}');
  openapiYamlData.value = YAML.stringify(openapiMetaData.value, null, 2);
  loading.value = false;
};

const accessToken = ref();
const docOrigin = ref();
onMounted(async () => {
  accessToken.value = cookieUtils.getTokenInfo().access_token;
  // docOrigin.value = await site.getUrl('apis');
  docOrigin.value = DomainManager.getInstance().getApiDomain(AppOrServiceRoute.tester);
  await loadData();
});

const exportVisible = ref(false);

const openExportModal = () => {
  exportVisible.value = true;
};

defineExpose({
  loadData
});
</script>
<template>
  <div v-if="props.mode === 'UI'">
    <rapi-doc
      v-if="docOrigin"
      :specUrl="openapiMetaDataStr"
      specIsContent="true"
      theme="light"
      renderStyle="focused"
      headerColor="#fff"
      updateRoute="false"
      navBgColor="#fff"
      showInfo="true"
      bgColor="#fff"
      allowAdvancedSearch="false"
      allowSpecUrlLoad="false"
      allowSpecFileLoad="false"
      allowSpecFileDownload="false"
      allowTry="false"
      schemaStyle="table"
      showHeader="false"
      schemaExpandLevel="20">
      <Button
        v-if="docOrigin"
        slot="extra"
        class="text-3 ml-2"
        @click="openExportModal">
        {{ t('actions.export') }}
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
