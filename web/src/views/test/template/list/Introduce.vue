<script setup lang="ts">
import { useI18n } from 'vue-i18n';

const { t } = useI18n();
</script>

<template>
  <div class="flex flex-col">
    <div class="text-4 font-semibold mb-2">{{ t('testTemplate.title') }}</div>
    <div class="text-3 text-theme-sub-content leading-6">
      {{ t('testTemplate.introduce.description') }}
    </div>
  </div>
</template>

