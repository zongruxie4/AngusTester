<script setup lang="ts">
import { ref } from 'vue';
import { Colon, Modal, notification } from '@xcan-angus/vue-ui';
import { RadioGroup } from 'ant-design-vue';
import { ApiType, ApiUrlBuilder, download, routerUtils } from '@xcan-angus/infra';

interface Props {
  visible: boolean;
  id: string|undefined;
}

const props = withDefaults(defineProps<Props>(), {
  visible: false,
  id: undefined
});

const emit = defineEmits<{(e: 'update:visible', value: boolean): void }>();

const handleCancel = () => {
  emit('update:visible', false);
};

const exportLoading = ref(false);

const format = ref('JSON');
const handleOk = async () => {
  if (exportLoading.value || !props.id) {
    return;
  }

  exportLoading.value = true;
  // const host = await site.getUrl('apis');
  const routeConfig = routerUtils.getTesterApiRouteConfig(ApiType.API);
  const exportUrl = ApiUrlBuilder.buildApiUrl(routeConfig, `/script/${props.id}/export?format=${format.value}`);
  // const exportUrl = `${host}${TESTER}/script/${props.id}/export?format=${format.value}`;
  const [error] = await download(exportUrl);
  exportLoading.value = false;
  if (error) {
    notification.error(error.message);
    return;
  }

  handleCancel();
};

const formatTypes = [{
  label: 'JSON',
  value: 'JSON'
}, {
  label: 'YAML',
  value: 'YAML'
}];
</script>
<template>
  <Modal
    :visible="props.visible"
    :confirmLoading="exportLoading"
    title="导出脚本"
    @cancel="handleCancel"
    @ok="handleOk">
    <div class="mt-1.5">
      <span class="mr-3.5">格式<Colon class="ml-1" /></span>
      <RadioGroup v-model:value="format" :options="formatTypes" />
    </div>
  </Modal>
</template>
