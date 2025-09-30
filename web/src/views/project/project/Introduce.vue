<script lang="ts" setup>
// Vue composition API imports
import { useI18n } from 'vue-i18n';

// Custom UI components
import { Icon } from '@xcan-angus/vue-ui';

// Static assets
import StepImage from '@/views/project/project/images/step.png';

// Initialize i18n
const { t } = useI18n();

// Process workflow configuration
const stepData = [
  {
    type: 'step',
    text: t('project.introduce.steps.addProject.text'),
    title: t('project.introduce.steps.addProject.title'),
    icon: 'icon-chuangjianxiangmu'
  },
  {
    type: 'icon',
    icon: 'icon-zhankaijiantou1'
  },
  {
    type: 'step',
    text: t('project.introduce.steps.sprintAndPlan.text'),
    title: t('project.introduce.steps.sprintAndPlan.title'),
    icon: 'icon-jihua1'
  },
  {
    type: 'icon',
    icon: 'icon-zhankaijiantou1'
  },
  {
    type: 'step',
    text: t('project.introduce.steps.developAndTest.text'),
    title: t('project.introduce.steps.developAndTest.title'),
    icon: 'icon-ceshirenwu'
  },
  {
    type: 'icon',
    icon: 'icon-zhankaijiantou1'
  },
  {
    type: 'step',
    text: t('project.introduce.steps.reportResults.text'),
    title: t('project.introduce.steps.reportResults.title'),
    icon: 'icon-dingdanmokuai'
  }
];
</script>

<template>
  <div>
    <div class="text-3.5 font-semibold mb-1.5">
      {{ t('project.introduce.aboutProject') }}
    </div>
    <div class="mb-6 text-3.5 font-serif">
      <div>{{ t('project.introduce.description') }}</div>
    </div>

    <div class="flex items-center justify-center font-serif">
      <div
        v-for="(item, idx) in stepData"
        :key="idx"
        class="flex items-center">
        <div v-if="item.type === 'step'" class="process-step">
          <div class="step-header">
            <div class="step-icon-wrapper">
              <Icon :icon="item.icon" class="step-icon" />
            </div>
            <div class="step-title">{{ item.title }}</div>
          </div>
          <div class="step-content pl-2">
            <div class="step-text text-3.5">{{ item.text }}</div>
          </div>
        </div>
        <div v-else class="step-connector">
          <img :src="StepImage" class="connector-icon" />
        </div>
      </div>
    </div>
  </div>
</template>

<style scoped>
.process-step {
  @apply relative flex flex-col items-start p-3 rounded-xl bg-gradient-to-br from-blue-50 to-indigo-50 border border-blue-200/60 shadow-sm hover:shadow-md transition-all duration-300;
  min-width: 200px;
  backdrop-filter: blur(8px);
}

.process-step:hover {
  @apply border-blue-300/80 shadow-lg;
  transform: translateY(-2px);
}

.step-header {
  @apply flex items-center mb-2 space-x-3 justify-start;
}

.step-icon-wrapper {
  @apply w-8 h-8 rounded-full bg-gradient-to-br from-blue-500 to-indigo-600 flex items-center justify-center shadow-md;
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
