<script lang="ts" setup>
import { useI18n } from 'vue-i18n';
import { Icon } from '@xcan-angus/vue-ui';

const { t } = useI18n();
</script>
<template>
  <div class="flex content items-center flex-1">
    <img src="../../../assets/images/home/report.png" class="w-45 h-45" />
    <div class="ml-3.25 text-content flex-1">
      <div>{{ t('reportHome.summary.description') }}</div>
      <div class="mt-2">{{ t('reportHome.summary.workTitle') }}</div>

      <div class="mt-1">
        <Icon icon="icon-duihaolv" class="mr-2.5" />
        <span>{{ t('reportHome.summary.features.generateReport') }}</span>
      </div>

      <div class="mt-1">
        <Icon icon="icon-duihaolv" class="mr-2.5" />
        <span>{{ t('reportHome.summary.features.viewReport') }}</span>
      </div>
    </div>
  </div>
</template>
