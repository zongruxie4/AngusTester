<script lang="ts" setup>
import { useI18n } from 'vue-i18n';
import { IntroductionTip } from './types';

const { t } = useI18n();

/**
 * <p>Introduction tip items for user guidance</p>
 * <p>Provides helpful information about key features</p>
 */
const introductionTips: IntroductionTip[] = [
  {
    title: t('dataHome.introduce.variable.title'),
    content: t('dataHome.introduce.variable.content')
  },
  {
    title: t('dataHome.introduce.dataset.title'),
    content: t('dataHome.introduce.dataset.content')
  },
  {
    title: t('dataHome.introduce.mockFunction.title'),
    content: t('dataHome.introduce.mockFunction.content')
  },
  {
    title: t('dataHome.introduce.samplingExtraction.title'),
    content: t('dataHome.introduce.samplingExtraction.content')
  }
];
</script>

<template>
  <div class="bg-white rounded border border-border-divider p-5">
    <!-- Welcome Section -->
    <div class="text-3.5 font-semibold mb-2.5 text-text-title">
      {{ t('dataHome.introduce.welcome') }}
    </div>
    
    <!-- Introduction Image and Description -->
    <div class="flex items-center justify-between space-x-2.5 mb-4">
      <img 
        class="w-1/3" 
        src="./images/introduce.png" 
        alt="Introduction illustration" />
      <div class="text-text-content">
        {{ t('dataHome.introduce.description') }}
      </div>
    </div>
    
    <!-- Feature Tips Section -->
    <div class="mt-2">
      <div class="font-semibold mb-1.5 text-text-title">
        {{ t('dataHome.introduce.title') }}
      </div>
      <div
        v-for="tip in introductionTips"
        :key="tip.title"
        class="mb-1 text-text-content">
        <span class="font-semibold">{{ tip.title }}: </span>
        <span>{{ tip.content }}</span>
      </div>
    </div>
  </div>
</template>
