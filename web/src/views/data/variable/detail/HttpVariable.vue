<script lang="ts" setup>
import { defineAsyncComponent, ref } from 'vue';
import { useI18n } from 'vue-i18n';
import { Button, TabPane, Tabs } from 'ant-design-vue';
import { AsyncComponent, Hints, Icon, IconRequired, Input, Toggle, Validate, SelectApisByServiceModal } from '@xcan-angus/vue-ui';
import SelectEnum from '@/components/selectEnum/index.vue';
import { useHttpVariable } from './composables/useHttpVariable';
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
const HTTPConfigs = defineAsyncComponent(() => import('@/views/data/variable/detail/http/config/index.vue'));

// Use the HTTP variable composable for form logic
const {
  // State
  activeKey,
  variableName,
  variableNameError,
  description,
  previewData,

  // HTTP configuration fields
  method,
  location,
  parameterName,
  defaultValue,
  expression,
  matchItem,
  selectApiVisible,
  requestConfigs,

  // Methods
  nameChange,
  nameBlur,
  buttonGroupClick,
  toSelectApi,
  selectApiOk,
  okButtonDisabled,
  variableId,
  editFlag
} = useHttpVariable(props, emit);

// Reference to HTTPConfigs component for validation
const httpConfigsRef = ref();
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
    <div class="flex items-center flex-shrink-0 mr-2.5 leading-7">
      <IconRequired />
      <span>{{ t('dataVariable.detail.httpVariable.name') }}</span>
    </div>
    <Validate
      class="flex-1"
      :text="t('dataVariable.detail.httpVariable.nameSupport')"
      mode="error"
      :error="variableNameError">
      <Input
        v-model:value="variableName"
        :maxlength="100"
        :error="variableNameError"
        dataType="mixin-en"
        excludes="{}"
        includes="\!\$%\^&\*_\-+=\.\/"
        :placeholder="t('dataVariable.detail.httpVariable.namePlaceholder')"
        trimAll
        @change="nameChange"
        @blur="nameBlur" />
    </Validate>
  </div>

  <!-- Variable description textarea -->
  <div class="flex items-start">
    <div class="mr-2.5 flex items-center flex-shrink-0 transform-gpu translate-y-1">
      <IconRequired class="invisible" />
      <span>{{ t('dataVariable.detail.httpVariable.description') }}</span>
    </div>
    <Input
      v-model:value="description"
      :maxlength="200"
      :autoSize="{ minRows: 3, maxRows: 5 }"
      showCount
      type="textarea"
      class="flex-1"
      :placeholder="t('dataVariable.detail.httpVariable.descriptionPlaceholder')"
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
          <span>{{ t('dataVariable.detail.httpVariable.extract') }}</span>
        </div>
      </template>

      <div>
        <!-- Configuration hints -->
        <Hints class="mb-2.5" :text="t('dataVariable.detail.httpVariable.hints')" />

        <!-- HTTP request configuration -->
        <Toggle :title="t('dataVariable.detail.httpVariable.readConfig')" class="leading-5 mb-3.5">
          <!-- API selection button -->
          <div class="flex items-center justify-start mb-3.5">
            <Button
              type="link"
              size="small"
              class="flex items-center p-0 border-none h-3.5 leading-3.5 space-x-1"
              @click="toSelectApi">
              <Icon icon="icon-xuanze" class="text-3.5" />
              <span>{{ t('dataVariable.detail.httpVariable.selectApi') }}</span>
            </Button>
          </div>

          <!-- HTTP interface configuration -->
          <div class="flex items-start mb-3.5">
            <div class="w-26 flex-shrink-0 transform-gpu translate-y-1 text-3">
              <IconRequired />
              <span>{{ t('dataVariable.detail.httpVariable.interfaceConfig') }}</span>
            </div>
            <HTTPConfigs
              ref="httpConfigsRef"
              :value="requestConfigs"
              class="mb-1.5" />
          </div>
        </Toggle>

        <!-- Extraction configuration -->
        <Toggle :title="t('dataVariable.detail.httpVariable.extractConfig')" class="text-3 leading-5">
          <!-- Exact value extraction method -->
          <template v-if="method === 'EXACT_VALUE'">
            <!-- Configuration for request/response body -->
            <template v-if="['REQUEST_RAW_BODY', 'RESPONSE_BODY'].includes(location)">
              <div class="flex items-center space-x-5 mb-3.5">
                <div class="w-1/2 flex items-center">
                  <div class="w-26 flex-shrink-0 text-3">
                    <IconRequired />
                    <span>{{ t('dataVariable.detail.httpVariable.extractMethod') }}</span>
                  </div>
                  <SelectEnum
                    v-model:value="method"
                    enumKey="ExtractionMethod"
                    :placeholder="t('dataVariable.detail.httpVariable.extractMethodPlaceholder')"
                    class="w-full-16" />
                </div>

                <div class="w-1/2 flex items-center">
                  <div class="w-26 flex-shrink-0 text-3">
                    <IconRequired />
                    <span>{{ t('dataVariable.detail.httpVariable.extractPosition') }}</span>
                  </div>
                  <SelectEnum
                    v-model:value="location"
                    enumKey="HttpExtractionLocation"
                    class="w-full-16" />
                </div>
              </div>

              <div class="flex items-center space-x-5 mb-3.5">
                <div class="w-1/2 flex items-center">
                  <div class="w-26 flex-shrink-0 text-3">
                    <IconRequired class="invisible" />
                    <span>{{ t('dataVariable.detail.httpVariable.defaultValue') }}</span>
                  </div>
                  <Input
                    v-model:value="defaultValue"
                    :placeholder="t('dataVariable.detail.httpVariable.defaultValuePlaceholder')"
                    class="w-full-16"
                    trim
                    :maxlength="4096" />
                </div>
              </div>
            </template>

            <!-- Configuration for other locations (headers, parameters, etc.) -->
            <template v-else>
              <div class="flex items-center space-x-5 mb-3.5">
                <div class="w-1/2 flex items-center">
                  <div class="w-26 flex-shrink-0 text-3">
                    <IconRequired />
                    <span>{{ t('dataVariable.detail.httpVariable.extractMethod') }}</span>
                  </div>
                  <SelectEnum
                    v-model:value="method"
                    enumKey="ExtractionMethod"
                    :placeholder="t('dataVariable.detail.httpVariable.extractMethodPlaceholder')"
                    class="w-full-16" />
                </div>

                <div class="w-1/2 flex items-center">
                  <div class="w-26 flex-shrink-0 text-3">
                    <IconRequired />
                    <span>{{ t('dataVariable.detail.httpVariable.extractPosition') }}</span>
                  </div>
                  <SelectEnum
                    v-model:value="location"
                    enumKey="HttpExtractionLocation"
                    class="w-full-16" />
                </div>
              </div>

              <div class="flex items-center space-x-5 mb-3.5">
                <div class="w-1/2 flex items-center">
                  <div class="w-26 flex-shrink-0 text-3">
                    <IconRequired />
                    <span>{{ t('dataVariable.detail.httpVariable.expression') }}</span>
                  </div>
                  <Input
                    v-model:value="parameterName"
                    :placeholder="t('dataVariable.detail.httpVariable.parameterNamePlaceholder')"
                    class="w-full-16"
                    trimAll
                    :maxlength="400" />
                </div>

                <div class="w-1/2 flex items-center">
                  <div class="w-26 flex-shrink-0 text-3">
                    <IconRequired class="invisible" />
                    <span>{{ t('dataVariable.detail.httpVariable.defaultValue') }}</span>
                  </div>
                  <Input
                    v-model:value="defaultValue"
                    :placeholder="t('dataVariable.detail.httpVariable.defaultValuePlaceholder')"
                    class="w-full-16"
                    trim
                    :maxlength="4096" />
                </div>
              </div>
            </template>
          </template>

          <!-- Pattern-based extraction methods (JSON_PATH, REGEX, X_PATH) -->
          <template v-else>
            <!-- Configuration for request/response body -->
            <template v-if="['REQUEST_RAW_BODY', 'RESPONSE_BODY'].includes(location)">
              <div class="flex items-center space-x-5 mb-3.5">
                <div class="w-1/2 flex items-center">
                  <div class="w-26 flex-shrink-0 text-3">
                    <IconRequired />
                    <span>{{ t('dataVariable.detail.httpVariable.extractMethod') }}</span>
                  </div>
                  <SelectEnum
                    v-model:value="method"
                    enumKey="ExtractionMethod"
                    :placeholder="t('dataVariable.detail.httpVariable.extractMethodPlaceholder')"
                    class="w-full-20.5" />
                </div>

                <div class="w-1/2 flex items-center">
                  <div class="w-26 flex-shrink-0 text-3">
                    <IconRequired />
                    <span>{{ t('dataVariable.detail.httpVariable.extractPosition') }}</span>
                  </div>
                  <SelectEnum
                    v-model:value="location"
                    enumKey="HttpExtractionLocation"
                    class="w-full-20.5" />
                </div>
              </div>

              <div class="flex items-center space-x-5 mb-3.5">
                <div class="w-1/2 flex items-center">
                  <div class="w-26 flex-shrink-0 text-3">
                    <IconRequired />
                    <span>{{ t('dataVariable.detail.httpVariable.expression') }}</span>
                  </div>
                  <Input
                    v-model:value="expression"
                    :placeholder="t('dataVariable.detail.httpVariable.expressionPlaceholder')"
                    class="w-full-16"
                    trim
                    :maxlength="1024" />
                </div>

                <div class="w-1/2 flex items-center">
                  <div class="w-26 flex-shrink-0 text-3">
                    <IconRequired class="invisible" />
                    <span>{{ t('dataVariable.detail.httpVariable.matchItem') }}</span>
                  </div>
                  <Input
                    v-model:value="matchItem"
                    :placeholder="t('dataVariable.detail.httpVariable.matchItemPlaceholder')"
                    class="w-full-20.5"
                    dataType="number"
                    trimAll
                    :max="2000"
                    :maxlength="4" />
                  <MatchItemPopover />
                </div>
              </div>

              <div class="flex items-center space-x-5 mb-3.5">
                <div class="w-1/2 flex items-center">
                  <div class="w-26 flex-shrink-0 text-3">
                    <IconRequired class="invisible" />
                    <span>{{ t('dataVariable.detail.httpVariable.defaultValue') }}</span>
                  </div>
                  <Input
                    v-model:value="defaultValue"
                    :placeholder="t('dataVariable.detail.httpVariable.defaultValuePlaceholder')"
                    class="w-full-16"
                    trim
                    :maxlength="4096" />
                </div>
              </div>
            </template>

            <!-- Configuration for other locations (headers, parameters, etc.) -->
            <template v-else>
              <div class="flex items-center space-x-5 mb-3.5">
                <div class="w-1/2 flex items-center">
                  <div class="w-26 flex-shrink-0">
                    <IconRequired />
                    <span>{{ t('dataVariable.detail.httpVariable.extractMethod') }}</span>
                  </div>
                  <SelectEnum
                    v-model:value="method"
                    enumKey="ExtractionMethod"
                    :placeholder="t('dataVariable.detail.httpVariable.extractMethodPlaceholder')"
                    class="w-full-20.5" />
                </div>

                <div class="w-1/2 flex items-center">
                  <div class="w-26 flex-shrink-0">
                    <IconRequired />
                    <span>{{ t('dataVariable.detail.httpVariable.extractPosition') }}</span>
                  </div>
                  <SelectEnum
                    v-model:value="location"
                    enumKey="HttpExtractionLocation"
                    class="w-full-16" />
                </div>
              </div>

              <div class="flex items-center space-x-5 mb-3.5">
                <div class="w-1/2 flex items-center">
                  <div class="w-26 flex-shrink-0">
                    <IconRequired :class="{ invisible: ['REQUEST_RAW_BODY', 'RESPONSE_BODY'].includes(location) }" />
                    <span>{{ t('dataVariable.detail.httpVariable.parameterName') }}</span>
                  </div>
                  <Input
                    v-model:value="parameterName"
                    :placeholder="t('dataVariable.detail.httpVariable.parameterNamePlaceholder')"
                    class="w-full-20.5"
                    trimAll
                    :maxlength="400" />
                </div>

                <div class="w-1/2 flex items-center">
                  <div class="w-26 flex-shrink-0">
                    <IconRequired />
                    <span>{{ t('dataVariable.detail.httpVariable.expression') }}</span>
                  </div>
                  <Input
                    v-model:value="expression"
                    :placeholder="t('dataVariable.detail.httpVariable.expressionPlaceholder ')"
                    class="w-full-16"
                    trim
                    :maxlength="1024" />
                </div>
              </div>

              <div class="flex items-center space-x-5 mb-3.5">
                <div class="w-1/2 flex items-center">
                  <div class="w-26 flex-shrink-0">
                    <IconRequired class="invisible" />
                    <span>{{ t('dataVariable.detail.httpVariable.matchItem') }}</span>
                  </div>
                  <Input
                    v-model:value="matchItem"
                    :placeholder="t('dataVariable.detail.httpVariable.matchItemPlaceholder')"
                    class="w-full-20.5"
                    dataType="number"
                    trimAll
                    :max="2000"
                    :maxlength="4" />
                  <MatchItemPopover />
                </div>

                <div class="w-1/2 flex items-center">
                  <div class="w-26 flex-shrink-0">
                    <IconRequired class="invisible" />
                    <span>{{ t('dataVariable.detail.httpVariable.defaultValue') }}</span>
                  </div>
                  <Input
                    v-model:value="defaultValue"
                    :placeholder="t('dataVariable.detail.httpVariable.defaultValuePlaceholder')"
                    class="w-full-16"
                    trim
                    :maxlength="4096" />
                </div>
              </div>
            </template>
          </template>
        </Toggle>
      </div>
    </TabPane>

    <!-- Preview tab -->
    <TabPane key="preview">
      <template #tab>
        <div class="flex items-center font-normal">
          <span>{{ t('dataVariable.detail.httpVariable.preview') }}</span>
        </div>
      </template>

      <PreviewData :dataSource="previewData" />
    </TabPane>

    <!-- Usage tab - only visible for existing variables -->
    <TabPane v-if="variableId" key="use">
      <template #tab>
        <div class="flex items-center font-normal">
          <span>{{ t('dataVariable.detail.httpVariable.use') }}</span>
        </div>
      </template>

      <VariableUsageList :id="variableId" />
    </TabPane>
  </Tabs>

  <!-- API selection modal -->
  <AsyncComponent :visible="selectApiVisible">
    <SelectApisByServiceModal
      v-model:visible="selectApiVisible"
      :selectSingle="true"
      :projectId="props.projectId"
      @ok="selectApiOk" />
  </AsyncComponent>
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
</style>

<style>
.ant-modal.overflow-auto-modal-container .ant-modal-content {
  overflow: hidden;
}

.ant-modal.overflow-auto-modal-container .ant-modal-content .ant-modal-body {
  max-height: calc(95vh - 84px);
  overflow: auto;
}

.ant-tabs.ant-tabs-nav-mb-2\.5>.ant-tabs-nav,
.ant-tabs>.ant-tabs-nav-mb-2\.5>div>.ant-tabs-nav {
  margin-bottom: 12px;
}

.w-1\/2 {
  width: calc((100% - 20px)/2);
}

.w-full-20\.5 {
  width: calc(100% - 82px);
}

.w-full-16 {
  width: calc(100% - 64px);
}
</style>
