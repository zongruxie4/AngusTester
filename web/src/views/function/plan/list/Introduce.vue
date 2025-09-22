<script setup lang="ts">
import { useI18n } from 'vue-i18n';

const { t } = useI18n();
</script>

<template>
  <div>
    <div class="text-3.5 font-semibold mb-2.5">{{ t('functionPlan.introduce.title') }}</div>
    <div class="mb-6">
      <div>{{ t('functionPlan.introduce.description') }}</div>
    </div>

    <div class="space-y-6">
      <div class="flex items-start justify-between">
        <div class="flex items-start space-x-3 w-1/3">
          <img src="./images/1.png" class="w-10 flex-shrink-0 transform-gpu translate-y-0.5">
          <div class="space-y-1 pr-10">
            <div class="text-3.5 font-semibold">{{ t('functionPlan.introduce.testScope.title') }}</div>
            <div>{{ t('functionPlan.introduce.testScope.description') }}</div>
          </div>
        </div>

        <div class="flex items-start space-x-3 w-1/3">
          <img src="./images/2.png" class="w-10 flex-shrink-0 transform-gpu translate-y-0.5">
          <div class="space-y-1 pr-10">
            <div class="text-3.5 font-semibold">{{ t('functionPlan.introduce.timeManagement.title') }}</div>
            <div>{{ t('functionPlan.introduce.timeManagement.description') }}</div>
          </div>
        </div>

        <div class="flex items-start space-x-3 w-1/3">
          <img src="./images/3.png" class="w-10 flex-shrink-0 transform-gpu translate-y-0.5">
          <div class="space-y-1 pr-10">
            <div class="text-3.5 font-semibold">{{ t('functionPlan.introduce.riskManagement.title') }}</div>
            <div>{{ t('functionPlan.introduce.riskManagement.description') }}</div>
          </div>
        </div>
      </div>

      <div class="flex items-start justify-between">
        <div class="flex items-start space-x-3 w-1/3">
          <img src="./images/4.png" class="w-10 flex-shrink-0 transform-gpu translate-y-0.5">
          <div class="space-y-1 pr-10">
            <div class="text-3.5 font-semibold">{{ t('functionPlan.introduce.communication.title') }}</div>
            <div>{{ t('functionPlan.introduce.communication.description') }}</div>
          </div>
        </div>

        <div class="flex items-start space-x-3 w-1/3">
          <img src="./images/5.png" class="w-10 flex-shrink-0 transform-gpu translate-y-0.5">
          <div class="space-y-1 pr-10">
            <div class="text-3.5 font-semibold">{{ t('functionPlan.introduce.continuousImprovement.title') }}</div>
            <div>{{ t('functionPlan.introduce.continuousImprovement.description') }}</div>
          </div>
        </div>

        <div class="flex items-start space-x-3 w-1/3">
          <img src="./images/6.png" class="w-10 flex-shrink-0 transform-gpu translate-y-0.5">
          <div class="space-y-1 pr-10">
            <div class="text-3.5 font-semibold">{{ t('functionPlan.introduce.standards.title') }}</div>
            <div>{{ t('functionPlan.introduce.standards.description') }}</div>
          </div>
        </div>
      </div>
    </div>
  </div>
</template>
