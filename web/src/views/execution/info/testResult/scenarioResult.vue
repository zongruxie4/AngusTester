<script setup lang="ts">
import { computed, defineAsyncComponent } from 'vue';
import { Colon, Icon } from '@xcan-angus/vue-ui';

interface Props {
  value: {[key:string]:any};
  largePageLayout: boolean;
}

const props = withDefaults(defineProps<Props>(), {
  value: () => ({}),
  largePageLayout: true
});

const PerfResult = defineAsyncComponent(() => import('./perfResult.vue'));
const StabilityResult = defineAsyncComponent(() => import('./stabilityResult.vue'));

const configInfo = [
  [{ label: '总共', dataIndex: 'totalNum' },
    { label: '成功', dataIndex: 'successNum' },
    { label: '失败', dataIndex: 'failNum' },
    { label: '未启用', dataIndex: 'disabledNum' }
  ]
];

const dataSource = computed(() => {
  return props.value;
});

const scriptSourceId = computed(() => dataSource.value?.scriptSourceId);
const scriptSourceName = computed(() => dataSource.value?.scriptSourceName);
const passed = computed(() => dataSource.value?.passed);
const failureMessage = computed(() => dataSource.value?.failureMessage);
const testNum = computed(() => +(dataSource.value?.testNum || 0));
const testFailureNum = computed(() => +(dataSource.value?.testFailureNum || 0));
const onePassText = computed(() => {
  if (testNum.value <= 0) {
    return '--';
  }

  return testFailureNum.value === 0 ? '是' : '否';
});
</script>
<template>
  <div class="text-3 leading-5">
    <div class="font-semibold mb-2">基本信息</div>
    <div v-if="props.largePageLayout===false" class="space-y-2.5">
      <div class="flex items-start space-x-5">
        <div class="relative w-1/2 flex items-start">
          <div class="w-18.5 flex items-center whitespace-nowrap flex-shrink-0">
            <span>场景ID</span>
            <Colon class="w-1" />
          </div>

          <div class="whitespace-pre-wrap break-words break-all">{{ scriptSourceId }}</div>
        </div>

        <div class="relative w-1/2 flex items-start">
          <div class="w-15.5 flex items-center whitespace-nowrap flex-shrink-0">
            <span>场景名称</span>
            <Colon class="w-1" />
          </div>

          <div class="whitespace-pre-wrap break-words break-all">{{ scriptSourceName }}</div>
        </div>
      </div>

      <div class="flex items-start space-x-5">
        <div class="relative w-1/2 flex items-start">
          <div class="w-18.5 flex items-center whitespace-nowrap flex-shrink-0">
            <span>测试结果</span>
            <Colon class="w-1" />
          </div>

          <div class="flex items-center">
            <template v-if="passed">
              <Icon icon="icon-duihao" class="mr-1 text-status-success" />
              <span>通过</span>
            </template>

            <template v-else>
              <Icon icon="icon-chahao" class="mr-1 text-status-error" />
              <span>不通过</span>
            </template>
          </div>
        </div>

        <div class="relative w-1/2 flex items-start">
          <div class="w-15.5 flex items-center whitespace-nowrap flex-shrink-0">
            <span>失败原因</span>
            <Colon class="w-1" />
          </div>

          <div class="whitespace-pre-wrap break-words break-all">{{ failureMessage }}</div>
        </div>
      </div>

      <div class="flex items-start space-x-5">
        <div class="relative w-1/2 flex items-start">
          <div class="w-18.5 flex items-center whitespace-nowrap flex-shrink-0">
            <span>测试次数</span>
            <Colon class="w-1" />
          </div>

          <div class="whitespace-pre-wrap break-words break-all">{{ testNum }}</div>
        </div>

        <div class="relative w-1/2 flex items-start">
          <div class="w-15.5 flex items-center whitespace-nowrap flex-shrink-0">
            <span>失败次数</span>
            <Colon class="w-1" />
          </div>

          <div class="whitespace-pre-wrap break-words break-all">{{ testFailureNum }}</div>
        </div>
      </div>

      <div class="flex items-start space-x-5">
        <div class="relative w-1/2 flex items-start">
          <div class="w-18.5 flex items-center whitespace-nowrap flex-shrink-0">
            <span>一次性通过</span>
            <Colon class="w-1" />
          </div>

          <div class="whitespace-pre-wrap break-words break-all">{{ onePassText }}</div>
        </div>
      </div>
    </div>

    <div v-else-if="props.largePageLayout===true" class="space-y-2.5">
      <div class="flex items-start space-x-5">
        <div class="relative w-1/3 flex items-start">
          <div class="w-18.5 flex items-center whitespace-nowrap flex-shrink-0">
            <span>场景ID</span>
            <Colon class="w-1" />
          </div>

          <div class="whitespace-pre-wrap break-words break-all">{{ scriptSourceId }}</div>
        </div>

        <div class="relative w-1/3 flex items-start">
          <div class="w-15.5 flex items-center whitespace-nowrap flex-shrink-0">
            <span>测试结果</span>
            <Colon class="w-1" />
          </div>

          <div class="flex items-center">
            <template v-if="passed">
              <Icon icon="icon-duihao" class="mr-1 text-status-success" />
              <span>通过</span>
            </template>

            <template v-else>
              <Icon icon="icon-chahao" class="mr-1 text-status-error" />
              <span>不通过</span>
            </template>
          </div>
        </div>

        <div class="relative w-1/3 flex items-start">
          <div class="w-15.5 flex items-center whitespace-nowrap flex-shrink-0">
            <span>测试次数</span>
            <Colon class="w-1" />
          </div>

          <div class="whitespace-pre-wrap break-words break-all">{{ testNum }}</div>
        </div>
      </div>

      <div class="flex items-start space-x-5">
        <div class="relative w-1/3 flex items-start">
          <div class="w-18.5 flex items-center whitespace-nowrap flex-shrink-0">
            <span>场景名称</span>
            <Colon class="w-1" />
          </div>

          <div class="whitespace-pre-wrap break-words break-all">{{ scriptSourceName }}</div>
        </div>

        <div class="relative w-1/3 flex items-start">
          <div class="w-15.5 flex items-center whitespace-nowrap flex-shrink-0">
            <span>失败原因</span>
            <Colon class="w-1" />
          </div>

          <div class="whitespace-pre-wrap break-words break-all">{{ failureMessage }}</div>
        </div>

        <div class="relative w-1/3 flex items-start">
          <div class="w-15.5 flex items-center whitespace-nowrap flex-shrink-0">
            <span>失败次数</span>
            <Colon class="w-1" />
          </div>

          <div class="whitespace-pre-wrap break-words break-all">{{ testFailureNum }}</div>
        </div>
      </div>

      <div class="flex items-start space-x-5">
        <div class="relative w-1/3 flex items-start">
          <div class="w-18.5 flex items-center whitespace-nowrap flex-shrink-0">
            <span>一次性通过</span>
            <Colon class="w-1" />
          </div>

          <div class="whitespace-pre-wrap break-words break-all">{{ onePassText }}</div>
        </div>
      </div>
    </div>

    <div class="mt-5 mb-2 font-semibold">
      测试场景
    </div>

    <div class="space-y-2">
      <li
        v-for="(line, idx) in configInfo"
        :key="idx"
        class="flex space-x-2">
        <div
          v-for="item in line"
          :key="item.dataIndex"
          class="flex h-7 leading-7  w-30">
          <span v-if="item.label" class="flex-1 text-white bg-blue-6 px-2 rounded">{{ item.label }}</span>
          <span v-if="item.dataIndex" class="flex-1 bg-gray-light px-2 rounded-r">{{
            dataSource.targetSummary?.[item.dataIndex] }}</span>
        </div>
      </li>
    </div>

    <template v-if="dataSource.scriptType?.value === 'TEST_PERFORMANCE'">
      <div class="font-semibold mt-5 mb-2">结果信息</div>
      <PerfResult :indicatorPerf="dataSource.indicatorPerf" :result="dataSource.sampleSummary" />
    </template>

    <template v-if="dataSource.scriptType?.value === 'TEST_STABILITY'">
      <div class="font-semibold mt-5 mb-2">结果信息</div>
      <StabilityResult :indicatorStability="dataSource.indicatorStability" :result="dataSource.sampleSummary" />
    </template>
  </div>
</template>

<style scoped>
.w-1\/2 {
  width: calc((100% - 20px)/2);
}

.w-1\/3 {
  width: calc((100% - 40px)/3);
}
</style>
