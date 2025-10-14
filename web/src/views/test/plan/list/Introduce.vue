<script setup lang="ts">
import { useI18n } from 'vue-i18n';

const { t } = useI18n();
</script>

<template>
  <div>
    <div class="text-3.5 font-semibold mb-2.5">{{ t('testPlan.introduce.title') }}</div>
    <div class="text-3.5 mb-6 font-serif">{{ t('testPlan.introduce.description') }}</div>

    <div>
      <div class="flex items-start justify-between font-serif">
        <div class="group hover:bg-gray-50 rounded-lg p-4 transition-colors duration-200 flex items-start space-x-3 w-1/3">
          <img src="./images/1.png" class="w-10 flex-shrink-0 transform-gpu translate-y-0.5">
          <div class="space-y-1 pr-10">
            <div class="text-3.5 font-semibold text-gray-800 transition-colors group-hover:text-blue-600">{{ t('testPlan.introduce.testScope.title') }}</div>
            <div class="text-3.5">{{ t('testPlan.introduce.testScope.description') }}</div>
          </div>
        </div>

        <div class="group hover:bg-gray-50 rounded-lg p-4 transition-colors duration-200 flex items-start space-x-3 w-1/3">
          <img src="./images/2.png" class="w-10 flex-shrink-0 transform-gpu translate-y-0.5">
          <div class="space-y-1 pr-10">
            <div class="text-3.5 font-semibold text-gray-800 transition-colors group-hover:text-purple-600">{{ t('testPlan.introduce.timeManagement.title') }}</div>
            <div class="text-3.5">{{ t('testPlan.introduce.timeManagement.description') }}</div>
          </div>
        </div>

        <div class="group hover:bg-gray-50 rounded-lg p-4 transition-colors duration-200 flex items-start space-x-3 w-1/3">
          <img src="./images/3.png" class="w-10 flex-shrink-0 transform-gpu translate-y-0.5">
          <div class="space-y-1 pr-10">
            <div class="text-3.5 font-semibold text-gray-800 transition-colors group-hover:text-indigo-600">{{ t('testPlan.introduce.riskManagement.title') }}</div>
            <div class="text-3.5">{{ t('testPlan.introduce.riskManagement.description') }}</div>
          </div>
        </div>
      </div>

      <div class="flex items-start justify-between">
        <div class="group hover:bg-gray-50 rounded-lg p-4 transition-colors duration-200 flex items-start space-x-3 w-1/3">
          <img src="./images/4.png" class="w-10 flex-shrink-0 transform-gpu translate-y-0.5">
          <div class="space-y-1 pr-10">
            <div class="text-3.5 font-semibold text-gray-800 transition-colors group-hover:text-blue-600">{{ t('testPlan.introduce.communication.title') }}</div>
            <div class="text-3.5">{{ t('testPlan.introduce.communication.description') }}</div>
          </div>
        </div>

        <div class="group hover:bg-gray-50 rounded-lg p-4 transition-colors duration-200 flex items-start space-x-3 w-1/3">
          <img src="./images/5.png" class="w-10 flex-shrink-0 transform-gpu translate-y-0.5">
          <div class="space-y-1 pr-10">
            <div class="text-3.5 font-semibold text-gray-800 transition-colors group-hover:text-blue-600">{{ t('testPlan.introduce.continuousImprovement.title') }}</div>
            <div class="text-3.5">{{ t('testPlan.introduce.continuousImprovement.description') }}</div>
          </div>
        </div>

        <div class="group hover:bg-gray-50 rounded-lg p-4 transition-colors duration-200 flex items-start space-x-3 w-1/3">
          <img src="./images/6.png" class="w-10 flex-shrink-0 transform-gpu translate-y-0.5">
          <div class="space-y-1 pr-10">
            <div class="text-3.5 font-semibold text-gray-800 transition-colors group-hover:text-blue-600">{{ t('testPlan.introduce.standards.title') }}</div>
            <div class="text-3.5">{{ t('testPlan.introduce.standards.description') }}</div>
          </div>
        </div>
      </div>
    </div>
  </div>
</template>
