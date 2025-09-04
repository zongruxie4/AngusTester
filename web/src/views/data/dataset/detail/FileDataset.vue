<script lang="ts" setup>
import { defineAsyncComponent } from 'vue';
import { useI18n } from 'vue-i18n';
import { TabPane, Tabs } from 'ant-design-vue';
import { IconRequired, Tooltip, Input, Icon, Hints, Toggle, SelectInput } from '@xcan-angus/vue-ui';
import { useFileDataset } from './composables/useFileDataset';
import { DataSetItem } from '../types';

const { t } = useI18n();

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

const ButtonGroup = defineAsyncComponent(() => import('@/views/data/dataset/detail/ButtonGroup.vue'));
const ParameterNameInput = defineAsyncComponent(() => import('@/views/data/dataset/detail/ParameterNameInput.vue'));
const PreviewData = defineAsyncComponent(() => import('@/views/data/dataset/preview/index.vue'));
const DataSetUseList = defineAsyncComponent(() => import('@/views/data/dataset/detail/UseList.vue'));
const MatchItemPopover = defineAsyncComponent(() => import('@/views/data/dataset/detail/MatchItemPopover.vue'));
const SelectEnum = defineAsyncComponent(() => import('@/components/selectEnum/index.vue'));

// Use the composable for file dataset logic
const {
  // State
  activeKey,
  dataSetName,
  description,
  defaultParameters,
  filePath,
  fileType,
  encoding,
  rowIndex,
  columnIndex,
  separatorChar,
  escapeChar,
  quoteChar,
  method,
  defaultValue,
  expression,
  matchItem,
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
} = useFileDataset(props, emit as any);

// Expose methods for parent components
defineExpose({
  initialize,
  updatePreviewData
});

const encodingOptions = [
  {
    label: 'UTF-8',
    value: 'UTF-8'
  },
  {
    label: 'UTF-16',
    value: 'UTF-16'
  },
  {
    label: 'UTF-16BE',
    value: 'UTF-16BE'
  },
  {
    label: 'UTF-16LE',
    value: 'UTF-16LE'
  },
  {
    label: 'US-ASCII',
    value: 'US-ASCII'
  },
  {
    label: 'ISO-8859-1',
    value: 'ISO-8859-1'
  }
];

