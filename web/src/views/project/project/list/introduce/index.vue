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

    <div class="flex items-center justify-center">
      <div v-for="(item, idx) in processData" :key="idx" class="flex items-center">
        <div v-if="item.type === 'step'" class="process-step">
          <div class="step-header">
            <div class="step-icon-wrapper">
              <Icon :icon="item.icon" class="step-icon" />
            </div>
            <div class="step-title">{{ item.title }}</div>
          </div>
          <div class="step-content">
            <div class="step-text">{{ item.text }}</div>
          </div>
        </div>
        <div v-else class="step-connector">
          <img :src="FlowPng" class="connector-icon" />
        </div>
      </div>
    </div>
  </div>
</template>

<style scoped>
.process-step {
  @apply relative flex flex-col items-start p-5 rounded-xl bg-gradient-to-br from-blue-50 to-indigo-50 border border-blue-200/60 shadow-sm hover:shadow-md transition-all duration-300;
  min-width: 200px;
  backdrop-filter: blur(8px);
}

.process-step:hover {
  @apply border-blue-300/80 shadow-lg;
  transform: translateY(-2px);
}

.step-header {
  @apply flex items-center mb-3 space-x-3 justify-start;
}

.step-icon-wrapper {
  @apply w-10 h-10 rounded-full bg-gradient-to-br from-blue-500 to-indigo-600 flex items-center justify-center shadow-md;
}

.step-icon {
  @apply text-white text-xl;
}

.step-title {
  @apply font-bold text-gray-800 text-sm;
}

.step-content {
  @apply text-left;
}

.step-text {
  @apply text-gray-600 leading-relaxed;
}

.step-connector {
  @apply mx-4 flex items-center justify-center;
}

.connector-icon {
  @apply w-20 h-10 opacity-60 transition-opacity duration-300;
}

.step-connector:hover .connector-icon {
  @apply opacity-80;
}

/* 响应式设计 */
@media (max-width: 768px) {
  .process-step {
    @apply min-w-0 w-full max-w-xs;
  }
  
  .step-connector {
    @apply mx-2;
  }
  
  .connector-icon {
    @apply w-8 h-8;
  }
}
</style>
