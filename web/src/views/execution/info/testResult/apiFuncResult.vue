<script setup lang="ts">
import { onMounted, ref, watch } from 'vue';
import { Icon } from '@xcan-angus/vue-ui';

interface Props {
  dataSource: {[key: string]: string}
}

const props = withDefaults(defineProps<Props>(), {
  dataSource: () => ({})
});

const configInfo = [
  [{ label: '总共', dataIndex: 'totalNum', bgColor: 'bg-blue-1' }, { label: '通过', dataIndex: 'successNum', bgColor: 'bg-status-success' }, { label: '未通过', dataIndex: 'failNum', bgColor: 'bg-status-error' }, { label: '未启用', dataIndex: 'disabledNum', bgColor: 'bg-gray-icon' }]
//   [],
];
const CaseTypeIconConfig = {
  SMOKE: 'icon-maoyanceshi',
  SECURITY: 'icon-anquanceshi',
  USER_DEFINED: 'icon-zidingyiceshi'
};

const statisticsData = ref({});
const caseResult = ref([]);
onMounted(() => {
  watch(() => props.dataSource, newValue => {
    statisticsData.value = newValue.caseSummary || {};
    caseResult.value = newValue.caseResults || [];
  }, {
    immediate: true
  });
});

</script>
<template>
  <div class="font-semibold mt-5 mb-2">测试用例</div>
  <div class="space-y-2 text-3">
    <div
      v-for="(line, idx) in configInfo"
      :key="idx"
      class="flex space-x-2">
      <div
        v-for="item in line"
        :key="item.dataIndex"
        class="flex h-6 leading-6 w-40">
        <span class="flex-1 text-white  px-2 rounded" :class="item.bgColor">{{ item.label }}</span>
        <span class="flex-1 bg-gray-light px-2 rounded-r">{{ statisticsData[item.dataIndex] }}</span>
      </div>
    </div>
    <div class="font-semibold mt-5 mb-2">已测试用例</div>
    <div class="text-3 space-y-1">
      <div
        v-for="item in caseResult"
        :key="item.id"
        class="border border-gray-light rounded bg-gray-light">
        <!-- <Tooltip placement="rightTop"> -->
        <!-- <template #title>
            <div class="max-w-100 overflow-y-auto">
              <Grid
                :dataSource="item.samplingSummary || item"
                :columns="columns" />
            </div>
          </template> -->
        <div class="px-1 flex h-6 items-center">
          <Icon :icon="CaseTypeIconConfig[item.caseType?.value] || 'icon-jiekouyongli2'" class="mr-1 text-4" />
          <span class="min-w-10 truncate flex-1" :title="item.apisName || item.caseName || item.summary">{{ item.apisName || item.caseName || item.summary }}</span>
          <span
            v-if="!item.enabled"
            class="px-2 rounded">未启用</span>
          <span
            v-else-if="item.passed"
            class="px-2 rounded text-status-success">通过</span>
          <span v-else class="px-2 rounded text-status-error ">
            未通过：{{ item.failureMessage }}
          </span>
        </div>
        <!-- </Tooltip> -->
      </div>
    </div>
  </div>
</template>
