<script setup lang="ts">
import { useI18n } from 'vue-i18n';

const { t } = useI18n();

const tipItems = [
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
  <div>
    <div class="text-3.5 font-semibold mb-2.5">{{ t('dataHome.introduce.welcome') }}</div>
    <div class="flex items-center justify-between space-x-2.5">
      <img class="w-1/3" src="./images/introduce.png" />
      <div>{{ t('dataHome.introduce.description') }}</div>
    </div>
    <div class="mt-2 mr-5">
      <div class="font-semibold mb-1.5">{{ t('dataHome.introduce.title') }}</div>
      <div
        v-for="item in tipItems"
        :key="item.title"
        class="mb-1">
        <span class="font-semibold">{{ item.title }}: </span>
        <span>{{ item.content }}</span>
      </div>
    </div>
  </div>
</template>
