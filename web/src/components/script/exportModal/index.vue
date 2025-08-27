<script setup lang="ts">
import { ref } from 'vue';
import { useI18n } from 'vue-i18n';
import { Colon, Modal, notification } from '@xcan-angus/vue-ui';
import { RadioGroup } from 'ant-design-vue';
import { download, routerUtils, ApiType, ApiUrlBuilder } from '@xcan-angus/infra';

const { t } = useI18n();

interface Props {
  visible: boolean;
  ids: string[];
}

const props = withDefaults(defineProps<Props>(), {
  visible: false,
  ids: () => []
});

const emit = defineEmits<{(e: 'update:visible', value: boolean): void }>();

const handleCancel = () => {
  emit('update:visible', false);
};

const exportLoading = ref(false);

const format = ref('JSON');
const handleOk = async () => {
  if (exportLoading.value || !props.ids?.length) {
    return;
  }

  exportLoading.value = true;
  // const host = await site.getUrl('apis');
  const routeConfig = routerUtils.getTesterApiRouteConfig(ApiType.API);
  const urls:string[] = props.ids.map((item) => {
    // return `${host}${TESTER}/script/${item}/export?format=${format.value}`;
    return ApiUrlBuilder.buildApiUrl(routeConfig, `/script/${item}/export?format=${format.value}`);
  });
  const res = await download(urls);
  exportLoading.value = false;
  const totalNum = props.ids.length;
  if (totalNum > 1) {
    // const errorNum = res?.filter(([_error]) => _error)?.length || 0;
    // if (errorNum === 0) {
    //   notification.success(`选中的 ${totalNum} 脚本全部导出成功`);
    // } else if (errorNum === totalNum) {
    //   notification.error(`选中的 ${totalNum} 脚本全部导出失败`);
    // } else {
    //   notification.warning(`选中的 ${totalNum - errorNum} 条脚本导出成功，${errorNum} 条脚本导出失败`);
    // }
  } else {
    const _error = res[0];
    if (_error) {
      notification.error(_error?.message);
    }
  }

  handleCancel();
};

const formatTypes = [{
  label: t('commonComp.exportScriptModal.json'),
  value: 'JSON'
}, {
  label: t('commonComp.exportScriptModal.yaml'),
  value: 'YAML'
}];
</script>
<template>
  <Modal
    :visible="props.visible"
    :confirmLoading="exportLoading"
    :title="t('commonComp.exportScriptModal.title')"
    @cancel="handleCancel"
    @ok="handleOk">
    <div class="mt-1.5">
      <span class="mr-3.5">{{ t('commonComp.exportScriptModal.format') }}
        <Colon class="ml-1" />
      </span>
      <RadioGroup v-model:value="format" :options="formatTypes" />
    </div>
  </Modal>
</template>
