<script lang="ts" setup>
import { defineAsyncComponent } from 'vue';
import { useI18n } from 'vue-i18n';
import { TabPane, Tabs } from 'ant-design-vue';
import { Hints, Icon, IconRequired, Input, SelectInput, Toggle, Tooltip, Validate } from '@xcan-angus/vue-ui';
import SelectEnum from '@/components/enum/SelectEnum.vue';
import { useFileVariable } from './composables/useFileVariable';
import { VariableItem } from '../types';
import { VariableDataProps } from '@/views/data/variable/detail/types';

const { t } = useI18n();

const props = withDefaults(defineProps<VariableDataProps>(), {
  projectId: undefined,
  userInfo: undefined,
  dataSource: undefined
});

/**
 * Component emit definition
 */
// eslint-disable-next-line func-call-spacing
const emit = defineEmits<{
  /** Emit when form is submitted successfully */
  (e: 'ok', data: VariableItem, isEdit: boolean): void;

  /** Emit when delete action is requested */
  (e: 'delete', value: string): void;

  /** Emit when export action is requested */
  (e: 'export', value: string): void;

  /** Emit when clone action is requested */
  (e: 'clone', value: string): void;

  /** Emit when copy link action is requested */
  (e: 'copyLink', value: string): void;

  /** Emit when refresh action is requested */
  (e: 'refresh', value: string): void;
}>();

// Async components for better code splitting and performance
const ButtonGroup = defineAsyncComponent(() => import('@/views/data/variable/detail/ButtonGroup.vue'));
const PreviewData = defineAsyncComponent(() => import('@/views/data/variable/detail/PreviewData.vue'));
const VariableUsageList = defineAsyncComponent(() => import('@/views/data/variable/detail/UsageList.vue'));
const MatchItemPopover = defineAsyncComponent(() => import('@/views/data/variable/detail/MatchItemPopover.vue'));

// Use the file variable composable for form logic
const {
  // State
  activeKey,
  variableName,
  variableNameError,
  description,
  previewData,

  // File configuration fields
  filePath,
  fileType,
  encoding,
  rowIndex,
  columnIndex,
  separatorChar,
  escapeChar,
  quoteChar,

  // Extraction configuration fields
  method,
  defaultValue,
  expression,
  matchItem,

  // Options and props
  encodingOptions,
  inputProps,

  // Methods
  nameChange,
  nameBlur,
  buttonGroupClick,
  okButtonDisabled,
  variableId,
  editFlag
} = useFileVariable(props, emit);
</script>

