<script lang="ts" setup>
import { useI18n } from 'vue-i18n';
import FlowPng from '@/views/project/project/list/introduce/images/step.png';
import { Icon } from '@xcan-angus/vue-ui';

const { t } = useI18n();

const processData = [
  {
    type: 'step',
    text: t('project.addProjectStep.text'),
    title: t('project.addProjectStep.title'),
    icon: 'icon-chuangjianxiangmu'
  },
  {
    type: 'icon',
    icon: 'icon-zhankaijiantou1'
  },
  {
    type: 'step',
    text: t('project.planIterationAndTest.text'),
    title: t('project.planIterationAndTest.title'),
    icon: 'icon-jihua1'
  },
  {
    type: 'icon',
    icon: 'icon-zhankaijiantou1'
  },
  {
    type: 'step',
    text: t('project.developAndTest.text'),
    title: t('project.developAndTest.title'),
    icon: 'icon-ceshirenwu'
  },
  {
    type: 'icon',
    icon: 'icon-zhankaijiantou1'
  },
  {
    type: 'step',
    text: t('project.reportResults.text'),
    title: t('project.reportResults.title'),
    icon: 'icon-dingdanmokuai'
  }
];
</script>

<template>
  <div>
    <div class="text-3.5 font-semibold mb-2.5">{{ t('project.aboutProject') }}</div>
    <div class="mb-6">
      <div>{{ t('project.projectDescription') }}</div>
    </div>

    <div class="flex items-center">
      <div v-for="(item, idx) in processData" :key="idx">
        <div v-if="item.type === 'step'" class="p-4 border rounded border-blue-border bg-blue-bg4">
          <div class="font-semibold mb-2 flex items-center space-x-2"><Icon :icon="item.icon" class="text-4" /><span>{{ item.title }}</span></div>
          <div>{{ item.text }}</div>
        </div>
        <div v-else class="w-20 text-center flex justify-center">
          <!-- <Icon :icon="item.icon" class="text-10 text-blue-tips" /> -->
          <img :src="FlowPng" class="w-12" />
        </div>
      </div>
    </div>
  </div>
</template>
