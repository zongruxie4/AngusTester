<script setup lang="ts">
import { reactive, watch, onMounted, ref, defineAsyncComponent, nextTick } from 'vue';
import { Input, notification, Icon, Hints, ShortDuration } from '@xcan-angus/vue-ui';
import { Form, FormItem, RadioButton, RadioGroup, Button, Switch, Tooltip } from 'ant-design-vue';
import IndicatorAssert from '@/components/IndicatorAssert/index.vue';
import { enumUtils, http, TESTER } from '@xcan-angus/infra';
import { useI18n } from 'vue-i18n';

import { indicator } from './apis';
import { splitDuration, maxDuration } from '@/utils/ApiUtils';

const { t } = useI18n();

export interface Props {
  disabled:boolean;
  type: 'API' | 'SCENARIO';
  id: string;
  test?: {
    testFunc: boolean;
    testPerf: boolean;
    testStability: boolean;
  }
}

const props = withDefaults(defineProps<Props>(), {
  disabled: false,
  type: 'API',
  id: '',
  test: undefined
});

// eslint-disable-next-line func-call-spacing
const emit = defineEmits<{
  (e: 'rendered', value: true);
}>();

const ResponseTime = defineAsyncComponent(() => import('./response-time.vue'));

// 获取指标信息 api 配置
const loadInfoConfig = {
  perf: indicator.loadPerf,
  stability: indicator.loadStaibility,
  func: indicator.loadFunc
};

const modifyConfig = {
  perf: indicator.modifyPerf,
  stability: indicator.modifyStability,
  func: indicator.modifyFunc
};

// 删除指标配置
const delConfig = {
  perf: indicator.delPerf,
  stability: indicator.delStaibility,
  func: indicator.delFunc
};

// 启用禁用指标
const enableConfig = {
  API: (params): Promise<[Error | null, any]> => {
    return http.put(`${TESTER}/apis/${props.id}/test/enabled`, params, {
      paramsType: true
    });
  },
  SCENARIO: (params): Promise<[Error | null, any]> => {
    return http.put(`${TESTER}/scenario/${props.id}/test/enabled`, params, {
      paramsType: true
    });
  }
};

// 获取启用禁用数据
const loadEnableConfig = {
  API: (): Promise<[Error | null, any]> => {
    return http.get(`${TESTER}/apis/${props.id}`);
  },
  SCENARIO: (): Promise<[Error | null, any]> => {
    return http.get(`${TESTER}/scenario/${props.id}`);
  }
};

const tipsConfig = {
  // hints: {
  //   API: '接口指标设定值优先级高于服务指标',
  //   SERVICE: '配置服务下接口默认指标，接口指标设定值优先级高于服务指标。',
  // },
  perfHints: [
    t('xcan_indicator.performanceTestDescription'),
    t('xcan_indicator.performanceTestDefault')
  ],
  stabilityHints: [
    t('xcan_indicator.stabilityTestDescription'),
    t('xcan_indicator.stabilityTestDefault')
  ],
  funcHints: [
    t('xcan_indicator.functionalTestDescription')
  ]
};

const indicatorType = ref<'perf' | 'stability'|'func'>(props.type === 'SCENARIO' ? 'perf' : 'func');

const state = reactive({
  enableds: []
});

const perfData = reactive({
  info: {
    threads: '',
    errorRate: '',
    tps: '',
    art: '',
    percentile: 'ALL',
    duration: '1min',
    rampUpThreads: undefined,
    rampUpInterval: '1min',
    enabled: true
  },
  disabled: true,
  operation: '<='
});

const stability = reactive({
  info: {
    threads: '',
    duration: '1min',
    errorRate: '',
    tps: '',
    art: '',
    percentile: 'ALL',
    cpu: '',
    memory: '',
    disk: '',
    network: '',
    enabled: true
  },
  disabled: true
});

const funcData = reactive({
  info: {
    smoke: true, // 使用冒烟测试，
    security: true, // 使用安全测试
    securityCheckSetting: 'NOT_SECURITY_CODE',
    smokeCheckSetting: 'API_AVAILABLE',
    userDefinedSecurityAssertion: undefined,
    userDefinedSmokeAssertion: undefined
  },
  disabled: true
});

let initPerData = {}; // 用于保存编辑前的原始值, 为后面提交前数据对比做铺垫
let initStability = {};
let initFunc = {};

