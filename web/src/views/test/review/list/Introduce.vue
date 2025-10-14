<script setup lang="ts">
import { useI18n } from 'vue-i18n';

const { t } = useI18n();
</script>

<template>
  <div>
    <div class="text-3.5 font-semibold mb-2.5">{{ t('testCaseReview.introduce.title') }}</div>
    <div class="text-3.5 mb-6 font-serif">{{ t('testCaseReview.introduce.description') }}</div>

    <div class="font-serif">
      <div class="flex items-start justify-between">
        <div class="group hover:bg-gray-50 rounded-lg p-4 transition-colors duration-200 flex items-start space-x-3 w-1/3">
          <img src="./images/tigaoceshizhiliang.png" class="w-10 flex-shrink-0 transform-gpu translate-y-0.5">
          <div class="space-y-1 pr-10">
            <div class="text-3.5 font-semibold text-gray-800 transition-colors group-hover:text-blue-600">{{ t('testCaseReview.introduce.improveQuality.title') }}</div>
            <div class="text-3.5">{{ t('testCaseReview.introduce.improveQuality.description') }}</div>
          </div>
        </div>

        <div class="group hover:bg-gray-50 rounded-lg p-4 transition-colors duration-200 flex items-start space-x-3 w-1/3">
          <img src="./images/quebaofugailv.png" class="w-10 flex-shrink-0 transform-gpu translate-y-0.5">
          <div class="space-y-1 pr-10">
            <div class="text-3.5 font-semibold text-gray-800 transition-colors group-hover:text-purple-600">{{ t('testCaseReview.introduce.ensureCoverage.title') }}</div>
            <div class="text-3.5">{{ t('testCaseReview.introduce.ensureCoverage.description') }}</div>
          </div>
        </div>

        <div class="group hover:bg-gray-50 rounded-lg p-4 transition-colors duration-200 flex items-start space-x-3 w-1/3">
          <img src="./images/faxianqianzaiquexian.png" class="w-10 flex-shrink-0 transform-gpu translate-y-0.5">
          <div class="space-y-1 pr-10">
            <div class="text-3.5 font-semibold text-gray-800 transition-colors group-hover:text-indigo-600">{{ t('testCaseReview.introduce.findDefects.title') }}</div>
            <div class="text-3.5">{{ t('testCaseReview.introduce.findDefects.description') }}</div>
          </div>
        </div>
      </div>

      <div class="flex items-start justify-between">
        <div class="group hover:bg-gray-50 rounded-lg p-4 transition-colors duration-200 flex items-start space-x-3 w-1/3">
          <img src="./images/cujinzhishigongxiang.png" class="w-10 flex-shrink-0 transform-gpu translate-y-0.5">
          <div class="space-y-1 pr-10">
            <div class="text-3.5 font-semibold text-gray-800 transition-colors group-hover:text-blue-600">{{ t('testCaseReview.introduce.promoteKnowledge.title') }}</div>
            <div class="text-3.5">{{ t('testCaseReview.introduce.promoteKnowledge.description') }}</div>
          </div>
        </div>

        <div class="group hover:bg-gray-50 rounded-lg p-4 transition-colors duration-200 flex items-start space-x-3 w-1/3">
          <img src="./images/youhuaceshicelve.png" class="w-10 flex-shrink-0 transform-gpu translate-y-0.5">
          <div class="space-y-1 pr-10">
            <div class="text-3.5 font-semibold text-gray-800 transition-colors group-hover:text-blue-600">{{ t('testCaseReview.introduce.optimizeStrategy.title') }}</div>
            <div class="text-3.5">{{ t('testCaseReview.introduce.optimizeStrategy.description') }}</div>
          </div>
        </div>

        <div class="group hover:bg-gray-50 rounded-lg p-4 transition-colors duration-200 flex items-start space-x-3 w-1/3">
          <img src="./images/jianshaochonggong.png" class="w-10 flex-shrink-0 transform-gpu translate-y-0.5">
          <div class="space-y-1 pr-10">
            <div class="text-3.5 font-semibold text-gray-800 transition-colors group-hover:text-blue-600">{{ t('testCaseReview.introduce.reduceRework.title') }}</div>
            <div class="text-3.5">{{ t('testCaseReview.introduce.reduceRework.description') }}</div>
          </div>
        </div>
      </div>
    </div>
  </div>
</template>
