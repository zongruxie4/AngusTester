<script lang="ts" setup>
import { defineAsyncComponent, onMounted, ref, watch } from 'vue';

interface Props {
  analysisInfo?: Record<string, any>;
}

const props = withDefaults(defineProps<Props>(), {
  analysisInfo: undefined
});

const Echart = defineAsyncComponent(() => import('./echart.vue'));

const totalValue = ref({

});

const personValues = ref([]);

onMounted(() => {
  watch(() => props.analysisInfo, (newValue) => {
    if (newValue) {
      const sourceData = newValue.data?.totalOverview || {};
      const { completedNum = 0, completedRate = 0, completedWorkload = 0, completedWorkloadRate = 0, evalWorkload = 0, totalNum = 0 } = sourceData;
      totalValue.value.value0 = [{ name: '未完成', value: totalNum - completedNum }, { name: '已完成', value: completedNum }];
      totalValue.value.value1 = [{ name: '未完成', value: evalWorkload - completedWorkload }, { name: '已完成', value: completedWorkload }];

      totalValue.value.title0 = completedRate + '%';
      totalValue.value.title1 = completedWorkloadRate + '%';

      if (newValue?.containsUserAnalysis) {
        const sourceData = newValue.data?.assigneesOverview || {};
        const assignees = newValue.data?.assignees || [];
        Object.keys(sourceData).forEach(userId => {
          const viewData = sourceData[userId] || {};
          const { completedNum = 0, completedRate = 0, completedWorkload = 0, completedWorkloadRate = 0, evalWorkload = 0, totalNum = 0 } = viewData;
          const chartData = {
            value0: [{ name: '未完成', value: totalNum - completedNum }, { name: '已完成', value: completedNum }],
            value1: [{ name: '未完成', value: evalWorkload - completedWorkload }, { name: '已完成', value: completedWorkload }],
            title0: completedRate + '%',
            title1: completedWorkloadRate + '%'
          };

          personValues.value.push({
            userName: assignees[userId]?.fullName,
            chartData,
            id: userId
          });
        });
      }
    } else {
      totalValue.value = {};
      personValues.value = [];
    }
  }, {
    immediate: true,
    deep: true
  });
});

const totalChartRef = ref();
const chartListRef = [];
defineExpose({
  resize: () => {
    totalChartRef.value.resize();
    chartListRef.forEach(item => {
      item.resize();
    });
  }
});

</script>
<template>
  <div>
    <div class="font-semibold pl-3">总共</div>
    <Echart ref="totalChartRef" v-bind="totalValue" />
  </div>

  <div
    v-for="item in personValues"
    :key="item.id"
    class="mt-5">
    <div class="font-semibold pl-3">{{ item.userName }}</div>
    <Echart ref="chartListRef" v-bind="item.chartData" />
  </div>
</template>
