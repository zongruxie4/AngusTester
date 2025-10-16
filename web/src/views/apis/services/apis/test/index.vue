<script lang="ts" setup>
import { ref, watch, onMounted, computed, defineAsyncComponent } from 'vue';
import { useI18n } from 'vue-i18n';
import { Hints } from '@xcan-angus/vue-ui';
import { services } from '@/api/tester';

// Lazy-load heavy sub-components to reduce initial bundle size
const Chart = defineAsyncComponent(() => import('./Chart.vue'));
const TestApis = defineAsyncComponent(() => import('./TestApis.vue'));

// Props provided by parent container
interface Props {
  serviceId: string;
}

const props = withDefaults(defineProps<Props>(), {
  serviceId: ''
});

const { t } = useI18n();

// Raw service test result payload
const testResult = ref<{[key: string]: any}>({});

// Aggregated counters for various test states
const testResultCount = computed(() => {
  return testResult.value?.testResultCount || {
    enabledTestNum: 0,
    testPassedNum: 0,
    testUnPassedNum: 0,
    testedNum: 0,
    unTestedNum: 0
  };
});

// Test APIs classification & enabling info
const testApis = computed(() => {
  return testResult.value?.testApis || {
    enabledFuncTestNum: 0,
    enabledPerfTestNum: 0,
    enabledStabilityTestNum: 0,
    enabledTestNum: 0,
    totalApisNum: 0,
    enabledTestApiIds: {}
  };
});

// Overall testing progress
const progress = computed(() => {
  return testResult.value?.progress || {
    total: 0,
    completed: 0,
    completedRate: 0
  };
});

// Flat list for results rendering
const testResultInfos = computed(() => {
  return testResult.value?.testResultInfos || [];
});

// Bar values for progress chart: [tested, untested, passed, unpassed]
const progressValues = computed(() => {
  return [
    testResultCount.value.testedNum,
    testResultCount.value.unTestedNum,
    testResultCount.value.testPassedNum,
    testResultCount.value.testUnPassedNum // fixed typo: estResultCount -> testResultCount
  ];
});

// Bar values for type chart: [total enabled, functional, performance, stability]
const typeValues = computed(() => {
  return [
    testApis.value.enabledTestNum,
    testApis.value.enabledFuncTestNum,
    testApis.value.enabledPerfTestNum,
    testApis.value.enabledStabilityTestNum
  ];
});

// Fetch latest test result for current service
const loadResult = async () => {
  const [error, { data }] = await services.getTestResult(props.serviceId);
  if (error) return;
  testResult.value = data;
};

// Auto load when serviceId becomes available/changes
onMounted(() => {
  watch(() => props.serviceId, (newValue) => {
    if (newValue) loadResult();
  }, { immediate: true });
});

</script>
<template>
  <div class="pr-4 text-3 h-full flex flex-col">
    <div class="rounded border border-blue-6 p-3 bg-blue-5">
      <Hints class="!text-text-title" :text="t('service.serviceTestDetail.hints.testEnableDescription')" />
      <div class="text-3 text-text-sub-content pl-4">
        <li>{{ t('service.serviceTestDetail.hints.singleApiEnable') }}</li>
        <li>{{ t('service.serviceTestDetail.hints.batchEnable') }}</li>
      </div>
    </div>

    <div class="flex mt-2 items-center">
      <div class="w-50 text-center space-y-2">
        <div>
          <div class="text-6 font-semibold">{{ progress.completedRate }}%</div>
          <span>{{ t('service.serviceTestDetail.stats.progressRate') }}</span>
        </div>
        <div class="flex items-center">
          <div class="flex-1">
            <div class="text-6  font-semibold">{{ progress.total }}</div>
            <span>{{ t('service.serviceTestDetail.stats.totalApis') }}</span>
          </div>
          <div class="flex-1">
            <div class="text-6  font-semibold">{{ testResultCount.enabledTestNum }}</div>
            <span>{{ t('service.serviceTestDetail.stats.enabledTestApis') }}</span>
          </div>
        </div>
      </div>

      <Chart :progressValues="progressValues" :typeValues="typeValues" />
    </div>

    <div class="text-text-title text-3.5 font-medium mt-5">{{ t('service.serviceTestDetail.title.testDetail') }}</div>
    <TestApis
      class="flex-1 mt-2"
      :dataSource="testResultInfos"
      :enabledTestApiIds="testApis?.enabledTestApiIds" />
  </div>
</template>
