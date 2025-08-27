<script setup lang="ts">
import { onMounted, ref, watch } from 'vue';
import { useI18n } from 'vue-i18n';
import { Form, FormItem, Switch, Tooltip } from 'ant-design-vue';
import { Icon, Input, SelectEnum, ShortDuration, TaskPriority, ApiUtils as apiUtils } from '@xcan-angus/vue-ui';

import { splitDuration } from '@/utils/utils';

const { t } = useI18n();

interface Props {
  value: {
    threads: string;
    duration: string;
  },
  testType: 'PERFORMANCE'|'STABILITY'|'FUNCTIONAL';
  setType: 'create'|'update';
  type: 'API'|'PROJECT'|'SERVICE';
}

const props = withDefaults(defineProps<Props>(), {
  value: () => ({
    threads: '1',
    duration: '1s',
    priority: 'MEDIUM',
    iterations: '1'
  }),
  testType: 'PERFORMANCE',
  type: 'API'
});

const formData = ref({
  threads: '1',
  duration: '1s',
  iterations: '1',
  priority: 'MEDIUM',
  authFlag: false,
  updateTestResult: false,
  ignoreAssertions: true,
  rampUpThreads: undefined,
  rampUpInterval: '1min'
  // variableIncludeStrategy: 'REF'
});

const blurDuration = () => {
  const [durationValue, unit] = splitDuration(formData.value.duration);
  if (!durationValue || durationValue === '0') {
    formData.value.duration = 1 + unit;
  }
  if (props.testType === 'PERFORMANCE') {
    const [durationValue] = splitDuration(formData.value.rampUpInterval);
    if (+durationValue > 0) {
      const max = apiUtils.maxDuration(formData.value.duration, formData.value.rampUpInterval);
      if (max === formData.value.rampUpInterval) {
        formData.value.rampUpInterval = formData.value.duration;
      }
    }
  }
};

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

watch(() => props.value, newValue => {
  formData.value = {
    ...formData.value,
    ...newValue
  };
}, {
  deep: true,
  immediate: props.testType === 'FUNCTIONAL'
});

const changeThreads = () => {
  if (!formData.value.threads) {
    formData.value.threads = '1';
  }
};

const changeIterations = () => {
  if (!formData.value.iterations) {
    formData.value.iterations = '1';
  }
};

const getData = () => {
  const result: {[key: string]: any} = {
    testType: props.testType,
    ...formData.value
  };
  if (props.testType === 'STABILITY' || !result.rampUpThreads) {
    delete result.rampUpInterval;
    delete result.rampUpThreads;
  }
  if (props.testType === 'FUNCTIONAL') {
    delete result.rampUpInterval;
    delete result.rampUpThreads;
    delete result.threads;
  }

  return result;
};

defineExpose({
  getData
});
</script>
<template>
  <Form
    :labelCol="{span: 8}"
    :colon="false"
    class="w-90">
    <FormItem :label="t('commonComp.genTestScriptModal.testForm.concurrentThreads')" required>
      <Input
        v-model:value="formData.threads"
        :min="1"
        :disabled="props.testType === 'FUNCTIONAL'"
        dataType="number"
        @blur="changeThreads" />
    </FormItem>
    <template v-if="props.testType === 'FUNCTIONAL'">
      <FormItem :label="t('commonComp.genTestScriptModal.testForm.iterations')" required>
        <Input
          v-model:value="formData.iterations"
          :min="1"
          :max="10"
          dataType="number"
          @blur="changeIterations" />
      </FormItem>
    </template>
    <template v-else>
      <FormItem :label="t('commonComp.genTestScriptModal.testForm.executionTime')" required>
        <ShortDuration
          v-model:value="formData.duration"
          :selectProps="{style: 'width: 60px'}"
          class="w-full"
          :inputProps="{
            onblur: blurDuration,
          }" />
      </FormItem>
    </template>
    <template v-if="props.testType==='PERFORMANCE'">
      <FormItem :label="t('commonComp.genTestScriptModal.testForm.rampUpThreads')">
        <div class="flex items-center relative">
          <Input
            v-model:value="formData.rampUpThreads" />
          <Tooltip placement="rightTop">
            <template #title>
              {{ t('commonComp.genTestScriptModal.testForm.rampUpTooltip') }}
            </template>
            <Icon
              icon="icon-tishi1"
              class="text-blue-icon absolute -right-5 text-3.5" />
          </Tooltip>
        </div>
      </FormItem>
      <FormItem :label="t('commonComp.genTestScriptModal.testForm.rampUpDuration')">
        <ShortDuration
          v-model:value="formData.rampUpInterval"
          :selectProps="{style: 'width: 60px'}"
          :inputProps="{
            onblur: blurRampUpInterval
          }"
          class="w-full" />
      </FormItem>
    </template>
    <FormItem :label="t('commonComp.genTestScriptModal.testForm.ignoreAssertions')">
      <Switch
        v-model:checked="formData.ignoreAssertions"
        size="small" />
      <Tooltip placement="rightTop">
        <template #title>
          {{ t('commonComp.genTestScriptModal.testForm.ignoreAssertionsTooltip') }}
        </template>
        <Icon
          icon="icon-tishi1"
          class="text-blue-icon ml-2 text-3.5" />
      </Tooltip>
    </FormItem>
    <FormItem :label="t('commonComp.genTestScriptModal.testForm.updateTestResult')">
      <Switch
        v-model:checked="formData.updateTestResult"
        size="small" />
      <Tooltip placement="rightTop">
        <template #title>
          {{ t('commonComp.genTestScriptModal.testForm.updateTestResultTooltip') }}
        </template>
        <Icon
          icon="icon-tishi1"
          class="text-blue-icon ml-2 text-3.5" />
      </Tooltip>
    </FormItem>
    <FormItem :label="t('commonComp.genTestScriptModal.testForm.priority')" required>
      <SelectEnum
        v-model:value="formData.priority"
        class="w-full"
        enumKey="Priority"
        :allowClear="false"
        size="small">
        <template #option="record">
          <TaskPriority :value="record" />
        </template>
      </SelectEnum>
    </FormItem>
    <FormItem :label="t('commonComp.genTestScriptModal.testForm.authControl')" required>
      <Switch
        v-model:checked="formData.authFlag"
        size="small" />
      <Tooltip placement="rightTop">
        <template #title>
          {{ t('commonComp.genTestScriptModal.testForm.authControlTooltip') }}
        </template>
        <Icon
          icon="icon-tishi1"
          class="text-blue-icon ml-2 text-3.5" />
      </Tooltip>
    </FormItem>
  </Form>
</template>
