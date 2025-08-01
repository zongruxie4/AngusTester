<script setup lang="ts">
import { onMounted, ref, watch } from 'vue';
import { Form, FormItem, Switch, Tooltip } from 'ant-design-vue';
import { Icon, Input, SelectEnum, ShortDuration, TaskPriority, ApiUtils as apiUtils } from '@xcan-angus/vue-ui';
import { enumUtils } from '@xcan-angus/infra';

import { splitDuration } from '@/utils/utils';

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

const variableIncludeStrategyTip = {
  ALL: '包含所有全局变量、继承项目/服务变量、以及当前接口变量',
  REF: '只包含接口引用的全局变量、继承项目/服务变量、以及当前接口变量',
  IGNORE: '忽略变量，生成脚本不包含变量'
};
const variableIncludeStrategyOpt = ref<{ message: string; value: string; }[]>([]);
const loadVariableEnum = async () => {
  const [error, data] = await enumUtils.enumToMessages('VariableIncludeStrategy');
  if (error) {
    return;
  }
  variableIncludeStrategyOpt.value = (data || []).map(i => ({ ...i, label: i.message, tip: variableIncludeStrategyTip[i.value] }));
};

onMounted(() => {
  loadVariableEnum();
});

defineExpose({
  getData
});
</script>
<template>
  <Form
    :labelCol="{span: 8}"
    :colon="false"
    class="w-90">
    <FormItem label="并发数（线程）" required>
      <Input
        v-model:value="formData.threads"
        :min="1"
        :disabled="props.testType === 'FUNCTIONAL'"
        dataType="number"
        @blur="changeThreads" />
    </FormItem>
    <template v-if="props.testType === 'FUNCTIONAL'">
      <FormItem label="迭代次数" required>
        <Input
          v-model:value="formData.iterations"
          :min="1"
          :max="10"
          dataType="number"
          @blur="changeIterations" />
      </FormItem>
    </template>
    <template v-else>
      <FormItem label="执行时间" required>
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
      <FormItem label="增压并发数">
        <div class="flex items-center relative">
          <Input
            v-model:value="formData.rampUpThreads" />
          <Tooltip placement="rightTop">
            <template #title>
              用于在测试过程中增加并发数以增加系统压力，通过逐渐加压方式可以观测系统在不同压力下性能和稳定性表现。
            </template>
            <Icon
              icon="icon-tishi1"
              class="text-blue-icon absolute -right-5 text-3.5" />
          </Tooltip>
        </div>
      </FormItem>
      <FormItem label="增压测试时长">
        <ShortDuration
          v-model:value="formData.rampUpInterval"
          :selectProps="{style: 'width: 60px'}"
          :inputProps="{
            onblur: blurRampUpInterval
          }"
          class="w-full" />
      </FormItem>
    </template>
    <FormItem label="是否忽略断言">
      <Switch
        v-model:checked="formData.ignoreAssertions"
        size="small" />
      <Tooltip placement="rightTop">
        <template #title>
          如果不忽略，断言失败时按采样错误处理。
        </template>
        <Icon
          icon="icon-tishi1"
          class="text-blue-icon ml-2 text-3.5" />
      </Tooltip>
    </FormItem>
    <FormItem label="更新测试结果">
      <Switch
        v-model:checked="formData.updateTestResult"
        size="small" />
      <Tooltip placement="rightTop">
        <template #title>
          是否将测试结果更新到关联资源(API、用例或场景)。
        </template>
        <Icon
          icon="icon-tishi1"
          class="text-blue-icon ml-2 text-3.5" />
      </Tooltip>
    </FormItem>
    <FormItem label="优先级" required>
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
    <FormItem label="是否授权控制" required>
      <Switch
        v-model:checked="formData.authFlag"
        size="small" />
      <Tooltip placement="rightTop">
        <template #title>
          生成的脚本是否受权限控制。受权限控制时，默认只有自己可见且有全部权限，其他用户需要通过"脚本" -> "授权"手动授权；不受权限控制所有用户可见且可操作。
        </template>
        <Icon
          icon="icon-tishi1"
          class="text-blue-icon ml-2 text-3.5" />
      </Tooltip>
    </FormItem>
  </Form>
</template>
