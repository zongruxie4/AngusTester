<script setup lang="ts">
import { useI18n } from 'vue-i18n';

const { t } = useI18n();
</script>

<template>
  <div>
    <div class="text-3.5 font-semibold mb-2.5">{{ t('evaluation.introduce.title') }}</div>
    <div class="text-3.5 mb-6 font-serif">{{ t('evaluation.introduce.description') }}</div>

    <div>
      <div class="flex items-start justify-between font-serif">
        <div class="group hover:bg-gray-50 rounded-lg p-4 transition-colors duration-200 flex items-start space-x-3 w-1/3">
          <div class="w-10 h-10 flex-shrink-0 flex items-center justify-center bg-blue-100 rounded-lg">
            <span class="text-2xl">📊</span>
          </div>
          <div class="space-y-1 pr-10">
            <div class="text-3.5 font-semibold text-gray-800 transition-colors group-hover:text-blue-600">{{ t('evaluation.introduce.scope.title') }}</div>
            <div class="text-3.5">{{ t('evaluation.introduce.scope.description') }}</div>
          </div>
        </div>

        <div class="group hover:bg-gray-50 rounded-lg p-4 transition-colors duration-200 flex items-start space-x-3 w-1/3">
          <div class="w-10 h-10 flex-shrink-0 flex items-center justify-center bg-purple-100 rounded-lg">
            <span class="text-2xl">🎯</span>
          </div>
          <div class="space-y-1 pr-10">
            <div class="text-3.5 font-semibold text-gray-800 transition-colors group-hover:text-purple-600">{{ t('evaluation.introduce.purpose.title') }}</div>
            <div class="text-3.5">{{ t('evaluation.introduce.purpose.description') }}</div>
          </div>
        </div>

        <div class="group hover:bg-gray-50 rounded-lg p-4 transition-colors duration-200 flex items-start space-x-3 w-1/3">
          <div class="w-10 h-10 flex-shrink-0 flex items-center justify-center bg-indigo-100 rounded-lg">
            <span class="text-2xl">📈</span>
          </div>
          <div class="space-y-1 pr-10">
            <div class="text-3.5 font-semibold text-gray-800 transition-colors group-hover:text-indigo-600">{{ t('evaluation.introduce.result.title') }}</div>
            <div class="text-3.5">{{ t('evaluation.introduce.result.description') }}</div>
          </div>
        </div>
      </div>
    </div>
  </div>
</template>

