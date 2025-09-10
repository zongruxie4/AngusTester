<script setup lang="ts">
import { useI18n } from 'vue-i18n';

const { t } = useI18n();
</script>

<template>
  <div>
    <div class="text-3.5 font-semibold mb-2.5">{{ t('functionBaseline.list.aboutBaseline') }}</div>
    <div class="mb-6">
      <div>{{ t('functionBaseline.list.baselineDescription') }}</div>
    </div>

    <div class="space-y-6">
      <div class="flex items-start justify-between">
        <div class="flex items-start space-x-3 w-1/3">
          <img src="imagesanbenkongzhi.png" class="w-10 flex-shrink-0 transform-gpu translate-y-0.5">
          <div class="space-y-1 pr-10">
            <div class="text-3.5 font-semibold">{{ t('functionBaseline.list.versionControl') }}</div>
            <div>{{ t('functionBaseline.list.versionControlDescription') }}</div>
          </div>
        </div>

        <div class="flex items-start space-x-3 w-1/3">
          <img src="imagesiangengguanli.png" class="w-10 flex-shrink-0 transform-gpu translate-y-0.5">
          <div class="space-y-1 pr-10">
            <div class="text-3.5 font-semibold">{{ t('functionBaseline.list.changeManagement') }}</div>
            <div>{{ t('functionBaseline.list.changeManagementDescription') }}</div>
          </div>
        </div>

        <div class="flex items-start space-x-3 w-1/3">
          <img src="imageshenjihehegui.png" class="w-10 flex-shrink-0 transform-gpu translate-y-0.5">
          <div class="space-y-1 pr-10">
            <div class="text-3.5 font-semibold">{{ t('functionBaseline.list.auditCompliance') }}</div>
            <div>{{ t('functionBaseline.list.auditComplianceDescription') }}</div>
          </div>
        </div>
      </div>
    </div>
  </div>
</template>