watch(() => indicatorType.value, () => {
  loadIndicator();
});

const smokeAssertRef = ref();
const securityAssertRef = ref();
// 冒烟测试指标选项
const smokeEnumOpt = ref<{value: string; label: string}[]>([]);
// 安全测试指标选项
const SecurityEnumOpt = ref<{value: string; label: string}[]>([]);
const loadFuncEnumOpt = async () => {
  const data1 = enumUtils.enumToMessages('SmokeCheckSetting'); // TODO 功能后期需要重新设计实现
  smokeEnumOpt.value = (data1 || []).map(i => ({ ...i, label: i.message }));
  const data2 = enumUtils.enumToMessages('SecurityCheckSetting'); // TODO 功能后期需要重新设计实现
  SecurityEnumOpt.value = (data2 || []).map(i => ({ ...i, label: i.message }));
};

// 获取指标数据
const loadIndicator = async () => {
  if (!props.id) {
    return;
  }
  const [error, res] = await loadInfoConfig[indicatorType.value](props.type, props.id);
  if (error) {
    return;
  }
  if (indicatorType.value === 'perf') {
    const showData = res.data;
    perfData.info = showData;
    perfData.info.percentile = showData.percentile.value;
  } else if (indicatorType.value === 'stability') {
    const showData = res.data;
    stability.info = {
      ...showData
    };
  } else if (indicatorType.value === 'func') {
    const { securityCheckSetting, smokeCheckSetting, ...other } = res.data;
    funcData.info = {
      ...other,
      securityCheckSetting: securityCheckSetting.value || securityCheckSetting,
      smokeCheckSetting: smokeCheckSetting.value || smokeCheckSetting
    };
  }
};

const handleDurationChange = (value: string) => {
  if (indicatorType.value === 'perf') {
    perfData.info.duration = value;
  } else {
    stability.info.duration = value;
  }
};

// 开放修改指标
const edit = () => {
  if (indicatorType.value === 'perf') {
    perfData.disabled = false;
    initPerData = JSON.parse(JSON.stringify(perfData.info));
  } else if (indicatorType.value === 'stability') {
    initStability = JSON.parse(JSON.stringify(stability.info));
    stability.disabled = false;
  } else {
    initFunc = JSON.parse(JSON.stringify(funcData.info));
    funcData.disabled = false;
  }
};

// 取消
const cancel = () => {
  if (indicatorType.value === 'perf') {
    perfData.disabled = true;
    isValidatePerf.value = false;
    perfData.info = JSON.parse(JSON.stringify(initPerData));
  } else if (indicatorType.value === 'stability') {
    stability.disabled = true;
    isValidateStability.value = false;
    stability.info = JSON.parse(JSON.stringify(initStability));
  } else {
    funcData.disabled = true;
    funcData.info = JSON.parse(JSON.stringify(initFunc));
  }
  loadIndicator();
};

const isValidatePerf = ref(false);
// 校验性能指标数据
const validatePerf = ():boolean => {
  isValidatePerf.value = true;
  const [durationValue] = splitDuration(perfData.info.duration);
  if (!durationValue || !perfData.info.threads || !perfData.info.errorRate || !perfData.info.tps) {
    return false;
  }
  if (perfData.info.threads === '0') {
    notification.error(t('xcan_indicator.concurrencyMustBeGreaterThanZero'));
    return false;
  }
  if (durationValue === '0') {
    notification.error(t('xcan_indicator.testDurationMustBeGreaterThanZero'));
    return false;
  }
  if (perfData.info.art === '0') {
    notification.error(t('xcan_indicator.responseTimeMustBeGreaterThanZero'));
    return false;
  }
  if (perfData.info.tps === '0') {
    notification.error(t('xcan_indicator.throughputMustBeGreaterThanZero'));
    return false;
  }
  if (+perfData.info.errorRate > 100) {
    notification.warning(t('xcan_indicator.errorRateShouldBeLessThan100'));
    return false;
  }
  isValidatePerf.value = false;
  return true;
};