<template>
  <!-- Button group for variable actions -->
  <ButtonGroup
    :editFlag="editFlag"
    :okButtonDisabled="okButtonDisabled"
    class="mb-5"
    @click="buttonGroupClick" />

  <!-- Variable name input -->
  <div class="flex items-start mb-3.5">
    <div class="flex justify-end items-center flex-shrink-0 mr-2.5  w-18 font-semibold leading-7">
      <IconRequired />
      <span>{{ t('dataVariable.detail.fileVariable.name') }}</span>
    </div>
    <Validate
      class="flex-1"
      :text="t('dataVariable.detail.fileVariable.nameSupport')"
      mode="error"
      :error="variableNameError">
      <Input
        v-model:value="variableName"
        :maxlength="100"
        :error="variableNameError"
        dataType="mixin-en"
        excludes="{}"
        includes="\!\$%\^&\*_\-+=\.\/"
        :placeholder="t('dataVariable.detail.fileVariable.namePlaceholder')"
        trimAll
        @change="nameChange"
        @blur="nameBlur" />
    </Validate>
  </div>

  <!-- Variable description textarea -->
  <div class="flex items-start">
    <div class="mr-2.5 w-18 font-semibold flex justify-end items-center flex-shrink-0 transform-gpu translate-y-1">
      <IconRequired class="invisible" />
      <span>{{ t('common.description') }}</span>
    </div>
    <Input
      v-model:value="description"
      :maxlength="200"
      :autoSize="{ minRows: 3, maxRows: 5 }"
      showCount
      type="textarea"
      class="flex-1"
      :placeholder="t('dataVariable.detail.fileVariable.descriptionPlaceholder')"
      trim />
  </div>

  <!-- Tabbed interface for configuration, preview, and usage -->
  <Tabs
    v-model:activeKey="activeKey"
    size="small"
    class="ant-tabs-nav-mb-2.5">
    <!-- Configuration tab -->
    <TabPane key="value">
      <template #tab>
        <div class="flex items-center font-normal">
          <IconRequired />
          <span>{{ t('dataVariable.detail.fileVariable.extract') }}</span>
        </div>
      </template>

      <div>
        <!-- Configuration hints -->
        <Hints class="mb-2.5" :text="t('dataVariable.detail.fileVariable.extractHint')" />

        <!-- File reading configuration -->
        <Toggle :title="t('dataVariable.detail.fileVariable.readConfig')" class="text-3 leading-5 mb-3.5">
          <!-- File path input -->
          <div class="flex items-center mb-3.5">
            <div class="w-28 flex-shrink-0 text-3 font-semibold flex justify-end items-center mr-2.5">
              <IconRequired />
              <span>{{ t('dataVariable.detail.fileVariable.filePath') }}</span>
            </div>
            <Input
              v-model:value="filePath"
              :maxlength="800"
              style="width:calc(100% - 82px);"
              :placeholder="t('dataVariable.detail.fileVariable.filePathPlaceholder')"
              trimAll />
          </div>

          <!-- File type and encoding selection -->
          <div class="flex items-center space-x-5 mb-3.5">
            <div class="w-1/2 flex items-center">
              <div class="w-28 flex-shrink-0 text-3 font-semibold flex justify-end items-center mr-2.5">
                <IconRequired />
                <span>{{ t('dataVariable.detail.fileVariable.fileType') }}</span>
              </div>
              <SelectEnum
                v-model:value="fileType"
                enumKey="ExtractionFileType"
                class="w-full-20.5 " />
            </div>

            <div class="w-1/2 flex items-center">
              <div class="w-28 flex-shrink-0 text-3 font-semibold flex justify-end items-center mr-2.5">
                <IconRequired />
                <span>{{ t('dataVariable.detail.fileVariable.fileEncoding') }}</span>
              </div>
              <SelectInput
                v-model:value="encoding"
                :options="encodingOptions"
                :inputProps="inputProps"
                class="w-full-20.5 " />
            </div>
          </div>

          <!-- Row and column index inputs -->
          <div class="flex items-center space-x-5 mb-3.5">
            <div class="w-1/2 flex items-center">
              <div class="w-28 flex-shrink-0 text-3 font-semibold flex justify-end items-center mr-2.5">
                <IconRequired />
                <span>{{ t('dataVariable.detail.fileVariable.readStartRow') }}</span>
              </div>
              <Input
                v-model:value="rowIndex"
                :maxlength="4"
                dataType="number"
                :placeholder="t('dataVariable.detail.fileVariable.readStartRowPlaceholder')"
                trimAll />
              <Tooltip :title="t('dataVariable.detail.fileVariable.readStartRowTooltip')" class="text-tips ml-1 text-3.5 cursor-pointer">
                <Icon icon="icon-tishi1" class="text-tips ml-1 text-3.5 cursor-pointer" />
              </Tooltip>
            </div>

            <div class="w-1/2 flex items-center">
              <div class="w-28 flex-shrink-0 text-3 font-semibold flex justify-end items-center mr-2.5">
                <IconRequired />
                <span>{{ t('dataVariable.detail.fileVariable.readStartColumn') }}</span>
              </div>
              <Input
                v-model:value="columnIndex"
                :maxlength="4"
                dataType="number"
                :placeholder="t('dataVariable.detail.fileVariable.readStartColumnPlaceholder')"
                trimAll />
              <Tooltip :title="t('dataVariable.detail.fileVariable.readStartColumnTooltip')" class="text-tips ml-1 text-3.5 cursor-pointer">
                <Icon icon="icon-tishi1" class="text-tips ml-1 text-3.5 cursor-pointer" />
              </Tooltip>
            </div>
          </div>

          <!-- Separator, escape, and quote character inputs -->
          <div class="flex items-center space-x-5 mb-3.5">
            <div class="w-1/2 flex items-center">
              <div class="w-28 flex-shrink-0 text-3 font-semibold flex justify-end items-center mr-2.5">
                <IconRequired />
                <span>{{ t('dataVariable.detail.fileVariable.separator') }}</span>
              </div>
              <Input
                v-model:value="separatorChar"
                :maxlength="1"
                trimAll />
              <Tooltip :title="t('dataVariable.detail.fileVariable.separatorTooltip')" class="text-tips ml-1 text-3.5 cursor-pointer">
                <Icon icon="icon-tishi1" class="text-tips ml-1 text-3.5 cursor-pointer" />
              </Tooltip>
            </div>

            <div class="w-1/2 flex items-center">
              <div class="w-28 flex-shrink-0 text-3 font-semibold flex justify-end items-center mr-2.5">
                <IconRequired />
                <span>{{ t('dataVariable.detail.fileVariable.escapeChar') }}</span>
              </div>
              <Input
                v-model:value="escapeChar"
                :maxlength="1"
                trimAll />
              <Tooltip :title="t('dataVariable.detail.fileVariable.escapeCharTooltip')" class="text-tips ml-1 text-3.5 cursor-pointer">
                <Icon icon="icon-tishi1" class="text-tips ml-1 text-3.5 cursor-pointer" />
              </Tooltip>
            </div>
          </div>

          <div class="flex items-center space-x-5 mb-3.5">
            <div class="w-1/2 flex items-center">
              <div class="w-28 flex-shrink-0 text-3 font-semibold flex justify-end items-center mr-2.5">
                <IconRequired />
                <span>{{ t('dataVariable.detail.fileVariable.quoteChar') }}</span>
              </div>
              <Input
                v-model:value="quoteChar"
                :maxlength="1"
                trimAll />
              <Tooltip :title="t('dataVariable.detail.fileVariable.quoteCharTooltip')" class="text-tips ml-1 text-3.5 cursor-pointer">
                <Icon icon="icon-tishi1" class="text-tips ml-1 text-3.5 cursor-pointer" />
              </Tooltip>
            </div>
          </div>
        </Toggle>

        <!-- Extraction configuration -->
        <Toggle :title="t('dataVariable.detail.fileVariable.extractConfig')" class="text-3 leading-5">
          <!-- Exact value extraction method -->
          <template v-if="method === 'EXACT_VALUE'">
            <div class="flex items-center space-x-5 mb-3.5">
              <div class="w-1/2 flex items-center">
                <div class="w-28 flex-shrink-0 text-3 font-semibold flex justify-end items-center mr-2.5">
                  <IconRequired />
                  <span>{{ t('dataVariable.detail.fileVariable.extractMethod') }}</span>
                </div>
                <SelectEnum
                  v-model:value="method"
                  enumKey="ExtractionMethod"
                  :placeholder="t('dataVariable.detail.fileVariable.extractMethodPlaceholder')"
                  class="w-full-20.5 " />
              </div>

              <div class="w-1/2 flex items-center">
                <div class="w-28 flex-shrink-0 text-3 font-semibold flex justify-end items-center mr-2.5">
                  <IconRequired class="invisible" />
                  <span>{{ t('common.defaultValue') }}</span>
                </div>
                <Input
                  v-model:value="defaultValue"
                  :placeholder="t('dataVariable.detail.fileVariable.defaultValuePlaceholder')"
                  class="w-full-20.5 "
                  trim
                  :maxlength="4096" />
              </div>
            </div>
          </template>

          <!-- Pattern-based extraction methods (JSON_PATH, REGEX, X_PATH) -->
          <template v-else>
            <div class="flex items-center space-x-5 mb-3.5">
              <div class="w-1/2 flex items-center">
                <div class="w-28 flex-shrink-0 text-3 font-semibold flex justify-end items-center mr-2.5">
                  <IconRequired />
                  <span>{{ t('dataVariable.detail.fileVariable.extractMethod') }}</span>
                </div>
                <SelectEnum
                  v-model:value="method"
                  enumKey="ExtractionMethod"
                  :placeholder="t('dataVariable.detail.fileVariable.extractMethodPlaceholder')"
                  class="w-full-20.5 " />
              </div>

              <div class="w-1/2 flex items-center">
                <div class="w-28 flex-shrink-0 text-3 font-semibold flex justify-end items-center mr-2.5">
                  <IconRequired />
                  <span>{{ t('dataVariable.detail.fileVariable.expression') }}</span>
                </div>
                <Input
                  v-model:value="expression"
                  :placeholder="t('dataVariable.detail.fileVariable.expressionPlaceholder')"
                  class="w-full-20.5 "
                  trimAll />
              </div>
            </div>

            <div class="flex items-center space-x-5 mb-3.5">
              <div class="w-1/2 flex items-center">
                <div class="w-28 flex-shrink-0 text-3 font-semibold flex justify-end items-center mr-2.5">
                  <IconRequired class="invisible" />
                  <span>{{ t('dataVariable.detail.fileVariable.matchItem') }}</span>
                </div>
                <Input
                  v-model:value="matchItem"
                  :placeholder="t('dataVariable.detail.fileVariable.matchItemPlaceholder')"
                  class="w-full-20.5 "
                  dataType="number"
                  trimAll
                  :max="2000"
                  :maxlength="4" />
                <MatchItemPopover />
              </div>

              <div class="w-1/2 flex items-center">
                <div class="w-28 flex-shrink-0 text-3 font-semibold flex justify-end items-center mr-2.5">
                  <IconRequired class="invisible" />
                  <span>{{ t('common.defaultValue') }}</span>
                </div>
                <Input
                  v-model:value="defaultValue"
                  :placeholder="t('dataVariable.detail.fileVariable.defaultValuePlaceholder')"
                  class="w-full-20.5 "
                  trim
                  :maxlength="4096" />
              </div>
            </div>
          </template>
        </Toggle>
      </div>
    </TabPane>

    <!-- Preview tab -->
    <TabPane key="preview">
      <template #tab>
        <div class="flex items-center font-normal">
          <span>{{ t('dataVariable.detail.fileVariable.preview') }}</span>
        </div>
      </template>

      <PreviewData :dataSource="previewData" />
    </TabPane>

    <!-- Usage tab - only visible for existing variables -->
    <TabPane v-if="variableId" key="use">
      <template #tab>
        <div class="flex items-center font-normal">
          <span>{{ t('dataVariable.detail.fileVariable.use') }}</span>
        </div>
      </template>

      <VariableUsageList :id="variableId" />
    </TabPane>
  </Tabs>
</template>

<style scoped>
:deep(.toggle-title) {
  color: var(--content-text-content);
  font-size: 12px;
  font-weight: normal;
}

:deep(.ant-collapse-ghost)>.ant-collapse-item>.ant-collapse-content {
  margin-top: 14px;
}

:deep(.ant-collapse) .ant-collapse-content>.ant-collapse-content-box {
  padding-left: 22px;
}

.w-1\/2 {
  width: calc((100% - 20px)/2);
}

.w-full-20\.5 {
  width: calc(100% - 82px);
}
</style>
