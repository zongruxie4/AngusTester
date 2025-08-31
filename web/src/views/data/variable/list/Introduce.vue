<script setup lang="ts">
import { useI18n } from 'vue-i18n';

const { t } = useI18n();
</script>

<template>
  <div>
    <div class="text-3.5 font-semibold mb-2.5">{{ t('dataVariable.introduce.title') }}</div>
    <div class="mb-6">
      <div>{{ t('dataVariable.introduce.description') }}</div>
      <div>{{ t('dataVariable.introduce.note') }}</div>
    </div>

    <div class="flex items-start justify-between">
      <div class="flex items-start space-x-3 w-1/3">
        <img src="./images/1.png" class="w-10 flex-shrink-0 transform-gpu translate-y-0.5">
        <div class="space-y-1 pr-10">
          <div class="text-3.5 font-semibold">{{ t('dataVariable.introduce.types.fileExtract.title') }}</div>
          <div>{{ t('dataVariable.introduce.types.fileExtract.description') }}</div>
        </div>
      </div>

      <div class="flex items-start space-x-3 w-1/3">
        <img src="./images/2.png" class="w-10 flex-shrink-0 transform-gpu translate-y-0.5">
        <div class="space-y-1 pr-10">
          <div class="text-3.5 font-semibold">{{ t('dataVariable.introduce.types.httpExtract.title') }}</div>
          <div>{{ t('dataVariable.introduce.types.httpExtract.description') }}</div>
        </div>
      </div>

      <div class="flex items-start space-x-3 w-1/3">
        <img src="./images/3.png" class="w-10 flex-shrink-0 transform-gpu translate-y-0.5">
        <div class="space-y-1 pr-10">
          <div class="text-3.5 font-semibold">{{ t('dataVariable.introduce.types.jdbcExtract.title') }}</div>
          <div>{{ t('dataVariable.introduce.types.jdbcExtract.description') }}</div>
        </div>
      </div>
    </div>
  </div>
</template>
