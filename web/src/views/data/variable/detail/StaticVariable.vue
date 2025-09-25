<script lang="ts" setup>
import { defineAsyncComponent } from 'vue';
import { useI18n } from 'vue-i18n';
import { Radio, RadioGroup, TabPane, Tabs } from 'ant-design-vue';
import { FunctionsButton, Hints, IconRequired, Input, Validate } from '@xcan-angus/vue-ui';
import { useStaticVariable } from './composables/useStaticVariable';
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
 * Defines the events that this component can emit to its parent
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
const ParamTextarea = defineAsyncComponent(() => import('@/components/ParamTextarea/index.vue'));

// Use the static variable composable for form logic
const {
  // State management
  activeKey,
  variableName,
  variableNameError,
  description,
  previewData,
  passwordValue,
  variableValue,

  // Event handlers
  nameChange,
  nameBlur,
  buttonGroupClick,

  // Computed properties
  okButtonDisabled,
  variableId,
  editFlag
} = useStaticVariable(props, emit);

/**
 * Handle value input blur event
 * Updates the variableValue ref when the textarea loses focus
 *
 * @param targetText - The text value from the input
 */
const handleBlurValue = (targetText: string) => {
  variableValue.value = targetText;
};
</script>

<template>
  <!-- Button group for variable actions -->
  <ButtonGroup
    :editFlag="editFlag"
    :okButtonDisabled="okButtonDisabled"
    class="mb-5"
    @click="buttonGroupClick" />

  <!-- Variable name input field -->
  <!-- Required field with validation -->
  <div class="flex items-start mb-3.5">
    <div class="flex justify-end items-center flex-shrink-0 mr-2.5 w-18 font-semibold leading-7">
      <IconRequired />
      <span>{{ t('dataVariable.detail.staticVariable.name') }}</span>
    </div>
    <Validate
      class="flex-1"
      :text="t('dataVariable.detail.staticVariable.nameSupport')"
      mode="error"
      :error="variableNameError">
      <Input
        v-model:value="variableName"
        :maxlength="100"
        :error="variableNameError"
        dataType="mixin-en"
        excludes="{}"
        includes="\!\$%\^&\*_\-+=\.\/"
        :placeholder="t('dataVariable.detail.staticVariable.namePlaceholder')"
        trimAll
        @change="nameChange"
        @blur="nameBlur" />
    </Validate>
  </div>

  <!-- Password value toggle -->
  <!-- Allows user to mark variable value as sensitive -->
  <div class="flex items-center mb-3.5">
    <div class="flex justify-end items-center flex-shrink-0 mr-2.5 w-18 font-semibold">
      <IconRequired />
      <span>{{ t('dataVariable.detail.staticVariable.password') }}</span>
    </div>
    <RadioGroup v-model:value="passwordValue" name="passwordValue">
      <Radio :value="false">{{ t('status.no') }}</Radio>
      <Radio :value="true">
        <div class="flex items-center">
          <span class="mr-2">{{ t('status.yes') }}</span>
          <Hints :text="t('dataVariable.detail.staticVariable.passwordHint')" />
        </div>
      </Radio>
    </RadioGroup>
  </div>

  <!-- Variable description textarea -->
  <!-- Optional field for variable documentation -->
  <div class="flex items-start">
    <div class="flex justify-end items-center flex-shrink-0 mr-2.5 w-18 font-semibold transform-gpu translate-y-1">
      <IconRequired class="invisible" />
      <span>{{ t('common.description') }}</span>
    </div>
    <Input
      v-model:value="description"
      :maxlength="200"
      :autoSize="{minRows:3,maxRows:5}"
      showCount
      type="textarea"
      class="flex-1"
      :placeholder="t('dataVariable.detail.staticVariable.descriptionPlaceholder')"
      trim />
  </div>

  <!-- Tabbed interface for value, preview, and usage -->
  <Tabs
    v-model:activeKey="activeKey"
    size="small"
    class="ant-tabs-nav-mb-2.5">
    <!-- Value configuration tab -->
    <TabPane key="value">
      <template #tab>
        <div class="flex items-center font-normal">
          <IconRequired />
          <span>{{ t('common.value') }}</span>
        </div>
      </template>

      <div class="flex">
        <Hints class="mb-2.5" :text="t('dataVariable.detail.staticVariable.hints')" />
        <FunctionsButton class="ml-1 text-3.5" />
      </div>
      <ParamTextarea
        :value="variableValue"
        :maxLength="4096"
        :height="60"
        :placeholder="t('dataVariable.detail.staticVariable.valuePlaceholder')"
        @blur="handleBlurValue" />
    </TabPane>

    <!-- Preview tab -->
    <TabPane key="preview">
      <template #tab>
        <div class="flex items-center font-normal">
          <span>{{ t('dataVariable.detail.staticVariable.preview') }}</span>
        </div>
      </template>

      <PreviewData :dataSource="previewData" />
    </TabPane>

    <!-- Usage tab - only visible for existing variables -->
    <TabPane v-if="variableId" key="use">
      <template #tab>
        <div class="flex items-center font-normal">
          <span>{{ t('dataVariable.detail.staticVariable.use') }}</span>
        </div>
      </template>

      <VariableUsageList :id="variableId" />
    </TabPane>
  </Tabs>
</template>
