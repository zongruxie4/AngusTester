<script lang="ts" setup>
import { Icon } from '@xcan-angus/vue-ui';
import TespPng from './Image/step.png';

const listData = [
  {
    type: 'step',
    text: '建立管理组织开发和测试活动的工作流程基本单元。为团队提供了一个集中化方式来计划、跟踪、报告和验收工作进展。',
    title: '添加项目',
    icon: 'icon-chuangjianxiangmu'
  },
  {
    type: 'icon',
    icon: 'icon-zhankaijiantou1'
  },
  {
    type: 'step',
    text: '明确开发和测试活动的负责人、时间、目标、验收标准等。帮助团队有组织地开展活动，保证软件产品的质量和可靠性。',
    title: '制定迭代和测试计划',
    icon: 'icon-jihua1'
  },
  {
    type: 'icon',
    icon: 'icon-zhankaijiantou1'
  },
  {
    type: 'step',
    text: '按照计划开展开发和测试活动。随时跟踪和管理项目中的工作任务、缺陷、需求、用户故事、测试等工作项。',
    title: '开展开发和测试活动',
    icon: 'icon-ceshirenwu'
  },
  {
    type: 'icon',
    icon: 'icon-zhankaijiantou1'
  },
  {
    type: 'step',
    text: '总结研发活动的各个方面，包括项目进展、效能分析、数据资产、测试结果等。为项目的成功交付提供有力支持。',
    title: '报告结果',
    icon: 'icon-dingdanmokuai'
  }
];

</script>
<template>
  <div>
    <div class="text-3.5 font-semibold mb-2.5">项目流程</div>
    <div class="flex items-center">
      <div v-for="(item, idx) in listData" :key="idx">
        <div v-if="item.type === 'step'" class="p-4 border rounded border-blue-border bg-blue-bg4">
          <div class="font-semibold mb-2 flex items-center space-x-2"><Icon :icon="item.icon" class="text-4" /><span>{{ item.title }}</span></div>
          <div>{{ item.text }}</div>
        </div>
        <div v-else class="w-20 text-center flex justify-center">
          <!-- <Icon :icon="item.icon" class="text-10 text-blue-tips" /> -->
          <img :src="TespPng" class="w-12" />
        </div>
      </div>
    </div>
  </div>
</template>