const inputProps = {
  maxlength: 20,
  trimAll: true
};
</script>
<template>
  <ButtonGroup
    :editFlag="editFlag"
    :okButtonDisabled="okButtonDisabled"
    class="mb-5"
    @click="handleButtonClick" />

  <div class="flex items-center mb-3.5">
    <div class="mr-2.5 flex-shrink-0 font-semibold w-18 text-3 text-right">
      <IconRequired />
      <span>{{ t('dataset.detail.fileDataset.form.name') }}</span>
    </div>
    <Input
      v-model:value="dataSetName"
      :maxlength="100"
      :placeholder="t('dataset.detail.fileDataset.form.namePlaceholder')"
      trimAll
      excludes="{}" />
  </div>

  <div class="flex items-start">
    <div class="mr-2.5 flex items-center flex-shrink-0 transform-gpu translate-y-1 font-semibold w-18 text-3 text-right">
      <IconRequired class="invisible" />
      <span>{{ t('dataset.detail.fileDataset.form.description') }}</span>
    </div>
    <Input
      v-model:value="description"
      :maxlength="200"
      :autoSize="{ minRows: 3, maxRows: 5 }"
      showCount
      type="textarea"
      class="flex-1"
      :placeholder="t('dataset.detail.fileDataset.form.descriptionPlaceholder')"
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
          <span>{{ t('dataset.detail.fileDataset.form.extraction') }}</span>
        </div>
      </template>

      <div>
        <Hints class="mb-2.5" :text="t('dataset.detail.fileDataset.form.hints')" />

        <Toggle :title="t('dataset.detail.fileDataset.form.parameterName')" class="text-3 leading-5 mb-3.5 params-container">
          <ParameterNameInput
            ref="parametersRef"
            :columnIndex="columnIndex"
            :defaultValue="defaultParameters"
            @change="handleParametersChange" />
        </Toggle>

        <p />

        <Toggle :title="t('dataset.detail.fileDataset.form.readConfig')" class="text-3 leading-5 mb-3.5">
          <div class="flex items-center mb-3.5">
            <div class="w-28 flex-shrink-0 font-semibold text-3 text-right mr-2.5">
              <IconRequired />
              <span>{{ t('dataset.detail.fileDataset.form.filePath') }}</span>
            </div>
            <Input
              v-model:value="filePath"
              :maxlength="800"
              style="width:calc(100% - 82px);"
              :placeholder="t('dataset.detail.fileDataset.form.filePathPlaceholder')"
              trimAll />
          </div>

          <div class="flex items-center space-x-5 mb-3.5">
            <div class="w-1/2 flex items-center">
              <div class="w-28 flex-shrink-0 font-semibold text-3 text-right mr-2.5">
                <IconRequired />
                <span>{{ t('dataset.detail.fileDataset.form.fileType') }}</span>
              </div>
              <SelectEnum
                v-model:value="fileType"
                enumKey="ExtractionFileType"
                class="w-full-20.5" />
            </div>

            <div class="w-1/2 flex items-center">
              <div class="w-28 flex-shrink-0 font-semibold text-3 text-right mr-2.5">
                <IconRequired />
                <span>{{ t('dataset.detail.fileDataset.form.fileEncoding') }}</span>
              </div>
              <SelectInput
                v-model:value="encoding"
                :options="encodingOptions"
                :inputProps="inputProps"
                class="w-full-20.5" />
            </div>
          </div>

          <div class="flex items-center space-x-5 mb-3.5">
            <div class="w-1/2 flex items-center">
              <div class="w-28 flex-shrink-0 font-semibold text-3 text-right mr-2.5">
                <IconRequired />
                <span>{{ t('dataset.detail.fileDataset.form.readStartRow') }}</span>
              </div>
              <Input
                v-model:value="rowIndex"
                :maxlength="4"
                dataType="number"
                :placeholder="t('dataset.detail.fileDataset.form.readStartRowPlaceholder')"
                trimAll />
              <Tooltip :title="t('dataset.detail.fileDataset.form.readStartRowTooltip')">
                <Icon icon="icon-tishi1" class="text-tips ml-1 text-3.5 cursor-pointer" />
              </Tooltip>
            </div>

            <div class="w-1/2 flex items-center">
              <div class="w-28 flex-shrink-0 font-semibold text-3 text-right mr-2.5">
                <IconRequired />
                <span>{{ t('dataset.detail.fileDataset.form.readStartColumn') }}</span>
              </div>
              <Input
                v-model:value="columnIndex"
                :maxlength="4"
                dataType="number"
                :placeholder="t('dataset.detail.fileDataset.form.readStartColumnPlaceholder')"
                trimAll />
              <Tooltip :title="t('dataset.detail.fileDataset.form.readStartColumnTooltip')">
                <Icon icon="icon-tishi1" class="text-tips ml-1 text-3.5 cursor-pointer" />
              </Tooltip>
            </div>
          </div>

          <div class="flex items-center space-x-5 mb-3.5">
            <div class="w-1/2 flex items-center">
              <div class="w-28 flex-shrink-0 font-semibold text-3 text-right mr-2.5">
                <IconRequired />
                <span>{{ t('dataset.detail.fileDataset.form.separator') }}</span>
              </div>
              <Input
                v-model:value="separatorChar"
                :maxlength="1"
                trimAll />
              <Tooltip :title="t('dataset.detail.fileDataset.form.separatorTooltip')">
                <Icon icon="icon-tishi1" class="text-tips ml-1 text-3.5 cursor-pointer" />
              </Tooltip>
            </div>

            <div class="w-1/2 flex items-center">
              <div class="w-28 flex-shrink-0 font-semibold text-3 text-right mr-2.5">
                <IconRequired />
                <span>{{ t('dataset.detail.fileDataset.form.escapeChar') }}</span>
              </div>
              <Input
                v-model:value="escapeChar"
                :maxlength="1"
                trimAll />
              <Tooltip :title="t('dataset.detail.fileDataset.form.escapeCharTooltip')">
                <Icon icon="icon-tishi1" class="text-tips ml-1 text-3.5 cursor-pointer" />
              </Tooltip>
            </div>
          </div>

          <div class="flex items-center space-x-5 mb-3.5">
            <div class="w-1/2 flex items-center">
              <div class="w-28 flex-shrink-0 font-semibold text-3 text-right mr-2.5">
                <IconRequired />
                <span>{{ t('dataset.detail.fileDataset.form.quoteChar') }}</span>
              </div>
              <Input
                v-model:value="quoteChar"
                :maxlength="1"
                trimAll />
              <Tooltip :title="t('dataset.detail.fileDataset.form.quoteCharTooltip')">
                <Icon icon="icon-tishi1" class="text-tips ml-1 text-3.5 cursor-pointer" />
              </Tooltip>
            </div>
          </div>
        </Toggle>

        <Toggle :title="t('dataset.detail.fileDataset.form.extractionConfig')" class="text-3 leading-5">
          <template v-if="method === 'EXACT_VALUE'">
            <div class="flex items-center space-x-5 mb-3.5">
              <div class="w-1/2 flex items-center">
                <div class="w-28 flex-shrink-0 font-semibold text-3 text-right mr-2.5">
                  <IconRequired />
                  <span>{{ t('dataset.detail.fileDataset.form.extractionMethod') }}</span>
                </div>
                <SelectEnum
                  v-model:value="method"
                  enumKey="ExtractionMethod"
                  :placeholder="t('dataset.detail.fileDataset.form.extractionMethodPlaceholder')"
                  class="w-full-20.5" />
              </div>

              <div class="w-1/2 flex items-center">
                <div class="w-28 flex-shrink-0 font-semibold text-3 text-right mr-2.5">
                  <IconRequired class="invisible" />
                  <span>{{ t('dataset.detail.fileDataset.form.defaultValue') }}</span>
                </div>
                <Input
                  v-model:value="defaultValue"
                  :placeholder="t('dataset.detail.fileDataset.form.defaultValuePlaceholder')"
                  class="w-full-20.5"
                  trim
                  :maxlength="4096" />
              </div>
            </div>
          </template>

          <template v-else>
            <div class="flex items-center space-x-5 mb-3.5">
              <div class="w-1/2 flex items-center">
                <div class="w-28 flex-shrink-0 font-semibold text-3 text-right mr-2.5">
                  <IconRequired />
                  <span>{{ t('dataset.detail.fileDataset.form.extractionMethod') }}</span>
                </div>
                <SelectEnum
                  v-model:value="method"
                  enumKey="ExtractionMethod"
                  :placeholder="t('dataset.detail.fileDataset.form.extractionMethodPlaceholder')"
                  class="w-full-20.5" />
              </div>

              <div class="w-1/2 flex items-center">
                <div class="w-28 flex-shrink-0 font-semibold text-3 text-right mr-2.5">
                  <IconRequired />
                  <span>{{ t('dataset.detail.fileDataset.form.expression') }}</span>
                </div>
                <Input
                  v-model:value="expression"
                  :placeholder="t('dataset.detail.fileDataset.form.expressionPlaceholder')"
                  class="w-full-20.5"
                  trimAll />
              </div>
            </div>

            <div class="flex items-center space-x-5 mb-3.5">
              <div class="w-1/2 flex items-center">
                <div class="w-28 flex-shrink-0 font-semibold text-3 text-right mr-2.5">
                  <IconRequired class="invisible" />
                  <span>{{ t('dataset.detail.fileDataset.form.matchItem') }}</span>
                </div>
                <Input
                  v-model:value="matchItem"
                  :placeholder="t('dataset.detail.fileDataset.form.matchItemPlaceholder')"
                  class="w-full-20.5"
                  dataType="number"
                  trimAll
                  :max="2000"
                  :maxlength="4" />
                <MatchItemPopover />
              </div>

              <div class="w-1/2 flex items-center">
                <div class="w-28 flex-shrink-0 font-semibold text-3 text-right mr-2.5">
                  <IconRequired class="invisible" />
                  <span>{{ t('dataset.detail.fileDataset.form.defaultValue') }}</span>
                </div>
                <Input
                  v-model:value="defaultValue"
                  :placeholder="t('dataset.detail.fileDataset.form.defaultValuePlaceholder')"
                  class="w-full-20.5"
                  trim
                  :maxlength="4096" />
              </div>
            </div>
          </template>
        </Toggle>
      </div>
    </TabPane>

    <TabPane key="preview">
      <template #tab>
        <div class="flex items-center font-normal">
          <span>{{ t('dataset.detail.fileDataset.tabs.preview') }}</span>
        </div>
      </template>

      <PreviewData :dataSource="previewData" />
    </TabPane>

    <TabPane v-if="dataSetId" key="use">
      <template #tab>
        <div class="flex items-center font-normal">
          <span>{{ t('dataset.detail.fileDataset.tabs.use') }}</span>
        </div>
      </template>

      <DataSetUseList :id="dataSetId" />
    </TabPane>
  </Tabs>
</template>

<style scoped>
:deep(.toggle-title) {
  color: var(--content-text-content);
  font-size: 12px;
  font-weight: normal;
}

:deep(.ant-collapse)>.ant-collapse-item>.ant-collapse-content {
  margin-top: 14px;
}

:deep(.ant-collapse) .ant-collapse-content>.ant-collapse-content-box {
  padding-left: 22px;
}

:deep(.params-container.ant-collapse)>.ant-collapse-item>.ant-collapse-content {
  margin-top: 10px;
}

.w-1\/2 {
  width: calc((100% - 20px)/2);
}

.w-full-20\.5 {
  width: calc(100% - 82px);
}
</style>
