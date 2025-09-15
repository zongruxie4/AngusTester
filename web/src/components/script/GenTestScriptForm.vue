<script setup lang="ts">
import { ref, watch } from 'vue';
import { useI18n } from 'vue-i18n';
import { Form, FormItem, Switch, Tooltip } from 'ant-design-vue';
import { Icon, Input, SelectEnum, ShortDuration } from '@xcan-angus/vue-ui';
import { CombinedTargetType, Priority } from '@xcan-angus/infra';
import { TestType } from '@/enums/enums';
import TaskPriority from '@/components/TaskPriority/index.vue';

import apiUtils from '@/utils/ApiUtils/index';

import { splitDuration } from '@/utils/utils';

const { t } = useI18n();

// ===== Component Props and Emits =====
interface Props {
  value: {
    threads: string;
    duration: string;
  },
  priority: Priority;
  testType: TestType.PERFORMANCE|TestType.STABILITY|TestType.FUNCTIONAL;
  setType: 'create'|'update';
  type: CombinedTargetType.API | CombinedTargetType.SERVICE | CombinedTargetType.PROJECT;
}

const props = withDefaults(defineProps<Props>(), {
  value: () => ({
    threads: '1',
    duration: '1s',
    priority: Priority.MEDIUM,
    iterations: '1'
  }),
  testType: TestType.PERFORMANCE,
  type: CombinedTargetType.API
});

// ===== Reactive Data and State =====
const formData = ref({
  threads: '1',
  duration: '1s',
  iterations: '1',
  priority: Priority.MEDIUM,
  auth: false,
  updateTestResult: false,
  ignoreAssertions: true,
  rampUpThreads: undefined,
  rampUpInterval: '1min'
});

// ===== Event Handlers =====
/**
 * <p>
 * Handles duration input blur event.
 * <p>
 * Validates and normalizes duration values, and adjusts ramp-up interval if needed.
 */
const blurDuration = () => {
  const [durationValue, unit] = splitDuration(formData.value.duration);
  if (!durationValue || durationValue === '0') {
    formData.value.duration = 1 + unit;
  }
  if (props.testType === TestType.PERFORMANCE) {
    const [durationValue] = splitDuration(formData.value.rampUpInterval);
    if (+durationValue > 0) {
      const max = apiUtils.maxDuration(formData.value.duration, formData.value.rampUpInterval);
      if (max === formData.value.rampUpInterval) {
        formData.value.rampUpInterval = formData.value.duration;
      }
    }
  }
};

/**
 * <p>
 * Handles ramp-up interval input blur event.
 * <p>
 * Validates and normalizes ramp-up interval values.
 */
const blurRampUpInterval = () => {
  const [durationValue, unit] = splitDuration(formData.value.rampUpInterval);
  if (!durationValue) {
    formData.value.rampUpInterval = 0 + unit;
  }
  if (+durationValue > 0) {
    const max = apiUtils.maxDuration(formData.value.duration, formData.value.rampUpInterval);
    if (max === formData.value.rampUpInterval) {
      formData.value.rampUpInterval = formData.value.duration;
    }
  }
};

/**
 * <p>
 * Handles threads input change event.
 * <p>
 * Ensures threads value is not empty.
 */
const changeThreads = () => {
  if (!formData.value.threads) {
    formData.value.threads = '1';
  }
};

/**
 * <p>
 * Handles iterations input change event.
 * <p>
 * Ensures iterations value is not empty.
 */
const changeIterations = () => {
  if (!formData.value.iterations) {
    formData.value.iterations = '1';
  }
};

// ===== Methods =====
/**
 * <p>
 * Gets the form data for submission.
 * <p>
 * Returns processed form data with test type specific fields filtered out.
 * @returns Object containing the form data for API submission
 */
const getData = () => {
  const result: {[key: string]: any} = {
    testType: props.testType,
    ...formData.value
  };
  if (props.testType === TestType.STABILITY || !result.rampUpThreads) {
    delete result.rampUpInterval;
    delete result.rampUpThreads;
  }
  if (props.testType === TestType.FUNCTIONAL) {
    delete result.rampUpInterval;
    delete result.rampUpThreads;
    delete result.threads;
  }

  return result;
};