const isValidateStability = ref(false);
// 校验稳定性指标数据
const validateStability = (): boolean => {
  isValidateStability.value = true;
  const [durationValue] = splitDuration(stability.info.duration);
  if (!durationValue || !stability.info.threads || !stability.info.errorRate || !stability.info.tps) {
    return false;
  }
  if (stability.info.threads === '0') {
    notification.error(t('xcan_indicator.concurrencyMustBeGreaterThanZero'));
    return false;
  }
  if (durationValue === '0') {
    notification.error(t('xcan_indicator.testDurationMustBeGreaterThanZero'));
    return false;
  }
  if (+stability.info.errorRate > 100) {
    notification.error(t('xcan_indicator.errorRateMustBeLessThan100'));
    return false;
  }
  if (+stability.info.cpu > 100) {
    notification.error(t('xcan_indicator.cpuUsageMustBeLessThan100'));
    return false;
  }
  if (+stability.info.memory > 100) {
    notification.error(t('xcan_indicator.memoryUsageMustBeLessThan100'));
    return false;
  }
  if (+stability.info.disk > 100) {
    notification.error(t('xcan_indicator.diskUsageMustBeLessThan100'));
    return false;
  }
  if (+stability.info.network > 100) {
    notification.error(t('xcan_indicator.networkUsageMustBeLessThan100'));
    return false;
  }
  isValidateStability.value = false;
  return true;
};

// 修改接口性能指标
const putPerf = async () => {
  let params;
  if (indicatorType.value === 'perf') {
    if (!validatePerf()) {
      return;
    }
    const valueKey = Object.keys(perfData.info);
    if (valueKey.every(key => initPerData[key] === perfData.info[key])) {
      perfData.disabled = true;
      return;
    }
    params = {
      targetId: props.id,
      ...perfData.info,
      targetType: props.type
    };
  } else if (indicatorType.value === 'stability') {
    if (!validateStability()) {
      return;
    }
    const valueKey = Object.keys(stability.info);
    if (valueKey.every(key => initStability[key] === stability.info[key])) {
      stability.disabled = true;
      return;
    }
    params = {
      targetId: props.id,
      ...stability.info,
      targetType: props.type
    };
  } else if (indicatorType.value === 'func') {
    params = funcData.info;
    if (smokeAssertRef.value) {
      if (smokeAssertRef.value.validate()) {
        params.userDefinedSmokeAssertion = smokeAssertRef.value.getData();
      } else {
        return;
      }
    } else {
      params.userDefinedSmokeAssertion = undefined;
    }
    if (securityAssertRef.value) {
      if (securityAssertRef.value.validate()) {
        params.userDefinedSecurityAssertion = smokeAssertRef.value.getData();
      } else {
        return;
      }
    } else {
      params.userDefinedSecurityAssertion = undefined;
    }
  }
  const [error] = await modifyConfig[indicatorType.value](params);
  if (error) {
    return;
  }
  notification.success(t('xcan_indicator.indicatorModifiedSuccessfully'));
  if (indicatorType.value === 'perf') {
    perfData.disabled = true;
  } else if (indicatorType.value === 'stability') {
    stability.disabled = true;
  } else {
    funcData.disabled = true;
  }
  loadIndicator();
};

// 恢复默认指标
const delCurrent = async () => {
  const [error] = await delConfig[indicatorType.value](props.type, props.id);
  if (error) {
    return;
  }
  notification.success(t('xcan_indicator.resetToDefaultIndicator'));
  loadIndicator();
};

const durationInputProps = {
  onblur: () => handleDurationData()
};

const handleDurationData = () => {
  const value = indicatorType.value === 'perf' ? perfData.info.duration : stability.info.duration;
  const [durationValue, unit] = splitDuration(value);
  if (!durationValue || durationValue === '0') {
    if (indicatorType.value === 'perf') {
      perfData.info.duration = '1' + unit;
    } else {
      stability.info.duration = '1' + unit;
    }
  }
  if (indicatorType.value === 'perf') {
    const maxValue = maxDuration(perfData.info.duration, perfData.info.rampUpInterval);
    if (maxValue === perfData.info.rampUpInterval) {
      perfData.info.rampUpInterval = perfData.info.duration;
    }
  }
};

const rampUpIntervalInputProps = {
  onblur: () => handleRampUpIntervalBlur()
};

const handleRampUpIntervalBlur = () => {
  const [durationValue, unit] = splitDuration(perfData.info.rampUpInterval);
  if (!durationValue) {
    perfData.info.rampUpInterval = '0' + unit;
  }
  const maxValue = maxDuration(perfData.info.duration, perfData.info.rampUpInterval);
  if (maxValue === perfData.info.rampUpInterval) {
    perfData.info.rampUpInterval = perfData.info.duration;
  }
};

