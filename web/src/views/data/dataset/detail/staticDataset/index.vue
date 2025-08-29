<script lang="ts" setup>
import { useI18n } from 'vue-i18n';
import { computed, defineAsyncComponent, onMounted, ref, watch } from 'vue';
import { TabPane, Tabs } from 'ant-design-vue';
import { Hints, IconRequired, Input, notification } from '@xcan-angus/vue-ui';
import { cloneDeep, isEqual } from 'lodash-es';
import { dataSet } from '@/api/tester';

const { t } = useI18n();

import { DataSetItem } from '../../PropsType';
import { FormState } from './PropsType';

type Props = {
  projectId: string;
  userInfo: { id: string; };
  visible: boolean;
  dataSource?: DataSetItem;
}

const props = withDefaults(defineProps<Props>(), {
  projectId: undefined,
  userInfo: undefined,
  visible: false,
  dataSource: undefined
});

// eslint-disable-next-line func-call-spacing
const emit = defineEmits<{
  (e: 'ok', data: DataSetItem, isEdit: boolean): void;
  (e: 'delete', value: string): void;
  (e: 'export', value: string): void;
  (e: 'clone', value: string): void;
  (e: 'copyLink', value: string): void;
  (e: 'refresh', value: string): void;
}>();

const ButtonGroup = defineAsyncComponent(() => import('@/views/data/dataset/detail/buttonGroup/index.vue'));
const ParameterInput = defineAsyncComponent(() => import('./parameterInput.vue'));
const PreviewData = defineAsyncComponent(() => import('@/views/data/dataset/previewData/index.vue'));
const DataSetUseList = defineAsyncComponent(() => import('@/views/data/dataset/detail/useList/index.vue'));

const parametersRef = ref();

const confirmLoading = ref(false);
const activeKey = ref<'value' | 'preview' | 'use'>('value');

const dataSetName = ref<string>('');
const description = ref<string>('');
const parameters = ref<{ name: string; value: string; }[]>([]);
const defaultParameters = ref<{ name: string; value: string; }[]>([]);

const previewData = ref<{
  id: string | undefined;
  projectId: string;
  name: string;
  parameters: { name: string; value: string; }[];
}>();

const buttonGroupClick = (key: 'ok' | 'delete' | 'export' | 'clone' | 'copyLink' | 'refresh') => {
  if (key === 'ok') {
    ok();
    return;
  }

  if (key === 'delete') {
    emit('delete', dataSetId.value);
    return;
  }

  if (key === 'export') {
    emit('export', dataSetId.value);
    return;
  }

  if (key === 'clone') {
    emit('clone', dataSetId.value);
    return;
  }

  if (key === 'copyLink') {
    emit('copyLink', dataSetId.value);
    return;
  }

  if (key === 'refresh') {
    emit('refresh', dataSetId.value);
  }
};

const ok = async () => {
  let validFlag = true;
  if (typeof parametersRef.value?.isValid === 'function') {
    validFlag = parametersRef.value.isValid();
  }

  if (!validFlag) {
    return;
  }

  if (editFlag.value) {
    toEdit();
    return;
  }

  toCreate();
};

const toEdit = async () => {
  const params = getParams();
  confirmLoading.value = true;
  const [error] = await dataSet.putDataSet(params);
  confirmLoading.value = false;
  if (error) {
    return;
  }

  notification.success(t('dataset.detail.staticDataset.notifications.editSuccess'));
  emit('ok', params, true);
};

const toCreate = async () => {
  const params = getParams();
  confirmLoading.value = true;
  const [error, res] = await dataSet.addDataSet(params);
  confirmLoading.value = false;
  if (error) {
    return;
  }

  notification.success(t('dataset.detail.staticDataset.notifications.addSuccess'));
  const id = res?.data?.id;
  emit('ok', { ...params, id }, false);
};

const parametersChange = (data: { name: string; value: string; }[]) => {
  parameters.value = data;
};

const getParams = (): FormState => {
  const params: FormState = {
    projectId: props.projectId,
    name: dataSetName.value,
    parameters: parameters.value,
    description: description.value
  };

  const id = dataSetId.value;
  if (id) {
    params.id = id;
  }

  return params;
};

const initialize = () => {
  const data = props.dataSource;
  if (!data) {
    return;
  }

  dataSetName.value = data.name;
  description.value = data.description;
  parameters.value = data.parameters;
  defaultParameters.value = cloneDeep(data.parameters);
};

onMounted(() => {
  watch(() => props.dataSource, (newValue) => {
    if (!newValue) {
      return;
    }

    initialize();
  }, { immediate: true });

  watch(() => activeKey.value, (newValue) => {
    if (newValue !== 'preview') {
      return;
    }

    const newData = {
      id: props.dataSource?.id,
      projectId: props.projectId,
      name: dataSetName.value,
      parameters: parameters.value
    };

    if (!isEqual(newData, previewData.value)) {
      previewData.value = newData;
    }
  }, { immediate: true });
});

const dataSetId = computed(() => {
  return props.dataSource?.id || '';
});

const editFlag = computed(() => {
  return !!props.dataSource?.id;
});

const okButtonDisabled = computed(() => {
  return !dataSetName.value || !parameters.value?.length;
});
</script>
<template>
  <ButtonGroup
    :editFlag="editFlag"
    :okButtonDisabled="okButtonDisabled"
    class="mb-5"
    @click="buttonGroupClick" />

  <div class="flex items-center mb-3.5">
    <div class="flex items-center flex-shrink-0 mr-2.5">
      <IconRequired />
      <span>{{ t('dataset.detail.staticDataset.name') }}</span>
    </div>
    <Input
      v-model:value="dataSetName"
      :maxlength="100"
      :placeholder="t('dataset.detail.staticDataset.namePlaceholder')"
      trimAll
      excludes="{}" />
  </div>

  <div class="flex items-start">
    <div class="flex items-center flex-shrink-0 mr-2.5 transform-gpu translate-y-1">
      <IconRequired class="invisible" />
      <span>{{ t('dataset.detail.staticDataset.description') }}</span>
    </div>
    <Input
      v-model:value="description"
      :maxlength="200"
      :autoSize="{ minRows: 3, maxRows: 5 }"
      showCount
      type="textarea"
      class="flex-1"
      :placeholder="t('dataset.detail.staticDataset.descriptionPlaceholder')"
      trim />
  </div>

  <Tabs
    v-model:activeKey="activeKey"
    size="small"
    class="ant-tabs-nav-mb-2.5">
    <TabPane key="value">
      <template #tab>
        <div class="flex items-center font-normal">
          <IconRequired />
          <span>{{ t('dataset.detail.staticDataset.parameters') }}</span>
        </div>
      </template>

      <div>
        <Hints class="mb-2" :text="t('dataset.detail.staticDataset.hints')" />
        <ParameterInput
          ref="parametersRef"
          :defaultValue="defaultParameters"
          @change="parametersChange" />
      </div>
    </TabPane>

    <TabPane key="preview">
      <template #tab>
        <div class="flex items-center font-normal">
          <span>{{ t('dataset.detail.staticDataset.preview') }}</span>
        </div>
      </template>

      <PreviewData :dataSource="previewData" />
    </TabPane>

    <TabPane v-if="dataSetId" key="use">
      <template #tab>
        <div class="flex items-center font-normal">
          <span>{{ t('dataset.detail.staticDataset.use') }}</span>
        </div>
      </template>

      <DataSetUseList :id="dataSetId" />
    </TabPane>
  </Tabs>
</template>
