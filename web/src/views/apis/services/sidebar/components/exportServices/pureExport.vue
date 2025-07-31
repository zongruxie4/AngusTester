<script setup lang="ts">
import {onMounted, ref} from 'vue';
import {Colon, Modal, Spin} from '@xcan-angus/vue-ui';
import {RadioGroup} from 'ant-design-vue';
import {
  ApiType,
  ApiUrlBuilder,
  cookieUtils,
  DomainManager,
  download,
  http,
  routerUtils,
  TESTER
} from '@xcan-angus/infra';
import {createPdf} from '@xcan-angus/rapipdf';

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
const emit = defineEmits<{(e: 'update:visible', value: boolean): void }>();

const exportType = ref<'SERVICE' | 'API'>(props.type);
const format = ref<'json' | 'yaml' | 'pdf'>('json');
const docOrigin = ref();
const accessToken = ref();

// 按接口导出绑定的项目服务的id
const serviceId = ref<string>(props.id);
const exportLoading = ref(false);

// 导出
const handleOk = async () => {
  if (exportLoading.value) {
    return;
  }

  const params: {
    serviceIds: string[];
    exportScope: 'SERVICE' | 'API';
    format: 'json' | 'yaml' | 'pdf';
  } = {
    serviceIds: [serviceId.value],
    exportScope: exportType.value,
    format: format.value
  };
  if (format.value === 'pdf') {
    let apiUrl = '';
    const routeConfig = routerUtils.getTesterApiRouteConfig(ApiType.API);
    if (props.type === 'API') {
      apiUrl = ApiUrlBuilder.buildApiUrl(routeConfig, `/apis/${props.id}/openapi/export?format=yaml&access_token=${accessToken.value}`)
      // apiUrl = `${docOrigin.value}${TESTER}/apis/${props.id}/openapi/export?format=yaml&access_token=${accessToken.value}`;
      createPdf(apiUrl);
    } else {
      exportLoading.value = true;
      // services/export?exportScope=SERVICE&serviceIds=${props.serviceId}&format=yaml&access_token=${accessToken}&gzipCompression=false
      const url = ApiUrlBuilder.buildApiUrl(routeConfig, '/services/export');
      const [error, res] = await http.post(url, {
        access_token: accessToken.value,
        exportScope: exportType.value,
        format: 'json',
        gzipCompression: false,
        serviceIds: [props.id]
      });
      exportLoading.value = false;
      if (error) {
        return;
      }
      createPdf(res.data);
    }
    handleCancel();
    return;
  }
  exportLoading.value = true;
  // const host = await site.getUrl('apis');
  const routeConfig = routerUtils.getTesterApiRouteConfig(ApiType.API);

  // 单接口导出
  if (props.type === 'API') {
    const apiUrl = ApiUrlBuilder.buildApiUrl(routeConfig, `/apis/${props.id}/openapi/export?format=${format.value}`)
    // const apiUrl = `${host}${TESTER}/apis/${props.id}/openapi/export?format=${format.value}`;
    const [error] = await download(apiUrl);
    exportLoading.value = false;
    if (error) {
      return;
    }
  } else {
    const [error, res] = await http.post(`${host}${TESTER}/services/export`, {
      serviceIds: params.serviceIds,
      exportScope: params.exportScope,
      format: params.format
    }, { responseType: 'blob', timeout: 600000 });
    exportLoading.value = false;
    if (error) {
      return;
    }
    const disposition = res.headers['content-disposition'] || res.headers['Content-Disposition'];
    let filename:string;
    if (disposition) {
      filename = disposition.replace(/"/gi, '').replace(/[\s\S]*filename=(\S+.\S+)[\s\S]*/i, '$1').replace(/(\S\s+);\S*/, '$1').replace(/\s$/, '');
      filename = decodeURIComponent(filename);
    }
    const url = URL.createObjectURL(res.data);
    downloadFile(url, filename);
    window.URL.revokeObjectURL(url);
  }
  handleCancel();
};

function downloadFile (url: string, filename: string) {
  const a = document.createElement('a');
  a.style.display = 'none';
  a.href = url;
  a.download = filename;
  document.body.appendChild(a);
  a.click();
  document.body.removeChild(a);
}

const handleCancel = () => {
  emit('update:visible', false);
};

const formatTypes = [{
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

onMounted(async () => {
  accessToken.value = cookieUtils.get('access_token');
  docOrigin.value = DomainManager.getInstance().getApiDomain('tester');
});

</script>
<template>
  <Modal
    :visible="props.visible"
    :width="800"
    title="导出"
    @cancel="handleCancel"
    @ok="handleOk">
    <Spin :spinning="exportLoading">
      <div class="mt-1.5">
        <span>格式<Colon class="ml-1 mr-3.5" /></span>
        <RadioGroup
          v-model:value="format"
          :options="formatTypes">
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
