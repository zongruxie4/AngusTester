<script setup lang="ts">
import { useI18n } from 'vue-i18n';

const { t } = useI18n();
</script>

<template>
  <div>
    <div class="text-3.5 font-semibold mb-2.5">{{ t('testCaseBaseline.introduce.title') }}</div>
    <div class="mb-6 text-3.5 font-serif">{{ t('testCaseBaseline.introduce.description') }}</div>

    <div>
      <div class="flex items-start justify-between font-serif">
        <div class="group hover:bg-gray-50 rounded-lg p-4 transition-colors duration-200 flex items-start space-x-3 w-1/3">
          <img src="./images/banbenkongzhi.png" class="w-10 flex-shrink-0 transform-gpu translate-y-0.5">
          <div class="space-y-1 pr-10">
            <div class="text-3.5 font-semibold text-gray-800 transition-colors group-hover:text-blue-600">{{ t('testCaseBaseline.introduce.versionControl') }}</div>
            <div class="text-3.5">{{ t('testCaseBaseline.introduce.versionControlDescription') }}</div>
          </div>
        </div>

        <div class="group hover:bg-gray-50 rounded-lg p-4 transition-colors duration-200 flex items-start space-x-3 w-1/3">
          <img src="./images/biangengguanli.png" class="w-10 flex-shrink-0 transform-gpu translate-y-0.5">
          <div class="space-y-1 pr-10">
            <div class="text-3.5 font-semibold text-gray-800 transition-colors group-hover:text-purple-600">{{ t('testCaseBaseline.introduce.changeManagement') }}</div>
            <div class="text-3.5">{{ t('testCaseBaseline.introduce.changeManagementDescription') }}</div>
          </div>
        </div>

        <div class="group hover:bg-gray-50 rounded-lg p-4 transition-colors duration-200 flex items-start space-x-3 w-1/3">
          <img src="./images/shenjihehegui.png" class="w-10 flex-shrink-0 transform-gpu translate-y-0.5">
          <div class="space-y-1 pr-10">
            <div class="text-3.5 font-semibold text-gray-800 transition-colors group-hover:text-indigo-600">{{ t('testCaseBaseline.introduce.auditCompliance') }}</div>
            <div class="text-3.5">{{ t('testCaseBaseline.introduce.auditComplianceDescription') }}</div>
          </div>
        </div>
      </div>
    </div>
  </div>
</template>