// ===== Lifecycle Hooks =====
watch(() => props.value, newValue => {
  formData.value = {
    ...formData.value,
    ...newValue
  };
}, {
  deep: true,
  immediate: props.testType === TestType.FUNCTIONAL
});

// ===== Exposed Methods =====
defineExpose({
  getData
});
</script>
<template>
  <div class="test-form-container">
    <Form
      :labelCol="{span: 8}"
      :colon="false"
      class="test-form">
      <!-- Basic configuration section -->
      <div class="form-section">
        <h4 class="section-subtitle">{{ t('commonComp.genTestScriptModal.testForm.basicConfig') }}</h4>

        <!-- Concurrent threads -->
        <FormItem
          :label="t('commonComp.genTestScriptModal.testForm.concurrentThreads')"
          required
          class="form-item">
          <Input
            v-model:value="formData.threads"
            :min="1"
            :disabled="props.testType === TestType.FUNCTIONAL"
            dataType="number"
            class="form-input"
            @blur="changeThreads" />
        </FormItem>

        <!-- Iterations (for functional tests) -->
        <template v-if="props.testType === TestType.FUNCTIONAL">
          <FormItem
            :label="t('commonComp.genTestScriptModal.testForm.iterations')"
            required
            class="form-item">
            <Input
              v-model:value="formData.iterations"
              :min="1"
              :max="10"
              dataType="number"
              class="form-input"
              @blur="changeIterations" />
          </FormItem>
        </template>

        <!-- Execution time (for performance/stability tests) -->
        <template v-else>
          <FormItem
            :label="t('commonComp.genTestScriptModal.testForm.executionTime')"
            required
            class="form-item">
            <ShortDuration
              v-model:value="formData.duration"
              :selectProps="{style: 'width: 60px'}"
              class="form-duration"
              :inputProps="{
                onblur: blurDuration,
              }" />
          </FormItem>
        </template>
      </div>

      <!-- Performance-specific configuration -->
      <template v-if="props.testType === TestType.PERFORMANCE">
        <div class="form-section">
          <h4 class="section-subtitle">{{ t('commonComp.genTestScriptModal.testForm.performanceConfig') }}</h4>

          <!-- Ramp-up threads -->
          <FormItem
            :label="t('commonComp.genTestScriptModal.testForm.rampUpThreads')"
            class="form-item">
            <div class="input-with-tooltip">
              <Input
                v-model:value="formData.rampUpThreads"
                class="form-input" />
              <Tooltip placement="rightTop">
                <template #title>
                  {{ t('commonComp.genTestScriptModal.testForm.rampUpTooltip') }}
                </template>
                <Icon
                  icon="icon-tishi1"
                  class="tooltip-icon" />
              </Tooltip>
            </div>
          </FormItem>

          <!-- Ramp-up duration -->
          <FormItem
            :label="t('commonComp.genTestScriptModal.testForm.rampUpDuration')"
            class="form-item">
            <ShortDuration
              v-model:value="formData.rampUpInterval"
              :selectProps="{style: 'width: 60px'}"
              :inputProps="{
                onblur: blurRampUpInterval
              }"
              class="form-duration" />
          </FormItem>
        </div>
      </template>

      <!-- Advanced configuration section -->
      <div class="form-section">
        <h4 class="section-subtitle">{{ t('commonComp.genTestScriptModal.testForm.advancedConfig') }}</h4>

        <!-- Ignore assertions -->
        <FormItem
          :label="t('commonComp.genTestScriptModal.testForm.ignoreAssertions')"
          class="form-item">
          <div class="switch-with-tooltip">
            <Switch
              v-model:checked="formData.ignoreAssertions"
              size="small" />
            <Tooltip placement="rightTop">
              <template #title>
                {{ t('commonComp.genTestScriptModal.testForm.ignoreAssertionsTooltip') }}
              </template>
              <Icon
                icon="icon-tishi1"
                class="tooltip-icon" />
            </Tooltip>
          </div>
        </FormItem>

        <!-- Update test result -->
        <FormItem
          :label="t('commonComp.genTestScriptModal.testForm.updateTestResult')"
          class="form-item">
          <div class="switch-with-tooltip">
            <Switch
              v-model:checked="formData.updateTestResult"
              size="small" />
            <Tooltip placement="rightTop">
              <template #title>
                {{ t('commonComp.genTestScriptModal.testForm.updateTestResultTooltip') }}
              </template>
              <Icon
                icon="icon-tishi1"
                class="tooltip-icon" />
            </Tooltip>
          </div>
        </FormItem>

        <!-- Priority -->
        <FormItem
          :label="t('commonComp.genTestScriptModal.testForm.priority')"
          required
          class="form-item">
          <SelectEnum
            v-model:value="formData.priority"
            class="form-select"
            enumKey="Priority"
            :allowClear="false"
            size="small">
            <template #option="record">
              <TaskPriority :value="record" />
            </template>
          </SelectEnum>
        </FormItem>

        <!-- Auth control -->
        <FormItem
          :label="t('commonComp.genTestScriptModal.testForm.authControl')"
          required
          class="form-item">
          <div class="switch-with-tooltip">
            <Switch
              v-model:checked="formData.auth"
              size="small" />
            <Tooltip placement="rightTop">
              <template #title>
                {{ t('commonComp.genTestScriptModal.testForm.authControlTooltip') }}
              </template>
              <Icon
                icon="icon-tishi1"
                class="tooltip-icon" />
            </Tooltip>
          </div>
        </FormItem>
      </div>
    </Form>
  </div>
