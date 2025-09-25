<script setup lang="ts">
import { useI18n } from 'vue-i18n';

const { t } = useI18n();
</script>

<template>
  <div>
    <div class="text-3.5 font-semibold mb-2.5">{{ t('caseReview.introduce.aboutReview') }}</div>
    <div class="text-3.5 mb-6">
      <div class="text-3.5">{{ t('common.description') }}</div>
    </div>

    <div class="space-y-6">
      <div class="flex items-start justify-between">
        <div class="flex items-start space-x-3 w-1/3">
          <img src="./images/tigaoceshizhiliang.png" class="w-10 flex-shrink-0 transform-gpu translate-y-0.5">
          <div class="space-y-1 pr-10">
            <div class="text-3.5 font-semibold">{{ t('caseReview.introduce.improveQuality.title') }}</div>
            <div class="text-3.5">{{ t('common.description') }}</div>
          </div>
        </div>

        <div class="flex items-start space-x-3 w-1/3">
          <img src="./images/quebaofugailv.png" class="w-10 flex-shrink-0 transform-gpu translate-y-0.5">
          <div class="space-y-1 pr-10">
            <div class="text-3.5 font-semibold">{{ t('caseReview.introduce.ensureCoverage.title') }}</div>
            <div class="text-3.5">{{ t('common.description') }}</div>
          </div>
        </div>

        <div class="flex items-start space-x-3 w-1/3">
          <img src="./images/faxianqianzaiquexian.png" class="w-10 flex-shrink-0 transform-gpu translate-y-0.5">
          <div class="space-y-1 pr-10">
            <div class="text-3.5 font-semibold">{{ t('caseReview.introduce.findDefects.title') }}</div>
            <div class="text-3.5">{{ t('common.description') }}</div>
          </div>
        </div>
      </div>

      <div class="flex items-start justify-between">
        <div class="flex items-start space-x-3 w-1/3">
          <img src="./images/cujinzhishigongxiang.png" class="w-10 flex-shrink-0 transform-gpu translate-y-0.5">
          <div class="space-y-1 pr-10">
            <div class="text-3.5 font-semibold">{{ t('caseReview.introduce.promoteKnowledge.title') }}</div>
            <div class="text-3.5">{{ t('common.description') }}</div>
          </div>
        </div>

        <div class="flex items-start space-x-3 w-1/3">
          <img src="./images/youhuaceshicelve.png" class="w-10 flex-shrink-0 transform-gpu translate-y-0.5">
          <div class="space-y-1 pr-10">
            <div class="text-3.5 font-semibold">{{ t('caseReview.introduce.optimizeStrategy.title') }}</div>
            <div class="text-3.5">{{ t('common.description') }}</div>
          </div>
        </div>

        <div class="flex items-start space-x-3 w-1/3">
          <img src="./images/jianshaochonggong.png" class="w-10 flex-shrink-0 transform-gpu translate-y-0.5">
          <div class="space-y-1 pr-10">
            <div class="text-3.5 font-semibold">{{ t('caseReview.introduce.reduceRework.title') }}</div>
            <div class="text-3.5">{{ t('common.description') }}</div>
          </div>
        </div>
      </div>
    </div>
  </div>
</template>
