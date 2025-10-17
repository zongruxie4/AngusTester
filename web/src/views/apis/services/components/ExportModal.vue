<script setup lang="ts">
import { onMounted, ref } from 'vue';
import { Colon, Modal, Spin } from '@xcan-angus/vue-ui';
import { RadioGroup } from 'ant-design-vue';
import {
  ApiType, ApiUrlBuilder, cookieUtils, DomainManager, download, http, routerUtils, TESTER
} from '@xcan-angus/infra';
import { createPdf } from '@xcan-angus/rapipdf';
import { useI18n } from 'vue-i18n';

interface Props {
  visible: boolean;
  type?: 'SERVICE' | 'API';
  id: string;
}

const props = withDefaults(defineProps<Props>(), {
  visible: true,
  type: 'SERVICE',
  id: ''
});
const { t } = useI18n();
const emit = defineEmits<{(e: 'update:visible', value: boolean): void }>();

// Ref for the export type (SERVICE or API)
const exportType = ref<'SERVICE' | 'API'>(props.type);

// Ref for the export format (json, yaml, or pdf)
const exportFormat = ref<'json' | 'yaml' | 'pdf'>('json');

// Ref for the document origin
const documentOrigin = ref();

// Ref for the access token
const accessToken = ref();

// Ref for the service ID when exporting by API
const serviceId = ref<string>(props.id);

// Ref for tracking export loading state
const isExportLoading = ref(false);

/**
 * Handle the export action
 * Supports exporting in JSON, YAML, or PDF formats
 * Handles both service and API level exports
 */
const handleExport = async () => {
  // Prevent multiple simultaneous exports
  if (isExportLoading.value) {
    return;
  }

  // Prepare export parameters
  const exportParams: {
    serviceIds: string[];
    exportScope: 'SERVICE' | 'API';
    format: 'json' | 'yaml' | 'pdf';
  } = {
    serviceIds: [serviceId.value],
    exportScope: exportType.value,
    format: exportFormat.value
  };

  // Handle PDF export specially
  if (exportFormat.value === 'pdf') {
    let apiUrl = '';
    const routeConfig = routerUtils.getTesterApiRouteConfig(ApiType.API);

    // Export a single API as PDF
    if (props.type === 'API') {
      apiUrl = ApiUrlBuilder.buildApiUrl(routeConfig, `/apis/${props.id}/openapi/export?format=yaml&access_token=${accessToken.value}`);
      createPdf(apiUrl);
    } else {
      // Export service as PDF
      isExportLoading.value = true;
      const url = ApiUrlBuilder.buildApiUrl(routeConfig, '/services/export');
      const [error, res] = await http.post(url, {
        access_token: accessToken.value,
        exportScope: exportType.value,
        format: 'json',
        gzipCompression: false,
        serviceIds: [props.id]
      });
      isExportLoading.value = false;
      if (error) {
        return;
      }
      createPdf(res.data);
    }
    handleCloseModal();
    return;
  }

  // Handle JSON/YAML exports
  isExportLoading.value = true;
  const routeConfig = routerUtils.getTesterApiRouteConfig(ApiType.API);

  // Export a single API
  if (props.type === 'API') {
    const apiUrl = ApiUrlBuilder.buildApiUrl(routeConfig, `/apis/${props.id}/openapi/export?format=${exportFormat.value}`);
    const [error] = await download(apiUrl);
    isExportLoading.value = false;
    if (error) {
      return;
    }
  } else {
    // Export service
    // Get the API host URL
    const host = DomainManager.getInstance().getApiDomain('tester');
    const [error, res] = await http.post(`${host}${TESTER}/services/export`, {
      serviceIds: exportParams.serviceIds,
      exportScope: exportParams.exportScope,
      format: exportParams.format
    }, { responseType: 'blob', timeout: 600000 });
    isExportLoading.value = false;
    if (error) {
      return;
    }

    // Extract filename from response headers
    const disposition = res.headers['content-disposition'] || res.headers['Content-Disposition'];
    let filename = 'exported-file';
    if (disposition) {
      filename = disposition.replace(/"/gi, '').replace(/[\s\S]*filename=(\S+.\S+)[\s\S]*/i, '$1').replace(/(\S\s+);\S*/, '$1').replace(/\s$/, '');
      filename = decodeURIComponent(filename);
    }

    // Create object URL and trigger download
    const url = URL.createObjectURL(res.data);
    triggerFileDownload(url, filename);
    window.URL.revokeObjectURL(url);
  }
  handleCloseModal();
};

/**
 * Trigger file download using a dynamically created anchor element
 * @param url - The URL of the file to download
 * @param filename - The name to save the file as
 */
function triggerFileDownload (url: string, filename: string) {
  const downloadLink = document.createElement('a');
  downloadLink.style.display = 'none';
  downloadLink.href = url;
  downloadLink.download = filename;
  document.body.appendChild(downloadLink);
  downloadLink.click();
  document.body.removeChild(downloadLink);
}

/**
 * Handle closing the modal
 */
const handleCloseModal = () => {
  emit('update:visible', false);
};

// Available export format options
const exportFormatOptions = [{
  label: 'JSON',
  value: 'json'
},
{
  label: 'YAML',
  value: 'yaml'
},
{
  label: 'PDF',
  value: 'pdf'
}];

/**
 * Initialize component on mount
 * Retrieve access token and document origin
 */
onMounted(async () => {
  accessToken.value = cookieUtils.getTokenInfo().access_token;
  documentOrigin.value = DomainManager.getInstance().getApiDomain('tester');
});
</script>
<template>
  <Modal
    :visible="props.visible"
    :width="800"
    :title="t('service.exportModal.title')"
    @cancel="handleCloseModal"
    @ok="handleExport">
    <Spin :spinning="isExportLoading">
      <div class="mt-1.5">
        <span>{{ t('service.exportModal.formatLabel') }}<Colon class="ml-1 mr-3.5" /></span>
        <RadioGroup
          v-model:value="exportFormat"
          :options="exportFormatOptions">
        </RadioGroup>
      </div>
    </Spin>
  </Modal>
</template>
<style scoped>
:deep(.checkbox-border-black .ant-checkbox-inner) {
  border-color: #d3dce6;
}
</style>
