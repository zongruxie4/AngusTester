<script lang="ts" setup>
import { ref, watch, onMounted, computed, defineAsyncComponent } from 'vue';
import { TESTER, http } from '@xcan-angus/tools';
import { Hints } from '@xcan-angus/vue-ui';

interface Props {
  serviceId: string;
}

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
  const [error, { data }] = await http.get(`${TESTER}/services/${props.serviceId}/test/result`);
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
      <Hints class="!text-text-title" text="只有接口测试被启用后，测试才会被标记为必须的活动，并且测试结果将包含在整体分析中。这确保了全面而精确的测试覆盖。您可以通过以下两种方式启用接口测试:" />
      <div class="text-3 text-text-sub-content pl-4">
        <li>1.单个接口启用：进入接口调试页面 -> 在右侧标签栏中选择"接口指标" -> 为所需的测试类型启用相应选项。</li>
        <li>2.批量启用：在服务列表中找到目标服务 -> 右键点击，选择"启用或禁用测试" -> 批量操作服务下的所有接口测试。</li>
      </div>
    </div>

    <div class="flex mt-2 items-center">
      <div class="w-50 text-center space-y-2">
        <div>
          <div class="text-6 font-semibold">{{ progress.completedRate }}%</div>
          <span>接口测试进度</span>
        </div>
        <div class="flex items-center">
          <div class="flex-1">
            <div class="text-6  font-semibold">{{ progress.total }}</div>
            <span>总接口数</span>
          </div>
          <div class="flex-1">
            <div class="text-6  font-semibold">{{ testResultCount.enabledTestNum }}</div>
            <span>开启测试接口数</span>
          </div>
        </div>
      </div>

      <Chart :progressValues="progressValues" :typeValues="typeValues" />
    </div>

    <div class="text-text-title text-3.5 font-medium mt-5">接口测试明细</div>
    <ServiceTestApis
      class="flex-1 mt-2"
      :dataSource="testResultInfos"
      :enabledTestApiIds="testApis?.enabledTestApiIds" />
  </div>
</template>
