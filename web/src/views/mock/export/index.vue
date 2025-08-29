<script setup lang="ts">
import { computed, inject, ref, watch } from 'vue';
import { useI18n } from 'vue-i18n';
import { Colon, Modal, notification, SelectApi, Spin } from '@xcan-angus/vue-ui';
import { RadioGroup } from 'ant-design-vue';
import { TESTER, download } from '@xcan-angus/infra';

const { t } = useI18n();

import { MockServiceObj } from '../PropsType';

interface Props {
  visible: boolean;
  mockService?: MockServiceObj;
}

const props = withDefaults(defineProps<Props>(), {
  visible: true,
  mockService: undefined
});

const emit = defineEmits<{(e: 'update:visible', value: boolean): void }>();

const projectInfo = inject('projectInfo', ref({ id: '' }));
const projectId = computed(() => {
  return projectInfo?.value?.id;
});

const format = ref<'json' | 'yaml'>('json');

// 当前选中的接口的ids
const apiIds = ref<string[] | null >([]);
const selectApis = (data:{projectId:string;apiIds:string[], _apiOptions, checkedAll:boolean}) => {
  mockServiceId.value = data.projectId;
  if (data.projectId) {
    scrollProps.value = {
      action: `${TESTER}/mock/apis?mockServiceId=${props.mockService ? props.mockService.id : mockServiceId.value}`,
      params: undefined
    };
  }
  apiIds.value = data.checkedAll ? null : data.apiIds;
};

const loading = ref(false);

const handleOk = async () => {
  if (loading.value) {
    return;
  }
  loading.value = true;
  const [error] = apiIds.value
    ? await download(`${TESTER}/mock/service/export?mockServiceId=${props.mockService ? props.mockService.id : mockServiceId.value}&format=${format.value}&mockApiIds=${apiIds.value}&exportScope=PROJECT`)
    : await download(`${TESTER}/mock/service/export?mockServiceId=${props.mockService ? props.mockService.id : mockServiceId.value}&format=${format.value}&exportScope=PROJECT`);
  loading.value = false;
  if (error) {
    return;
  }
  notification.success(t('mock.exportModal.notifications.exportSuccess'));
  emit('update:visible', false);
};

const handleCancel = () => {
  emit('update:visible', false);
};

const treeProps = ref({
  action: `${TESTER}/mock/service?fullTextSearch=true`,
  disabled: !!props.mockService,
  params: {
    // hasPermission: 'EXPORT',
    admin: true,
    projectId: projectId.value
  },
  defaultValue: {
    name: props.mockService?.name,
    id: props.mockService?.id
  }
});
const mockServiceId = ref();
const scrollProps = ref({
  action: `${TESTER}/mock/apis?mockServiceId=${props.mockService ? props.mockService.id : mockServiceId.value}`,
  params: undefined
});

watch(() => props.visible, (newValue) => {
  if (!newValue) {
    return;
  }

  format.value = 'json';
  apiIds.value = [];
  mockServiceId.value = undefined;
  if (props.mockService) {
    treeProps.value = {
      action: `${TESTER}/mock/service?fullTextSearch=true`,
      disabled: !!props.mockService,
      params: {
        // hasPermission: 'EXPORT',
        admin: true,
        projectId: projectId.value
      },
      defaultValue: {
        name: props.mockService?.name,
        id: props.mockService?.id
      }
    };
    scrollProps.value = {
      action: `${TESTER}/mock/apis?mockServiceId=${props.mockService ? props.mockService.id : mockServiceId.value}`,
      params: undefined
    };
  } else {
    treeProps.value = {
      action: `${TESTER}/mock/service`,
      params: {
        // hasPermission: 'EXPORT',
        admin: true,
        projectId: projectId.value
      },
      disabled: !!props.mockService,
      defaultValue: undefined
    };
  }
}, {
  immediate: true
});

const formatTypes = [{
  label: 'JSON',
  value: 'json'
}, {
  label: 'YAML',
  value: 'yaml'
}];

</script>
<template>
  <Modal
    :visible="props.visible"
    :width="800"
    :title="t('mock.exportModal.title')"
    @cancel="handleCancel"
    @ok="handleOk">
    <Spin :spinning="loading">
      <div class="flex items-center">
        <span>{{ t('mock.exportModal.format') }}<Colon class="ml-1 mr-3.5" /></span>
        <RadioGroup
          v-model:value="format"
          :options="formatTypes">
        </RadioGroup>
      </div>
      <SelectApi
        v-if="props.visible"
        style="height: 448px;"
        class="mt-2"
        mode="multiple"
        :fields=" [
          { key: 'endpoint', name: t('mock.exportModal.fields.endpoint') },
          { key: 'summary', name: t('mock.exportModal.fields.summary') }
        ]"
        treeLabel=""
        :treeProps="treeProps"
        :scrollProps="(props.mockService || mockServiceId)?scrollProps:undefined"
        @change="selectApis" />
    </Spin>
  </Modal>
</template>
<style scoped>
:deep(.checkbox-border-black .ant-checkbox-inner) {
  border-color: #d3dce6;
}
</style>
