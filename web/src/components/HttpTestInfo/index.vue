<script lang="ts" setup>
import { defineAsyncComponent, reactive, computed, watch, ref, onMounted, nextTick } from 'vue';
import { RadioGroup, RadioButton, Tabs, TabPane } from 'ant-design-vue';
import { Spin } from '@xcan-angus/vue-ui';
import { TESTER, http } from '@xcan-angus/infra';
import { useI18n } from 'vue-i18n';

const { t } = useI18n();
const ServiceTestCase = defineAsyncComponent(() => import('./ServiceTestCase.vue'));
const ServiceBasicInfo = defineAsyncComponent(() => import('./ServiceBasicInfo.vue'));
const ServiceProgress = defineAsyncComponent(() => import('./ServiceProgress.vue'));
const ServiceTestApis = defineAsyncComponent(() => import('./ServiceTestApis.vue'));

const ApiOrScenairoResult = defineAsyncComponent(() => import('./ApiOrScenairoResult.vue'));

type Type = 'API'|'SERVICE'|'SCENARIO';

interface Props {
  id: string,
  type: Type
}

const props = withDefaults(defineProps<Props>(), {
  id: '', // 235929348207544484
  type: 'API' // SERVICE
});

// eslint-disable-next-line func-call-spacing
const emit = defineEmits<{
  (e: 'rendered', value: true);
}>();

interface StateType {
  checked: 'TEST_FUNCTIONALITY' | 'TEST_PERFORMANCE' | 'TEST_STABILITY';
  info:Record<string, any>
}

const enums = computed(() => {
  return [
    { value: 'TEST_FUNCTIONALITY', message: t('xcan_httpTestInfo.functionalTest') },
    { value: 'TEST_PERFORMANCE', message: t('xcan_httpTestInfo.performanceTest') },
    { value: 'TEST_STABILITY', message: t('xcan_httpTestInfo.stabilityTest') }
  ].filter(Boolean);
});

const state:StateType = reactive({
  checked: 'TEST_FUNCTIONALITY',
  info: {}
});

const loading = ref(false);

const apiOrScenairoData = ref({});

// 接口测试信息
const loadApiTestInfo = async () => {
  loading.value = true;
  const [error, resp] = await http.get(`${TESTER}/apis/${props.id}/test/result/detail`);
  loading.value = false;
  // resetData();
  if (error) {
    apiOrScenairoData.value = {};
    // showEmpty.value = true;
    return;
  }
  apiOrScenairoData.value = resp.data || {};
};

// 场景测试信息
const loadScenarioTestInfo = async () => {
  loading.value = true;
  const [error, resp] = await http.get(`${TESTER}/scenario/${props.id}/test/result`);
  // resetData();
  loading.value = false;
  if (error) {
    apiOrScenairoData.value = {};
    return;
  }
  apiOrScenairoData.value = resp.data || {};
};

const projectTestResult = ref({});
const loadProjectTestInfo = async () => {
  loading.value = true;
  const [error, resp] = await http.get(`${TESTER}/services/${props.id}/test/result`);
  loading.value = false;
  // resetData();

  if (error) {
    projectTestResult.value = {};
    return;
  }
  projectTestResult.value = resp.data || {};
};

watch(() => props.id, newValue => {
  // resetData();
  if (newValue) {
    if (props.type === 'API') {
      loadApiTestInfo();
      return;
    }
    if (props.type === 'SCENARIO') {
      loadScenarioTestInfo();
      return;
    }
    if (props.type === 'SERVICE') {
      loadProjectTestInfo();
    }
  }
}, {
  immediate: true
});

onMounted(() => {
  nextTick(() => {
    emit('rendered', true);
  });
});
</script>

<template>
  <Spin
    :spinning="loading"
    class="h-full">
    <div class="pb-3" :class="{'flex flex-col h-full': props.type === 'SERVICE'}">
      <RadioGroup
        v-show="props.type !== 'SERVICE'"
        v-model:value="state.checked"
        size="small"
        class="test-group w-full"
        buttonStyle="solid">
        <RadioButton
          v-for="item in enums"
          :key="item.value"
          class="border-none text-3 text-center"
          :value="item.value">
          {{ item.message }}
        </RadioButton>
      </RadioGroup>
      <Tabs
        v-if="['API', 'SCENARIO'].includes(props.type)"
        :activeKey="state.checked"
        class="h-full flex-1 ghost-tab">
        <TabPane
          v-for="item in enums"
          :key="item.value"
          :tab="item.message">
          <ApiOrScenairoResult
            :type="props.type"
            :testType="item.value"
            :dataSource="apiOrScenairoData" />
        </TabPane>
      </Tabs>
      <template v-if="['SERVICE'].includes(props.type)">
        <ServiceBasicInfo :value="projectTestResult.progress" />
        <div class="text-3 mt-4 mb-1 font-semibold">
          {{ t('xcan_httpTestInfo.testInterface') }}
        </div>
        <ServiceTestCase :dataSource="projectTestResult.testApis" />
        <div class="text-3 mt-4 mb-1 font-semibold">
          {{ t('xcan_httpTestInfo.testStatistics') }}
        </div>
        <ServiceProgress :value="projectTestResult.testResultCount" />
        <div class="text-3 mt-4 mb-1 font-semibold">
          {{ t('xcan_httpTestInfo.serviceInterface') }}
        </div>
        <ServiceTestApis
          class="flex-1"
          :dataSource="projectTestResult?.testResultInfos"
          :enabledTestApiIds="projectTestResult?.testApis?.enabledTestApiIds" />
      </template>
    </div>
  </Spin>
</template>
<style scoped>
.test-group .ant-radio-button-wrapper:not(.ant-radio-button-wrapper-checked) {
  @apply bg-gray-light;
}

.test-group :deep(.ant-radio-button-wrapper::before) {
  @apply hidden;
}

.test-group .ant-radio-button-wrapper:first-child {
  @apply border-l-0;
}

.indicator-tabs > .ant-tabs-content-holder {
  width:
  100%;
}

.ghost-tab>:deep(.ant-tabs-nav) {
  display: none;
}
</style>
