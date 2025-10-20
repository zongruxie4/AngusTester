<script lang="ts" setup>
import { defineAsyncComponent } from 'vue';
import { useI18n } from 'vue-i18n';
import { Button, TabPane, Tabs } from 'ant-design-vue';
import { Hints, Icon, IconRequired, Input, Toggle, Tooltip, Validate } from '@xcan-angus/vue-ui';
import { ExtractionMethod } from '@xcan-angus/infra';
import { VariableDetail } from '../types';
import { BasicDataSourceProps } from '@/types/types';

import { useJdbcVariable } from './composables/useJdbcVariable';

import SelectEnum from '@/components/form/enum/SelectEnum.vue';

const { t } = useI18n();

const props = withDefaults(defineProps<BasicDataSourceProps<VariableDetail>>(), {
  projectId: undefined,
  userInfo: undefined,
  dataSource: undefined
});

/**
 * Component emit definition
 */

const emit = defineEmits<{
  /** Emit when form is submitted successfully */
  (e: 'ok', data: VariableDetail, isEdit: boolean): void;

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

  /** Emit when cancel action is requested */
  (e: 'cancel'): void;
}>();

// Async components for better code splitting and performance
const ButtonGroup = defineAsyncComponent(() => import('@/views/data/variable/detail/ButtonGroup.vue'));
const PreviewData = defineAsyncComponent(() => import('@/views/data/variable/detail/PreviewData.vue'));
const VariableUsageList = defineAsyncComponent(() => import('@/views/data/variable/detail/UsageList.vue'));
const MatchItemPopover = defineAsyncComponent(() => import('@/views/data/variable/detail/MatchItemPopover.vue'));
const SelectDataSourceModal = defineAsyncComponent(() => import('@/views/data/variable/detail/jdbc/SelectDatasource.vue'));

