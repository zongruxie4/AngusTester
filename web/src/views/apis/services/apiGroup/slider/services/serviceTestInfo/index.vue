<script lang="ts" setup>
import { ref, watch, onMounted, computed, defineAsyncComponent } from 'vue';
import { useI18n } from 'vue-i18n';
import { Hints } from '@xcan-angus/vue-ui';
import { services } from '@/api/tester';

interface Props {
  serviceId: string;
}

const { t } = useI18n();
const props = withDefaults(defineProps<Props>(), {
  serviceId: ''
});

const Chart = defineAsyncComponent(() => import('./chart.vue'));
const ServiceTestApis = defineAsyncComponent(() => import('./serviceTestApis.vue'));

const testResult = ref<{[key: string]: any}>({});

const testResultCount = computed(() => {
  return testResult.value?.testResultCount || {
    enabledTestNum: 0,
    testPassedNum: 0,
    testUnPassedNum: 0,
    testedNum: 0,
    unTestedNum: 0
  };
});

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

const progress = computed(() => {
  return testResult.value?.progress || {
    total: 0,
    completed: 0,
    completedRate: 0
  };
});

const testResultInfos = computed(() => {
  return testResult.value?.testResultInfos || [];
});

const progressValues = computed(() => {
  return [testResultCount.value.testedNum, testResultCount.value.unTestedNum, testResultCount.value.testPassedNum, testResultCount.value.testUnPassedNum];
});

const typeValues = computed(() => {
  return [testApis.value.enabledTestNum, testApis.value.enabledFuncTestNum, testApis.value.enabledPerfTestNum, testApis.value.enabledStabilityTestNum];
});

const loadResult = async () => {
  const [error, { data }] = await services.getTestResult(props.serviceId);
  if (error) {
    return;
  }
  testResult.value = data;
};

onMounted(() => {
  watch(() => props.serviceId, (newValue) => {
    if (newValue) {
      loadResult();
    }
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
    <ServiceTestApis
      class="flex-1 mt-2"
      :dataSource="testResultInfos"
      :enabledTestApiIds="testApis?.enabledTestApiIds" />
  </div>
</template>
