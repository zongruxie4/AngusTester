<script lang="ts" setup>
import { useI18n } from 'vue-i18n';
import { Icon } from '@xcan-angus/vue-ui';
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
  <div class="bg-white rounded px-5 pt-0 pb-0 text-3.5">
    <!-- Welcome Section -->
    <div class="font-semibold text-text-title">
      {{ t('dataHome.introduce.title') }}
    </div>

    <!-- Introduction Image and Description -->
    <div class="flex items-center justify-between space-x-2.5">
      <img
        class="w-1/3"
        src="./images/introduce.png"
        alt="Introduction illustration" />
      <div class="text-text-content">
        {{ t('dataHome.introduce.description') }}
      </div>
    </div>

    <!-- Feature Tips Section -->
    <div
      v-for="tip in introductionTips"
      :key="tip.title"
      class="mb-1 text-text-content">
      <Icon icon="icon-duihaolv" class="flex-shrink-0 mr-1.5 text-3.5 transform-gpu translate-y" />
      <span class="font-semibold">{{ tip.title }}: </span>
      <span>{{ tip.content }}</span>
    </div>
  </div>
</template>