</template>

<style scoped>
/* Form container */
.test-form-container {
  width: 100%;
}

.test-form {
  width: 100%;
}

/* Form sections */
.form-section {
  margin-bottom: 20px;
  padding: 16px;
  background-color: #fafafa;
  border: 1px solid #f0f0f0;
  border-radius: 6px;
}

.form-section:last-child {
  margin-bottom: 0;
}

.section-subtitle {
  font-size: 12px;
  font-weight: 600;
  color: #262626;
  margin: 0 0 16px 0;
  padding-bottom: 8px;
  border-bottom: 1px solid #e8e8e8;
}

/* Form items */
.form-item {
  margin-bottom: 16px;
}

.form-item:last-child {
  margin-bottom: 0;
}

/* Form inputs */
.form-input {
  width: 100%;
}

.form-duration {
  width: 100%;
}

.form-select {
  width: 100%;
}

/* Input with tooltip */
.input-with-tooltip {
  position: relative;
  display: flex;
  align-items: center;
}

.tooltip-icon {
  color: #1890ff;
  font-size: 14px;
  margin-left: 8px;
  cursor: help;
}

/* Switch with tooltip */
.switch-with-tooltip {
  display: flex;
  align-items: center;
}

/* Deep style overrides */
:deep(.ant-form-item-label) {
  font-size: 12px;
  font-weight: 500;
  color: #262626;
  padding-bottom: 4px;
}

:deep(.ant-form-item-control-input) {
  min-height: 32px;
}

:deep(.ant-input) {
  font-size: 12px;
  height: 32px;
  border-radius: 4px;
}

:deep(.ant-select) {
  font-size: 12px;
}

:deep(.ant-select-selector) {
  height: 32px;
  border-radius: 4px;
}

:deep(.ant-switch) {
  font-size: 12px;
}

:deep(.ant-switch-small) {
  min-width: 28px;
  height: 16px;
  line-height: 16px;
}

:deep(.ant-tooltip-inner) {
  font-size: 12px;
  max-width: 200px;
}

/* Responsive design */
@media (max-width: 768px) {
  .form-section {
    padding: 12px;
  }

  .section-subtitle {
    font-size: 11px;
    margin-bottom: 12px;
  }

  .form-item {
    margin-bottom: 12px;
  }
}
</style>
