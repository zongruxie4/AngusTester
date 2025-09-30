<script setup lang="ts">
import { useI18n } from 'vue-i18n';

const { t } = useI18n();
</script>

<template>
  <div>
    <div class="text-3.5 font-semibold mb-2.5">{{ t('functionBaseline.list.aboutBaseline') }}</div>
    <div class="mb-6 text-3.5 font-serif">{{ t('functionBaseline.list.baselineDescription') }}</div>

    <div class="space-y-6">
      <div class="flex items-start justify-between font-serif">
        <div class="flex items-start space-x-3 w-1/3">
          <img src="./images/banbenkongzhi.png" class="w-10 flex-shrink-0 transform-gpu translate-y-0.5">
          <div class="space-y-1 pr-10">
            <div class="text-3.5 font-semibold">{{ t('functionBaseline.list.versionControl') }}</div>
            <div class="text-3.5">{{ t('functionBaseline.list.versionControlDescription') }}</div>
          </div>
        </div>

        <div class="flex items-start space-x-3 w-1/3">
          <img src="./images/biangengguanli.png" class="w-10 flex-shrink-0 transform-gpu translate-y-0.5">
          <div class="space-y-1 pr-10">
            <div class="text-3.5 font-semibold">{{ t('functionBaseline.list.changeManagement') }}</div>
            <div class="text-3.5">{{ t('functionBaseline.list.changeManagementDescription') }}</div>
          </div>
        </div>

        <div class="flex items-start space-x-3 w-1/3">
          <img src="./images/shenjihehegui.png" class="w-10 flex-shrink-0 transform-gpu translate-y-0.5">
          <div class="space-y-1 pr-10">
            <div class="text-3.5 font-semibold">{{ t('functionBaseline.list.auditCompliance') }}</div>
            <div class="text-3.5">{{ t('functionBaseline.list.auditComplianceDescription') }}</div>
          </div>
        </div>
      </div>
    </div>
  </div>
</template>
