<script lang="ts" setup>
import { useI18n } from 'vue-i18n';
import { defineAsyncComponent } from 'vue';
import { TabPane, Tabs } from 'ant-design-vue';
import { Hints, IconRequired, Input } from '@xcan-angus/vue-ui';
import { useStaticDataset } from './composables/useStaticDataset';
import { DataSetItem } from '../types';

const { t } = useI18n();

// Define async components
const ButtonGroup = defineAsyncComponent(() => import('@/views/data/dataset/detail/ButtonGroup.vue'));
const ParameterInput = defineAsyncComponent(() => import('./ParameterInput.vue'));
const PreviewData = defineAsyncComponent(() => import('@/views/data/dataset/detail/PreviewData.vue'));
const DataSetUseList = defineAsyncComponent(() => import('@/views/data/dataset/detail/UseList.vue'));

// Define component props
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

// Define component emits
// eslint-disable-next-line func-call-spacing
const emit = defineEmits<{
  (e: 'ok', data: DataSetItem, isEdit: boolean): void;
  (e: 'delete', value: string): void;
  (e: 'export', value: string): void;
  (e: 'clone', value: string): void;
  (e: 'copyLink', value: string): void;
  (e: 'refresh', value: string): void;
}>();

// Use the composable for static dataset logic
const {
  // State
  activeKey,
  dataSetName,
  description,
  defaultParameters,
  previewData,
  parametersRef,

  // Computed properties
  dataSetId,
  editFlag,
  okButtonDisabled,

  // Methods
  handleButtonClick,
  handleParametersChange,
  initialize,
  updatePreviewData
} = useStaticDataset(props, emit as any);

// Expose methods for parent components
defineExpose({
  initialize,
  updatePreviewData
});
</script>

<template>
  <ButtonGroup
    :editFlag="editFlag"
    :okButtonDisabled="okButtonDisabled"
    class="mb-5"
    @click="handleButtonClick" />

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
          @change="handleParametersChange" />
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