// Use the JDBC variable composable for form logic
const {
  // State
  activeKey,
  variableName,
  variableNameError,
  description,
  previewData,
  modalVisible,

  // Database configuration fields
  dbType,
  jdbcUrl,
  username,
  password,
  selectSqlString,
  rowIndex,
  columnIndex,

  // Extraction configuration fields
  method,
  defaultValue,
  expression,
  matchItem,

  // Methods
  nameChange,
  nameBlur,
  buttonGroupClick,
  toSelectDataSource,
  selectedDataSourceOk,
  okButtonDisabled,
  variableId,
  editFlag
} = useJdbcVariable(props, emit);
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
    <div class="flex justify-end items-center flex-shrink-0 mr-2.5 w-18 font-semibold leading-7">
      <IconRequired />
      <span>{{ t('common.name') }}</span>
    </div>
    <Validate
      class="flex-1"
      :text="t('commonData.common.nameSupportPlaceholder')"
      mode="error"
      :error="variableNameError">
      <Input
        v-model:value="variableName"
        :maxlength="100"
        :error="variableNameError"
        dataType="mixin-en"
        excludes="{}"
        includes="\!\$%\^&\*_\-+=\.\/"
        :placeholder="t('common.placeholders.searchKeyword')"
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
      :placeholder="t('common.placeholders.inputDescription')"
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
          <span>{{ t('actions.extract') }}</span>
        </div>
      </template>

      <div>
        <!-- Configuration hints -->
        <Hints class="mb-2.5" :text="t('variable.detail.jdbcVariable.hints')" />

        <!-- Database connection configuration -->
        <Toggle :title="t('dataCommon.common.readConfig')" class="text-3 leading-5 mb-3.5">
          <!-- Data source selection button -->
          <div class="flex items-center justify-start mb-3.5">
            <div class="w-28 flex-shrink-0 text-3 font-semibold flex justify-end items-center mr-2.5">
            </div>
            <Button
              type="link"
              size="small"
              class="flex items-center p-0 border-none h-3.5 leading-3.5 space-x-1"
              @click="toSelectDataSource">
              <Icon icon="icon-xuanze" class="text-3.5" />
              <span>{{ t('variable.detail.jdbcVariable.selectDataSource') }}</span>
            </Button>
          </div>

          <!-- Database type and JDBC URL -->
          <div class="flex items-center space-x-5 mb-3.5">
            <div class="w-1/2 flex items-center">
              <div class="w-28 flex-shrink-0 text-3 font-semibold flex justify-end items-center mr-2.5">
                <IconRequired />
                <span>{{ t('variable.detail.jdbcVariable.databaseType') }}</span>
              </div>
              <SelectEnum
                v-model:value="dbType"
                enumKey="DatabaseType"
                :placeholder="t('variable.detail.jdbcVariable.databaseTypePlaceholder')"
                class="w-full-24" />
            </div>

            <div class="w-1/2 flex items-center mb-3.5">
              <div class="w-25 flex-shrink-0 text-3 font-semibold flex justify-end items-center mr-2.5">
                <IconRequired />
                <span>{{ t('variable.detail.jdbcVariable.jdbcUrl') }}</span>
              </div>
              <Input
                v-model:value="jdbcUrl"
                :maxlength="2048"
                class="w-full-24"
                :placeholder="t('variable.detail.jdbcVariable.jdbcUrlPlaceholder')"
                trimAll />
            </div>
          </div>

          <!-- Username and password -->
          <div class="flex items-center space-x-5 mb-3.5">
            <div class="w-1/2 flex items-center">
              <div class="w-28 flex-shrink-0 text-3 font-semibold flex justify-end items-center mr-2.5">
                <IconRequired />
                <span>{{ t('common.username') }}</span>
              </div>
              <Input
                v-model:value="username"
                class="w-full-24"
                :maxlength="200"
                :placeholder="t('variable.detail.jdbcVariable.usernamePlaceholder')"
                trimAll />
            </div>

            <div class="w-1/2 flex items-center">
              <div class="w-25 flex-shrink-0 text-3 font-semibold flex justify-end items-center mr-2.5">
                <IconRequired />
                <span>{{ t('common.password') }}</span>
              </div>
              <Input
                v-model:value="password"
                :maxlength="800"
                class="w-full-24"
                :placeholder="t('variable.detail.jdbcVariable.passwordPlaceholder')"
                trimAll />
            </div>
          </div>

          <!-- SQL select statement -->
          <div class="flex items-start mb-3.5">
            <div class="w-28 flex-shrink-0 transform-gpu translate-y-1 text-3 font-semibold flex justify-end items-center mr-2.5">
              <IconRequired />
              <span>{{ t('variable.detail.jdbcVariable.selectStatement') }}</span>
            </div>
            <Input
              v-model:value="selectSqlString"
              :placeholder="t('variable.detail.jdbcVariable.selectStatementPlaceholder')"
              class="w-full-24"
              type="textarea"
              trim
              :autoSize="{ minRows: 5, maxRows: 8 }"
              :maxlength="1024" />
          </div>

          <!-- Row and column index inputs -->
          <div class="flex items-center space-x-5 mb-3.5">
            <div class="w-1/2 flex items-center">
              <div class="w-28 flex-shrink-0 text-3 font-semibold flex justify-end items-center mr-2.5">
                <IconRequired />
                <span>{{ t('dataCommon.common.readStartRow') }}</span>
              </div>
              <Input
                v-model:value="rowIndex"
                :maxlength="4"
                dataType="number"
                :placeholder="t('dataCommon.common.readStartRowPlaceholder')"
                trimAll />
              <Tooltip :title="t('variable.detail.jdbcVariable.readStartRowTooltip')">
                <Icon icon="icon-tishi1" class="text-tips ml-1 text-3.5 cursor-pointer" />
              </Tooltip>
            </div>

            <div class="w-1/2 flex items-center">
              <div class="w-25 flex-shrink-0 text-3 font-semibold flex justify-end items-center mr-2.5">
                <IconRequired />
                <span>{{ t('dataCommon.common.readStartColumn') }}</span>
              </div>
              <Input
                v-model:value="columnIndex"
                :maxlength="4"
                dataType="number"
                :placeholder="t('dataCommon.common.readStartColumnPlaceholder')"
                trimAll />
              <Tooltip :title="t('dataCommon.common.readStartColumnTooltip')">
                <Icon icon="icon-tishi1" class="text-tips ml-1 text-3.5 cursor-pointer" />
              </Tooltip>
            </div>
          </div>
        </Toggle>

        <!-- Extraction configuration -->
        <Toggle :title="t('dataCommon.common.extractConfig')" class="text-3 leading-5">
          <!-- Exact value extraction method -->
          <template v-if="method === ExtractionMethod.EXACT_VALUE">
            <div class="flex items-center space-x-5 mb-3.5">
              <div class="w-1/2 flex items-center">
                <div class="w-28 flex-shrink-0 text-3 font-semibold flex justify-end items-center mr-2.5">
                  <IconRequired />
                  <span>{{ t('dataCommon.common.extractMethod') }}</span>
                </div>
                <SelectEnum
                  v-model:value="method"
                  enumKey="ExtractionMethod"
                  :placeholder="t('dataCommon.common.extractMethodPlaceholder')"
                  class="w-full-24 " />
              </div>

              <div class="w-1/2 flex items-center">
                <div class="w-25 flex-shrink-0 text-3 font-semibold flex justify-end items-center mr-2.5">
                  <IconRequired class="invisible" />
                  <span>{{ t('common.defaultValue') }}</span>
                </div>
                <Input
                  v-model:value="defaultValue"
                  :placeholder="t('dataCommon.common.defaultValuePlaceholder')"
                  class="w-full-20.5"
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
                  <span>{{ t('dataCommon.common.extractMethod') }}</span>
                </div>
                <SelectEnum
                  v-model:value="method"
                  enumKey="ExtractionMethod"
                  :placeholder="t('dataCommon.common.extractMethodPlaceholder')"
                  class="w-full-24" />
              </div>

              <div class="w-1/2 flex items-center">
                <div class="w-28 flex-shrink-0 text-3 font-semibold flex justify-end items-center mr-2.5">
                  <IconRequired />
                  <span>{{ t('dataCommon.common.expression') }}</span>
                </div>
                <Input
                  v-model:value="expression"
                  :placeholder="t('dataCommon.common.expressionPlaceholder')"
                  class="w-full-20.5 "
                  trimAll />
              </div>
            </div>

            <div class="flex items-center space-x-5 mb-3.5">
              <div class="w-1/2 flex items-center">
                <div class="w-28 flex-shrink-0 text-3 font-semibold flex justify-end items-center mr-2.5">
                  <IconRequired class="invisible" />
                  <span>{{ t('dataCommon.common.matchItem') }}</span>
                </div>
                <Input
                  v-model:value="matchItem"
                  :placeholder="t('dataCommon.common.matchItemPlaceholder')"
                  class="w-full-24"
                  dataType="number"
                  trimAll
                  :max="2000"
                  :maxlength="4" />
                <MatchItemPopover />
              </div>

              <div class="w-1/2 flex items-center">
                <div class="w-25 flex-shrink-0 text-3 font-semibold flex justify-end items-center mr-2.5">
                  <IconRequired class="invisible" />
                  <span>{{ t('common.defaultValue') }}</span>
                </div>
                <Input
                  v-model:value="defaultValue"
                  :placeholder="t('dataCommon.common.defaultValuePlaceholder')"
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
          <span>{{ t('actions.preview') }}</span>
        </div>
      </template>

      <PreviewData :dataSource="previewData" />
    </TabPane>

    <!-- Usage tab - only visible for existing variables -->
    <TabPane v-if="variableId" key="use">
      <template #tab>
        <div class="flex items-center font-normal">
          <span>{{ t('actions.use') }}</span>
        </div>
      </template>

      <VariableUsageList :id="variableId" />
    </TabPane>
  </Tabs>

  <!-- Data source selection modal -->
  <SelectDataSourceModal
    v-model:visible="modalVisible"
    :projectId="props.projectId"
    :userInfo="props.userInfo"
    @ok="selectedDataSourceOk" />
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

.w-full-24 {
  width: calc(100% - 96px);
}

.w-full-20\.5 {
  width: calc(100% - 82px);
}
</style>