const loadTargetTest = async () => {
  const [error, { data }] = await loadEnableConfig[props.type]();
  if (error) {
    return;
  }

  state.enableds = [];
  if (data.testStability) {
    state.enableds.push('STABILITY');
  }
  if (data.testPerf) {
    state.enableds.push('PERFORMANCE');
  }
  if (data.testFunc) {
    state.enableds.push('FUNCTIONAL');
  }
};

const changeTest = async (event, flagKey) => {
  const params = { enabled: event, testTypes: [...state.enableds] };
  if (event) {
    params.testTypes.push(flagKey);
  } else {
    params.testTypes = params.testTypes.filter(i => i !== flagKey);
  }
  const [error] = await enableConfig[props.type](params);
  if (error) {
    return;
  }
  state.enableds = params.testTypes;
};

watch(() => props.id, () => {
  loadIndicator();
  if (!props.test) {
    loadTargetTest();
    return;
  }
  state.enableds = [];
  if (props.test?.testStability) {
    state.enableds.push('STABILITY');
  }
  if (props.test?.testPerf) {
    state.enableds.push('PERFORMANCE');
  }
  if (props.test?.testFunc) {
    state.enableds.push('FUNCTIONAL');
  }
});

onMounted(() => {
  loadIndicator();
  loadFuncEnumOpt();
  nextTick(() => {
    emit('rendered', true);
  });
  if (!props.test) {
    loadTargetTest();
    return;
  }
  state.enableds = [];
  if (props.test?.testStability) {
    state.enableds.push('STABILITY');
  }
  if (props.test?.testPerf) {
    state.enableds.push('PERFORMANCE');
  }
  if (props.test?.testFunc) {
    state.enableds.push('FUNCTIONAL');
  }
});
</script>

