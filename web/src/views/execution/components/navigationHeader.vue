<script lang="ts" setup>
import { defineAsyncComponent, ref } from 'vue';
import { Icon } from '@xcan-angus/vue-ui';

const PieChart = defineAsyncComponent(() => import('@/views/execution/components/pieChart/index.vue'));

const presentation = {
  title: '通过“执行”可以直接运行一个测试脚本或Mock数据脚本，当运行测试脚本时，可以通过执行配置中“更新测试结果”选项指定是否将执行测试结果更新到测试资源(场景、接口或用例)。',
  subTitle: '注意：“执行”配置比“脚本”配置优先级高；执行添加后只允许执行添加人、执行脚本添加人、应用管理员和系统管理员修改和运行，其他用户只允许查看。',
  content: {
    title: '在这里您和您的团队可以完成以下工作：',
    list: ['根据一个测试脚本发起测试或者根据一个Mock数据脚本生成测试数据；', '实时查看执行采样指标及对测试结果进行分析。']
  }
};

const countRef = ref();
const updateCount = () => {
  countRef.value.loadCount();
};

defineExpose({
  updateCount
});
</script>
<template>
  <div class="bg-gray-2 px-7.5 flex items-center rounded text-3 justify-between">
    <div class="flex content items-center">
      <img
        class="mr-7.75"
        src="@/assets/home/exec.svg"
        style="width: 180px;height: 180px;" />
      <div class="ml-3.25 text-content">
        <div>{{ presentation.title }}</div>
        <div>{{ presentation.subTitle }}</div>
        <div class="mt-2">{{ presentation.content.title }}</div>
        <div
          v-for="(item,index) in presentation.content.list"
          :key="index"
          class="mt-1">
          <Icon icon="icon-duihaolv" class="mr-2.5" />{{ item }}
        </div>
      </div>
    </div>
    <PieChart ref="countRef" class="ml-10" />
  </div>
</template>
