<script lang="ts" setup>
// Vue core imports
import { defineAsyncComponent, reactive, computed, watch, ref, onMounted, nextTick } from 'vue';
import { useI18n } from 'vue-i18n';

// UI component imports
import { RadioGroup, RadioButton, Tabs, TabPane } from 'ant-design-vue';
import { Spin } from '@xcan-angus/vue-ui';

// Infrastructure imports
import { TESTER, http } from '@xcan-angus/infra';

const { t } = useI18n();

// Async component definitions
const ServiceTestCase = defineAsyncComponent(() => import('./ServiceTestCase.vue'));
const ServiceBasicInfo = defineAsyncComponent(() => import('./ServiceBasicInfo.vue'));
const ServiceProgress = defineAsyncComponent(() => import('./ServiceProgress.vue'));
const ServiceTestApis = defineAsyncComponent(() => import('./ServiceTestApis.vue'));
const ApiOrScenairoResult = defineAsyncComponent(() => import('./ApiOrScenairoResult.vue'));

// Type definitions
type TestType = 'API' | 'SERVICE' | 'SCENARIO';

/**
 * Component props interface for HTTP test information
 */
interface Props {
  id: string;
  type: TestType;
}

// Component props with default values
const props = withDefaults(defineProps<Props>(), {
  id: '',
  type: 'API'
});

// Component events
const emit = defineEmits<{
  (e: 'rendered', value: true);
}>();

/**
 * Component state interface for test type selection
 */
interface ComponentState {
  checked: 'TEST_FUNCTIONALITY' | 'TEST_PERFORMANCE' | 'TEST_STABILITY';
  info: Record<string, any>;
}

/**
 * Computed property for test type options
 */
const testTypeOptions = computed(() => {
  return [
    { value: 'TEST_FUNCTIONALITY', message: t('xcan_httpTestInfo.functionalTest') },
    { value: 'TEST_PERFORMANCE', message: t('xcan_httpTestInfo.performanceTest') },
    { value: 'TEST_STABILITY', message: t('xcan_httpTestInfo.stabilityTest') }
  ].filter(Boolean);
});

// Component state management
const componentState: ComponentState = reactive({
  checked: 'TEST_FUNCTIONALITY',
  info: {}
});

// Loading and data state
const isLoading = ref(false);
const apiOrScenarioTestData = ref<any>({});

/**
 * Load API test information from server
 */
const loadApiTestInformation = async () => {
  isLoading.value = true;
  const [error, response] = await http.get(`${TESTER}/apis/${props.id}/test/result/detail`);
  isLoading.value = false;
  if (error) {
    apiOrScenarioTestData.value = {};
    return;
  }
  apiOrScenarioTestData.value = response.data || {};
};

/**
 * Load scenario test information from server
 */
const loadScenarioTestInformation = async () => {
  isLoading.value = true;
  const [error, response] = await http.get(`${TESTER}/scenario/${props.id}/test/result`);
  isLoading.value = false;
  if (error) {
    apiOrScenarioTestData.value = {};
    return;
  }
  apiOrScenarioTestData.value = response.data || {};
};

// Project test result data
const projectTestResultData = ref<any>({});

/**
 * Load project test information from server
 */
const loadProjectTestInformation = async () => {
  isLoading.value = true;
  const [error, response] = await http.get(`${TESTER}/services/${props.id}/test/result`);
  isLoading.value = false;
  if (error) {
    projectTestResultData.value = {};
    return;
  }
  projectTestResultData.value = response.data || {};
};

// Component watchers
watch(() => props.id, (newId: string) => {
  if (newId) {
    if (props.type === 'API') {
      loadApiTestInformation();
      return;
    }
    if (props.type === 'SCENARIO') {
      loadScenarioTestInformation();
      return;
    }
    if (props.type === 'SERVICE') {
      loadProjectTestInformation();
    }
  }
}, {
  immediate: true
});

// Component lifecycle hooks
onMounted(() => {
  nextTick(() => {
    emit('rendered', true);
  });
});
</script>

<template>
  <Spin
    :spinning="isLoading"
    class="h-full">
    <div class="pb-3" :class="{'flex flex-col h-full': props.type === 'SERVICE'}">
      <RadioGroup
        v-show="props.type !== 'SERVICE'"
        v-model:value="componentState.checked"
        size="small"
        class="test-group w-full"
        buttonStyle="solid">
        <RadioButton
          v-for="item in testTypeOptions"
          :key="item.value"
          class="border-none text-3 text-center"
          :value="item.value">
          {{ item.message }}
        </RadioButton>
      </RadioGroup>
      <Tabs
        v-if="['API', 'SCENARIO'].includes(props.type)"
        :activeKey="componentState.checked"
        class="h-full flex-1 ghost-tab">
        <TabPane
          v-for="item in testTypeOptions"
          :key="item.value"
          :tab="item.message">
          <ApiOrScenairoResult
            :type="props.type as 'API' | 'SCENARIO'"
            :testType="item.value as 'TEST_FUNCTIONALITY' | 'TEST_PERFORMANCE' | 'TEST_STABILITY'"
            :dataSource="apiOrScenarioTestData" />
        </TabPane>
      </Tabs>
      <template v-if="['SERVICE'].includes(props.type)">
        <ServiceBasicInfo :value="projectTestResultData?.progress" />
        <div class="text-3 mt-4 mb-1 font-semibold">
          {{ t('xcan_httpTestInfo.testInterface') }}
        </div>
        <ServiceTestCase :dataSource="projectTestResultData?.testApis" />
        <div class="text-3 mt-4 mb-1 font-semibold">
          {{ t('xcan_httpTestInfo.testStatistics') }}
        </div>
        <ServiceProgress :value="projectTestResultData?.testResultCount" />
        <div class="text-3 mt-4 mb-1 font-semibold">
          {{ t('xcan_httpTestInfo.serviceInterface') }}
        </div>
        <ServiceTestApis
          class="flex-1"
          :dataSource="projectTestResultData?.testResultInfos"
          :enabledTestApiIds="projectTestResultData?.testApis?.enabledTestApiIds" />
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