<template>
  <div>
    <!-- <Hints :text="tipsConfig.hints[props.type]" /> -->
    <RadioGroup
      v-model:value="indicatorType"
      buttonStyle="solid"
      class="my-2 w-full flex"
      size="small">
      <RadioButton value="func" class="text-3 flex-1 text-center">{{ t('xcan_indicator.functionalIndicator') }}</RadioButton>
      <RadioButton value="perf" class="text-3 flex-1 text-center">{{ t('xcan_indicator.performanceIndicator') }}</RadioButton>
      <RadioButton value="stability" class="text-3 flex-1 text-center">{{ t('xcan_indicator.stabilityIndicator') }}</RadioButton>
    </RadioGroup>
    <Form
      v-if="indicatorType === 'perf'"
      layout="vertical"
      disabled>
      <Hints
        v-for="text in tipsConfig.perfHints"
        :key="text"
        :text="text"
        class="mb-2" />
      <div class="flex items-center space-x-2 text-3 my-2">
        <Switch
          :checked="state.enableds.includes('PERFORMANCE')"
          size="small"
          @change="changeTest($event, 'PERFORMANCE')" />
        <span>{{ t('xcan_indicator.enableTest') }}</span>
        <Tooltip placement="top">
          <template #title>
            {{ t('xcan_indicator.enableTestTooltip') }}
          </template>
          <Icon
            icon="icon-tishi1"
            class="text-blue-badge text-3.5" />
        </Tooltip>
      </div>
      <FormItem :label="t('xcan_indicator.concurrency')" required>
        <div class="flex items-center">
          <Input
            v-model:value="perfData.info.threads"
            dataType="number"
            :disabled="perfData.disabled"
            :allowClear="false"
            :error="isValidatePerf && !perfData.info.threads"
            size="small" />
          <span class="w-8"></span>
        </div>
      </FormItem>
      <FormItem :label="t('xcan_indicator.testDuration')" required>
        <div class="flex items-center">
          <ShortDuration
            :value="perfData.info.duration"
            :disabled="perfData.disabled"
            :error="isValidatePerf && !perfData.info.duration"
            :selectProps="{class: '!w-20'}"
            :inputProps="durationInputProps"
            @change="handleDurationChange" />
          <span class="w-8"></span>
        </div>
      </FormItem>
      <FormItem :label="t('xcan_indicator.rampUpConcurrency')">
        <div class="flex items-center">
          <Input
            v-model:value="perfData.info.rampUpThreads"
            :disabled="perfData.disabled"
            :allowClear="false"
            size="small"
            dataType="number" />
          <span class="w-8 text-center"></span>
        </div>
      </FormItem>
      <FormItem :label="t('xcan_indicator.rampUpDuration')">
        <div class="flex items-center">
          <ShortDuration
            v-model:value="perfData.info.rampUpInterval"
            :disabled="perfData.disabled"
            :selectProps="{class: '!w-20'}"
            :inputProps="rampUpIntervalInputProps" />
          <span class="w-8 text-center"></span>
        </div>
      </FormItem>
      <FormItem :label="t('xcan_indicator.responseTime')">
        <ResponseTime
          v-model:percentile="perfData.info.percentile"
          v-model:art="perfData.info.art"
          :disabled="perfData.disabled" />
      </FormItem>
      <FormItem :label="t('xcan_indicator.transactionsPerSecond')" required>
        <div class="flex items-center">
          <span class="pr-3">>=</span>
          <Input
            v-model:value="perfData.info.tps"
            :disabled="perfData.disabled"
            :error="isValidatePerf && !perfData.info.tps"
            :allowClear="false"
            dataType="number"
            size="small" />
          <span class="w-8"></span>
        </div>
      </FormItem>
      <FormItem :label="t('xcan_indicator.errorRate')" required>
        <div class="flex items-center">
          <span class="pr-3">&lt;=</span>
          <Input
            v-model:value="perfData.info.errorRate"
            :disabled="perfData.disabled"
            :error="isValidatePerf && !perfData.info.errorRate"
            :allowClear="false"
            size="small"
            dataType="float"
            :decimalPoint="10" />
          <span class="w-8 text-center">%</span>
        </div>
      </FormItem>
      <div class="mt-5">
        <FormItem v-if="perfData && perfData.disabled">
          <Button
            class="rounded text-3 leading-3 size-sm"
            type="primary"
            :disabled="disabled"
            @click="edit">
            {{ t('xcan_indicator.modifyIndicator') }}
          </Button>
          <Button
            class="rounded text-3 leading-3 size-sm ml-2"
            type="primary"
            :disabled="disabled"
            @click="delCurrent">
            {{ t('xcan_indicator.useDefaultIndicator') }}
          </Button>
        </FormItem>
        <FormItem v-if="!perfData.disabled">
          <Button
            class="mr-2 rounded text-3 leading-3 size-sm"
            @click="cancel">
            {{ t('actions.cancel') }}
          </Button>
          <Button
            class="rounded text-3 leading-3 size-sm"
            type="primary"
            @click="putPerf">
            {{ t('xcan_indicator.submitModification') }}
          </Button>
        </FormItem>
      </div>
    </Form>
    <Form
      v-else-if="indicatorType === 'stability'"
      layout="vertical"
      disabled>
      <Hints
        v-for="text in tipsConfig.stabilityHints"
        :key="text"
        :text="text"
        class="mb-2" />
      <div class="flex items-center space-x-2 text-3 my-2">
        <Switch
          :checked="state.enableds.includes('STABILITY')"
          size="small"
          @change="changeTest($event, 'STABILITY')" />
        <span>{{ t('xcan_indicator.enableTest') }}</span>
        <Tooltip placement="top">
          <template #title>
            {{ t('xcan_indicator.enableTestTooltip') }}
          </template>
          <Icon
            icon="icon-tishi1"
            class="text-blue-badge text-3.5" />
        </Tooltip>
      </div>
      <FormItem :label="t('xcan_indicator.concurrency')" required>
        <div class="flex items-center">
          <Input
            v-model:value="stability.info.threads"
            :error="isValidateStability && !stability.info.threads"
            dataType="number"
            :disabled="stability.disabled"
            :allowClear="false"
            size="small" />
          <span class="w-8"></span>
        </div>
      </FormItem>
      <FormItem :label="t('xcan_indicator.testDuration')" required>
        <div class="flex items-center">
          <ShortDuration
            :selectProps="{class: '!w-20'}"
            :disabled="stability.disabled"
            :error="isValidateStability && !stability.info.duration"
            :value="stability.info.duration"
            :inputProps="durationInputProps"
            @change="handleDurationChange" />
          <span class="w-8"></span>
        </div>
      </FormItem>
      <FormItem :label="t('xcan_indicator.responseTime')">
        <ResponseTime
          v-model:percentile="stability.info.percentile"
          v-model:art="stability.info.art"
          :disabled="stability.disabled" />
      </FormItem>
      <FormItem :label="t('xcan_indicator.errorRate')" required>
        <div class="flex items-center">
          <span class="w-7">&lt;=</span>
          <Input
            v-model:value="stability.info.errorRate"
            dataType="float"
            :error="isValidateStability && !stability.info.errorRate"
            :disabled="stability.disabled"
            :allowClear="false"
            :decimalPoint="10"
            size="small" />
          <span class="w-8 text-center">%</span>
        </div>
      </FormItem>
      <FormItem :label="t('xcan_indicator.transactionsPerSecond')" required>
        <div class="flex items-center">
          <span class="w-7">>=</span>
          <Input
            v-model:value="stability.info.tps"
            :disabled="stability.disabled"
            :allowClear="false"
            :error="isValidateStability && !stability.info.tps"
            size="small"
            dataType="number" />
          <span class="w-8 text-center"></span>
        </div>
      </FormItem>
      <FormItem :label="t('xcan_indicator.applicationSystemAverageLoad')">
        <div class="text-3 flex items-center mb-2 ">
          <span class="pr-2">{{ t('xcan_indicator.cpuUsage') }}&nbsp;&lt;=</span>
          <div class="flex items-center flex-1">
            <Input
              v-model:value="stability.info.cpu"
              size="small"
              dataType="float"
              class="rounded-border"
              :disabled="stability.disabled" />
            <span class="w-8 text-center">%</span>
          </div>
        </div>
        <div class="text-3 flex items-center mb-2">
          <span class="pr-2">{{ t('xcan_indicator.memoryUsage') }}&nbsp;&lt;=</span>
          <div class="flex items-center flex-1">
            <Input
              v-model:value="stability.info.memory"
              size="small"
              dataType="float"
              class="rounded-border"
              :disabled="stability.disabled" />
            <span class="w-8 text-center">%</span>
          </div>
        </div>
        <div class="text-3 flex items-center mb-2 ">
          <span class="pr-2">{{ t('xcan_indicator.diskUsage') }}&nbsp;&lt;=</span>
          <div class="flex items-center flex-1">
            <Input
              v-model:value="stability.info.disk"
              size="small"
              dataType="float"
              class="rounded-border"
              :disabled="stability.disabled" />
            <span class="w-8 text-center">%</span>
          </div>
        </div>
        <div class="text-3 flex items-center mb-2">
          <span class="pr-2">{{ t('xcan_indicator.networkUsage') }}&nbsp;&lt;=</span>
          <div class="flex items-center flex-1">
            <Input
              v-model:value="stability.info.network"
              size="small"
              :max="10000"
              :decimalPoint="2"
              dataType="float"
              class="rounded-border"
              :disabled="stability.disabled" />
            <span class="w-8 text-center">MB</span>
          </div>
        </div>
      </FormItem>
      <div class="mt-5">
        <FormItem v-if="stability && stability.disabled">
          <Button
            class="rounded text-3 leading-3 size-sm"
            type="primary"
            :disabled="disabled"
            @click="edit">
            {{ t('xcan_indicator.modifyIndicator') }}
          </Button>
          <Button
            class="rounded text-3 leading-3 size-sm ml-2"
            type="primary"
            :disabled="disabled"
            @click="delCurrent">
            {{ t('xcan_indicator.useDefaultIndicator') }}
          </Button>
        </FormItem>
        <FormItem v-if="!stability.disabled">
          <Button
            class="mr-2 rounded text-3 leading-3 size-sm"
            @click="cancel">
            {{ t('actions.cancel') }}
          </Button>
          <Button
            class="rounded text-3 leading-3 size-sm"
            type="primary"
            @click="putPerf">
            {{ t('xcan_indicator.submitModification') }}
          </Button>
        </FormItem>
      </div>
    </Form>
    <Form
      v-else-if="indicatorType === 'func'"
      layout="vertical"
      disabled>
      <Hints
        v-for="text in tipsConfig.funcHints"
        :key="text"
        :text="text"
        class="mb-2" />
      <div class="flex items-center space-x-2 text-3 my-2">
        <Switch
          :checked="state.enableds.includes('FUNCTIONAL')"
          size="small"
          @change="changeTest($event, 'FUNCTIONAL')" />
        <span>{{ t('xcan_indicator.enableTest') }}</span>
        <Tooltip placement="top">
          <template #title>
            {{ t('xcan_indicator.enableTestTooltip') }}
          </template>
          <Icon
            icon="icon-tishi1"
            class="text-blue-badge text-3.5" />
        </Tooltip>
      </div>
      <template v-if="props.type === 'API'">
        <div class="space-x-2 flex items-center">
          <Icon icon="icon-maoyanceshi" />
          <span class="title-normal">{{ t('xcan_indicator.smokeTest') }}</span>
          <Switch
            v-model:checked="funcData.info.smoke"
            :disabled="funcData.disabled"
            size="small" />
        </div>

        <div class="my-2 text-sub-content">{{ t('xcan_indicator.smokeTestDescription') }}</div>
        <RadioGroup
          v-model:value="funcData.info.smokeCheckSetting"
          :options="smokeEnumOpt"
          :disabled="funcData.disabled"
          class="flex flex-col space-y-1" />

        <div class="mt-2">
          <IndicatorAssert
            v-if="funcData.info.smokeCheckSetting === 'USER_DEFINED_ASSERTION'"
            ref="smokeAssertRef"
            class="max-w-100"
            :value="funcData.info.userDefinedSmokeAssertion"
            :viewType="funcData.disabled" />
        </div>

        <div class="mt-4 space-x-2 flex items-center">
          <Icon icon="icon-anquanceshi" />
          <span class="title-normal ">{{ t('xcan_indicator.securityTest') }}</span>
          <Switch
            v-model:checked="funcData.info.security"
            :disabled="funcData.disabled"
            size="small" />
        </div>

        <div class="my-2 text-sub-content">{{ t('xcan_indicator.securityTestDescription') }}</div>
        <RadioGroup
          v-model:value="funcData.info.securityCheckSetting"
          :options="SecurityEnumOpt"
          :disabled="funcData.disabled"
          class="flex flex-col space-y-1" />

        <div>
          <IndicatorAssert
            v-if="funcData.info.securityCheckSetting === 'USER_DEFINED_ASSERTION'"
            ref="securityAssertRef"
            class="max-w-100"
            :value="funcData.info.userDefinedSecurityAssertion"
            :viewType="funcData.disabled" />
        </div>

        <div class="mt-5">
          <FormItem v-if="funcData && funcData.disabled">
            <Button
              class="rounded text-3 leading-3 size-sm"
              type="primary"
              :disabled="disabled"
              @click="edit">
              {{ t('xcan_indicator.modifyIndicator') }}
            </Button>
            <Button
              class="rounded text-3 leading-3 size-sm ml-2"
              type="primary"
              :disabled="disabled"
              @click="delCurrent">
              {{ t('xcan_indicator.useDefaultIndicator') }}
            </Button>
          </FormItem>
          <FormItem v-if="!funcData.disabled">
            <Button
              class="mr-2 rounded text-3 leading-3 size-sm"
              @click="cancel">
              {{ t('actions.cancel') }}
            </Button>
            <Button
              class="rounded text-3 leading-3 size-sm"
              type="primary"
              @click="putPerf">
              {{ t('xcan_indicator.submitModification') }}
            </Button>
          </FormItem>
        </div>
      </template>
    </Form>
  </div>
</template>

<style scoped>

.h-full-12 {
  height: calc(100% - 12px);
}

:deep(.ant-input-group-wrapper .ant-input-group-addon) {
  @apply bg-transparent border-none leading-7;
}

:deep(.hints-text > svg) {
  visibility: hidden;
}

.rounded-border :deep(.ant-input-group-addon) {
  @apply h-7 leading-7;
}

button.size-sm {
  @apply h-7 text-3 leading-3;
}

.input-select-container > :deep(.ant-select) {
  @apply h-6.5;
}

:deep(.rounded-border.ant-input-group-wrapper .ant-input-group .ant-input-affix-wrapper) {
  @apply !rounded;
}

:deep(.ant-radio-disabled+span) {
 color: rgb(82, 90, 101)
}

</style>
